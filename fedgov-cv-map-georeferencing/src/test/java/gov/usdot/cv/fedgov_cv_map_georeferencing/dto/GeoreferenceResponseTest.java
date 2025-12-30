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
package gov.usdot.cv.fedgov_cv_map_georeferencing.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeoreferenceResponseTest {

    @Test
    void testGeoreferenceResponse_ValidConstruction_CreatesCorrectObject() {
        // Arrange
        boolean success = true;
        String message = "Processing successful";
        GeoreferenceResponse.GeoreferenceDetails details = new GeoreferenceResponse.GeoreferenceDetails();
        details.setImageSize(1024);
        details.setOriginalImageName("test.jpg");

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setDetails(details);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(details, response.getDetails());
        assertEquals(1024, response.getDetails().getImageSize());
        assertEquals("test.jpg", response.getDetails().getOriginalImageName());
    }

    @Test
    void testGeoreferenceResponse_FailureCase_CreatesCorrectObject() {
        // Arrange
        boolean success = false;
        String message = "Processing failed";
        GeoreferenceResponse.GeoreferenceDetails details = new GeoreferenceResponse.GeoreferenceDetails();
        details.setStatus("FAILED");

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse();
        response.setSuccess(success);
        response.setMessage(message);
        response.setDetails(details);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals("FAILED", response.getDetails().getStatus());
    }

    @Test
    void testGeoreferenceResponse_EmptyMessage_AllowsEmptyString() {
        // Arrange
        String emptyMessage = "";
        GeoreferenceResponse.GeoreferenceDetails details = new GeoreferenceResponse.GeoreferenceDetails();

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse();
        response.setSuccess(true);
        response.setMessage(emptyMessage);
        response.setDetails(details);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("", response.getMessage());
        assertEquals(details, response.getDetails());
    }

    @Test
    void testGeoreferenceResponse_ComplexDetails_HandlesComplexObjects() {
        // Arrange
        GeoreferenceResponse.GeoreferenceDetails details = new GeoreferenceResponse.GeoreferenceDetails();
        details.setOriginalImageName("test.jpg");
        details.setImageSize(1024);
        details.setGcpCount(6);
        details.setProcessedImageUrl("/api/georeference/image/123");
        
        GeoreferenceResponse.GeoreferenceDetails.Extent extent = new GeoreferenceResponse.GeoreferenceDetails.Extent();
        extent.setMinLongitude(-77.15);
        extent.setMaxLongitude(-77.14);
        extent.setMinLatitude(38.95);
        extent.setMaxLatitude(38.96);
        details.setExtent(extent);
        
        // Act
        GeoreferenceResponse response = new GeoreferenceResponse();
        response.setSuccess(true);
        response.setMessage("Complex processing completed");
        response.setDetails(details);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Complex processing completed", response.getMessage());
        assertNotNull(response.getDetails());
        assertEquals("test.jpg", response.getDetails().getOriginalImageName());
        assertEquals(1024, response.getDetails().getImageSize());
        assertEquals(6, response.getDetails().getGcpCount());
        assertEquals("/api/georeference/image/123", response.getDetails().getProcessedImageUrl());
        assertNotNull(response.getDetails().getExtent());
        assertEquals(-77.15, response.getDetails().getExtent().getMinLongitude());
        assertEquals(-77.14, response.getDetails().getExtent().getMaxLongitude());
        assertEquals(38.95, response.getDetails().getExtent().getMinLatitude());
        assertEquals(38.96, response.getDetails().getExtent().getMaxLatitude());
    }

    @Test
    void testGeoreferenceResponse_NullDetails_HandlesNullCorrectly() {
        // Act
        GeoreferenceResponse response = new GeoreferenceResponse();
        response.setSuccess(false);
        response.setMessage("Error with no details");
        response.setDetails(null);

        // Assert
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("Error with no details", response.getMessage());
        assertNull(response.getDetails());
    }

    @Test
    void testGeoreferenceResponse_Equality_SameValues() {
        // Arrange
        GeoreferenceResponse.GeoreferenceDetails details1 = new GeoreferenceResponse.GeoreferenceDetails();
        details1.setOriginalImageName("test.jpg");
        details1.setImageSize(1024);
        
        GeoreferenceResponse.GeoreferenceDetails details2 = new GeoreferenceResponse.GeoreferenceDetails();
        details2.setOriginalImageName("test.jpg");
        details2.setImageSize(1024);
        
        GeoreferenceResponse response1 = new GeoreferenceResponse();
        response1.setSuccess(true);
        response1.setMessage("Success");
        response1.setDetails(details1);
        
        GeoreferenceResponse response2 = new GeoreferenceResponse();
        response2.setSuccess(true);
        response2.setMessage("Success");
        response2.setDetails(details2);

        // Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testGeoreferenceResponse_Equality_DifferentValues() {
        // Arrange
        GeoreferenceResponse.GeoreferenceDetails details1 = new GeoreferenceResponse.GeoreferenceDetails();
        details1.setOriginalImageName("test1.jpg");
        
        GeoreferenceResponse.GeoreferenceDetails details2 = new GeoreferenceResponse.GeoreferenceDetails();
        details2.setOriginalImageName("test2.jpg");
        
        GeoreferenceResponse response1 = new GeoreferenceResponse();
        response1.setSuccess(true);
        response1.setMessage("Success");
        response1.setDetails(details1);
        
        GeoreferenceResponse response2 = new GeoreferenceResponse();
        response2.setSuccess(false);
        response2.setMessage("Failure");
        response2.setDetails(details2);

        // Assert
        assertNotEquals(response1, response2);
        assertNotEquals(response1.isSuccess(), response2.isSuccess());
        assertNotEquals(response1.getMessage(), response2.getMessage());
        assertNotEquals(response1.getDetails(), response2.getDetails());
    }

    @Test
    void testGeoreferenceResponse_LombokMethods_WorkCorrectly() {
        // Arrange
        GeoreferenceResponse.GeoreferenceDetails details = new GeoreferenceResponse.GeoreferenceDetails();
        details.setOriginalImageName("test.jpg");
        details.setGcpCount(6);
        
        GeoreferenceResponse response = new GeoreferenceResponse();
        response.setSuccess(true);
        response.setMessage("Test message");
        response.setDetails(details);
        
        // Test getter methods
        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals(details, response.getDetails());
        
        // Test toString method (from Lombok @Data)
        assertNotNull(response.toString());
        assertTrue(response.toString().contains("GeoreferenceResponse"));
        
        // Test equals and hashCode (from Lombok @Data)
        GeoreferenceResponse sameResponse = new GeoreferenceResponse();
        sameResponse.setSuccess(true);
        sameResponse.setMessage("Test message");
        sameResponse.setDetails(details);
        
        assertEquals(response, sameResponse);
        assertEquals(response.hashCode(), sameResponse.hashCode());
    }

    @Test
    void testGeoreferenceDetails_ExtentFunctionality() {
        // Test extent creation and usage
        GeoreferenceResponse.GeoreferenceDetails.Extent extent = new GeoreferenceResponse.GeoreferenceDetails.Extent();
        extent.setMinLongitude(-77.15036875159703);
        extent.setMaxLongitude(-77.14684744260903);
        extent.setMinLatitude(38.95507925856188);
        extent.setMaxLatitude(38.9567156465794);

        // Test getters
        assertEquals(-77.15036875159703, extent.getMinLongitude());
        assertEquals(-77.14684744260903, extent.getMaxLongitude());
        assertEquals(38.95507925856188, extent.getMinLatitude());
        assertEquals(38.9567156465794, extent.getMaxLatitude());

        // Test toString
        assertNotNull(extent.toString());
        assertTrue(extent.toString().contains("Extent"));
    }

    @Test
    void testAllArgsConstructor() {
        // Test @AllArgsConstructor from Lombok
        GeoreferenceResponse.GeoreferenceDetails details = new GeoreferenceResponse.GeoreferenceDetails();
        details.setOriginalImageName("test.jpg");
        
        GeoreferenceResponse response = new GeoreferenceResponse(true, "Success", details);
        
        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertEquals(details, response.getDetails());
    }

    @Test
    void testNoArgsConstructor() {
        // Test @NoArgsConstructor from Lombok
        GeoreferenceResponse response = new GeoreferenceResponse();
        
        // Default values should be false/null
        assertFalse(response.isSuccess());
        assertNull(response.getMessage());
        assertNull(response.getDetails());
    }
}