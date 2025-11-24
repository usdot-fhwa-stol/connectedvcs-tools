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
import gov.usdot.cv.fedgov_cv_map_georeferencing.gdal.GdalFacade;
import gov.usdot.cv.fedgov_cv_map_georeferencing.gdal.GdalException;
import gov.usdot.cv.fedgov_cv_map_georeferencing.config.GeoreferenceProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class GeoreferenceServiceTest {

    @Mock
    private GdalFacade gdalFacade;
    
    @Mock
    private GeoreferenceProperties georeferenceProperties;
    
    @Mock
    private GeoreferenceProperties.Gcp gcpProperties;
    
    @Mock
    private GeoreferenceProperties.Image imageProperties;

    private GeoreferenceService georeferenceService;

    private MultipartFile validImageFile;
    private MultipartFile emptyImageFile;
    private List<GCP> validGcps;
    private List<GCP> invalidGcps;

    @BeforeEach
    void setUp() throws GdalException {
        // Setup mock properties
        lenient().when(georeferenceProperties.getGcp()).thenReturn(gcpProperties);
        lenient().when(georeferenceProperties.getImage()).thenReturn(imageProperties);
        lenient().when(gcpProperties.getMinCount()).thenReturn(6);
        lenient().when(gcpProperties.getMaxCount()).thenReturn(10);
        lenient().when(imageProperties.getSupportedFormatsAsSet()).thenReturn(
            Set.of("jpg", "jpeg", "png", "tif", "tiff")
        );
        
        // Create the service with the mocked facade and properties
        georeferenceService = new GeoreferenceService(gdalFacade, georeferenceProperties);
        
        // Setup lenient mock behavior for GdalFacade
        lenient().when(gdalFacade.isGdalAvailable()).thenReturn(false); // Use mock mode by default
        lenient().when(gdalFacade.getGdalVersion()).thenReturn("GDAL 3.4.1 (mocked)");
        lenient().doNothing().when(gdalFacade).validateGdalUtilities();
        
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
    void testProcess_WithGdalAvailable_ProcessesWithGdal() throws Exception {
        // Arrange
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        
        // Mock GDAL operations to create files 
        doAnswer(invocation -> {
            // Create the PNG file that the service expects
            Path outputPath = invocation.getArgument(1);
            Files.createDirectories(outputPath.getParent());
            Files.write(outputPath, "mock png content".getBytes());
            return null;
        }).when(gdalFacade).translateImage(any(), any(), eq("PNG"), eq("-scale"));
        
        doNothing().when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());
        doNothing().when(gdalFacade).warpImage(any(), any(), any(), any(), any());
        
        Map<String, Object> mockImageInfo = new HashMap<>();
        mockImageInfo.put("gdalinfo_json", "{\"wgs84Extent\":{\"coordinates\":[[[-77.151679,38.957170],[-77.151679,38.954914],[-77.146619,38.954914],[-77.146619,38.957170],[-77.151679,38.957170]]]},\"size\":[898,516]}");
        when(gdalFacade.getComprehensiveImageInfo(any())).thenReturn(mockImageInfo);

        // Act
        Map<String, Object> result = georeferenceService.process(validImageFile, validGcps);

        // Assert
        assertNotNull(result);
        assertEquals("processed_gdal_facade", result.get("status"));
        assertEquals(6, result.get("gcpCount"));
        assertNotNull(result.get("extent"));
    }

    @Test
    void testProcess_GdalThrowsException_FallsBackToMock() throws Exception {
        // Arrange
        when(gdalFacade.isGdalAvailable()).thenReturn(true);
        
        // Make GDAL operations throw an exception to trigger fallback
        doThrow(new RuntimeException("GDAL processing failed")).when(gdalFacade).createVrtWithGcps(any(), any(), any(), any());

        // Act
        Map<String, Object> result = georeferenceService.process(validImageFile, validGcps);

        // Assert
        assertNotNull(result);
        assertEquals("mock_processed", result.get("status"));
        assertEquals(6, result.get("gcpCount"));
        assertNotNull(result.get("extent"));
        assertTrue(result.get("message").toString().contains("Mock response"));
    }

    @Test
    void testProcess_ValidInputs_ReturnsExpectedResult() throws Exception {
        // Act
        Map<String, Object> result = georeferenceService.process(validImageFile, validGcps);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        // Verify expected keys exist
        assertTrue(result.containsKey("originalImageName"));
        assertTrue(result.containsKey("imageSize"));
        assertTrue(result.containsKey("gcpCount"));
        assertTrue(result.containsKey("status"));
        
        // Verify values
        assertEquals("test-image.jpg", result.get("originalImageName"));
        assertEquals(6, result.get("gcpCount"));
        assertEquals("mock_processed", result.get("status"));
    }

    @Test
    void testProcess_NullImage_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> georeferenceService.process(null, validGcps)
        );
        assertEquals("Image file is required and cannot be empty", exception.getMessage());
    }

    @Test
    void testProcess_EmptyImage_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> georeferenceService.process(emptyImageFile, validGcps)
        );
        assertEquals("Image file is required and cannot be empty", exception.getMessage());
    }

    @Test
    void testProcess_NullGcps_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> georeferenceService.process(validImageFile, null)
        );
        assertEquals("At least 6 ground control points are required for high precision georeferencing. Provided: 0", exception.getMessage());
    }

    @Test
    void testProcess_InsufficientGcps_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> georeferenceService.process(validImageFile, invalidGcps)
        );
        assertTrue(exception.getMessage().contains("At least 6 ground control points are required"));
        assertTrue(exception.getMessage().contains("Provided: 3"));
    }

    @Test
    void testProcess_GcpWithNullValues_ThrowsException() {
        // Arrange
        List<GCP> gcpsWithNull = Arrays.asList(
            new GCP(null, 100, 200, -77.123, 38.456), // null pointId
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> georeferenceService.process(validImageFile, gcpsWithNull)
        );
        assertEquals("All ground control points must have valid point IDs", exception.getMessage());
    }

    @Test
    void testProcess_GcpWithNullCoordinates_ThrowsException() {
        // Arrange
        List<GCP> gcpsWithNullCoords = Arrays.asList(
            new GCP("GCP1", null, 200, -77.123, 38.456), // null imageX
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459),
            new GCP("GCP5", 200, 300, -77.127, 38.460),
            new GCP("GCP6", 600, 700, -77.128, 38.461)
        );
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> georeferenceService.process(validImageFile, gcpsWithNullCoords)
        );
        assertEquals("All ground control points must have valid image coordinates", exception.getMessage());
    }

    @Test
    void testProcess_ValidResult_ContainsExpectedFields() throws Exception {
        // Act
        Map<String, Object> result = georeferenceService.process(validImageFile, validGcps);
        
        // Assert
        assertNotNull(result.get("originalImageName"));
        assertNotNull(result.get("imageSize"));
        assertNotNull(result.get("gcpCount"));
        assertNotNull(result.get("extent"));
        assertNotNull(result.get("status"));
        assertNotNull(result.get("message"));
        
        // Verify extent contains proper coordinate bounds
        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) result.get("extent");
        assertNotNull(extent.get("minLongitude"));
        assertNotNull(extent.get("maxLongitude"));
        assertNotNull(extent.get("minLatitude"));
        assertNotNull(extent.get("maxLatitude"));
        
        // Verify logical coordinate bounds
        assertTrue(extent.get("minLongitude") < extent.get("maxLongitude"));
        assertTrue(extent.get("minLatitude") < extent.get("maxLatitude"));
    }

    @Test
    void testProcess_WithValidGcps_CalculatesExtentFromGcps() throws Exception {
        // Act
        Map<String, Object> result = georeferenceService.process(validImageFile, validGcps);
        
        // Assert
        @SuppressWarnings("unchecked")
        Map<String, Double> extent = (Map<String, Double>) result.get("extent");
        
        // The extent should be derived from the GCP coordinates
        // validGcps longitude range: -77.128 to -77.123
        // validGcps latitude range: 38.456 to 38.461
        assertTrue(extent.get("minLongitude") <= -77.128);
        assertTrue(extent.get("maxLongitude") >= -77.123);
        assertTrue(extent.get("minLatitude") <= 38.456);
        assertTrue(extent.get("maxLatitude") >= 38.461);
    }
}