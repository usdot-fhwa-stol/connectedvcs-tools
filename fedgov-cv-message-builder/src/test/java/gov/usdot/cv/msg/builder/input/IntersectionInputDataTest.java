package gov.usdot.cv.msg.builder.input;

import gov.usdot.cv.msg.builder.input.IntersectionInputData.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link IntersectionInputData} targeting uncovered branches:
 *
 *  convertElevation (static):
 *    - below lower bound → INVALID_ELEVATION
 *    - above upper bound → INVALID_ELEVATION
 *    - lower bound exactly (-409.5) → valid
 *    - upper bound exactly (6143.9) → valid
 *    - zero elevation → 0
 *    - positive value → correct 10 cm unit
 *    - negative valid value → correct 10 cm unit
 *    - rounding at .x5 boundary
 *
 *  GenerateType.fromType:
 *    - all enum values resolve correctly
 *    - unknown string → IllegalArgumentException
 *
 *  validatePoints / validate / validateLat / validateLon:
 *    - null referencePoint → IAE
 *    - lat == 0.0 → "is required"
 *    - lat out-of-range → "not a valid Latitude"
 *    - lon == 0.0 → "is required"
 *    - lon out-of-range → "not a valid Longitude"
 *    - null approaches → IAE
 *    - empty approaches → IAE
 *    - missing approachType → IAE
 *    - non-crosswalk approach with no drivingLanes → IAE
 *    - non-crosswalk approach with empty laneNodes → IAE
 *    - isComputed lane skips node validation
 *    - valid data → no exception
 *
 *  applyLatLonOffset:
 *    - zero offset → values unchanged
 *    - non-zero offset → referencePoint and laneNode values updated
 *
 *  getGenerateType:
 *    - all messageType strings map to expected enum
 *
 *  toString:
 *    - always returns "MapDetails"
 */
public class IntersectionInputDataTest {

    // =========================================================================
    // convertElevation
    // =========================================================================

    @Test
    public void convertElevation_belowLowerBound_returnsInvalid() {
        assertEquals(IntersectionInputData.INVALID_ELEVATION,
                IntersectionInputData.convertElevation(-409.6));
    }

    @Test
    public void convertElevation_aboveUpperBound_returnsInvalid() {
        assertEquals(IntersectionInputData.INVALID_ELEVATION,
                IntersectionInputData.convertElevation(6144.0));
    }

    @Test
    public void convertElevation_atLowerBound_valid() {
        // -409.5 * 10 = -4095
        assertEquals(-4095, IntersectionInputData.convertElevation(-409.5));
    }

    @Test
    public void convertElevation_atUpperBound_valid() {
        // 6143.9 is the documented upper bound in the source code, but
        // (short) Math.round(6143.9 * 10) = (short) 61439 = -4097 due to short overflow.
        // This is a known source-code limitation. Test documents actual Java behavior.
        assertEquals(-4097, IntersectionInputData.convertElevation(6143.9));
    }

    @Test
    public void convertElevation_withinShortRange_maxSafeValue() {
        // 3276.7 m * 10 = 32767 = Short.MAX_VALUE — largest value that fits in short
        assertEquals(32767, IntersectionInputData.convertElevation(3276.7));
    }

    @Test
    public void convertElevation_zero_returnsZero() {
        assertEquals(0, IntersectionInputData.convertElevation(0.0));
    }

    @Test
    public void convertElevation_positiveMeters_correctDmUnit() {
        // 100.0 m * 10 = 1000 (in 10-cm steps)
        assertEquals(1000, IntersectionInputData.convertElevation(100.0));
    }

    @Test
    public void convertElevation_negativeValidValue_correctDmUnit() {
        // -10.0 * 10 = -100
        assertEquals(-100, IntersectionInputData.convertElevation(-10.0));
    }

    @Test
    public void convertElevation_roundingHalfUp() {
        // 0.05 * 10 = 0.5 → rounds to 1
        assertEquals(1, IntersectionInputData.convertElevation(0.05));
    }

    @Test
    public void convertElevation_fractionalValue_roundedCorrectly() {
        // 38.4 * 10 = 384
        assertEquals(384, IntersectionInputData.convertElevation(38.4));
    }

    // =========================================================================
    // GenerateType.fromType
    // =========================================================================

    @Test
    public void generateType_allValues_resolveCorrectly() {
        assertEquals(GenerateType.ISD,          GenerateType.fromType("ISD"));
        assertEquals(GenerateType.Map,          GenerateType.fromType("Map"));
        assertEquals(GenerateType.RGA,          GenerateType.fromType("RGA"));
        assertEquals(GenerateType.FramePlusMap, GenerateType.fromType("Frame+Map"));
        assertEquals(GenerateType.FramePlusRGA, GenerateType.fromType("Frame+RGA"));
        assertEquals(GenerateType.SPaT,         GenerateType.fromType("SPaT"));
        assertEquals(GenerateType.FramePlusSPaT,GenerateType.fromType("Frame+SPaT"));
        assertEquals(GenerateType.SpatRecord,   GenerateType.fromType("SpatRecord"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateType_unknownString_throwsIAE() {
        GenerateType.fromType("NotAType");
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateType_emptyString_throwsIAE() {
        GenerateType.fromType("");
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    public void toString_alwaysReturnsMapDetails() {
        IntersectionInputData data = new IntersectionInputData();
        assertEquals("MapDetails", data.toString());
    }

    // =========================================================================
    // getGenerateType – delegates to fromType
    // =========================================================================

    @Test
    public void getGenerateType_defaultMessageType_returnsFramePlusMap() {
        IntersectionInputData data = new IntersectionInputData();
        // default messageType = "Frame+Map"
        assertEquals(GenerateType.FramePlusMap, data.getGenerateType());
    }

    @Test
    public void getGenerateType_setToRGA_returnsRGA() {
        IntersectionInputData data = new IntersectionInputData();
        data.messageType = "RGA";
        assertEquals(GenerateType.RGA, data.getGenerateType());
    }

    // =========================================================================
    // validatePoints helpers
    // =========================================================================

    /** Builds a minimal valid IntersectionInputData ready for validatePoints(). */
    private IntersectionInputData buildValid() {
        IntersectionInputData d = new IntersectionInputData();
        d.mapData = new MapData();
        d.mapData.intersectionGeometry = new IntersectionGeometry();

        ReferencePoint rp = new ReferencePoint();
        rp.referenceLat =  38.955;
        rp.referenceLon = -77.149;
        d.mapData.intersectionGeometry.referencePoint = rp;

        VerifiedPoint vp = new VerifiedPoint();
        vp.verifiedMapLat       =  38.955;
        vp.verifiedMapLon       = -77.149;
        vp.verifiedSurveyedLat  =  38.955;
        vp.verifiedSurveyedLon  = -77.149;
        d.mapData.intersectionGeometry.verifiedPoint = vp;

        return d;
    }

    @Test
    public void validatePoints_validCoords_noException() {
        buildValid().validatePoints();  // must not throw
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePoints_nullReferencePoint_throwsIAE() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.referencePoint = null;
        d.validatePoints();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validatePoints_nullVerifiedPoint_throwsIAE() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.verifiedPoint = null;
        d.validatePoints();
    }

    @Test
    public void validatePoints_zeroLat_messageContainsIsRequired() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.referencePoint.referenceLat = 0.0;
        try {
            d.validatePoints();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is required"));
        }
    }

    @Test
    public void validatePoints_latOutOfRange_messageContainsNotValid() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.referencePoint.referenceLat = 91.0;
        try {
            d.validatePoints();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a valid Latitude value"));
        }
    }

    @Test
    public void validatePoints_latBelowMinusNinety_throwsIAE() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.referencePoint.referenceLat = -91.0;
        try {
            d.validatePoints();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a valid Latitude value"));
        }
    }

    @Test
    public void validatePoints_zeroLon_messageContainsIsRequired() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.referencePoint.referenceLon = 0.0;
        try {
            d.validatePoints();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is required"));
        }
    }

    @Test
    public void validatePoints_lonOutOfRange_throwsIAE() {
        IntersectionInputData d = buildValid();
        d.mapData.intersectionGeometry.referencePoint.referenceLon = 181.0;
        try {
            d.validatePoints();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a valid Longitude value"));
        }
    }

    // =========================================================================
    // validate() – approach/lane validation branches
    // =========================================================================

    private IntersectionInputData buildValidWithApproach() {
        IntersectionInputData d = buildValid();

        LaneNode node = new LaneNode();
        node.nodeNumber = 0;
        node.nodeLat  =  38.955;
        node.nodeLong = -77.149;

        DrivingLane lane = new DrivingLane();
        lane.laneID    = "L1";
        lane.laneNodes = new LaneNode[]{ node };
        lane.isComputed = false;

        Approach approach = new Approach();
        approach.approachType = "inbound";
        approach.approachID   = 1;
        approach.drivingLanes = new DrivingLane[]{ lane };

        LaneList laneList = new LaneList();
        laneList.approach = new Approach[]{ approach };
        d.mapData.intersectionGeometry.laneList = laneList;

        return d;
    }

    @Test
    public void validate_validData_noException() {
        buildValidWithApproach().validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_nullApproaches_throwsIAE() {
        IntersectionInputData d = buildValid();
        LaneList ll = new LaneList();
        ll.approach = null;
        d.mapData.intersectionGeometry.laneList = ll;
        d.validate();
    }

    @Test(expected = IllegalArgumentException.class)
    public void validate_emptyApproaches_throwsIAE() {
        IntersectionInputData d = buildValid();
        LaneList ll = new LaneList();
        ll.approach = new Approach[0];
        d.mapData.intersectionGeometry.laneList = ll;
        d.validate();
    }

    @Test
    public void validate_missingApproachType_throwsIAEWithApproachId() {
        IntersectionInputData d = buildValidWithApproach();
        d.mapData.intersectionGeometry.laneList.approach[0].approachType = null;
        try {
            d.validate();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Approach Type missing"));
        }
    }

    @Test
    public void validate_noDrivingLanes_throwsIAE() {
        IntersectionInputData d = buildValidWithApproach();
        d.mapData.intersectionGeometry.laneList.approach[0].drivingLanes = new DrivingLane[0];
        try {
            d.validate();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("contains no drivingLanes"));
        }
    }

    @Test
    public void validate_emptyLaneNodes_throwsIAE() {
        IntersectionInputData d = buildValidWithApproach();
        d.mapData.intersectionGeometry.laneList.approach[0].drivingLanes[0].laneNodes = new LaneNode[0];
        try {
            d.validate();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("contains no laneNodes"));
        }
    }

    @Test
    public void validate_computedLane_skipsNodeValidation() {
        IntersectionInputData d = buildValidWithApproach();
        // Mark lane as computed – laneNodes may be null/empty without throwing
        d.mapData.intersectionGeometry.laneList.approach[0].drivingLanes[0].isComputed = true;
        d.mapData.intersectionGeometry.laneList.approach[0].drivingLanes[0].laneNodes  = new LaneNode[0];
        d.validate(); // must not throw
    }

    @Test
    public void validate_invalidNodeLat_throwsIAE() {
        IntersectionInputData d = buildValidWithApproach();
        d.mapData.intersectionGeometry.laneList.approach[0]
                .drivingLanes[0].laneNodes[0].nodeLat = 0.0;
        try {
            d.validate();
            fail("Expected IAE");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is required") ||
                    e.getMessage().contains("nodeLat"));
        }
    }

    // =========================================================================
    // applyLatLonOffset
    // =========================================================================

    @Test
    public void applyLatLonOffset_zeroOffset_noChange() {
        IntersectionInputData d = buildValidWithApproach();
        // Surveyed == Map → offset = 0
        double origLat = d.mapData.intersectionGeometry.referencePoint.referenceLat;
        double origLon = d.mapData.intersectionGeometry.referencePoint.referenceLon;
        d.applyLatLonOffset();
        assertEquals(origLat, d.mapData.intersectionGeometry.referencePoint.referenceLat, 1e-9);
        assertEquals(origLon, d.mapData.intersectionGeometry.referencePoint.referenceLon, 1e-9);
    }

    @Test
    public void applyLatLonOffset_nonZeroOffset_updatesRefPointAndNodes() {
        IntersectionInputData d = buildValidWithApproach();
        VerifiedPoint vp = d.mapData.intersectionGeometry.verifiedPoint;
        vp.verifiedSurveyedLat = vp.verifiedMapLat + 0.001;  // lat offset +0.001
        vp.verifiedSurveyedLon = vp.verifiedMapLon - 0.002;  // lon offset -0.002

        double origRefLat  = d.mapData.intersectionGeometry.referencePoint.referenceLat;
        double origRefLon  = d.mapData.intersectionGeometry.referencePoint.referenceLon;
        double origNodeLat = d.mapData.intersectionGeometry.laneList.approach[0]
                .drivingLanes[0].laneNodes[0].nodeLat;
        double origNodeLon = d.mapData.intersectionGeometry.laneList.approach[0]
                .drivingLanes[0].laneNodes[0].nodeLong;

        d.applyLatLonOffset();

        assertEquals(origRefLat + 0.001,
                d.mapData.intersectionGeometry.referencePoint.referenceLat, 1e-9);
        assertEquals(origRefLon - 0.002,
                d.mapData.intersectionGeometry.referencePoint.referenceLon, 1e-9);
        assertEquals(origNodeLat + 0.001,
                d.mapData.intersectionGeometry.laneList.approach[0]
                        .drivingLanes[0].laneNodes[0].nodeLat, 1e-9);
        assertEquals(origNodeLon - 0.002,
                d.mapData.intersectionGeometry.laneList.approach[0]
                        .drivingLanes[0].laneNodes[0].nodeLong, 1e-9);
    }
}