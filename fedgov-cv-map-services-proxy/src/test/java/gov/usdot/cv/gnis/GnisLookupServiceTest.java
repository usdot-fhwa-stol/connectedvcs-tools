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
package gov.usdot.cv.gnis;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import gov.usdot.cv.gnis.models.GnisFeature;
import gov.usdot.cv.gnis.services.GnisLookupService;

public class GnisLookupServiceTest {
    private final GnisLookupService service = new GnisLookupService();

    @Test
    void composeFullURLReturn() {
        String url = service.composeFullURL(38.9847, -77.0947);
        assertTrue(url.startsWith("https://geocoding.geo.census.gov/geocoder/geographies/coordinates?x=-77.0947&y=38.9847"));
        assertTrue(url.contains("format=json"));
    }

    @Test
    void parseGnisResponseOrdersMostSpecificFirst() {
        String response = "{\"result\":{\"geographies\":{"
            + "\"States\":[{\"NAME\":\"Maryland\",\"STATENS\":\"01714934\",\"STUSAB\":\"MD\"}],"
            + "\"Counties\":[{\"NAME\":\"Montgomery County\",\"COUNTYNS\":\"01712500\"}],"
            + "\"Census Designated Places\":[{\"NAME\":\"Bethesda CDP\",\"PLACENS\":\"02389206\"}]}}}";
        List<GnisFeature> features = service.parseGnisResponse(response);
        assertEquals(3, features.size());
        assertEquals(2389206L, features.get(0).getGnisId());
        assertEquals("Place", features.get(0).getType());
        assertEquals("County", features.get(1).getType());
        assertEquals(1714934L, features.get(2).getGnisId());
        assertEquals("MD", features.get(2).getState());
        assertEquals("Montgomery County", features.get(0).getCounty());
        assertEquals("", features.get(2).getCounty());
        assertNull(features.get(0).getDistanceMiles());
    }

    @Test
    void parseGnisResponseSkipsInvalidCodes() {
        String response = "{\"result\":{\"geographies\":{"
            + "\"Counties\":[{\"NAME\":\"Bad\",\"COUNTYNS\":\"abc\"},{\"NAME\":\"Too Big\",\"COUNTYNS\":\"99999999\"}]}}}";
        assertTrue(service.parseGnisResponse(response).isEmpty());
    }

    @Test
    void parseGnisResponseNoGeographies() {
        assertNull(service.parseGnisResponse("{\"result\":{}}"));
    }

    @Test
    void parseGnisInvalidResponse() {
        assertNull(service.parseGnisResponse("{\"result\":{\"invalidJson\"}}"));
    }

    @Test
    void parseGnisNullResponse() {
        assertNull(service.parseGnisResponse(null));
    }

    @Test
    void composeNearbyURLReturn() {
        String url = service.composeNearbyURL(3, 38.9847, -77.0947);
        assertTrue(url.startsWith("https://carto.nationalmap.gov/arcgis/rest/services/geonames/MapServer/3/query?"));
        assertTrue(url.contains("distance=5.0"));
        assertTrue(url.contains("units=esriSRUnit_StatuteMile"));
    }

    @Test
    void parseNearbyResponseComputesDistance() {
        String response = "{\"features\":["
            + "{\"attributes\":{\"gaz_id\":2390788,\"gaz_name\":\"Town of Chevy Chase\",\"gaz_featureclass\":\"Civil\","
            + "\"state_alpha\":\"MD\",\"county_name\":\"Montgomery\"},\"geometry\":{\"points\":[[-77.0832,38.9819]]}},"
            + "{\"attributes\":{\"gaz_id\":528950,\"gaz_name\":\"Milkhouse Ford\",\"gaz_featureclass\":\"Crossing\","
            + "\"state_alpha\":\"DC\",\"county_name\":\"District of Columbia\"},\"geometry\":{\"x\":-77.0475,\"y\":38.9651}},"
            + "{\"attributes\":{\"gaz_id\":1,\"gaz_name\":\"No Geometry\"}}]}";
        List<GnisFeature> features = service.parseNearbyResponse(response, 38.9847, -77.0947);
        assertEquals(2, features.size());
        assertEquals("Incorporated Place", features.get(0).getType());
        assertEquals("Montgomery", features.get(0).getCounty());
        assertEquals(0.6, features.get(0).getDistanceMiles(), 0.05);
        assertEquals("Crossing", features.get(1).getType());
        assertEquals(2.9, features.get(1).getDistanceMiles(), 0.05);
    }

    @Test
    void parseNearbyInvalidResponse() {
        assertNull(service.parseNearbyResponse("{\"error\":{\"code\":400}}", 38.9847, -77.0947));
        assertNull(service.parseNearbyResponse(null, 38.9847, -77.0947));
    }

    @Test
    void mergeFeaturesDedupesAndSortsByDistance() {
        List<GnisFeature> containing = Arrays.asList(
            new GnisFeature(2389206L, "Bethesda CDP", "Place", "Montgomery County", "MD", null),
            new GnisFeature(1714934L, "Maryland", "State", "", "MD", null));
        List<GnisFeature> nearby = new ArrayList<>(Arrays.asList(
            new GnisFeature(3L, "Far", "Populated Place", "Montgomery", "MD", 4.0),
            new GnisFeature(2389206L, "Bethesda", "Census Designated Place", "Montgomery", "MD", 0.1),
            new GnisFeature(4L, "Near", "Crossing", "Montgomery", "MD", 1.0),
            new GnisFeature(4L, "Near", "Crossing", "District of Columbia", "DC", 1.2)));
        List<GnisFeature> features = service.mergeFeatures(containing, nearby);
        assertEquals(4, features.size());
        assertEquals("Bethesda CDP", features.get(0).getName());
        assertEquals("Maryland", features.get(1).getName());
        assertEquals("Near", features.get(2).getName());
        assertEquals("MD", features.get(2).getState());
        assertEquals("Far", features.get(3).getName());
    }

    @Test
    void mergeFeaturesWithoutContaining() {
        List<GnisFeature> nearby = new ArrayList<>(Arrays.asList(
            new GnisFeature(5L, "Only", "Populated Place", "Montgomery", "MD", 2.0)));
        assertEquals(1, service.mergeFeatures(null, nearby).size());
    }
}
