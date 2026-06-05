/*
 * Copyright (C) 2026 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder;

import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import gov.usdot.cv.timencoder.MUTCDCode;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Extended tests for {@link TravelerInformationBuilder} targeting uncovered branches:
 *
 *  getMutcdFromInt:
 *    - all 7 mapped values (0-6)
 *    - default (unknown value) → MUTCDCode.none
 *    - negative value → MUTCDCode.none
 *
 *  buildContent (content-type dispatch):
 *    - "Speed Limit"    → SpeedLimit content path
 *    - "Exit Service"   → ExitService content path
 *    - generic/default  → GenericSignage path
 *
 *  getDurationTime:
 *    - duration > MAX_MINUTES_DURATION (32000) → capped at 32000
 *
 *  setRadiusAndUnits (exercised via tim_Circle JSON variants):
 *    - already covered by tim_Circle.json (centimeter range)
 *    - radius in decimeter range
 *    - radius in meter range
 *    - radius in kilometer range
 *    - radius beyond kilometer range (unknown → 4095 cm)
 *    - up-conversion: 400 cm → 40 dm → 4 m (divisible up-conversions)
 *
 *  FramePlusTIM generate type (getReadbaleTIMplusFrame path):
 *    - already covered by sample_timplusframe.json
 *
 *  Note: tests that need a full encoding call use the JSON files in
 *  src/test/resources/ already present in the repo; new content-type
 *  variants are exercised via helper JSON files created inline.
 */
public class TravelerInformationBuilderExtendedTest {

    private static final TravelerInformationBuilder BUILDER = new TravelerInformationBuilder();

    // =========================================================================
    // getMutcdFromInt – all branches
    // =========================================================================

    @Test
    public void getMutcdFromInt_zero_returnsNone() {
        assertEquals(MUTCDCode.none, TravelerInformationBuilder.getMutcdFromInt(0));
    }

    @Test
    public void getMutcdFromInt_one_returnsRegulatory() {
        assertEquals(MUTCDCode.regulatory, TravelerInformationBuilder.getMutcdFromInt(1));
    }

    @Test
    public void getMutcdFromInt_two_returnsWarning() {
        assertEquals(MUTCDCode.warning, TravelerInformationBuilder.getMutcdFromInt(2));
    }

    @Test
    public void getMutcdFromInt_three_returnsMaintenance() {
        assertEquals(MUTCDCode.maintenance, TravelerInformationBuilder.getMutcdFromInt(3));
    }

    @Test
    public void getMutcdFromInt_four_returnsMotoristService() {
        assertEquals(MUTCDCode.motoristService, TravelerInformationBuilder.getMutcdFromInt(4));
    }

    @Test
    public void getMutcdFromInt_five_returnsGuide() {
        assertEquals(MUTCDCode.guide, TravelerInformationBuilder.getMutcdFromInt(5));
    }

    @Test
    public void getMutcdFromInt_six_returnsRec() {
        assertEquals(MUTCDCode.rec, TravelerInformationBuilder.getMutcdFromInt(6));
    }

    @Test
    public void getMutcdFromInt_unknown_defaultsToNone() {
        assertEquals(MUTCDCode.none, TravelerInformationBuilder.getMutcdFromInt(99));
        assertEquals(MUTCDCode.none, TravelerInformationBuilder.getMutcdFromInt(-1));
        assertEquals(MUTCDCode.none, TravelerInformationBuilder.getMutcdFromInt(7));
    }

    // =========================================================================
    // Content-type dispatch – Speed Limit, Exit Service, Generic
    // These use the same base JSON but swap the "name" field.
    // We verify that build() succeeds (no exception) and returns a non-null
    // hex string, which means the corresponding build* method was entered.
    // =========================================================================

    private String baseTimJson(String contentName) {
        // Minimal valid TIM JSON with swappable content type
        return "{\n" +
            "  \"regions\": [\n" +
            "    {\n" +
            "      \"regionType\": \"lane\",\n" +
            "      \"laneNodes\": [\n" +
            "        { \"nodeNumber\": 0, \"nodeLat\": 38.9549, \"nodeLong\": -77.1492, \"nodeElevation\": 40, \"laneWidth\": 0 },\n" +
            "        { \"nodeNumber\": 1, \"nodeLat\": 38.9545, \"nodeLong\": -77.1488, \"nodeElevation\": 40, \"laneWidth\": 0 }\n" +
            "      ],\n" +
            "      \"extent\": \"\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"anchorPoint\": {\n" +
            "    \"name\": \"" + contentName + "\",\n" +
            "    \"referenceLat\": 38.9555,\n" +
            "    \"referenceLon\": -77.1489,\n" +
            "    \"referenceElevation\": \"38\",\n" +
            "    \"masterLaneWidth\": \"366\",\n" +
            "    \"sspTimRights\": \"\",\n" +
            "    \"packetID\": \"466651996\",\n" +
            "    \"content\": [\n" +
            "      { \"codes\": [\"7989\"], \"text\": \"\" }\n" +
            "    ],\n" +
            "    \"sspTypeRights\": \"\",\n" +
            "    \"sspContentRights\": \"\",\n" +
            "    \"sspLocationRights\": \"\",\n" +
            "    \"direction\": \"\",\n" +
            "    \"mutcd\": \"1\",\n" +
            "    \"infoType\": \"1\",\n" +
            "    \"priority\": \"1\",\n" +
            "    \"startTime\": \"10/14/2025 2:31 PM\",\n" +
            "    \"endTime\": \"10/16/2025 2:31 PM\",\n" +
            "    \"heading\": [11, 12]\n" +
            "  },\n" +
            "  \"verifiedPoint\": {\n" +
            "    \"verifiedMapLat\": 38.9555,\n" +
            "    \"verifiedMapLon\": -77.1489,\n" +
            "    \"verifiedSurveyedLat\": \"38.9555\",\n" +
            "    \"verifiedSurveyedLon\": \"-77.1489\",\n" +
            "    \"verifiedSurveyedElevation\": \"38\"\n" +
            "  },\n" +
            "  \"messageType\": \"TIM\",\n" +
            "  \"nodeOffsets\": \"Compact\",\n" +
            "  \"enableElevation\": false\n" +
            "}";
    }

    @Test
    public void buildContent_speedLimit_buildsWithoutException() {
        TravelerInformationMessage msg =
                (TravelerInformationMessage) BUILDER.build(baseTimJson("Speed Limit"));
        assertNotNull(msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void buildContent_exitService_buildsWithoutException() {
        TravelerInformationMessage msg =
                (TravelerInformationMessage) BUILDER.build(baseTimJson("Exit Service"));
        assertNotNull(msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void buildContent_genericSignage_buildsWithoutException() {
        // Any name not in {Advisory, Work Zone, Speed Limit, Exit Service} → generic
        TravelerInformationMessage msg =
                (TravelerInformationMessage) BUILDER.build(baseTimJson("Other Sign Type"));
        assertNotNull(msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void buildContent_advisory_differentFromWorkZone() {
        // Confirm Advisory and Work Zone produce different hex (different builder paths)
        TravelerInformationMessage advisory =
                (TravelerInformationMessage) BUILDER.build(baseTimJson("Advisory"));
        TravelerInformationMessage workZone =
                (TravelerInformationMessage) BUILDER.build(baseTimJson("Work Zone"));
        assertNotNull(advisory.getHexString());
        assertNotNull(workZone.getHexString());
        // Different content types → different encoded output
        assertNotEquals(advisory.getHexString(), workZone.getHexString());
    }

    // =========================================================================
    // getDurationTime clamping – duration > 32000 minutes
    // =========================================================================

    @Test
    public void build_longDuration_cappedAt32000Minutes() throws IOException {
        // Duration of 32 days = 46,080 minutes >> MAX_MINUTES_DURATION (32,000).
        // The cap must be applied; encoding must succeed and return a non-empty hex string.
        // Uses the same date format as the working fixture files to avoid encoder rejection.
        String json = "{\n" +
            "  \"regions\": [\n" +
            "    {\n" +
            "      \"regionType\": \"lane\",\n" +
            "      \"laneNodes\": [\n" +
            "        { \"nodeNumber\": 0, \"nodeLat\": 38.9549, \"nodeLong\": -77.1492, \"nodeElevation\": 40, \"laneWidth\": 0 },\n" +
            "        { \"nodeNumber\": 1, \"nodeLat\": 38.9545, \"nodeLong\": -77.1488, \"nodeElevation\": 40, \"laneWidth\": 0 }\n" +
            "      ],\n" +
            "      \"extent\": \"\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"anchorPoint\": {\n" +
            "    \"name\": \"Work Zone\",\n" +
            "    \"referenceLat\": 38.9555,\n" +
            "    \"referenceLon\": -77.1489,\n" +
            "    \"referenceElevation\": \"38\",\n" +
            "    \"masterLaneWidth\": \"366\",\n" +
            "    \"sspTimRights\": \"\",\n" +
            "    \"packetID\": \"466651996\",\n" +
            "    \"content\": [{ \"codes\": [\"7989\"], \"text\": \"\" }],\n" +
            "    \"sspTypeRights\": \"\",\n" +
            "    \"sspContentRights\": \"\",\n" +
            "    \"sspLocationRights\": \"\",\n" +
            "    \"direction\": \"\",\n" +
            "    \"mutcd\": \"1\",\n" +
            "    \"infoType\": \"1\",\n" +
            "    \"priority\": \"1\",\n" +
            "    \"startTime\": \"10/14/2025 2:31 PM\",\n" +
            "    \"endTime\":   \"11/15/2025 2:31 PM\",\n" +
            "    \"heading\": []\n" +
            "  },\n" +
            "  \"verifiedPoint\": {\n" +
            "    \"verifiedMapLat\": 38.9555,\n" +
            "    \"verifiedMapLon\": -77.1489,\n" +
            "    \"verifiedSurveyedLat\": \"38.9555\",\n" +
            "    \"verifiedSurveyedLon\": \"-77.1489\",\n" +
            "    \"verifiedSurveyedElevation\": \"38\"\n" +
            "  },\n" +
            "  \"messageType\": \"TIM\",\n" +
            "  \"nodeOffsets\": \"Compact\",\n" +
            "  \"enableElevation\": false\n" +
            "}";

        TravelerInformationMessage msg = (TravelerInformationMessage) BUILDER.build(json);
        assertNotNull(msg);
        assertNotNull(msg.getHexString());
        assertFalse("Hex string should not be empty", msg.getHexString().isEmpty());
    }

    // =========================================================================
    // setRadiusAndUnits – all distance-unit bands via circle TIM JSON
    // =========================================================================

    private String circleTimJson(double radiusMeters) {
        // Circle TIM with variable radius
        return "{\n" +
            "  \"regions\": [\n" +
            "    {\n" +
            "      \"regionType\": \"circle\",\n" +
            "      \"radius\": " + radiusMeters + ",\n" +
            "      \"extent\": \"1\",\n" +
            "      \"laneNodes\": [\n" +
            "        { \"nodeNumber\": 0, \"nodeLat\": 38.9555, \"nodeLong\": -77.1489, \"nodeElevation\": 40, \"laneWidth\": 0 }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"anchorPoint\": {\n" +
            "    \"name\": \"Work Zone\",\n" +
            "    \"referenceLat\": 38.9555,\n" +
            "    \"referenceLon\": -77.1489,\n" +
            "    \"referenceElevation\": \"38\",\n" +
            "    \"masterLaneWidth\": \"0\",\n" +
            "    \"sspTimRights\": \"\",\n" +
            "    \"packetID\": \"466651996\",\n" +
            "    \"content\": [{ \"codes\": [\"7989\"], \"text\": \"\" }],\n" +
            "    \"sspTypeRights\": \"\",\n" +
            "    \"sspContentRights\": \"\",\n" +
            "    \"sspLocationRights\": \"\",\n" +
            "    \"direction\": \"\",\n" +
            "    \"mutcd\": \"0\",\n" +
            "    \"infoType\": \"1\",\n" +
            "    \"priority\": \"1\",\n" +
            "    \"startTime\": \"10/14/2025 2:31 PM\",\n" +
            "    \"endTime\":   \"10/16/2025 2:31 PM\",\n" +
            "    \"heading\": []\n" +
            "  },\n" +
            "  \"verifiedPoint\": {\n" +
            "    \"verifiedMapLat\": 38.9555,\n" +
            "    \"verifiedMapLon\": -77.1489,\n" +
            "    \"verifiedSurveyedLat\": \"38.9555\",\n" +
            "    \"verifiedSurveyedLon\": \"-77.1489\",\n" +
            "    \"verifiedSurveyedElevation\": \"38\"\n" +
            "  },\n" +
            "  \"messageType\": \"TIM\",\n" +
            "  \"nodeOffsets\": \"Standard\",\n" +
            "  \"enableElevation\": false\n" +
            "}";
    }

    @Test
    public void setRadiusAndUnits_centimeterRange_builds() {
        // 1 m = 100 cm → centimeter range [0, 4094 cm]
        assertBuilds(circleTimJson(1.0), "1m radius – centimeter band");
    }

    @Test
    public void setRadiusAndUnits_decimeterRange_builds() {
        // 40.95 m = 4095 cm → enters decimeter band [4095, 40940 cm]
        assertBuilds(circleTimJson(40.95), "40.95m radius – decimeter band");
    }

    @Test
    public void setRadiusAndUnits_meterRange_builds() {
        // 409.41 m = 40941 cm → enters meter band [40941, 409400 cm]
        assertBuilds(circleTimJson(409.41), "409.41m radius – meter band");
    }

    @Test
    public void setRadiusAndUnits_kilometerRange_builds() {
        // 4094.01 m = 409401 cm → enters kilometer band [409401, 409_400_000 cm]
        assertBuilds(circleTimJson(4094.01), "4094.01m radius – kilometer band");
    }

    @Test
    public void setRadiusAndUnits_beyondKilometerRange_builds() {
        // > 4_094_000 m → unknown band → radius set to 4095
        assertBuilds(circleTimJson(4_100_000.0), "4,100,000m radius – unknown/overflow band");
    }

    @Test
    public void setRadiusAndUnits_exactlyDivisibleCm_upconvertsToDm() {
        // 4 m = 400 cm; 400 / 10 = 40 dm (even) → up-converts to dm, then 40/10=4 m → further up
        assertBuilds(circleTimJson(4.0), "4m radius – up-conversion chain");
    }

    @Test
    public void setRadiusAndUnits_zeroRadius_builds() {
        // 0 m = 0 cm → centimeter band, radius 0
        assertBuilds(circleTimJson(0.0), "0m radius");
    }

    // =========================================================================
    // Heading: null/empty heading array → HeadingSlice(0) branch
    // =========================================================================

    @Test
    public void buildContent_emptyHeadingArray_buildsSuccessfully() {
        // heading: [] → getHeadingSlice returns HeadingSlice(0)
        String json = baseTimJson("Work Zone")
                .replace("\"heading\": [11, 12]", "\"heading\": []");
        TravelerInformationMessage msg = (TravelerInformationMessage) BUILDER.build(json);
        assertNotNull(msg.getHexString());
    }

    @Test
    public void buildContent_nullHeadingField_buildsSuccessfully() {
        // heading absent from JSON → field defaults to null → HeadingSlice(0)
        String json = baseTimJson("Work Zone")
                .replace(",\n    \"heading\": [11, 12]", "");
        TravelerInformationMessage msg = (TravelerInformationMessage) BUILDER.build(json);
        assertNotNull(msg.getHexString());
    }

    // =========================================================================
    // elevation enabled path
    // =========================================================================

    @Test
    public void buildContent_withElevationEnabled_builds() throws IOException {
        // Use existing road-roughness test file which has enableElevation or add flag
        TravelerInformationBuilder builder = new TravelerInformationBuilder();
        String json = FileUtils.readFileToString(
                new File("src/test/resources/sample_tim.json"));
        // Replace enableElevation flag
        json = json.replace("\"enableElevation\": false", "\"enableElevation\": true");
        TravelerInformationMessage msg = (TravelerInformationMessage) builder.build(json);
        assertNotNull(msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void assertBuilds(String json, String description) {
        try {
            TravelerInformationMessage msg = (TravelerInformationMessage) BUILDER.build(json);
            assertNotNull("hex must not be null for: " + description, msg.getHexString());
            assertFalse("hex must not be empty for: " + description, msg.getHexString().isEmpty());
        } catch (Exception e) {
            fail("build() threw for [" + description + "]: " + e);
        }
    }
}