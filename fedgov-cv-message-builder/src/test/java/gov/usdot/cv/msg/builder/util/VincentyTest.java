package gov.usdot.cv.msg.builder.util;

import gov.usdot.cv.msg.builder.util.Vincenty.DistanceAndBearing;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class VincentyTest {

    // Allow a small tolerance for floating point comparisons in meters
    private static final double DISTANCE_TOLERANCE_METERS = 0.001;
    // Allow a small tolerance for bearing comparisons in degrees
    private static final double BEARING_TOLERANCE_DEGREES = 0.001;

    /**
     * Reference test using the documented control points (Flinders Peak to Buninyong, Australia).
     * Expected results come from the Geoscience Australia Vincenty calculator referenced in the source.
     */
    @Test
    public void knownDistance_flindersPeakToBuninyong_matchesReferenceValue() {
        double lat1 = -37.95103341666667;
        double lon1 = 144.42486788888888;
        double lat2 = -37.65282113888889;
        double lon2 = 143.92649552777777;

        DistanceAndBearing result = Vincenty.getDistanceAndBearing(lat1, lon1, lat2, lon2);

        assertNotNull(result);
        assertEquals(54972.271, result.getDistance(), DISTANCE_TOLERANCE_METERS);
        assertEquals(306.8681583333333, result.getInitialBearing(), BEARING_TOLERANCE_DEGREES);
        assertEquals(127.17363056, result.getFinalBearing(), 0.01);
    }

    /**
     * When both points are the same (co-incident), the formula short-circuits and returns NaN values.
     * This protects against future regressions in the sinSigma == 0 branch.
     */
    @Test
    public void coIncidentPoints_returnsNaN() {
        DistanceAndBearing result = Vincenty.getDistanceAndBearing(40.0, -75.0, 40.0, -75.0);

        assertTrue("Distance should be NaN for co-incident points", Double.isNaN(result.getDistance()));
        assertTrue("Initial bearing should be NaN for co-incident points", Double.isNaN(result.getInitialBearing()));
        assertTrue("Final bearing should be NaN for co-incident points", Double.isNaN(result.getFinalBearing()));
    }

    /**
     * Distance from A to B should equal distance from B to A.
     * Bearings will differ since direction of travel is reversed.
     */
    @Test
    public void distance_isCommutative() {
        double lat1 = 40.7128, lon1 = -74.0060;   // New York
        double lat2 = 34.0522, lon2 = -118.2437;  // Los Angeles

        DistanceAndBearing forward = Vincenty.getDistanceAndBearing(lat1, lon1, lat2, lon2);
        DistanceAndBearing reverse = Vincenty.getDistanceAndBearing(lat2, lon2, lat1, lon1);

        assertEquals(forward.getDistance(), reverse.getDistance(), DISTANCE_TOLERANCE_METERS);
    }

    /**
     * Bearings should be normalized to the 0-360 degree range, never negative.
     */
    @Test
    public void bearings_areNormalizedToPositiveRange() {
        // Use points where the raw atan2 result would be negative without normalization
        DistanceAndBearing result = Vincenty.getDistanceAndBearing(40.0, -75.0, 40.0, -76.0);

        assertTrue("Initial bearing should be >= 0", result.getInitialBearing() >= 0);
        assertTrue("Initial bearing should be < 360", result.getInitialBearing() < 360);
        assertTrue("Final bearing should be >= 0", result.getFinalBearing() >= 0);
        assertTrue("Final bearing should be < 360", result.getFinalBearing() < 360);
    }

    /**
     * Sanity check: a very short distance should produce a small meter value.
     * Two points roughly 1 meter apart should be within a few meters.
     */
    @Test
    public void shortDistance_returnsSmallMeterValue() {
        // ~1 meter apart in latitude (1 degree ~ 111 km, so 0.00001 degree ~ 1.11m)
        DistanceAndBearing result = Vincenty.getDistanceAndBearing(40.0, -75.0, 40.00001, -75.0);

        assertTrue("Distance for ~1m apart should be < 5m", result.getDistance() < 5.0);
        assertTrue("Distance should be positive", result.getDistance() > 0.0);
    }

    /**
     * DistanceAndBearing constructor and getters should return what was set.
     */
    @Test
    public void distanceAndBearing_gettersReturnConstructorValues() {
        DistanceAndBearing dab = new DistanceAndBearing(100.5, 45.0, 225.0);

        assertEquals(100.5, dab.getDistance(), 0.0);
        assertEquals(45.0, dab.getInitialBearing(), 0.0);
        assertEquals(225.0, dab.getFinalBearing(), 0.0);
    }

    /**
     * toString should include the key fields so debugging output is useful.
     */
    @Test
    public void distanceAndBearing_toStringContainsAllFields() {
        DistanceAndBearing dab = new DistanceAndBearing(100.5, 45.0, 225.0);
        String str = dab.toString();

        assertTrue("toString should mention distance", str.contains("100.5"));
        assertTrue("toString should mention initial bearing", str.contains("45.0"));
        assertTrue("toString should mention final bearing", str.contains("225.0"));
    }
}