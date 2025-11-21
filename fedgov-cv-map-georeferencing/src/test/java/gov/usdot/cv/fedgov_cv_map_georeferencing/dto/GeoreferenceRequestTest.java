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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoreferenceRequestTest {

    private GeoreferenceRequest georeferenceRequest;
    private MultipartFile mockImageFile;
    private List<GCP> validGcps;

    @BeforeEach
    void setUp() {
        georeferenceRequest = new GeoreferenceRequest();
        
        mockImageFile = new MockMultipartFile(
            "image",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        validGcps = Arrays.asList(
            new GCP("GCP1", 100, 200, -77.123, 38.456),
            new GCP("GCP2", 300, 400, -77.124, 38.457),
            new GCP("GCP3", 500, 600, -77.125, 38.458),
            new GCP("GCP4", 700, 800, -77.126, 38.459)
        );
    }

    @Test
    void testGeoreferenceRequest_SetAndGetImage_WorksCorrectly() {
        // Act
        georeferenceRequest.setImage(mockImageFile);
        
        // Assert
        assertEquals(mockImageFile, georeferenceRequest.getImage());
        assertEquals("test-image.jpg", georeferenceRequest.getImage().getOriginalFilename());
    }

    @Test
    void testGeoreferenceRequest_SetAndGetGcps_WorksCorrectly() {
        // Act
        georeferenceRequest.setGcps(validGcps);
        
        // Assert
        assertEquals(validGcps, georeferenceRequest.getGcps());
        assertEquals(4, georeferenceRequest.getGcps().size());
        assertEquals("GCP1", georeferenceRequest.getGcps().get(0).pointId());
    }

    @Test
    void testGeoreferenceRequest_NullImage_AllowsNullValue() {
        // Act
        georeferenceRequest.setImage(null);
        
        // Assert
        assertNull(georeferenceRequest.getImage());
    }

    @Test
    void testGeoreferenceRequest_NullGcps_AllowsNullValue() {
        // Act
        georeferenceRequest.setGcps(null);
        
        // Assert
        assertNull(georeferenceRequest.getGcps());
    }

    @Test
    void testGeoreferenceRequest_EmptyGcps_AllowsEmptyList() {
        // Arrange
        List<GCP> emptyGcps = Arrays.asList();
        
        // Act
        georeferenceRequest.setGcps(emptyGcps);
        
        // Assert
        assertEquals(emptyGcps, georeferenceRequest.getGcps());
        assertEquals(0, georeferenceRequest.getGcps().size());
    }

    @Test
    void testGeoreferenceRequest_DefaultConstructor_CreatesEmptyObject() {
        // Arrange & Act
        GeoreferenceRequest newRequest = new GeoreferenceRequest();
        
        // Assert
        assertNotNull(newRequest);
        assertNull(newRequest.getImage());
        assertNull(newRequest.getGcps());
    }

    @Test
    void testGeoreferenceRequest_FullWorkflow_SetsBothFields() {
        // Act
        georeferenceRequest.setImage(mockImageFile);
        georeferenceRequest.setGcps(validGcps);
        
        // Assert
        assertNotNull(georeferenceRequest.getImage());
        assertNotNull(georeferenceRequest.getGcps());
        assertEquals("test-image.jpg", georeferenceRequest.getImage().getOriginalFilename());
        assertEquals(4, georeferenceRequest.getGcps().size());
    }

    @Test
    void testGeoreferenceRequest_ModifyGcpsList_ReflectsChanges() {
        // Arrange
        List<GCP> initialGcps = Arrays.asList(
            new GCP("GCP1", 100, 200, -77.123, 38.456)
        );
        georeferenceRequest.setGcps(initialGcps);
        
        // Act
        georeferenceRequest.setGcps(validGcps);
        
        // Assert
        assertEquals(4, georeferenceRequest.getGcps().size());
        assertNotEquals(initialGcps, georeferenceRequest.getGcps());
    }

    @Test
    void testGeoreferenceRequest_Equals_SameContent() {
        // Arrange
        GeoreferenceRequest request1 = new GeoreferenceRequest();
        request1.setImage(mockImageFile);
        request1.setGcps(validGcps);
        
        GeoreferenceRequest request2 = new GeoreferenceRequest();
        request2.setImage(mockImageFile);
        request2.setGcps(validGcps);
        
        // Act & Assert
        assertEquals(request1, request2);
    }

    @Test
    void testGeoreferenceRequest_ToString_ContainsClassInfo() {
        // Arrange
        georeferenceRequest.setImage(mockImageFile);
        georeferenceRequest.setGcps(validGcps);
        
        // Act
        String toString = georeferenceRequest.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("GeoreferenceRequest"));
    }

    @Test
    void testGeoreferenceRequest_HashCode_ConsistentForSameObject() {
        // Arrange
        georeferenceRequest.setImage(mockImageFile);
        georeferenceRequest.setGcps(validGcps);
        
        // Act
        int hashCode1 = georeferenceRequest.hashCode();
        int hashCode2 = georeferenceRequest.hashCode();
        
        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testGeoreferenceRequest_LombokGeneratedMethods_WorkCorrectly() {
        // This test verifies that Lombok-generated methods work correctly
        // Arrange
        georeferenceRequest.setImage(mockImageFile);
        georeferenceRequest.setGcps(validGcps);
        
        // Act & Assert - Test getter/setter consistency
        assertEquals(mockImageFile, georeferenceRequest.getImage());
        assertEquals(validGcps, georeferenceRequest.getGcps());
        
        // Test that object is properly constructed
        assertNotNull(georeferenceRequest);
        
        // Test toString is not null (Lombok generates this)
        assertNotNull(georeferenceRequest.toString());
        
        // Test hashCode is consistent (Lombok generates this)
        int hash1 = georeferenceRequest.hashCode();
        int hash2 = georeferenceRequest.hashCode();
        assertEquals(hash1, hash2);
    }
}