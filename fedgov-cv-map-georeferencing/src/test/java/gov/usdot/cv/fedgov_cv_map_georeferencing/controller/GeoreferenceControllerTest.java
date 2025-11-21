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
package gov.usdot.cv.fedgov_cv_map_georeferencing.controller;

import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GCP;
import gov.usdot.cv.fedgov_cv_map_georeferencing.dto.GeoreferenceResponse;
import gov.usdot.cv.fedgov_cv_map_georeferencing.service.GeoreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeoreferenceControllerTest {

    @Mock
    private GeoreferenceService georeferenceService;

    @InjectMocks
    private GeoreferenceController georeferenceController;

    private MultipartFile mockImageFile;
    private List<GCP> validGcps;
    private Map<String, Object> mockServiceResult;

    @BeforeEach
    void setUp() {
        // Create mock image file
        mockImageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // Create valid GCPs
        validGcps = Arrays.asList(
            new GCP("GCP1", 100, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459)
        );

        // Create mock service result
        mockServiceResult = new HashMap<>();
        mockServiceResult.put("originalImageName", "test-image.jpg");
        mockServiceResult.put("imageSize", 1024L);
        mockServiceResult.put("gcpCount", 4);
        mockServiceResult.put("status", "processed");
    }

    @Test
    void testGeoreference_ValidInput_ReturnsSuccessResponse() throws Exception {
        // Arrange
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenReturn(mockServiceResult);

        // Act
        ResponseEntity<GeoreferenceResponse> response = georeferenceController.georeference(mockImageFile, validGcps);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        GeoreferenceResponse responseBody = response.getBody();
        assertNotNull(responseBody);
        assertTrue(responseBody.success());
        assertEquals("Processed", responseBody.message());
        assertEquals(mockServiceResult, responseBody.details());
        
        // Verify service was called
        verify(georeferenceService, times(1)).process(mockImageFile, validGcps);
    }

    @Test
    void testGeoreference_ServiceThrowsException_PropagatesException() throws Exception {
        // Arrange
        Exception serviceException = new RuntimeException("Processing failed");
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenThrow(serviceException);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            georeferenceController.georeference(mockImageFile, validGcps);
        });
        
        assertEquals("Processing failed", exception.getMessage());
        verify(georeferenceService, times(1)).process(mockImageFile, validGcps);
    }

    @Test
    void testGeoreference_NullImage_ServiceHandlesValidation() throws Exception {
        // Arrange
        when(georeferenceService.process(isNull(), anyList()))
            .thenThrow(new IllegalArgumentException("Image file is required and cannot be empty"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            georeferenceController.georeference(null, validGcps);
        });
        
        assertEquals("Image file is required and cannot be empty", exception.getMessage());
        verify(georeferenceService, times(1)).process(null, validGcps);
    }

    @Test
    void testGeoreference_EmptyGcpList_ServiceHandlesValidation() throws Exception {
        // Arrange
        List<GCP> emptyGcps = Arrays.asList();
        when(georeferenceService.process(any(MultipartFile.class), eq(emptyGcps)))
            .thenThrow(new IllegalArgumentException("Exactly 4 ground control points are required"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            georeferenceController.georeference(mockImageFile, emptyGcps);
        });
        
        assertEquals("Exactly 4 ground control points are required", exception.getMessage());
        verify(georeferenceService, times(1)).process(mockImageFile, emptyGcps);
    }

    @Test
    void testConstructor_ValidService_CreatesController() {
        // Act
        GeoreferenceController controller = new GeoreferenceController(georeferenceService);
        
        // Assert
        assertNotNull(controller);
    }
}