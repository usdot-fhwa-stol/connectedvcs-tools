/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.usdot.cv.fedgov_cv_map_georeferencing.service;

import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GeoreferenceServiceTest {

    @InjectMocks
    private GeoreferenceService georeferenceService;

    private MultipartFile validImageFile;
    private MultipartFile emptyImageFile;
    private List<GCP> validGcps;
    private List<GCP> invalidGcps;

    @BeforeEach
    void setUp() {
        // Create valid image file
        validImageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Create empty image file
        emptyImageFile = new MockMultipartFile(
            "image",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        // Create valid GCPs (at least 6 for high precision)
        validGcps = Arrays.asList(
            new GCP("GCP1", 100, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );

        // Create invalid GCPs (only 3 - below minimum of 6)
        invalidGcps = Arrays.asList(
            new GCP("GCP1", 100, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458)
        );
    }

    @Test
    void testProcess_ValidInputs_ReturnsExpectedResult() throws Exception {
        // Act
        Object result = georeferenceService.process(validImageFile, validGcps);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        assertEquals("test-image.jpg", resultMap.get("originalImageName"));
        assertEquals((long) "test image content".getBytes().length, resultMap.get("imageSize"));
        assertEquals(6, resultMap.get("gcpCount"));
        assertEquals("mock_processed", resultMap.get("status"));
        assertEquals("Mock response - GDAL not available. Image processing simulated using 6 ground control points", resultMap.get("message"));
        assertNotNull(resultMap.get("extent"));
        
        // Verify extent metadata
        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) resultMap.get("extent");
        assertEquals(-77.128, extent.get("minLongitude"));
        assertEquals(-77.123, extent.get("maxLongitude"));
        assertEquals(38.456, extent.get("minLatitude"));
        assertEquals(38.461, extent.get("maxLatitude"));
    }

    @Test
    void testProcess_NullImage_ThrowsIllegalArgumentException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(null, validGcps);
        });
        
        assertEquals("Image file is required and cannot be empty", exception.getMessage());
    }

    @Test
    void testProcess_EmptyImage_ThrowsIllegalArgumentException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(emptyImageFile, validGcps);
        });
        
        assertEquals("Image file is required and cannot be empty", exception.getMessage());
    }

    @Test
    void testProcess_NullGcps_ThrowsIllegalArgumentException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(validImageFile, null);
        });
        
        assertEquals("At least 6 ground control points are required for high precision georeferencing. Provided: 0", exception.getMessage());
    }

    @Test
    void testProcess_IncorrectGcpCount_ThrowsIllegalArgumentException() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(validImageFile, invalidGcps);
        });
        
        assertEquals("At least 6 ground control points are required for high precision georeferencing. Provided: 3", exception.getMessage());
    }

    @Test
    void testProcess_GcpWithNullPointId_ThrowsIllegalArgumentException() {
        // Arrange
        List<GCP> gcpsWithNullId = Arrays.asList(
            new GCP(null, 100, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(validImageFile, gcpsWithNullId);
        });
        
        assertEquals("All ground control points must have valid point IDs", exception.getMessage());
    }

    @Test
    void testProcess_GcpWithEmptyPointId_ThrowsIllegalArgumentException() {
        // Arrange
        List<GCP> gcpsWithEmptyId = Arrays.asList(
            new GCP("", 100, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(validImageFile, gcpsWithEmptyId);
        });
        
        assertEquals("All ground control points must have valid point IDs", exception.getMessage());
    }

    @Test
    void testProcess_GcpWithNullImageCoordinates_ThrowsIllegalArgumentException() {
        // Arrange
        List<GCP> gcpsWithNullCoords = Arrays.asList(
            new GCP("GCP1", null, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(validImageFile, gcpsWithNullCoords);
        });
        
        assertEquals("All ground control points must have valid image coordinates", exception.getMessage());
    }

    @Test
    void testProcess_GcpWithNullGeographicCoordinates_ThrowsIllegalArgumentException() {
        // Arrange
        List<GCP> gcpsWithNullGeoCoords = Arrays.asList(
            new GCP("GCP1", 100, 200, null, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            georeferenceService.process(validImageFile, gcpsWithNullGeoCoords);
        });
        
        assertEquals("All ground control points must have valid geographic coordinates", exception.getMessage());
    }

    @Test
    void testProcess_ExtentCalculation_CalculatesCorrectBounds() throws Exception {
        // Act
        Object result = georeferenceService.process(validImageFile, validGcps);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) resultMap.get("extent");
        
        // Verify extent calculations
        assertEquals(-77.128, extent.get("minLongitude")); // Min of [-77.123, -77.124, -77.125, -77.126]
        assertEquals(-77.123, extent.get("maxLongitude")); // Max of [-77.123, -77.124, -77.125, -77.126]
        assertEquals(38.456, extent.get("minLatitude"));   // Min of [38.456, 38.457, 38.458, 38.459]
        assertEquals(38.461, extent.get("maxLatitude"));   // Max of [38.456, 38.457, 38.458, 38.459]
    }

    @Test
    void testProcess_ResultStructure_ContainsAllExpectedFields() throws Exception {
        // Act
        Object result = georeferenceService.process(validImageFile, validGcps);

        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Object> resultMap = (Map<String, Object>) result;
        
        // Verify all expected fields are present
        assertTrue(resultMap.containsKey("originalImageName"));
        assertTrue(resultMap.containsKey("imageSize"));
        assertTrue(resultMap.containsKey("gcpCount"));
        assertTrue(resultMap.containsKey("processedImageBytes"));
        assertTrue(resultMap.containsKey("extent"));
        assertTrue(resultMap.containsKey("status"));
        assertTrue(resultMap.containsKey("message"));
        
        // Verify extent structure
        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) resultMap.get("extent");
        assertTrue(extent.containsKey("minLongitude"));
        assertTrue(extent.containsKey("maxLongitude"));
        assertTrue(extent.containsKey("minLatitude"));
        assertTrue(extent.containsKey("maxLatitude"));
    }
}