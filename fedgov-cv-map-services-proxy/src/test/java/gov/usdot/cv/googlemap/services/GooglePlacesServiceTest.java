/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.googlemap.services;

import gov.usdot.cv.googlemap.models.GooglePlaceSuggestions;
import gov.usdot.cv.googlemap.models.GoogleSearchTextResponse;
import gov.usdot.cv.utils.RequestComposer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the pure Java (non-HTTP) methods of {@link GooglePlacesService}
 * and {@link RequestComposer}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * parseAutoCompletePlacesResponse() and parseSearchTextResponse() contain
 * real branch logic: success path (valid JSON), error path (JSON with "error"
 * key), null/invalid input path (exception handler). These branches determine
 * whether valid place suggestions are returned to the UI. A bug here means
 * the map UI gets null results silently instead of an informative error.
 * Tests run without any HTTP server — they use plain String inputs.
 *
 * RequestComposer.composeRequestBodyData() is the JSON serialisation utility
 * used to build every outbound HTTP request body to the Google Places API.
 */
public class GooglePlacesServiceTest {

    private GooglePlacesService service;

    @BeforeEach
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
        assertTrue(result.contains("\"input\""));
        assertTrue(result.contains("Washington DC"));
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

    // =========================================================================
    // GooglePlacesService.parseAutoCompletePlacesResponse
    // =========================================================================

    @Test
    public void parseAutoCompletePlacesResponse_validJson_returnsSuggestions() {
        String validResponse = "{\n" +
            "  \"suggestions\": [\n" +
            "    {\n" +
            "      \"placePrediction\": {\n" +
            "        \"text\": { \"text\": \"Washington, DC, USA\", \"matches\": [] },\n" +
            "        \"placeId\": \"ChIJW-T2Wt7Gt4kRKl2I1CJFUsI\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        GooglePlaceSuggestions result = service.parseAutoCompletePlacesResponse(validResponse);
        assertNotNull(result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_errorResponse_returnsNull() {
        String errorResponse = "{\n" +
            "  \"error\": {\n" +
            "    \"code\": 400,\n" +
            "    \"message\": \"API key not valid.\",\n" +
            "    \"status\": \"INVALID_ARGUMENT\"\n" +
            "  }\n" +
            "}";

        GooglePlaceSuggestions result = service.parseAutoCompletePlacesResponse(errorResponse);
        assertNull(result);
    }

    @Test
    public void parseAutoCompletePlacesResponse_nullInput_returnsNull() {
        assertNull(service.parseAutoCompletePlacesResponse(null));
    }

    @Test
    public void parseAutoCompletePlacesResponse_emptyString_returnsNull() {
        assertNull(service.parseAutoCompletePlacesResponse(""));
    }

    @Test
    public void parseAutoCompletePlacesResponse_malformedJson_returnsNull() {
        assertNull(service.parseAutoCompletePlacesResponse("not valid json {{{{"));
    }

    @Test
    public void parseAutoCompletePlacesResponse_validJsonNoSuggestions_doesNotThrow() {
        assertDoesNotThrow(() ->
            service.parseAutoCompletePlacesResponse("{\"suggestions\": []}")
        );
    }

    // =========================================================================
    // GooglePlacesService.parseSearchTextResponse
    // =========================================================================

    @Test
    public void parseSearchTextResponse_validJson_returnsResult() {
        String validResponse = "{\n" +
            "  \"places\": [\n" +
            "    {\n" +
            "      \"formattedAddress\": \"Washington, DC, USA\",\n" +
            "      \"location\": { \"latitude\": 38.9072, \"longitude\": -77.0369 }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

        GoogleSearchTextResponse result = service.parseSearchTextResponse(validResponse);
        assertNotNull(result);
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

        assertNull(service.parseSearchTextResponse(errorResponse));
    }

    @Test
    public void parseSearchTextResponse_nullInput_returnsNull() {
        assertNull(service.parseSearchTextResponse(null));
    }

    @Test
    public void parseSearchTextResponse_emptyString_returnsNull() {
        assertNull(service.parseSearchTextResponse(""));
    }

    @Test
    public void parseSearchTextResponse_malformedJson_returnsNull() {
        assertNull(service.parseSearchTextResponse("{invalid"));
    }

    @Test
    public void parseSearchTextResponse_missingPlacesKey_returnsNull() {
        // Valid JSON but missing "places" key — NullPointerException caught internally
        assertNull(service.parseSearchTextResponse("{ \"status\": \"OK\" }"));
    }
}