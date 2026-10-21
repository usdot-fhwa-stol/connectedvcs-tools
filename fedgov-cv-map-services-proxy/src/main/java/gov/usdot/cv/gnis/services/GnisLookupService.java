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

package gov.usdot.cv.gnis.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usdot.cv.gnis.models.GnisFeature;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Looks up GNIS feature IDs:
 * - the place, county and state that contain a point, from the Census geocoder
 *   (which publishes GNIS IDs as ANSI/NS codes), and
 * - states and counties matching a name, from Census TIGERweb, and
 * - localities matching a name, from the USGS GNIS (geonames) service.
 */
@Service
public class GnisLookupService {
    private static final String URL_BASE = "https://geocoding.geo.census.gov/geocoder/geographies/coordinates";
    private static final String SEARCH_URL_BASE = "https://carto.nationalmap.gov/arcgis/rest/services/geonames/MapServer/%d/query";
    private static final String TIGERWEB_URL_BASE = "https://tigerweb.geo.census.gov/arcgis/rest/services/TIGERweb/tigerWMS_Current/MapServer/%d/query";
    private static final int TIGERWEB_STATES_LAYER = 80;
    private static final int TIGERWEB_COUNTIES_LAYER = 82;
    // Largest value that fits the 3-byte GNIS portion of a TIM packetID
    private static final long MAX_GNIS_ID = 0xFFFFFFL;
    private static final long LOOKUP_TIMEOUT_SECONDS = 15;
    // Search names are limited to characters that appear in place names, so they are safe in a LIKE clause
    public static final String SEARCH_NAME_PATTERN = "^[A-Za-z0-9 .'-]{2,50}$";
    public static final String SEARCH_STATE_PATTERN = "^[A-Za-z]{2}$";
    private static final int SEARCH_RESULTS_PER_LAYER = 50;
    private static final int SEARCH_MAX_RESULTS = 30;
    // GNIS names official places like "City of Springfield" or "Bethesda Census Designated Place"
    private static final String SEARCH_NAME_PREFIXES = "^(CITY|TOWN|VILLAGE|BOROUGH|TOWNSHIP|MUNICIPALITY) OF ";
    // ...and counties like "Washington County" or "Orleans Parish"; this wording is ignored when ranking
    private static final String SEARCH_NAME_SUFFIXES =
        " (CENSUS DESIGNATED PLACE|CDP|COUNTY|PARISH|CITY AND BOROUGH|BOROUGH|CENSUS AREA|MUNICIPALITY|MUNICIPIO)$";
    // Census geocoder layer name, its GNIS code field, and the label shown to the user (most specific first)
    private static final String[][] LAYERS = {
        { "Incorporated Places", "PLACENS", "Incorporated Place" },
        { "Census Designated Places", "PLACENS", "Census Designated Place" },
        { "Counties", "COUNTYNS", "County" },
        { "States", "STATENS", "State" },
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
    private Logger logger = LogManager.getLogger(GnisLookupService.class);
    private final OkHttpClient client = new OkHttpClient();

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
        String where = nameLikeClause("gaz_name", name);
        if (state != null) {
            where += " AND state_alpha = '" + state.toUpperCase(Locale.US) + "'";
        }
        return HttpUrl.parse(String.format(SEARCH_URL_BASE, layerId)).newBuilder()
            .addQueryParameter("where", where)
            .addQueryParameter("outFields", "gaz_id,gaz_name,gaz_featureclass,state_alpha,county_name")
            .addQueryParameter("returnGeometry", "false")
            .addQueryParameter("orderByFields", "gaz_name")
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
        String where = nameLikeClause("NAME", name) + " OR UPPER(STUSAB) = '" + name.toUpperCase(Locale.US).replace("'", "''") + "'";
        return composeTigerwebURL(TIGERWEB_STATES_LAYER, where, "NAME,STATENS,STUSAB");
    }

    /**
     * @param name  must match SEARCH_NAME_PATTERN
     * @param state two-letter state code, or null to search every state
     * @return the query URL, or null when the state has no counties
     */
    public String composeCountySearchURL(String name, String state) {
        validateSearch(name, state);
        String where = nameLikeClause("NAME", name);
        if (state != null) {
            String fips = null;
            for (Map.Entry<String, String> entry : STATE_FIPS.entrySet()) {
                if (entry.getValue().equalsIgnoreCase(state)) {
                    fips = entry.getKey();
                }
            }
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
            .addQueryParameter("orderByFields", "NAME")
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

    /**
     * @return the place, county and state containing the point (most specific first), or null on failure
     */
    public List<GnisFeature> getGnisFeatures(double latitude, double longitude) {
        return parseGnisResponse(await(fetch(composeFullURL(latitude, longitude))));
    }

    /**
     * @return states, counties and localities whose name contains the search text, best matches first,
     *         or null on failure. Counties and localities are limited to the given state, if any.
     */
    public List<GnisFeature> searchGnisFeatures(String name, String state) {
        String term = name.trim();
        List<CompletableFuture<List<GnisFeature>>> calls = new ArrayList<>();
        calls.add(fetch(composeStateSearchURL(term)).thenApply(this::parseStateSearchResponse));
        String countyURL = composeCountySearchURL(term, state);
        if (countyURL != null) {
            calls.add(fetch(countyURL).thenApply(this::parseCountySearchResponse));
        }
        for (int layerId : SEARCH_LAYERS) {
            calls.add(fetch(composeSearchURL(layerId, term, state)).thenApply(this::parseSearchResponse));
        }
        List<GnisFeature> features = new ArrayList<>();
        boolean found = false;
        for (CompletableFuture<List<GnisFeature>> call : calls) {
            List<GnisFeature> layerFeatures = await(call);
            if (layerFeatures != null) {
                found = true;
                features.addAll(layerFeatures);
            }
        }
        return found ? rankSearchResults(features, term) : null;
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
            .comparingInt((GnisFeature f) -> {
                if ("State".equals(f.getType()) && term.equalsIgnoreCase(f.getState())) {
                    return 0;
                }
                String featureName = f.getName().toUpperCase(Locale.US)
                    .replaceFirst(SEARCH_NAME_PREFIXES, "").replaceFirst(SEARCH_NAME_SUFFIXES, "");
                return featureName.equals(term) ? 0 : featureName.startsWith(term) ? 1 : 2;
            })
            .thenComparingInt((GnisFeature f) -> "State".equals(f.getType()) ? 0 : "County".equals(f.getType()) ? 1 : 2)
            .thenComparing(GnisFeature::getName)
            .thenComparing(GnisFeature::getState));
        List<GnisFeature> ranked = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        for (GnisFeature feature : features) {
            if (ranked.size() >= SEARCH_MAX_RESULTS) {
                break;
            }
            if (seen.add(feature.getGnisId())) {
                ranked.add(feature);
            }
        }
        return ranked;
    }

    private CompletableFuture<String> fetch(String url) {
        CompletableFuture<String> future = new CompletableFuture<>();
        logger.info("GNIS lookup call: " + url);
        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException ex) {
                logger.error("IO error calling GNIS lookup API " + url + ". Detailed error: " + ex.getMessage());
                future.complete(null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    if (response.isSuccessful() && body != null) {
                        future.complete(body.string());
                    } else {
                        logger.error("Error response from GNIS lookup API. Detailed error: " + response);
                        future.complete(null);
                    }
                } catch (IOException ex) {
                    logger.error("IO error reading GNIS lookup response " + url + ". Detailed error: " + ex.getMessage());
                    future.complete(null);
                }
            }
        });
        return future;
    }

    private <T> T await(CompletableFuture<T> future) {
        try {
            return future.get(LOOKUP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException ex) {
            logger.error("GNIS lookup did not complete. Detailed error: " + ex.getMessage());
        }
        return null;
    }

    public List<GnisFeature> parseGnisResponse(String response) {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            JsonNode geographies = objMapper.readTree(response).path("result").path("geographies");
            if (geographies.isMissingNode()) {
                logger.error("Empty results from response: " + response);
                return null;
            }
            String state = geographies.path("States").path(0).path("STUSAB").asText("");
            String county = geographies.path("Counties").path(0).path("NAME").asText("");
            // Keyed by GNIS ID: one feature can fill several roles (DC is both the county and the state)
            Map<Long, GnisFeature> features = new LinkedHashMap<>();
            for (String[] layer : LAYERS) {
                for (JsonNode geo : geographies.path(layer[0])) {
                    Long gnisId = parseGnisId(geo.path(layer[1]).asText(""));
                    if (gnisId == null) {
                        continue;
                    }
                    GnisFeature existing = features.get(gnisId);
                    if (existing != null) {
                        features.put(gnisId, new GnisFeature(gnisId, existing.getName(),
                            existing.getType() + " / " + layer[2], existing.getCounty(), state));
                        continue;
                    }
                    String featureCounty = "State".equals(layer[2]) ? "" : county;
                    features.put(gnisId, new GnisFeature(gnisId, geo.path("NAME").asText(""), layer[2], featureCounty, state));
                }
            }
            return new ArrayList<>(features.values());
        } catch (IllegalArgumentException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        }
        return null;
    }

    public List<GnisFeature> parseSearchResponse(String response) {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            JsonNode root = objMapper.readTree(response);
            if (!root.has("features")) {
                logger.error("Empty results from response: " + response);
                return null;
            }
            List<GnisFeature> features = new ArrayList<>();
            for (JsonNode feature : root.path("features")) {
                JsonNode attrs = feature.path("attributes");
                Long gnisId = parseGnisId(attrs.path("gaz_id").asText(""));
                if (gnisId == null) {
                    continue;
                }
                String featureClass = attrs.path("gaz_featureclass").asText("");
                features.add(new GnisFeature(gnisId, attrs.path("gaz_name").asText(""),
                    SEARCH_FEATURE_CLASSES.getOrDefault(featureClass, featureClass),
                    attrs.path("county_name").asText(""), attrs.path("state_alpha").asText("")));
            }
            return features;
        } catch (IllegalArgumentException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        }
        return null;
    }

    public List<GnisFeature> parseStateSearchResponse(String response) {
        return parseTigerwebResponse(response, "STATENS", "State");
    }

    public List<GnisFeature> parseCountySearchResponse(String response) {
        return parseTigerwebResponse(response, "COUNTYNS", "County");
    }

    private List<GnisFeature> parseTigerwebResponse(String response, String gnisField, String type) {
        try {
            ObjectMapper objMapper = new ObjectMapper();
            JsonNode root = objMapper.readTree(response);
            if (!root.has("features")) {
                logger.error("Empty results from response: " + response);
                return null;
            }
            List<GnisFeature> features = new ArrayList<>();
            for (JsonNode feature : root.path("features")) {
                JsonNode attrs = feature.path("attributes");
                Long gnisId = parseGnisId(attrs.path(gnisField).asText(""));
                if (gnisId == null) {
                    continue;
                }
                String featureName = attrs.path("NAME").asText("");
                String state = attrs.has("STUSAB") ? attrs.path("STUSAB").asText("")
                    : STATE_FIPS.getOrDefault(attrs.path("STATE").asText(""), "");
                String county = "County".equals(type) ? featureName : "";
                features.add(new GnisFeature(gnisId, featureName, type, county, state));
            }
            return features;
        } catch (IllegalArgumentException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        }
        return null;
    }

    private Long parseGnisId(String code) {
        if (!code.matches("^[0-9]+$")) {
            return null;
        }
        long gnisId = Long.parseLong(code);
        if (gnisId > MAX_GNIS_ID) {
            logger.warn("Skipping GNIS ID " + gnisId + " that does not fit in 3 bytes");
            return null;
        }
        return gnisId;
    }
}
