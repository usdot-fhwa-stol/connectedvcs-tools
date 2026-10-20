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
 * Looks up GNIS feature IDs for a point:
 * - the state, county and places that contain it, from the Census geocoder
 *   (which publishes GNIS IDs as ANSI/NS codes), and
 * - named places and crossings within a radius, from the USGS GNIS (geonames) service.
 */
@Service
public class GnisLookupService {
    private static final String URL_BASE = "https://geocoding.geo.census.gov/geocoder/geographies/coordinates";
    private static final String NEARBY_URL_BASE = "https://carto.nationalmap.gov/arcgis/rest/services/geonames/MapServer/%d/query";
    // Largest value that fits the 3-byte GNIS portion of a TIM packetID
    private static final long MAX_GNIS_ID = 0xFFFFFFL;
    private static final long LOOKUP_TIMEOUT_SECONDS = 15;
    // Nearby features are searched within this radius, and only the closest are offered
    private static final double NEARBY_RADIUS_MILES = 5;
    private static final int NEARBY_MAX_RESULTS = 25;
    private static final double EARTH_RADIUS_MILES = 3958.8;
    // Census geocoder layer name, its GNIS code field, and the label shown to the user (most specific first)
    private static final String[][] LAYERS = {
        { "Incorporated Places", "PLACENS", "Place" },
        { "Census Designated Places", "PLACENS", "Place" },
        { "County Subdivisions", "COUSUBNS", "County Subdivision" },
        { "Counties", "COUNTYNS", "County" },
        { "States", "STATENS", "State" },
    };
    // USGS geonames layers: Incorporated Places, Unincorporated Places, Populated Places, Crossings
    private static final int[] NEARBY_LAYERS = { 1, 2, 3, 10 };
    private static final Map<String, String> NEARBY_FEATURE_CLASSES = new LinkedHashMap<>();
    static {
        NEARBY_FEATURE_CLASSES.put("Civil", "Incorporated Place");
        NEARBY_FEATURE_CLASSES.put("Census", "Census Designated Place");
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

    public String composeNearbyURL(int layerId, double latitude, double longitude) {
        return HttpUrl.parse(String.format(NEARBY_URL_BASE, layerId)).newBuilder()
            .addQueryParameter("geometry", longitude + "," + latitude)
            .addQueryParameter("geometryType", "esriGeometryPoint")
            .addQueryParameter("inSR", "4326")
            .addQueryParameter("spatialRel", "esriSpatialRelIntersects")
            .addQueryParameter("distance", Double.toString(NEARBY_RADIUS_MILES))
            .addQueryParameter("units", "esriSRUnit_StatuteMile")
            .addQueryParameter("outFields", "gaz_id,gaz_name,gaz_featureclass,state_alpha,county_name")
            .addQueryParameter("returnGeometry", "true")
            .addQueryParameter("outSR", "4326")
            .addQueryParameter("f", "json")
            .build().toString();
    }

    /**
     * @return features containing the point (most specific first) followed by the
     *         nearest features within the configured radius, or null if both lookups fail
     */
    public List<GnisFeature> getGnisFeatures(double latitude, double longitude) {
        CompletableFuture<String> containingCall = fetch(composeFullURL(latitude, longitude));
        List<CompletableFuture<String>> nearbyCalls = new ArrayList<>();
        for (int layerId : NEARBY_LAYERS) {
            nearbyCalls.add(fetch(composeNearbyURL(layerId, latitude, longitude)));
        }

        List<GnisFeature> containing = parseGnisResponse(await(containingCall));
        List<GnisFeature> nearby = new ArrayList<>();
        boolean nearbyFound = false;
        for (CompletableFuture<String> call : nearbyCalls) {
            List<GnisFeature> layerFeatures = parseNearbyResponse(await(call), latitude, longitude);
            if (layerFeatures != null) {
                nearbyFound = true;
                nearby.addAll(layerFeatures);
            }
        }
        if (containing == null && !nearbyFound) {
            return null;
        }
        return mergeFeatures(containing, nearby);
    }

    /**
     * Keeps every containing feature, then the closest nearby features that are not
     * already listed, up to the configured maximum.
     */
    public List<GnisFeature> mergeFeatures(List<GnisFeature> containing, List<GnisFeature> nearby) {
        List<GnisFeature> features = new ArrayList<>();
        Set<Long> seen = new HashSet<>();
        if (containing != null) {
            for (GnisFeature feature : containing) {
                if (seen.add(feature.getGnisId())) {
                    features.add(feature);
                }
            }
        }
        nearby.sort(Comparator.comparingDouble(GnisFeature::getDistanceMiles));
        int added = 0;
        for (GnisFeature feature : nearby) {
            if (added >= NEARBY_MAX_RESULTS) {
                break;
            }
            if (seen.add(feature.getGnisId())) {
                features.add(feature);
                added++;
            }
        }
        return features;
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

    private String await(CompletableFuture<String> future) {
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
            List<GnisFeature> features = new ArrayList<>();
            for (String[] layer : LAYERS) {
                for (JsonNode geo : geographies.path(layer[0])) {
                    Long gnisId = parseGnisId(geo.path(layer[1]).asText(""));
                    if (gnisId == null) {
                        continue;
                    }
                    String featureCounty = "State".equals(layer[2]) ? "" : county;
                    features.add(new GnisFeature(gnisId, geo.path("NAME").asText(""), layer[2], featureCounty, state, null));
                }
            }
            return features;
        } catch (IllegalArgumentException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: " + response + ". Detailed error: " + ex.getMessage());
        }
        return null;
    }

    public List<GnisFeature> parseNearbyResponse(String response, double latitude, double longitude) {
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
                double[] point = parsePoint(feature.path("geometry"));
                if (gnisId == null || point == null) {
                    continue;
                }
                String featureClass = attrs.path("gaz_featureclass").asText("");
                String type = NEARBY_FEATURE_CLASSES.getOrDefault(featureClass, featureClass);
                double distance = distanceMiles(latitude, longitude, point[1], point[0]);
                features.add(new GnisFeature(gnisId, attrs.path("gaz_name").asText(""), type,
                    attrs.path("county_name").asText(""), attrs.path("state_alpha").asText(""),
                    Math.round(distance * 10) / 10.0));
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

    // Geonames layers return multipoint geometry; accept a plain point as well
    private double[] parsePoint(JsonNode geometry) {
        JsonNode xy = geometry.path("points").path(0);
        if (xy.size() >= 2) {
            return new double[] { xy.get(0).asDouble(), xy.get(1).asDouble() };
        }
        if (geometry.has("x") && geometry.has("y")) {
            return new double[] { geometry.get("x").asDouble(), geometry.get("y").asDouble() };
        }
        return null;
    }

    private double distanceMiles(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return EARTH_RADIUS_MILES * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
