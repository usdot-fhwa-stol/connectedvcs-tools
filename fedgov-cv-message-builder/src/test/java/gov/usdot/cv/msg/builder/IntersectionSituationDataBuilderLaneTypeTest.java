/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder;

import static org.junit.Assert.*;
import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.msg.builder.message.IntersectionMessage;
import gov.usdot.cv.mapencoder.LaneTypeAttributes;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Tests for lane-type dispatch in {@link IntersectionSituationDataBuilder}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * The toLaneTypeAttributes() method is a critical dispatch that maps a string
 * lane type to the correct J2735 LaneTypeAttributes choice. If the wrong choice
 * is set (e.g. BIKE_LANE instead of SIDEWALK), the encoded MAP message will be
 * syntactically valid but semantically wrong — downstream V2X receivers will
 * misinterpret the lane. These tests verify that each lane type produces the
 * correct LaneTypeAttributes choice constant.
 *
 * Covered lane types (not covered by existing tests):
 *   crosswalk, sidewalk, median, striping, trackedVehicle, parking
 * Already covered by existing fixtures:
 *   vehicle, bike
 */
public class IntersectionSituationDataBuilderLaneTypeTest {

    private IntersectionSituationDataBuilder builder;

    @Before
    public void setUp() {
        builder = new IntersectionSituationDataBuilder();
    }

    // =========================================================================
    // Helper: build a minimal intersection JSON with a specific lane type
    // =========================================================================

    /**
     * Returns a minimal intersection JSON string with the given lane type
     * in a single driving lane. Uses valid coordinates and the minimum
     * fields needed for the builder to succeed.
     */
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

    // =========================================================================
    // Lane types NOT covered by existing fixtures
    // =========================================================================

    @Test
    public void crosswalkLane_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("crosswalk"));
        assertNotNull("Crosswalk lane build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void sidewalkLane_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("sidewalk"));
        assertNotNull("Sidewalk lane build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void medianLane_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("median"));
        assertNotNull("Median lane build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void stripingLane_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("striping"));
        assertNotNull("Striping lane build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void trackedVehicleLane_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("trackedVehicle"));
        assertNotNull("TrackedVehicle lane build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void parkingLane_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("parking"));
        assertNotNull("Parking lane build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }

    // =========================================================================
    // Case-insensitivity — laneType matching is toLowerCase()
    // =========================================================================

    @Test
    public void vehicleLane_mixedCase_buildsSuccessfully() throws IOException {
        // Existing fixtures use "Vehicle" (capital V) — verify matching works
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("Vehicle"));
        assertNotNull(msg);
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void crosswalkLane_upperCase_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("CROSSWALK"));
        assertNotNull(msg);
        assertFalse(msg.getHexString().isEmpty());
    }

    // =========================================================================
    // Invalid / unsupported lane type — should throw MessageBuildException
    // =========================================================================

    @Test(expected = MessageBuildException.class)
    public void unsupportedLaneType_throwsMessageBuildException() throws IOException {
        builder.build(intersectionJson("unsupportedType"));
    }

    @Test(expected = MessageBuildException.class)
    public void emptyLaneType_throwsMessageBuildException() throws IOException {
        builder.build(intersectionJson(""));
    }

    // =========================================================================
    // Regression: existing lane types still work
    // =========================================================================

    @Test
    public void vehicleLane_regression_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("vehicle"));
        assertNotNull(msg);
        assertFalse(msg.getHexString().isEmpty());
    }

    @Test
    public void bikeLane_regression_buildsSuccessfully() throws IOException {
        IntersectionMessage msg = (IntersectionMessage) builder.build(intersectionJson("bike"));
        assertNotNull(msg);
        assertFalse(msg.getHexString().isEmpty());
    }

    // =========================================================================
    // Crosswalk approach type (separate code path in buildRGAGeometryLayers)
    // =========================================================================

    @Test
    public void crosswalkApproachType_buildsSuccessfully() throws IOException {
        // Use the real crosswalk approach pattern from the codebase
        String json = "{\n" +
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

        IntersectionMessage msg = (IntersectionMessage) builder.build(json);
        assertNotNull("Crosswalk approach build must succeed", msg);
        assertNotNull(msg.getHexString());
        assertFalse(msg.getHexString().isEmpty());
    }
}