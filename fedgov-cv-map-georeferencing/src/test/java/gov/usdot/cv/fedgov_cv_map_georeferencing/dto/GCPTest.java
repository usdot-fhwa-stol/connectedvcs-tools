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

class GCPTest {

    @Test
    void testGCP_ValidConstruction_CreatesCorrectObject() {
        // Arrange
        String pointId = "GCP1";
        Integer imageX = 100;
        Integer imageY = 200;
        Double longitude = -77.123;
        Double latitude = 38.456;

        // Act
        GCP gcp = new GCP(pointId, imageX, imageY, longitude, latitude);

        // Assert
        assertNotNull(gcp);
        assertEquals(pointId, gcp.pointId());
        assertEquals(imageX, gcp.imageX());
        assertEquals(imageY, gcp.imageY());
        assertEquals(longitude, gcp.longitude());
        assertEquals(latitude, gcp.latitude());
    }

    @Test
    void testGCP_NullValues_AllowsNullConstruction() {
        // Act
        GCP gcp = new GCP(null, null, null, null, null);

        // Assert
        assertNotNull(gcp);
        assertNull(gcp.pointId());
        assertNull(gcp.imageX());
        assertNull(gcp.imageY());
        assertNull(gcp.longitude());
        assertNull(gcp.latitude());
    }

    @Test
    void testGCP_Equality_SameValues() {
        // Arrange
        GCP gcp1 = new GCP("GCP1", 100, 200, -77.123, 38.456);
        GCP gcp2 = new GCP("GCP1", 100, 200, -77.123, 38.456);

        // Act & Assert
        assertEquals(gcp1, gcp2);
        assertEquals(gcp1.hashCode(), gcp2.hashCode());
    }

    @Test
    void testGCP_Equality_DifferentValues() {
        // Arrange
        GCP gcp1 = new GCP("GCP1", 100, 200, -77.123, 38.456);
        GCP gcp2 = new GCP("GCP2", 100, 200, -77.123, 38.456);

        // Act & Assert
        assertNotEquals(gcp1, gcp2);
    }

    @Test
    void testGCP_ToString_ContainsAllFields() {
        // Arrange
        GCP gcp = new GCP("GCP1", 100, 200, -77.123, 38.456);

        // Act
        String toString = gcp.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("GCP1"));
        assertTrue(toString.contains("100"));
        assertTrue(toString.contains("200"));
        assertTrue(toString.contains("-77.123"));
        assertTrue(toString.contains("38.456"));
    }

    @Test
    void testGCP_RecordMethods_WorkCorrectly() {
        // Arrange
        GCP gcp = new GCP("TEST_ID", 150, 250, -77.999, 38.999);

        // Assert all accessor methods work
        assertEquals("TEST_ID", gcp.pointId());
        assertEquals(Integer.valueOf(150), gcp.imageX());
        assertEquals(Integer.valueOf(250), gcp.imageY());
        assertEquals(Double.valueOf(-77.999), gcp.longitude());
        assertEquals(Double.valueOf(38.999), gcp.latitude());
    }

    @Test
    void testGCP_NegativeCoordinates_AllowsNegativeValues() {
        // Arrange & Act
        GCP gcp = new GCP("NEG_TEST", -50, -100, -180.0, -90.0);

        // Assert
        assertEquals("NEG_TEST", gcp.pointId());
        assertEquals(Integer.valueOf(-50), gcp.imageX());
        assertEquals(Integer.valueOf(-100), gcp.imageY());
        assertEquals(Double.valueOf(-180.0), gcp.longitude());
        assertEquals(Double.valueOf(-90.0), gcp.latitude());
    }

    @Test
    void testGCP_ZeroCoordinates_AllowsZeroValues() {
        // Arrange & Act
        GCP gcp = new GCP("ZERO_TEST", 0, 0, 0.0, 0.0);

        // Assert
        assertEquals("ZERO_TEST", gcp.pointId());
        assertEquals(Integer.valueOf(0), gcp.imageX());
        assertEquals(Integer.valueOf(0), gcp.imageY());
        assertEquals(Double.valueOf(0.0), gcp.longitude());
        assertEquals(Double.valueOf(0.0), gcp.latitude());
    }

    @Test
    void testGCP_LargeValues_HandlesLargeNumbers() {
        // Arrange & Act
        GCP gcp = new GCP("LARGE_TEST", 99999, 88888, 180.0, 90.0);

        // Assert
        assertEquals("LARGE_TEST", gcp.pointId());
        assertEquals(Integer.valueOf(99999), gcp.imageX());
        assertEquals(Integer.valueOf(88888), gcp.imageY());
        assertEquals(Double.valueOf(180.0), gcp.longitude());
        assertEquals(Double.valueOf(90.0), gcp.latitude());
    }
}