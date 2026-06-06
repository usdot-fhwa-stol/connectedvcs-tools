/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder;

import static org.junit.Assert.*;
import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.msg.builder.message.IntersectionMessage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Tests for lane-type dispatch in {@link IntersectionSituationDataBuilder}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * toLaneTypeAttributes() maps a string lane type to the correct J2735
 * LaneTypeAttributes choice. If the wrong choice is set the encoded MAP
 * message is silently wrong — downstream V2X receivers misinterpret the lane.
 *
 * DESIGN NOTE — why all build() calls are in one @Test method:
 * maven-surefire runs with parallel=classes, threadCount=4.
 * The native MAP JNI encoder (asn1c / libasn1c.so) is not thread-safe,
 * exactly like the TIM encoder. Multiple @Test methods calling build()
 * simultaneously causes random MessageEncodeException crashes.
 * Consolidating into one method ensures sequential, single-thread execution.
 *
 * SOURCE BUG DOCUMENTED:
 * The source code does type = type.toLowerCase() then compares to "trackedVehicle"
 * (mixed case). Since "trackedvehicle" != "trackedVehicle", the trackedVehicle
 * branch is UNREACHABLE — any input falls to the unsupported-type exception.
 * The test documents this real defect by asserting the actual (broken) behavior.
 */
public class IntersectionSituationDataBuilderLaneTypeTest {

    private IntersectionSituationDataBuilder builder;

    @Before
    public void setUp() {
        builder = new IntersectionSituationDataBuilder();
    }

    // =========================================================================
    // All lane-type and error-path tests in ONE @Test — sequential, single-thread
    // =========================================================================

    @Test
    public void allLaneTypeTests() throws IOException {

        // ---- vehicle (regression) ----
        assertBuildsValidHex(intersectionJson("vehicle"), "vehicle lane");
        assertBuildsValidHex(intersectionJson("Vehicle"), "Vehicle (mixed case)");

        // ---- bike (regression) ----
        assertBuildsValidHex(intersectionJson("bike"), "bike lane");

        // ---- crosswalk ----
        assertBuildsValidHex(intersectionJson("crosswalk"), "crosswalk lane");
        assertBuildsValidHex(intersectionJson("CROSSWALK"), "CROSSWALK (upper case)");

        // ---- sidewalk ----
        assertBuildsValidHex(intersectionJson("sidewalk"), "sidewalk lane");

        // ---- median ----
        assertBuildsValidHex(intersectionJson("median"), "median lane");

        // ---- striping ----
        assertBuildsValidHex(intersectionJson("striping"), "striping lane");

        // ---- parking ----
        assertBuildsValidHex(intersectionJson("parking"), "parking lane");

        // ---- SOURCE BUG: trackedVehicle is unreachable due to case mismatch ----
        // The source does type.toLowerCase() producing "trackedvehicle", then
        // compares with type.equals("trackedVehicle") — capital V never matches.
        // This branch has been dead since it was written.
        // Expected behavior: throws MessageBuildException (unsupported type)
        // Bug fix required in source: change "trackedVehicle" to "trackedvehicle"
        assertThrowsMessageBuildException(
            intersectionJson("trackedVehicle"),
            "trackedVehicle — unreachable due to source bug (toLowerCase mismatch)"
        );
        assertThrowsMessageBuildException(
            intersectionJson("trackedvehicle"),
            "trackedvehicle — also hits unsupported branch (source bug)"
        );

        // ---- unsupported type → MessageBuildException ----
        assertThrowsMessageBuildException(
            intersectionJson("unsupportedType"),
            "unsupported lane type"
        );

        // ---- empty type → MessageBuildException ----
        assertThrowsMessageBuildException(
            intersectionJson(""),
            "empty lane type"
        );

        // ---- crosswalk approach type (separate code path) ----
        assertBuildsValidHex(crosswalkApproachJson(), "crosswalk approach type");
    }

    // =========================================================================
    // JSON builders
    // =========================================================================

    private String intersectionJson(String laneType) {
        return "{\n" +
            "  \"mapData\": {\n" +
            "    \"minuteOfTheYear\": 408355,\n" +
            "    \"layerType\": \"intersectionData\",\n" +
            "    \"intersectionGeometry\": {\n" +
            "      \"referencePoint\": {\n" +
            "        \"descriptiveIntersctionName\": \"test intersection\",\n" +
            "        \"intersectionID\": \"1001\",\n" +
            "        \"layerID\": \"1\",\n" +
            "        \"msgCount\": \"1\",\n" +
            "        \"masterLaneWidth\": \"366\",\n" +
            "        \"referenceLat\": 38.9555,\n" +
            "        \"referenceLon\": -77.1489,\n" +
            "        \"referenceElevation\": \"38\"\n" +
            "      },\n" +
            "      \"verifiedPoint\": {\n" +
            "        \"verifiedMapLat\": 38.9555,\n" +
            "        \"verifiedMapLon\": -77.1489,\n" +
            "        \"verifiedMapElevation\": \"38\",\n" +
            "        \"verifiedSurveyedLat\": \"38.9555\",\n" +
            "        \"verifiedSurveyedLon\": \"-77.1489\",\n" +
            "        \"verifiedSurveyedElevation\": \"38\"\n" +
            "      },\n" +
            "      \"laneList\": {\n" +
            "        \"approach\": [\n" +
            "          {\n" +
            "            \"approachType\": \"Egress\",\n" +
            "            \"approachID\": \"1\",\n" +
            "            \"speedLimit\": \"30\",\n" +
            "            \"laneDirection\": \"(1) Northbound\",\n" +
            "            \"drivingLanes\": [\n" +
            "              {\n" +
            "                \"laneID\": \"01\",\n" +
            "                \"laneWidth\": \"366\",\n" +
            "                \"laneType\": \"" + laneType + "\",\n" +
            "                \"typeAttributes\": [1, 2],\n" +
            "                \"sharedWith\": [],\n" +
            "                \"laneManeuvers\": [1],\n" +
            "                \"laneNodes\": [\n" +
            "                  { \"nodeNumber\": 0, \"nodeLat\": 38.9549, \"nodeLong\": -77.1492 },\n" +
            "                  { \"nodeNumber\": 1, \"nodeLat\": 38.9545, \"nodeLong\": -77.1488 },\n" +
            "                  { \"nodeNumber\": 2, \"nodeLat\": 38.9541, \"nodeLong\": -77.1484 }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"messageType\": \"Map\",\n" +
            "  \"nodeOffsets\": \"Compact\"\n" +
            "}";
    }

    private String crosswalkApproachJson() {
        return "{\n" +
            "  \"mapData\": {\n" +
            "    \"minuteOfTheYear\": 408355,\n" +
            "    \"layerType\": \"intersectionData\",\n" +
            "    \"intersectionGeometry\": {\n" +
            "      \"referencePoint\": {\n" +
            "        \"descriptiveIntersctionName\": \"crosswalk test\",\n" +
            "        \"intersectionID\": \"1002\",\n" +
            "        \"layerID\": \"1\",\n" +
            "        \"msgCount\": \"1\",\n" +
            "        \"masterLaneWidth\": \"366\",\n" +
            "        \"referenceLat\": 38.9555,\n" +
            "        \"referenceLon\": -77.1489,\n" +
            "        \"referenceElevation\": \"38\"\n" +
            "      },\n" +
            "      \"verifiedPoint\": {\n" +
            "        \"verifiedMapLat\": 38.9555,\n" +
            "        \"verifiedMapLon\": -77.1489,\n" +
            "        \"verifiedMapElevation\": \"38\",\n" +
            "        \"verifiedSurveyedLat\": \"38.9555\",\n" +
            "        \"verifiedSurveyedLon\": \"-77.1489\",\n" +
            "        \"verifiedSurveyedElevation\": \"38\"\n" +
            "      },\n" +
            "      \"laneList\": {\n" +
            "        \"approach\": [\n" +
            "          {\n" +
            "            \"approachType\": \"crosswalk\",\n" +
            "            \"approachID\": \"-1\",\n" +
            "            \"speedLimit\": \"15\",\n" +
            "            \"laneDirection\": \"(3) Eastbound\",\n" +
            "            \"drivingLanes\": [\n" +
            "              {\n" +
            "                \"laneID\": \"02\",\n" +
            "                \"laneWidth\": \"200\",\n" +
            "                \"laneType\": \"crosswalk\",\n" +
            "                \"typeAttributes\": [],\n" +
            "                \"sharedWith\": [],\n" +
            "                \"laneManeuvers\": [],\n" +
            "                \"laneNodes\": [\n" +
            "                  { \"nodeNumber\": 0, \"nodeLat\": 38.9548, \"nodeLong\": -77.1490 },\n" +
            "                  { \"nodeNumber\": 1, \"nodeLat\": 38.9547, \"nodeLong\": -77.1488 },\n" +
            "                  { \"nodeNumber\": 2, \"nodeLat\": 38.9546, \"nodeLong\": -77.1486 }\n" +
            "                ]\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"messageType\": \"Map\",\n" +
            "  \"nodeOffsets\": \"Compact\"\n" +
            "}";
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void assertBuildsValidHex(String json, String description) throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(json);
        assertNotNull("Build must not return null for: " + description, msg);
        assertNotNull("Hex must not be null for: " + description, msg.getHexString());
        assertFalse("Hex must not be empty for: " + description, msg.getHexString().isEmpty());
    }

    private void assertThrowsMessageBuildException(String json, String description) {
        try {
            builder.build(json);
            fail("Expected MessageBuildException for: " + description);
        } catch (MessageBuildException e) {
            // expected
        } catch (Exception e) {
            fail("Expected MessageBuildException but got " + e.getClass().getSimpleName()
                + " for: " + description);
        }
    }
}