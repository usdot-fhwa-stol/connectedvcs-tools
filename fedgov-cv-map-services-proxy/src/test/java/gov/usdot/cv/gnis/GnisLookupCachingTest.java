/*
 * Copyright (C) 2026 LEIDOS.
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import gov.usdot.cv.gnis.models.GnisFeature;
import gov.usdot.cv.gnis.services.GnisApiClient;
import gov.usdot.cv.gnis.services.GnisLookupService;

public class GnisLookupCachingTest {
    private static final String LOOKUP_RESPONSE = "{\"result\":{\"geographies\":{"
        + "\"States\":[{\"NAME\":\"Maryland\",\"STATENS\":\"01714934\",\"STUSAB\":\"MD\"}],"
        + "\"Counties\":[{\"NAME\":\"Montgomery County\",\"COUNTYNS\":\"01712500\"}]}}}";
    private static final String STATES_RESPONSE = "{\"features\":[{\"attributes\":"
        + "{\"NAME\":\"Maryland\",\"STATENS\":\"01714934\",\"STUSAB\":\"MD\"}}]}";
    private static final String EMPTY_SEARCH_RESPONSE = "{\"features\":[]}";

    /** Answers GNIS calls from canned responses and counts them, instead of calling out. */
    private static class FakeApiClient extends GnisApiClient {
        int calls = 0;
        String failingUrlPart = null;

        @Override
        public CompletableFuture<String> fetch(String url) {
            calls++;
            if (failingUrlPart != null && url.contains(failingUrlPart)) {
                return CompletableFuture.completedFuture(null);
            }
            if (url.contains("geocoder")) {
                return CompletableFuture.completedFuture(LOOKUP_RESPONSE);
            }
            if (url.contains("MapServer/80/")) {
                return CompletableFuture.completedFuture(STATES_RESPONSE);
            }
            return CompletableFuture.completedFuture(EMPTY_SEARCH_RESPONSE);
        }
    }

    private FakeApiClient apiClient;
    private GnisLookupService service;

    @BeforeEach
    void setUp() {
        apiClient = new FakeApiClient();
        service = new GnisLookupService(apiClient, new ConcurrentMapCacheManager(
            GnisLookupService.LOOKUP_CACHE, GnisLookupService.SEARCH_CACHE));
    }

    @Test
    void roundCoordinateKeepsFourDecimals() {
        assertEquals(38.9847, GnisLookupService.roundCoordinate(38.98471234));
        assertEquals(-77.0948, GnisLookupService.roundCoordinate(-77.09476));
    }

    @Test
    void lookupIsCachedForNearbyPoints() {
        List<GnisFeature> first = service.getGnisFeatures(38.98471, -77.09471);
        List<GnisFeature> second = service.getGnisFeatures(38.98474, -77.09474);
        assertEquals(1, apiClient.calls);
        assertEquals(2, first.size());
        assertEquals(first.get(0).getGnisId(), second.get(0).getGnisId());
    }

    @Test
    void lookupFailureIsNotCached() {
        apiClient.failingUrlPart = "geocoder";
        assertNull(service.getGnisFeatures(38.9847, -77.0947));
        apiClient.failingUrlPart = null;
        assertEquals(2, service.getGnisFeatures(38.9847, -77.0947).size());
        assertEquals(2, apiClient.calls);
    }

    @Test
    void searchIsCachedIgnoringCase() {
        List<GnisFeature> first = service.searchGnisFeatures("Maryland", "md");
        int callsForOneSearch = apiClient.calls;
        List<GnisFeature> second = service.searchGnisFeatures("MARYLAND", "MD");
        assertEquals(5, callsForOneSearch);
        assertEquals(callsForOneSearch, apiClient.calls);
        assertEquals("Maryland", first.get(0).getName());
        assertEquals(first.size(), second.size());
    }

    @Test
    void partialSearchResultsAreReturnedButNotCached() {
        apiClient.failingUrlPart = "nationalmap";
        List<GnisFeature> partial = service.searchGnisFeatures("Maryland", "MD");
        assertEquals("Maryland", partial.get(0).getName());
        service.searchGnisFeatures("Maryland", "MD");
        assertEquals(10, apiClient.calls);
    }

    @Test
    void searchReturnsNullWhenEverySourceFails() {
        apiClient.failingUrlPart = "https://";
        assertNull(service.searchGnisFeatures("Maryland", "MD"));
    }
}
