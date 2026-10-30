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

package gov.usdot.cv.gnis.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usdot.cv.gnis.models.GnisFeature;
import okhttp3.HttpUrl;

/**
 * Looks up GNIS feature IDs:
 * - the place, county and state that contain a point, from the Census geocoder
 *   (which publishes GNIS IDs as ANSI/NS codes), and
 * - states and counties matching a name, from Census TIGERweb, and
 * - localities matching a name, from the USGS GNIS (geonames) service.
 * Successful results are cached, since GNIS codes rarely change.
 */
@Service
public class GnisLookupService {
    public static final String LOOKUP_CACHE = "gnisLookupCache";
    public static final String SEARCH_CACHE = "gnisSearchCache";
    // Search names are limited to characters that appear in place names, so they are safe in a LIKE clause
    public static final String SEARCH_NAME_PATTERN = "^[A-Za-z0-9 .'-]{2,50}$";
    public static final String SEARCH_STATE_PATTERN = "^[A-Za-z]{2}$";

    private static final String URL_BASE = "https://geocoding.geo.census.gov/geocoder/geographies/coordinates";
    private static final String SEARCH_URL_BASE = "https://carto.nationalmap.gov/arcgis/rest/services/geonames/MapServer/%d/query";
    private static final String TIGERWEB_URL_BASE = "https://tigerweb.geo.census.gov/arcgis/rest/services/TIGERweb/tigerWMS_Current/MapServer/%d/query";
    private static final int TIGERWEB_STATES_LAYER = 80;
    private static final int TIGERWEB_COUNTIES_LAYER = 82;
    // Largest value that fits the 3-byte GNIS portion of a TIM packetID
    private static final long MAX_GNIS_ID = 0xFFFFFFL;
    // Lookups are cached by position rounded to about 10 m, so nearby markers share an entry
    private static final double COORDINATE_PRECISION = 10000.0;
    private static final int SEARCH_RESULTS_PER_LAYER = 50;
    private static final int SEARCH_MAX_RESULTS = 30;
    // GNIS names official places like "City of Springfield" or "Bethesda Census Designated Place"
    private static final String SEARCH_NAME_PREFIXES = "^(CITY|TOWN|VILLAGE|BOROUGH|TOWNSHIP|MUNICIPALITY) OF ";
    // ...and counties like "Washington County" or "Orleans Parish"; this wording is ignored when ranking
    private static final String SEARCH_NAME_SUFFIXES =
        " (CENSUS DESIGNATED PLACE|CDP|COUNTY|PARISH|CITY AND BOROUGH|BOROUGH|CENSUS AREA|MUNICIPALITY|MUNICIPIO)$";

    private static final String TYPE_STATE = "State";
    private static final String TYPE_COUNTY = "County";
    private static final String FIELD_FEATURES = "features";
    private static final String FIELD_ATTRIBUTES = "attributes";
    private static final String FIELD_NAME = "NAME";
    private static final String FIELD_STUSAB = "STUSAB";
    private static final String FIELD_GAZ_NAME = "gaz_name";
    private static final String EMPTY_RESULTS_LOG = "Empty results from response: {}";
    private static final String PARSE_ERROR_LOG = "Failed to parse response: {}. Detailed error: {}";

    // Census geocoder layer name, its GNIS code field, and the label shown to the user (most specific first)
    private static final String[][] LAYERS = {
        { "Incorporated Places", "PLACENS", "Incorporated Place" },
        { "Census Designated Places", "PLACENS", "Census Designated Place" },
        { "Counties", "COUNTYNS", TYPE_COUNTY },
        { "States", "STATENS", TYPE_STATE },
    };
    // USGS geonames layers: Incorporated Places, Unincorporated Places, Populated Places
    private static final int[] SEARCH_LAYERS = { 1, 2, 3 };
    private static final Map<String, String> SEARCH_FEATURE_CLASSES = new LinkedHashMap<>();
    static {
        SEARCH_FEATURE_CLASSES.put("Civil", "Incorporated Place");
        SEARCH_FEATURE_CLASSES.put("Census", "Census Designated Place");
    }
    // Census returns counties with the state's FIPS code; these codes are fixed by federal standard
    private static final Map<String, String> STATE_FIPS = new LinkedHashMap<>();
    static {
        String[] states = { "01:AL", "02:AK", "04:AZ", "05:AR", "06:CA", "08:CO", "09:CT", "10:DE", "11:DC", "12:FL",
            "13:GA", "15:HI", "16:ID", "17:IL", "18:IN", "19:IA", "20:KS", "21:KY", "22:LA", "23:ME", "24:MD", "25:MA",
            "26:MI", "27:MN", "28:MS", "29:MO", "30:MT", "31:NE", "32:NV", "33:NH", "34:NJ", "35:NM", "36:NY", "37:NC",
            "38:ND", "39:OH", "40:OK", "41:OR", "42:PA", "44:RI", "45:SC", "46:SD", "47:TN", "48:TX", "49:UT", "50:VT",
            "51:VA", "53:WA", "54:WV", "55:WI", "56:WY", "60:AS", "66:GU", "69:MP", "72:PR", "78:VI" };
        for (String state : states) {
            STATE_FIPS.put(state.substring(0, 2), state.substring(3));
        }
    }
    private static final Logger logger = LogManager.getLogger(GnisLookupService.class);

    private final GnisApiClient apiClient;
    private final CacheManager cacheManager;

    public GnisLookupService(GnisApiClient apiClient, CacheManager cacheManager) {
        this.apiClient = apiClient;
        this.cacheManager = cacheManager;
    }

    public String composeFullURL(double latitude, double longitude) {
        StringBuilder layers = new StringBuilder();
        for (String[] layer : LAYERS) {
            if (layers.length() > 0) {
                layers.append(",");
            }
            layers.append(layer[0]);
        }
        return HttpUrl.parse(URL_BASE).newBuilder()
            .addQueryParameter("x", Double.toString(longitude))
            .addQueryParameter("y", Double.toString(latitude))
            .addQueryParameter("benchmark", "Public_AR_Current")
            .addQueryParameter("vintage", "Current_Current")
            .addQueryParameter("layers", layers.toString())
            .addQueryParameter("format", "json")
            .build().toString();
    }

    /**
     * @param name  must match SEARCH_NAME_PATTERN
     * @param state two-letter state code, or null to search every state
     */
    public String composeSearchURL(int layerId, String name, String state) {
        validateSearch(name, state);
        String where = nameLikeClause(FIELD_GAZ_NAME, name);
        if (state != null) {
            where += " AND state_alpha = '" + state.toUpperCase(Locale.US) + "'";
        }
        return HttpUrl.parse(String.format(SEARCH_URL_BASE, layerId)).newBuilder()
            .addQueryParameter("where", where)
            .addQueryParameter("outFields", "gaz_id," + FIELD_GAZ_NAME + ",gaz_featureclass,state_alpha,county_name")
            .addQueryParameter("returnGeometry", "false")
            .addQueryParameter("orderByFields", FIELD_GAZ_NAME)
            .addQueryParameter("resultRecordCount", Integer.toString(SEARCH_RESULTS_PER_LAYER))
            .addQueryParameter("f", "json")
            .build().toString();
    }

    /**
     * States are matched by name or postal code, and are never limited to one state.
     * @param name must match SEARCH_NAME_PATTERN
     */
    public String composeStateSearchURL(String name) {
        validateSearch(name, null);
        String where = nameLikeClause(FIELD_NAME, name)
            + " OR UPPER(" + FIELD_STUSAB + ") = '" + name.toUpperCase(Locale.US).replace("'", "''") + "'";
        return composeTigerwebURL(TIGERWEB_STATES_LAYER, where, "NAME,STATENS," + FIELD_STUSAB);
    }

    /**
     * @param name  must match SEARCH_NAME_PATTERN
     * @param state two-letter state code, or null to search every state
     * @return the query URL, or null when the state has no counties
     */
    public String composeCountySearchURL(String name, String state) {
        validateSearch(name, state);
        String where = nameLikeClause(FIELD_NAME, name);
        if (state != null) {
            String fips = STATE_FIPS.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(state))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
            if (fips == null) {
                return null;
            }
            where += " AND STATE = '" + fips + "'";
        }
        return composeTigerwebURL(TIGERWEB_COUNTIES_LAYER, where, "NAME,COUNTYNS,STATE");
    }

    private String composeTigerwebURL(int layerId, String where, String outFields) {
        return HttpUrl.parse(String.format(TIGERWEB_URL_BASE, layerId)).newBuilder()
            .addQueryParameter("where", where)
            .addQueryParameter("outFields", outFields)
            .addQueryParameter("returnGeometry", "false")
            .addQueryParameter("orderByFields", FIELD_NAME)
            .addQueryParameter("resultRecordCount", Integer.toString(SEARCH_RESULTS_PER_LAYER))
            .addQueryParameter("f", "json")
            .build().toString();
    }

    private void validateSearch(String name, String state) {
        if (!name.matches(SEARCH_NAME_PATTERN) || (state != null && !state.matches(SEARCH_STATE_PATTERN))) {
            throw new IllegalArgumentException("Invalid GNIS search name or state");
        }
    }

    private String nameLikeClause(String field, String name) {
        return "UPPER(" + field + ") LIKE '%" + name.toUpperCase(Locale.US).replace("'", "''") + "%'";
    }

    public static double roundCoordinate(double value) {
        return Math.round(value * COORDINATE_PRECISION) / COORDINATE_PRECISION;
    }

    /**
     * @return the place, county and state containing the point (most specific first), or null on failure
     */
    public List<GnisFeature> getGnisFeatures(double latitude, double longitude) {
        double lat = roundCoordinate(latitude);
        double lon = roundCoordinate(longitude);
        String key = lat + "," + lon;
        List<GnisFeature> cached = getCached(LOOKUP_CACHE, key);
        if (cached != null) {
            return cached;
        }
        List<GnisFeature> features = parseGnisResponse(apiClient.await(apiClient.fetch(composeFullURL(lat, lon))));
        if (features != null) {
            putCached(LOOKUP_CACHE, key, features);
        }
        return features;
    }

    /**
     * @return states, counties and localities whose name contains the search text, best matches first,
     *         or null if every source failed. Counties and localities are limited to the given state, if any.
     *         Results are only cached when every source answered, so an outage is not remembered.
     */
    public List<GnisFeature> searchGnisFeatures(String name, String state) {
        String term = name.trim();
        String key = term.toUpperCase(Locale.US) + "|" + (state == null ? "" : state.toUpperCase(Locale.US));
        List<GnisFeature> cached = getCached(SEARCH_CACHE, key);
        if (cached != null) {
            return cached;
        }

        List<CompletableFuture<List<GnisFeature>>> calls = new ArrayList<>();
        calls.add(apiClient.fetch(composeStateSearchURL(term)).thenApply(this::parseStateSearchResponse));
        String countyURL = composeCountySearchURL(term, state);
        if (countyURL != null) {
            calls.add(apiClient.fetch(countyURL).thenApply(this::parseCountySearchResponse));
        }
        for (int layerId : SEARCH_LAYERS) {
            calls.add(apiClient.fetch(composeSearchURL(layerId, term, state)).thenApply(this::parseSearchResponse));
        }

        List<GnisFeature> features = new ArrayList<>();
        int answered = 0;
        for (CompletableFuture<List<GnisFeature>> call : calls) {
            List<GnisFeature> layerFeatures = apiClient.await(call);
            if (layerFeatures != null) {
                answered++;
                features.addAll(layerFeatures);
            }
        }
        if (answered == 0) {
            return null;
        }
        List<GnisFeature> ranked = rankSearchResults(features, term);
        if (answered == calls.size()) {
            putCached(SEARCH_CACHE, key, ranked);
        }
        return ranked;
    }

    private List<GnisFeature> getCached(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        GnisFeature[] cached = cache == null ? null : cache.get(key, GnisFeature[].class);
        return cached == null ? null : new ArrayList<>(Arrays.asList(cached));
    }

    private void putCached(String cacheName, String key, List<GnisFeature> features) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, features.toArray(new GnisFeature[0]));
        }
    }

    /**
     * Orders exact name matches first (or a state's postal code), then names starting with the search
     * text, then the rest; ties go to states, then counties, then localities. Duplicates are dropped
     * and at most SEARCH_MAX_RESULTS are kept. "City of"/"County"/"Census Designated Place" wording is
     * ignored, so official names rank with their plain names.
     */
    public List<GnisFeature> rankSearchResults(List<GnisFeature> features, String name) {
        String term = name.toUpperCase(Locale.US);
        features.sort(Comparator
            .comparingInt((GnisFeature f) -> matchRank(f, term))
            .thenComparingInt(GnisLookupService::typeRank)
            .thenComparing(GnisFeature::getName)
            .thenComparing(GnisFeature::getState));
        Set<Long> seen = new HashSet<>();
        return features.stream()
            .filter(feature -> seen.add(feature.getGnisId()))
            .limit(SEARCH_MAX_RESULTS)
            .collect(Collectors.toList());
    }

    // 0 for an exact name (or state postal code) match, 1 when the name starts with the term, 2 otherwise
    private static int matchRank(GnisFeature feature, String term) {
        if (TYPE_STATE.equals(feature.getType()) && term.equalsIgnoreCase(feature.getState())) {
            return 0;
        }
        String featureName = feature.getName().toUpperCase(Locale.US)
            .replaceFirst(SEARCH_NAME_PREFIXES, "").replaceFirst(SEARCH_NAME_SUFFIXES, "");
        if (featureName.equals(term)) {
            return 0;
        }
        if (featureName.startsWith(term)) {
            return 1;
        }
        return 2;
    }

    private static int typeRank(GnisFeature feature) {
        if (TYPE_STATE.equals(feature.getType())) {
            return 0;
        }
        if (TYPE_COUNTY.equals(feature.getType())) {
            return 1;
        }
        return 2;
    }

    public List<GnisFeature> parseGnisResponse(String response) {
        try {
            JsonNode geographies = new ObjectMapper().readTree(response).path("result").path("geographies");
            if (geographies.isMissingNode()) {
                logger.error(EMPTY_RESULTS_LOG, response);
                return null;
            }
            String state = geographies.path("States").path(0).path(FIELD_STUSAB).asText("");
            String county = geographies.path("Counties").path(0).path(FIELD_NAME).asText("");
            // Keyed by GNIS ID: one feature can fill several roles (DC is both the county and the state)
            Map<Long, GnisFeature> features = new LinkedHashMap<>();
            for (String[] layer : LAYERS) {
                for (JsonNode geo : geographies.path(layer[0])) {
                    Long gnisId = parseGnisId(geo.path(layer[1]).asText(""));
                    if (gnisId != null) {
                        addContainingFeature(features, gnisId, geo.path(FIELD_NAME).asText(""), layer[2], county, state);
                    }
                }
            }
            return new ArrayList<>(features.values());
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            logger.error(PARSE_ERROR_LOG, response, ex.getMessage());
        }
        return null;
    }

    private void addContainingFeature(Map<Long, GnisFeature> features, long gnisId, String name, String type,
            String county, String state) {
        GnisFeature existing = features.get(gnisId);
        if (existing != null) {
            features.put(gnisId, new GnisFeature(gnisId, existing.getName(), existing.getType() + " / " + type,
                existing.getCounty(), state));
        } else {
            String featureCounty = TYPE_STATE.equals(type) ? "" : county;
            features.put(gnisId, new GnisFeature(gnisId, name, type, featureCounty, state));
        }
    }

    public List<GnisFeature> parseSearchResponse(String response) {
        try {
            JsonNode root = new ObjectMapper().readTree(response);
            if (!root.has(FIELD_FEATURES)) {
                logger.error(EMPTY_RESULTS_LOG, response);
                return null;
            }
            List<GnisFeature> features = new ArrayList<>();
            for (JsonNode feature : root.path(FIELD_FEATURES)) {
                JsonNode attrs = feature.path(FIELD_ATTRIBUTES);
                Long gnisId = parseGnisId(attrs.path("gaz_id").asText(""));
                if (gnisId != null) {
                    String featureClass = attrs.path("gaz_featureclass").asText("");
                    features.add(new GnisFeature(gnisId, attrs.path(FIELD_GAZ_NAME).asText(""),
                        SEARCH_FEATURE_CLASSES.getOrDefault(featureClass, featureClass),
                        attrs.path("county_name").asText(""), attrs.path("state_alpha").asText("")));
                }
            }
            return features;
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            logger.error(PARSE_ERROR_LOG, response, ex.getMessage());
        }
        return null;
    }

    public List<GnisFeature> parseStateSearchResponse(String response) {
        return parseTigerwebResponse(response, "STATENS", TYPE_STATE);
    }

    public List<GnisFeature> parseCountySearchResponse(String response) {
        return parseTigerwebResponse(response, "COUNTYNS", TYPE_COUNTY);
    }

    private List<GnisFeature> parseTigerwebResponse(String response, String gnisField, String type) {
        try {
            JsonNode root = new ObjectMapper().readTree(response);
            if (!root.has(FIELD_FEATURES)) {
                logger.error(EMPTY_RESULTS_LOG, response);
                return null;
            }
            List<GnisFeature> features = new ArrayList<>();
            for (JsonNode feature : root.path(FIELD_FEATURES)) {
                JsonNode attrs = feature.path(FIELD_ATTRIBUTES);
                Long gnisId = parseGnisId(attrs.path(gnisField).asText(""));
                if (gnisId != null) {
                    String featureName = attrs.path(FIELD_NAME).asText("");
                    String state = attrs.has(FIELD_STUSAB) ? attrs.path(FIELD_STUSAB).asText("")
                        : STATE_FIPS.getOrDefault(attrs.path("STATE").asText(""), "");
                    String county = TYPE_COUNTY.equals(type) ? featureName : "";
                    features.add(new GnisFeature(gnisId, featureName, type, county, state));
                }
            }
            return features;
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            logger.error(PARSE_ERROR_LOG, response, ex.getMessage());
        }
        return null;
    }

    private Long parseGnisId(String code) {
        if (!code.matches("^\\d+$")) {
            return null;
        }
        long gnisId = Long.parseLong(code);
        if (gnisId > MAX_GNIS_ID) {
            logger.warn("Skipping GNIS ID {} that does not fit in 3 bytes", gnisId);
            return null;
        }
        return gnisId;
    }
}
