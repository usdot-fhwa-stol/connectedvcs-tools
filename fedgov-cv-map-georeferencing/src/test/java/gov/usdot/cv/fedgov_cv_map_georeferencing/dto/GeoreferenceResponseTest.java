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

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GeoreferenceResponseTest {

    @Test
    void testGeoreferenceResponse_ValidConstruction_CreatesCorrectObject() {
        // Arrange
        boolean success = true;
        String message = "Processing successful";
        Map<String, Object> details = new HashMap<>();
        details.put("imageSize", 1024);
        details.put("status", "processed");

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse(success, message, details);

        // Assert
        assertNotNull(response);
        assertTrue(response.success());
        assertEquals(message, response.message());
        assertEquals(details, response.details());
    }

    @Test
    void testGeoreferenceResponse_FailureCase_CreatesCorrectObject() {
        // Arrange
        boolean success = false;
        String message = "Processing failed";
        String errorDetails = "Invalid input parameters";

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse(success, message, errorDetails);

        // Assert
        assertNotNull(response);
        assertFalse(response.success());
        assertEquals(message, response.message());
        assertEquals(errorDetails, response.details());
    }

    @Test
    void testGeoreferenceResponse_NullValues_AllowsNullConstruction() {
        // Act
        GeoreferenceResponse response = new GeoreferenceResponse(false, null, null);

        // Assert
        assertNotNull(response);
        assertFalse(response.success());
        assertNull(response.message());
        assertNull(response.details());
    }

    @Test
    void testGeoreferenceResponse_EmptyMessage_AllowsEmptyString() {
        // Arrange
        String emptyMessage = "";
        Map<String, Object> details = new HashMap<>();

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse(true, emptyMessage, details);

        // Assert
        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("", response.message());
        assertEquals(details, response.details());
    }

    @Test
    void testGeoreferenceResponse_ComplexDetails_HandlesComplexObjects() {
        // Arrange
        boolean success = true;
        String message = "Complex processing complete";
        Map<String, Object> details = new HashMap<>();
        details.put("originalImageName", "test.jpg");
        details.put("imageSize", 2048L);
        details.put("gcpCount", 6);
        
        Map<String, Double> extent = new HashMap<>();
        extent.put("minLongitude", -77.126);
        extent.put("maxLongitude", -77.123);
        extent.put("minLatitude", 38.456);
        extent.put("maxLatitude", 38.459);
        details.put("extent", extent);

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse(success, message, details);

        // Assert
        assertNotNull(response);
        assertTrue(response.success());
        assertEquals(message, response.message());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseDetails = (Map<String, Object>) response.details();
        assertEquals("test.jpg", responseDetails.get("originalImageName"));
        assertEquals(2048L, responseDetails.get("imageSize"));
        assertEquals(6, responseDetails.get("gcpCount"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> responseExtent = (Map<String, Double>) responseDetails.get("extent");
        assertEquals(-77.126, responseExtent.get("minLongitude"));
    }

    @Test
    void testGeoreferenceResponse_Equality_SameValues() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("test", "value");
        
        GeoreferenceResponse response1 = new GeoreferenceResponse(true, "Test message", details);
        GeoreferenceResponse response2 = new GeoreferenceResponse(true, "Test message", details);

        // Act & Assert
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    void testGeoreferenceResponse_Equality_DifferentValues() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("test", "value");
        
        GeoreferenceResponse response1 = new GeoreferenceResponse(true, "Test message", details);
        GeoreferenceResponse response2 = new GeoreferenceResponse(false, "Test message", details);

        // Act & Assert
        assertNotEquals(response1, response2);
    }

    @Test
    void testGeoreferenceResponse_ToString_ContainsAllFields() {
        // Arrange
        Map<String, Object> details = new HashMap<>();
        details.put("status", "processed");
        GeoreferenceResponse response = new GeoreferenceResponse(true, "Success", details);

        // Act
        String toString = response.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("true"));
        assertTrue(toString.contains("Success"));
        assertTrue(toString.contains("processed"));
    }

    @Test
    void testGeoreferenceResponse_RecordMethods_WorkCorrectly() {
        // Arrange
        boolean expectedSuccess = true;
        String expectedMessage = "Test successful";
        Object expectedDetails = "test details";

        // Act
        GeoreferenceResponse response = new GeoreferenceResponse(expectedSuccess, expectedMessage, expectedDetails);

        // Assert
        assertEquals(expectedSuccess, response.success());
        assertEquals(expectedMessage, response.message());
        assertEquals(expectedDetails, response.details());
    }

    @Test
    void testGeoreferenceResponse_DifferentDetailTypes_HandlesVariousTypes() {
        // Test with String details
        GeoreferenceResponse stringResponse = new GeoreferenceResponse(false, "Error", "Error message");
        assertEquals("Error message", stringResponse.details());

        // Test with Integer details
        GeoreferenceResponse intResponse = new GeoreferenceResponse(true, "Count", 42);
        assertEquals(42, intResponse.details());

        // Test with Boolean details
        GeoreferenceResponse boolResponse = new GeoreferenceResponse(true, "Flag", Boolean.TRUE);
        assertEquals(Boolean.TRUE, boolResponse.details());
    }
}