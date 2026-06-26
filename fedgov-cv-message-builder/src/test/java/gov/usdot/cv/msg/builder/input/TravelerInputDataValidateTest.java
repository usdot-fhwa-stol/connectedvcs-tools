/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.input;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TravelerInputData#validate()}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * validate() is the gatekeeper for every TIM message build. It checks regions,
 * coordinates, content, mutcd, timing, infoType, and deposit validity before
 * any encoding begins. If this method has a bug — wrong valid range, wrong
 * null check — then invalid TIM messages reach the encoder and either fail
 * with an opaque exception or produce a semantically wrong output.
 *
 * These tests verify every branch: the success path (valid data passes),
 * and each specific invalid condition (null regions, wrong regionType,
 * bad extent, null anchorPoint, zero coordinates, bad mutcd, date ordering,
 * bad infoType, invalid deposit TTL).
 */
public class TravelerInputDataValidateTest {

    private TravelerInputData data;

    @Before
    public void setUp() {
        data = new TravelerInputData();
        data.regions = new TravelerInputData.Region[]{validRegion()};
        data.anchorPoint = validAnchorPoint();
        data.verifiedPoint = validVerifiedPoint();
    }

    // =========================================================================
    // Success path
    // =========================================================================

    @Test
    public void validate_allValidFields_doesNotThrow() {
        data.validate(); // should not throw
    }

    @Test
    public void validate_withValidDeposit_doesNotThrow() {
        TravelerInputData.Deposit dep = new TravelerInputData.Deposit();
        dep.timeToLive = 2; // valid: 0-5
        data.deposit = dep;
        data.validate();
    }

    @Test
    public void validate_depositTimeToLiveMinusOne_skipsValidation() {
        // default timeToLive = -1 means "not set", skip validation
        TravelerInputData.Deposit dep = new TravelerInputData.Deposit();
        dep.timeToLive = -1;
        data.deposit = dep;
        data.validate();
    }

    // =========================================================================
    // regions validation
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullRegions_throws() {
        data.regions = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_emptyRegions_throws() {
        data.regions = new TravelerInputData.Region[]{};
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullRegionType_throws() {
        data.regions[0].regionType = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidRegionType_throws() {
        data.regions[0].regionType = "polygon"; // not lane/region/circle
        data.validate();
    }

    @Test
    public void validate_regionTypeLane_valid() {
        data.regions[0].regionType = "lane";
        data.validate();
    }

    @Test
    public void validate_regionTypeRegion_valid() {
        data.regions[0].regionType = "region";
        data.validate();
    }

    @Test
    public void validate_regionTypeCircle_valid() {
        data.regions[0].regionType = "circle";
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidExtent_throws() {
        data.regions[0].extent = 99; // valid: 0-15
        data.validate();
    }

    @Test
    public void validate_extentMinusOne_skipsValidation() {
        data.regions[0].extent = -1; // default, skip validation
        data.validate();
    }

    @Test
    public void validate_extentZero_valid() {
        data.regions[0].extent = 0;
        data.validate();
    }

    @Test
    public void validate_extentFifteen_valid() {
        data.regions[0].extent = 15;
        data.validate();
    }

    // =========================================================================
    // laneNode coordinate validation
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void validate_laneNodeLatZero_throws() {
        data.regions[0].laneNodes[0].nodeLat = 0.0; // 0.0 = uninitialized
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_laneNodeLatOutOfRange_throws() {
        data.regions[0].laneNodes[0].nodeLat = 91.0; // > 90
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_laneNodeLonZero_throws() {
        data.regions[0].laneNodes[0].nodeLong = 0.0;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_laneNodeLonOutOfRange_throws() {
        data.regions[0].laneNodes[0].nodeLong = -181.0; // < -180
        data.validate();
    }

    // =========================================================================
    // anchorPoint validation
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullAnchorPoint_throws() {
        data.anchorPoint = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_anchorPointLatZero_throws() {
        data.anchorPoint.referenceLat = 0.0;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_anchorPointLonZero_throws() {
        data.anchorPoint.referenceLon = 0.0;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullContent_throws() {
        data.anchorPoint.content = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_emptyContent_throws() {
        data.anchorPoint.content = new TravelerInputData.ItisContent[]{};
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidMutcd_throws() {
        data.anchorPoint.mutcd = 7; // valid: 0-6
        data.validate();
    }

    @Test
    public void validate_mutcdZero_valid() {
        data.anchorPoint.mutcd = 0;
        data.validate();
    }

    @Test
    public void validate_mutcdSix_valid() {
        data.anchorPoint.mutcd = 6;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullStartTime_throws() {
        data.anchorPoint.startTime = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_emptyStartTime_throws() {
        data.anchorPoint.startTime = "";
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidStartTimeFormat_throws() {
        data.anchorPoint.startTime = "not-a-date";
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullEndTime_throws() {
        data.anchorPoint.endTime = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_startTimeAfterEndTime_throws() {
        data.anchorPoint.startTime = "10/16/2025 2:31 PM";
        data.anchorPoint.endTime   = "10/14/2025 2:31 PM"; // end before start
        data.validate();
    }

    @Test
    public void validate_startTimeEqualsEndTime_valid() {
        data.anchorPoint.startTime = "10/14/2025 2:31 PM";
        data.anchorPoint.endTime   = "10/14/2025 2:31 PM";
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidInfoType_throws() {
        data.anchorPoint.infoType = 4; // valid: 0-3
        data.validate();
    }

    @Test
    public void validate_infoTypeZero_valid() {
        data.anchorPoint.infoType = 0;
        data.validate();
    }

    @Test
    public void validate_infoTypeThree_valid() {
        data.anchorPoint.infoType = 3;
        data.validate();
    }

    // =========================================================================
    // verifiedPoint validation
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullVerifiedPoint_throws() {
        data.verifiedPoint = null;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_verifiedPointMapLatZero_throws() {
        data.verifiedPoint.verifiedMapLat = 0.0;
        data.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_verifiedPointSurveyedLonOutOfRange_throws() {
        data.verifiedPoint.verifiedSurveyedLon = 200.0; // > 180
        data.validate();
    }

    // =========================================================================
    // deposit TTL validation
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void validate_invalidDepositTimeToLive_throws() {
        TravelerInputData.Deposit dep = new TravelerInputData.Deposit();
        dep.timeToLive = 6; // valid: 0-5
        data.deposit = dep;
        data.validate();
    }

    // =========================================================================
    // applyLatLonOffset
    // =========================================================================

    @Test
    public void applyLatLonOffset_shiftsCoordinates() {
        data.verifiedPoint.verifiedMapLat = 38.9555;
        data.verifiedPoint.verifiedMapLon = -77.1489;
        data.verifiedPoint.verifiedMapElevation = 10.0;
        data.verifiedPoint.verifiedSurveyedLat = 38.9560;
        data.verifiedPoint.verifiedSurveyedLon = -77.1484;
        data.verifiedPoint.verifiedSurveyedElevation = 12.0;

        double originalLat = data.anchorPoint.referenceLat;
        double originalLon = data.anchorPoint.referenceLon;

        data.applyLatLonOffset();

        assertEquals(originalLat + 0.0005, data.anchorPoint.referenceLat, 1e-7);
        assertEquals(originalLon + 0.0005, data.anchorPoint.referenceLon, 1e-7);
        // Lane nodes also shifted
        assertEquals(38.9549 + 0.0005, data.regions[0].laneNodes[0].nodeLat, 1e-7);
    }

    // =========================================================================
    // initialzeReferencePoints
    // =========================================================================

    @Test
    public void initialzeReferencePoints_setsRefPointOnRegions() {
        assertNull("refPoint should be null before initialization", data.regions[0].refPoint);
        data.initialzeReferencePoints();
        assertNotNull("refPoint should be set after initialization", data.regions[0].refPoint);
        assertEquals(data.anchorPoint.referenceLat, data.regions[0].refPoint.getLat(), 1e-7);
        assertEquals(data.anchorPoint.referenceLon, data.regions[0].refPoint.getLon(), 1e-7);
    }

    // =========================================================================
    // Helpers — build valid fixture objects
    // =========================================================================

    private TravelerInputData.Region validRegion() {
        TravelerInputData.Region r = new TravelerInputData.Region();
        r.regionType = "lane";
        r.extent = -1; // skip extent validation
        r.laneNodes = new TravelerInputData.LaneNode[]{validLaneNode()};
        return r;
    }

    private TravelerInputData.LaneNode validLaneNode() {
        TravelerInputData.LaneNode node = new TravelerInputData.LaneNode();
        node.nodeNumber = 0;
        node.nodeLat = 38.9549;
        node.nodeLong = -77.1492;
        node.nodeElevation = 40.0;
        return node;
    }

    private TravelerInputData.AnchorPoint validAnchorPoint() {
        TravelerInputData.AnchorPoint ap = new TravelerInputData.AnchorPoint();
        ap.referenceLat = 38.9555;
        ap.referenceLon = -77.1489;
        ap.referenceElevation = 38.0;
        ap.content = new TravelerInputData.ItisContent[]{new TravelerInputData.ItisContent()};
        ap.mutcd = 1;
        ap.startTime = "10/14/2025 2:31 PM";
        ap.endTime   = "10/16/2025 2:31 PM";
        ap.infoType  = 1;
        return ap;
    }

    private TravelerInputData.VerifiedPoint validVerifiedPoint() {
        TravelerInputData.VerifiedPoint vp = new TravelerInputData.VerifiedPoint();
        vp.verifiedMapLat = 38.9555;
        vp.verifiedMapLon = -77.1489;
        vp.verifiedMapElevation = 38.0;
        vp.verifiedSurveyedLat = 38.9555;
        vp.verifiedSurveyedLon = -77.1489;
        vp.verifiedSurveyedElevation = 38.0;
        return vp;
    }
}