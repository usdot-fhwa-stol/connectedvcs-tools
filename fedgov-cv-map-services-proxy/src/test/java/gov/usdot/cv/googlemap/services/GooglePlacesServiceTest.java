/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.googlemap.services;

import gov.usdot.cv.googlemap.models.GooglePlaceSuggestions;
import gov.usdot.cv.googlemap.models.GoogleSearchTextResponse;
import gov.usdot.cv.utils.RequestComposer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for the pure Java (non-HTTP) methods of {@link GooglePlacesService}
 * and {@link RequestComposer}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * parseAutoCompletePlacesResponse() and parseSearchTextResponse() contain
 * real branch logic: success path (valid JSON), error path (JSON with "error"
 * key), null/invalid input path (exception handler), and NullPointerException
 * guard. These branches determine whether valid place suggestions and coordinates
 * are returned to the UI. A bug here means the map UI gets null results
 * silently instead of crashing visibly. Tests run without any HTTP server
 * — they use plain String inputs.
 *
 * RequestComposer.composeRequestBodyData() is the JSON serialisation utility
 * used to build every outbound HTTP request body. A bug here means malformed
 * request bodies sent to the Google Places API.
 */
public class GooglePlacesServiceTest {

    private GooglePlacesService service;

    @Before
    public void setUp() {
        service = new GooglePlacesService();
    }

    // =========================================================================
    // RequestComposer.composeRequestBodyData
    // =========================================================================

    @Test
    public void composeRequestBodyData_singleEntry_producesValidJson() {
        Map<String, String> fields = new HashMap<>();
        fields.put("input", "Washington DC");

        String result = new RequestComposer().composeRequestBodyData(fields);

        assertNotNull(result);
        assertTrue("Should contain the key", result.contains("\"input\""));
        assertTrue("Should contain the value", result.contains("Washington DC"));
    }

    @Test
    public void composeRequestBodyData_multipleEntries_includesAll() {
        Map<String, String> fields = new HashMap<>();
        fields.put("input", "Route 66");
        fields.put("sessionToken", "abc123");

        String result = new RequestComposer().composeRequestBodyData(fields);

        assertNotNull(result);
        assertTrue(result.contains("\"input\""));
        assertTrue(result.contains("Route 66"));
        assertTrue(result.contains("\"sessionToken\""));
        assertTrue(result.contains("abc123"));
    }

    @Test
    public void composeRequestBodyData_emptyMap_producesEmptyJsonObject() {
        String result = new RequestComposer().composeRequestBodyData(new HashMap<>());
        assertNotNull(result);
        assertEquals("{}", result);
    }

    @Test
    public void composeRequestBodyData_specialCharsInValue_escapedCorrectly() {
        Map<String, String> fields = new HashMap<>();
        fields.put("query", "O'Brien & Sons \"LLC\"");

        String result = new RequestComposer().composeRequestBodyData(fields);
        assertNotNull(result);
        // Jackson should produce valid JSON regardless of special chars
        assertTrue(result.startsWith("{"));
        assertTrue(result.endsWith("}"));
    }

    // =========================================================================
    // GooglePlacesService.parseAutoCompletePlacesResponse — success path
    // =========================================================================

    @Test
    public void parseAutoCompletePlacesResponse_validJson_returnsSuggestions() {
        String validResponse = "{\n" +
            "  \"suggestions\": [\n" +
            "    {\n" +
            "      \"placePrediction\": {\n" +
            "        \"text\": {\n" +
            "          \"text\": \"Washington, DC, USA\",\n" +
            "          \"matches\": []\n" +
            "        },\n" +
            "        \"placeId\": \"ChIJW-T2Wt7Gt4kRKl2I1CJFUsI\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        GooglePlaceSuggestions result =
                service.parseAutoCompletePlacesResponse(validResponse);

        assertNotNull("Valid response should parse successfully", result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_errorResponse_returnsNull() {
        // When the Google API returns an error object, service should return null
        String errorResponse = "{\n" +
            "  \"error\": {\n" +
            "    \"code\": 400,\n" +
            "    \"message\": \"API key not valid.\",\n" +
            "    \"status\": \"INVALID_ARGUMENT\"\n" +
            "  }\n" +
            "}";

        GooglePlaceSuggestions result =
                service.parseAutoCompletePlacesResponse(errorResponse);

        assertNull("Error response should return null", result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_nullInput_returnsNull() {
        GooglePlaceSuggestions result = service.parseAutoCompletePlacesResponse(null);
        assertNull("Null input should return null", result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_emptyString_returnsNull() {
        GooglePlaceSuggestions result = service.parseAutoCompletePlacesResponse("");
        assertNull("Empty string should return null", result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_malformedJson_returnsNull() {
        GooglePlaceSuggestions result =
                service.parseAutoCompletePlacesResponse("not valid json {{{{");
        assertNull("Malformed JSON should return null", result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_validJsonWithNoSuggestions_handlesGracefully() {
        String emptyResponse = "{\"suggestions\": []}";
        // Should not throw — returns either an empty result or null gracefully
        // No assertion on value; just verify no exception
        try {
            service.parseAutoCompletePlacesResponse(emptyResponse);
        } catch (Exception e) {
            fail("Should not throw exception for valid JSON with empty suggestions: " + e);
        }
    }

    // =========================================================================
    // GooglePlacesService.parseSearchTextResponse — success path
    // =========================================================================

    @Test
    public void parseSearchTextResponse_validJson_returnsResult() {
        String validResponse = "{\n" +
            "  \"places\": [\n" +
            "    {\n" +
            "      \"formattedAddress\": \"Washington, DC, USA\",\n" +
            "      \"location\": {\n" +
            "        \"latitude\": 38.9072,\n" +
            "        \"longitude\": -77.0369\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        GoogleSearchTextResponse result =
                service.parseSearchTextResponse(validResponse);

        assertNotNull("Valid response should parse successfully", result);
    }

    @Test
    public void parseSearchTextResponse_errorResponse_returnsNull() {
        String errorResponse = "{\n" +
            "  \"error\": {\n" +
            "    \"code\": 403,\n" +
            "    \"message\": \"Quota exceeded\",\n" +
            "    \"status\": \"RESOURCE_EXHAUSTED\"\n" +
            "  }\n" +
            "}";

        GoogleSearchTextResponse result = service.parseSearchTextResponse(errorResponse);
        assertNull("Error response should return null", result);
    }

    @Test
    public void parseSearchTextResponse_nullInput_returnsNull() {
        GoogleSearchTextResponse result = service.parseSearchTextResponse(null);
        assertNull("Null input should return null", result);
    }

    @Test
    public void parseSearchTextResponse_emptyString_returnsNull() {
        GoogleSearchTextResponse result = service.parseSearchTextResponse("");
        assertNull("Empty string should return null", result);
    }

    @Test
    public void parseSearchTextResponse_malformedJson_returnsNull() {
        GoogleSearchTextResponse result =
                service.parseSearchTextResponse("{invalid");
        assertNull("Malformed JSON should return null", result);
    }

    @Test
    public void parseSearchTextResponse_missingPlacesKey_returnsNull() {
        // Valid JSON but missing the "places" key — causes NullPointerException
        // which is caught and returns null
        String missingPlaces = "{ \"status\": \"OK\" }";
        GoogleSearchTextResponse result = service.parseSearchTextResponse(missingPlaces);
        assertNull("Missing 'places' key should return null", result);
    }
}