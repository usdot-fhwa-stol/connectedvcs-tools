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
import com.fasterxml.jackson.databind.ObjectMapper;

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

    private ObjectMapper objectMapper;
    private MultipartFile mockImageFile;
    private List<GCP> validGcps;
    private String validGcpsJson;
    private Map<String, Object> mockServiceResult;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        
        // Setup mock image file
        mockImageFile = new MockMultipartFile(
            "image",
            "test-image.png",
            "image/png",
            "mock image content".getBytes()
        );

        // Setup valid GCPs
        validGcps = Arrays.asList(
            new GCP("gcp1", 100, 200, -77.036, 38.895),
            new GCP("gcp2", 300, 200, -77.030, 38.895),
            new GCP("gcp3", 100, 400, -77.036, 38.890),
            new GCP("gcp4", 300, 400, -77.030, 38.890),
            new GCP("gcp5", 200, 300, -77.033, 38.892),
            new GCP("gcp6", 400, 350, -77.027, 38.887)
        );
        
        // Convert to JSON string
        validGcpsJson = objectMapper.writeValueAsString(validGcps);

        // Setup mock service result
        mockServiceResult = new HashMap<String, Object>();
        mockServiceResult.put("status", "processed");
        mockServiceResult.put("originalImageName", "test-image.png");
        mockServiceResult.put("imageSize", 1024L);
        mockServiceResult.put("gcpCount", 6);
    }

    @Test
    void testGeoreference_ValidInput_ReturnsSuccessResponse() throws Exception {
        // Arrange
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenReturn(mockServiceResult);

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, validGcpsJson);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("Processing completed successfully", responseBody.get("message"));
        
        // Verify service was called with parsed GCPs
        verify(georeferenceService, times(1)).process(eq(mockImageFile), eq(validGcps));
    }

    @Test
    void testGeoreference_ServiceThrowsException_ReturnsErrorResponse() throws Exception {
        // Arrange
        Exception serviceException = new RuntimeException("Processing failed");
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenThrow(serviceException);

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, validGcpsJson);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Processing failed"));
        assertEquals("INTERNAL_SERVER_ERROR", responseBody.get("error"));
    }

    @Test
    void testGeoreference_InvalidJsonFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        String invalidJson = "invalid json format";

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, invalidJson);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("Invalid GCP format"));
        assertEquals("JSON_PARSE_ERROR", responseBody.get("error"));
    }

    @Test
    void testGeoreference_InvalidGcpCount_ReturnsBadRequest() throws Exception {
        // Arrange - Create GCP list with wrong count
        List<GCP> invalidGcps = Arrays.asList(
            new GCP("gcp1", 100, 200, -77.036, 38.895),
            new GCP("gcp2", 300, 200, -77.030, 38.895)
        ); // Only 2 GCPs instead of 4
        String invalidGcpsJson = objectMapper.writeValueAsString(invalidGcps);

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, invalidGcpsJson);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("At least 6 Ground Control Points are required"));
        assertEquals("INVALID_GCP_COUNT", responseBody.get("error"));
    }

    @Test
    void testConstructor_ValidService_CreatesController() {
        // Act
        GeoreferenceController controller = new GeoreferenceController(georeferenceService);
        
        // Assert
        assertNotNull(controller);
    }
}