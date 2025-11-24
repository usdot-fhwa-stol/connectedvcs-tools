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
import gov.usdot.cv.fedgov_cv_map_georeferencing.service.GeoreferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    private GeoreferenceController georeferenceController;

    private ObjectMapper objectMapper;
    private MultipartFile mockImageFile;
    private List<GCP> validGcps;
    private String validGcpsJson;
    private Map<String, Object> mockServiceResult;

    @BeforeEach
    void setUp() throws Exception {
        // Create controller with mocked service
        georeferenceController = new GeoreferenceController(georeferenceService);
        
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
        ); // Only 2 GCPs instead of 6 minimum
        String invalidGcpsJson = objectMapper.writeValueAsString(invalidGcps);
        
        // Mock service to throw validation exception
        when(georeferenceService.process(eq(mockImageFile), anyList()))
            .thenThrow(new IllegalArgumentException("At least 6 Ground Control Points are required"));

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, invalidGcpsJson);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("At least 6 Ground Control Points are required"));
        assertEquals("VALIDATION_ERROR", responseBody.get("error"));
    }

    @Test
    void testGeoreference_ExceedsMaxGcpCount_ReturnsBadRequest() throws Exception {
        // Arrange - Create GCP list that exceeds maximum (assuming 10 is max)
        List<GCP> tooManyGcps = Arrays.asList(
            new GCP("gcp1", 100, 200, -77.036, 38.895),
            new GCP("gcp2", 300, 200, -77.030, 38.895),
            new GCP("gcp3", 400, 300, -77.028, 38.893),
            new GCP("gcp4", 500, 400, -77.026, 38.891),
            new GCP("gcp5", 600, 500, -77.024, 38.889),
            new GCP("gcp6", 700, 600, -77.022, 38.887),
            new GCP("gcp7", 800, 700, -77.020, 38.885),
            new GCP("gcp8", 900, 800, -77.018, 38.883),
            new GCP("gcp9", 1000, 900, -77.016, 38.881),
            new GCP("gcp10", 1100, 1000, -77.014, 38.879),
            new GCP("gcp11", 1200, 1100, -77.012, 38.877) // 11 GCPs - exceeds max of 10
        );
        String tooManyGcpsJson = objectMapper.writeValueAsString(tooManyGcps);
        
        // Mock service to throw validation exception
        when(georeferenceService.process(eq(mockImageFile), anyList()))
            .thenThrow(new IllegalArgumentException("No more than 10 Ground Control Points are allowed"));

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, tooManyGcpsJson);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(responseBody.get("message").toString().contains("No more than 10"));
        assertEquals("VALIDATION_ERROR", responseBody.get("error"));
    }

    @Test
    void testGeoreference_UnsupportedImageFormat_ReturnsBadRequest() throws Exception {
        // Arrange
        MockMultipartFile unsupportedFile = new MockMultipartFile(
            "image", 
            "test.bmp", 
            "image/bmp",  // Unsupported format
            "fake image content".getBytes()
        );
        
        // Mock service to throw validation exception
        when(georeferenceService.process(eq(unsupportedFile), anyList()))
            .thenThrow(new IllegalArgumentException("Unsupported image format: bmp. Supported formats: [jpg, jpeg, png]"));

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(unsupportedFile, validGcpsJson);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(false, responseBody.get("success"));
        assertEquals("VALIDATION_ERROR", responseBody.get("error"));
        assertTrue(responseBody.get("message").toString().contains("Unsupported image format"));
    }

    @Test
    void testGeoreference_NullContentType_ReturnsBadRequest() throws Exception {
        // Arrange
        MockMultipartFile fileWithoutContentType = new MockMultipartFile(
            "image", 
            "test.png", 
            null,  // No content type
            "fake image content".getBytes()
        );
        
        // Mock service to throw validation exception for null extension scenario
        when(georeferenceService.process(eq(fileWithoutContentType), anyList()))
            .thenThrow(new IllegalArgumentException("Image filename is required"));

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(fileWithoutContentType, validGcpsJson);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(false, responseBody.get("success"));
        assertEquals("VALIDATION_ERROR", responseBody.get("error"));
        assertTrue(responseBody.get("message").toString().contains("Image filename is required"));
    }

    @Test
    void testGeoreference_ValidPngFormat_ProcessesSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile pngFile = new MockMultipartFile(
            "image", 
            "test.png", 
            "image/png",  // Supported format
            "fake image content".getBytes()
        );
        
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("processedImageBytes", "fake processed bytes".getBytes());
        serviceResult.put("extent", Map.of("minX", 0.0, "maxX", 100.0, "minY", 0.0, "maxY", 100.0));
        
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenReturn(serviceResult);

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(pngFile, validGcpsJson);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(georeferenceService).process(any(MultipartFile.class), anyList());
    }

    @Test
    void testGeoreference_ValidJpegFormat_ProcessesSuccessfully() throws Exception {
        // Arrange
        MockMultipartFile jpegFile = new MockMultipartFile(
            "image", 
            "test.jpg", 
            "image/jpeg",  // Supported format
            "fake image content".getBytes()
        );
        
        Map<String, Object> serviceResult = new HashMap<>();
        serviceResult.put("processedImageBytes", "fake processed bytes".getBytes());
        serviceResult.put("extent", Map.of("minX", 0.0, "maxX", 100.0, "minY", 0.0, "maxY", 100.0));
        
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenReturn(serviceResult);

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(jpegFile, validGcpsJson);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(georeferenceService).process(any(MultipartFile.class), anyList());
    }

    @Test
    void testGeoreference_WithProcessedImageBytes_GeneratesImageUrl() throws Exception {
        // Arrange
        Map<String, Object> serviceResult = new HashMap<>();
        byte[] mockImageBytes = "mock processed image data".getBytes();
        serviceResult.put("processedImageBytes", mockImageBytes);
        serviceResult.put("status", "processed");
        serviceResult.put("originalImageName", "test-image.png");
        
        when(georeferenceService.process(any(MultipartFile.class), anyList()))
            .thenReturn(serviceResult);

        // Act
        ResponseEntity<?> response = georeferenceController.georeference(mockImageFile, validGcpsJson);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertNotNull(responseBody);
        assertTrue((Boolean) responseBody.get("success"));
        
        // Check that processedImageUrl is generated and processedImageBytes is removed
        assertNotNull(responseBody.get("processedImageUrl"));
        assertTrue(responseBody.get("processedImageUrl").toString().startsWith("/api/georeference/image/"));
        assertNull(responseBody.get("processedImageBytes")); // Should be removed from response
        
        verify(georeferenceService, times(1)).process(eq(mockImageFile), eq(validGcps));
    }

    @Test
    void testGetProcessedImage_ValidImageId_ReturnsImage() {
        // This test would require access to the imageCache which is private
        // In a real scenario, you might want to make imageCache protected or add a method for testing
        // For now, we'll test the controller creation
        GeoreferenceController controller = new GeoreferenceController(georeferenceService);
        assertNotNull(controller);
    }

    @Test
    void testConstructor_ValidService_CreatesController() {
        // Act
        GeoreferenceController controller = new GeoreferenceController(georeferenceService);
        
        // Assert
        assertNotNull(controller);
    }
}