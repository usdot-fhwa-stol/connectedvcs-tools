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

import java.net.URLDecoder;
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
        assertFalse(url.contains("County%20Subdivisions"));
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
        assertEquals("Census Designated Place", features.get(0).getType());
        assertEquals("County", features.get(1).getType());
        assertEquals(1714934L, features.get(2).getGnisId());
        assertEquals("MD", features.get(2).getState());
        assertEquals("Montgomery County", features.get(0).getCounty());
        assertEquals("", features.get(2).getCounty());
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
    void composeSearchURLBuildsWhereClause() throws Exception {
        String url = URLDecoder.decode(service.composeSearchURL(3, "Coeur d'Alene", "id"), "UTF-8");
        assertTrue(url.startsWith("https://carto.nationalmap.gov/arcgis/rest/services/geonames/MapServer/3/query?"));
        assertTrue(url.contains("where=UPPER(gaz_name) LIKE '%COEUR D''ALENE%' AND state_alpha = 'ID'"));
    }

    @Test
    void composeSearchURLWithoutState() throws Exception {
        String url = URLDecoder.decode(service.composeSearchURL(1, "Springfield", null), "UTF-8");
        assertTrue(url.contains("where=UPPER(gaz_name) LIKE '%SPRINGFIELD%'&"));
        assertFalse(url.contains("state_alpha ="));
    }

    @Test
    void composeSearchURLRejectsUnsafeInput() {
        assertThrows(IllegalArgumentException.class, () -> service.composeSearchURL(1, "x%' OR 1=1 --", null));
        assertThrows(IllegalArgumentException.class, () -> service.composeSearchURL(1, "Springfield", "MD' OR '1'='1"));
        assertThrows(IllegalArgumentException.class, () -> service.composeSearchURL(1, "a", null));
    }

    @Test
    void parseSearchResponseMapsFeatureClasses() {
        String response = "{\"features\":["
            + "{\"attributes\":{\"gaz_id\":2395940,\"gaz_name\":\"City of Springfield\",\"gaz_featureclass\":\"Civil\","
            + "\"state_alpha\":\"IL\",\"county_name\":\"Sangamon\"}},"
            + "{\"attributes\":{\"gaz_id\":426595,\"gaz_name\":\"Springfield\",\"gaz_featureclass\":\"Populated Place\","
            + "\"state_alpha\":\"IL\",\"county_name\":\"Sangamon\"}},"
            + "{\"attributes\":{\"gaz_name\":\"No ID\"}}]}";
        List<GnisFeature> features = service.parseSearchResponse(response);
        assertEquals(2, features.size());
        assertEquals("Incorporated Place", features.get(0).getType());
        assertEquals("Sangamon", features.get(0).getCounty());
        assertEquals("Populated Place", features.get(1).getType());
    }

    @Test
    void parseSearchInvalidResponse() {
        assertNull(service.parseSearchResponse("{\"error\":{\"code\":400}}"));
        assertNull(service.parseSearchResponse(null));
    }

    @Test
    void rankSearchResultsPutsExactMatchesFirst() {
        List<GnisFeature> features = new ArrayList<>(Arrays.asList(
            new GnisFeature(1L, "East Springfield", "Populated Place", "Sangamon", "IL"),
            new GnisFeature(2L, "Springfield Center", "Populated Place", "Sangamon", "IL"),
            new GnisFeature(3L, "Springfield", "Populated Place", "Sangamon", "IL"),
            new GnisFeature(3L, "Springfield", "Populated Place", "Sangamon", "IL")));
        List<GnisFeature> ranked = service.rankSearchResults(features, "springfield");
        assertEquals(3, ranked.size());
        assertEquals("Springfield", ranked.get(0).getName());
        assertEquals("Springfield Center", ranked.get(1).getName());
        assertEquals("East Springfield", ranked.get(2).getName());
    }

    @Test
    void rankSearchResultsCapsResults() {
        List<GnisFeature> features = new ArrayList<>();
        for (long i = 0; i < 100; i++) {
            features.add(new GnisFeature(i, "Place " + i, "Populated Place", "County", "MD"));
        }
        assertEquals(30, service.rankSearchResults(features, "Place").size());
    }

    @Test
    void rankSearchResultsIgnoresOfficialPlaceWording() {
        List<GnisFeature> features = new ArrayList<>(Arrays.asList(
            new GnisFeature(1L, "Kelley Corners", "Populated Place", "Lake", "IL"),
            new GnisFeature(2L, "City of Kelley", "Incorporated Place", "Story", "IA"),
            new GnisFeature(3L, "Kelley Census Designated Place", "Census Designated Place", "Story", "IA")));
        List<GnisFeature> ranked = service.rankSearchResults(features, "Kelley");
        assertEquals("City of Kelley", ranked.get(0).getName());
        assertEquals("Kelley Census Designated Place", ranked.get(1).getName());
        assertEquals("Kelley Corners", ranked.get(2).getName());
    }

    @Test
    void parseGnisResponseMergesSharedGnisIds() {
        String response = "{\"result\":{\"geographies\":{"
            + "\"States\":[{\"NAME\":\"District of Columbia\",\"STATENS\":\"01702382\",\"STUSAB\":\"DC\"}],"
            + "\"Counties\":[{\"NAME\":\"District of Columbia\",\"COUNTYNS\":\"01702382\"}],"
            + "\"Incorporated Places\":[{\"NAME\":\"Washington city\",\"PLACENS\":\"02390665\"}]}}}";
        List<GnisFeature> features = service.parseGnisResponse(response);
        assertEquals(2, features.size());
        assertEquals("Washington city", features.get(0).getName());
        assertEquals(1702382L, features.get(1).getGnisId());
        assertEquals("County / State", features.get(1).getType());
        assertEquals("DC", features.get(1).getState());
    }

    @Test
    void composeStateSearchURLMatchesNameOrPostalCode() throws Exception {
        String url = URLDecoder.decode(service.composeStateSearchURL("va"), "UTF-8");
        assertTrue(url.startsWith("https://tigerweb.geo.census.gov/arcgis/rest/services/TIGERweb/tigerWMS_Current/MapServer/80/query?"));
        assertTrue(url.contains("where=UPPER(NAME) LIKE '%VA%' OR UPPER(STUSAB) = 'VA'"));
    }

    @Test
    void composeCountySearchURLFiltersByStateFips() throws Exception {
        String url = URLDecoder.decode(service.composeCountySearchURL("Montgomery", "md"), "UTF-8");
        assertTrue(url.contains("/MapServer/82/query?"));
        assertTrue(url.contains("where=UPPER(NAME) LIKE '%MONTGOMERY%' AND STATE = '24'"));
        assertNull(service.composeCountySearchURL("Montgomery", "ZZ"));
        assertFalse(URLDecoder.decode(service.composeCountySearchURL("Montgomery", null), "UTF-8").contains("STATE ="));
        assertThrows(IllegalArgumentException.class, () -> service.composeCountySearchURL("x' OR 1=1", null));
    }

    @Test
    void parseStateAndCountySearchResponses() {
        List<GnisFeature> states = service.parseStateSearchResponse("{\"features\":["
            + "{\"attributes\":{\"NAME\":\"Virginia\",\"STATENS\":\"01779803\",\"STUSAB\":\"VA\"}}]}");
        assertEquals(1, states.size());
        assertEquals(1779803L, states.get(0).getGnisId());
        assertEquals("State", states.get(0).getType());
        assertEquals("VA", states.get(0).getState());

        List<GnisFeature> counties = service.parseCountySearchResponse("{\"features\":["
            + "{\"attributes\":{\"NAME\":\"Montgomery County\",\"COUNTYNS\":\"01712500\",\"STATE\":\"24\"}}]}");
        assertEquals(1, counties.size());
        assertEquals("County", counties.get(0).getType());
        assertEquals("MD", counties.get(0).getState());
        assertEquals("Montgomery County", counties.get(0).getCounty());

        assertNull(service.parseStateSearchResponse(null));
        assertNull(service.parseCountySearchResponse("{\"error\":{}}"));
    }

    @Test
    void rankSearchResultsPrefersStatesThenCounties() {
        List<GnisFeature> features = new ArrayList<>(Arrays.asList(
            new GnisFeature(1L, "City of Washington", "Incorporated Place", "District of Columbia", "DC"),
            new GnisFeature(2L, "Washington County", "County", "Washington County", "MD"),
            new GnisFeature(3L, "Washington Grove", "Incorporated Place", "Montgomery", "MD"),
            new GnisFeature(4L, "Washington", "State", "", "WA")));
        List<GnisFeature> ranked = service.rankSearchResults(features, "Washington");
        assertEquals("State", ranked.get(0).getType());
        assertEquals("County", ranked.get(1).getType());
        assertEquals("City of Washington", ranked.get(2).getName());
        assertEquals("Washington Grove", ranked.get(3).getName());
    }

    @Test
    void rankSearchResultsMatchesStatePostalCode() {
        List<GnisFeature> features = new ArrayList<>(Arrays.asList(
            new GnisFeature(1L, "Vaughn", "Populated Place", "Guadalupe", "NM"),
            new GnisFeature(2L, "Virginia", "State", "", "VA")));
        assertEquals("Virginia", service.rankSearchResults(features, "va").get(0).getName());
    }
}
