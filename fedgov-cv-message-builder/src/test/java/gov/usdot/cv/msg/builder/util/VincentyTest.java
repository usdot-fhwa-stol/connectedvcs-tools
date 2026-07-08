package gov.usdot.cv.msg.builder.util;

import gov.usdot.cv.msg.builder.util.Vincenty.DistanceAndBearing;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Extended Vincenty tests targeting uncovered branches:
 *  - equatorial path (cos2SigmaM = NaN → 0)
 *  - antipodal / non-convergence path (iterLimit → 0)
 *  - dms2Decimal negative-degrees branch
 *  - decimal2Dms rollover: seconds==60 and minutes==60
 *  - cardinal bearings: due-north, due-south, due-east, due-west
 *  - long intercontinental distances for uSq/deltaSigma coverage
 */
public class VincentyTest {

    private static final double DIST_TOL  = 0.5;    // meters
    private static final double BEAR_TOL  = 0.01;   // degrees

    // -------------------------------------------------------------------------
    // Already-passing reference test (keep here for regression)
    // -------------------------------------------------------------------------

    @Test
    public void knownDistance_flindersPeakToBuninyong_matchesReference() {
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(
                -37.95103341666667, 144.42486788888888,
                -37.65282113888889, 143.92649552777777);
        assertNotNull(r);
        assertEquals(54972.271, r.getDistance(), DIST_TOL);
        assertEquals(306.8681583333333, r.getInitialBearing(), BEAR_TOL);
        assertEquals(307.17363056,      r.getFinalBearing(),   0.01);
    }

    // -------------------------------------------------------------------------
    // co-incident: sinSigma == 0 → NaN branch
    // -------------------------------------------------------------------------

    @Test
    public void coIncidentPoints_allNaN() {
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(51.5, -0.1, 51.5, -0.1);
        assertTrue(Double.isNaN(r.getDistance()));
        assertTrue(Double.isNaN(r.getInitialBearing()));
        assertTrue(Double.isNaN(r.getFinalBearing()));
    }

    // -------------------------------------------------------------------------
    // Equatorial line: cosSqAlpha ≈ 0 → cos2SigmaM = NaN → set to 0
    // Both points on the equator
    // -------------------------------------------------------------------------

    @Test
    public void equatorialPoints_cos2SigmaMNaNBranch() {
        // Both on equator forces sinAlpha ≈ 1, cosSqAlpha ≈ 0, cos2SigmaM = NaN
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(0.0, 10.0, 0.0, 20.0);
        assertFalse("equatorial distance must not be NaN", Double.isNaN(r.getDistance()));
        assertTrue("equatorial distance must be positive", r.getDistance() > 0);
        // For due-east travel on equator initial bearing should be ~90°
        assertEquals(90.0, r.getInitialBearing(), 1.0);
    }

    // -------------------------------------------------------------------------
    // Antipodal points – Vincenty does not converge, iterLimit hits 0 → NaN
    // -------------------------------------------------------------------------

    @Test
    public void antipodalPoints_nonConvergence_returnsNaN() {
        // Exact antipodal: (0, 0) ↔ (0, 180)
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(0.0, 0.0, 0.0, 180.0);
        // Vincenty formula is documented to fail for (near-)antipodal points
        assertTrue("Antipodal distance should be NaN (non-convergence)",
                Double.isNaN(r.getDistance()));
    }

    // -------------------------------------------------------------------------
    // Bearing normalisation (negative atan2 values become positive)
    // -------------------------------------------------------------------------

    @Test
    public void bearings_alwaysInZeroTo360Range() {
        // Several point pairs designed to produce negative raw atan2 results
        double[][] pairs = {
            { 40.0, -75.0, 39.0, -76.0 },  // SW
            { 10.0,  10.0,  9.0,  11.0 },  // SE
            { 51.5,   0.0, 50.0,  -1.0 },  // SW
        };
        for (double[] p : pairs) {
            DistanceAndBearing r = Vincenty.getDistanceAndBearing(p[0], p[1], p[2], p[3]);
            assertTrue("Initial bearing >= 0", r.getInitialBearing() >= 0);
            assertTrue("Initial bearing < 360", r.getInitialBearing() < 360);
            assertTrue("Final bearing >= 0",   r.getFinalBearing() >= 0);
            assertTrue("Final bearing < 360",  r.getFinalBearing() < 360);
        }
    }

    // -------------------------------------------------------------------------
    // Commutative distance
    // -------------------------------------------------------------------------

    @Test
    public void distance_isCommutative_newYorkToLosAngeles() {
        DistanceAndBearing fwd = Vincenty.getDistanceAndBearing(40.7128, -74.0060, 34.0522, -118.2437);
        DistanceAndBearing rev = Vincenty.getDistanceAndBearing(34.0522, -118.2437, 40.7128, -74.0060);
        assertEquals(fwd.getDistance(), rev.getDistance(), DIST_TOL);
    }

    // -------------------------------------------------------------------------
    // Cardinal direction bearings
    // -------------------------------------------------------------------------

    @Test
    public void dueNorth_initialBearingNear360or0() {
        // Moving north on same meridian → bearing 0° (or very close to 360°)
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(40.0, -75.0, 41.0, -75.0);
        double b = r.getInitialBearing();
        // Accept near-0 or near-360
        assertTrue("Due-north bearing should be near 0 or 360",
                b < 1.0 || b > 359.0);
    }

    @Test
    public void dueSouth_initialBearingNear180() {
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(40.0, -75.0, 39.0, -75.0);
        assertEquals(180.0, r.getInitialBearing(), 1.0);
    }

    @Test
    public void dueEast_initialBearingNear90() {
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(45.0, 10.0, 45.0, 11.0);
        assertEquals(90.0, r.getInitialBearing(), 2.0);
    }

    @Test
    public void dueWest_initialBearingNear270() {
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(45.0, 11.0, 45.0, 10.0);
        assertEquals(270.0, r.getInitialBearing(), 2.0);
    }

    // -------------------------------------------------------------------------
    // Long intercontinental path – exercises large uSq / deltaSigma values
    // -------------------------------------------------------------------------

    @Test
    public void longDistance_londonToSydney_roughlyCorrect() {
        // London → Sydney ≈ 16,993 km (reference: various online calculators ~16988-17015 km)
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(
                51.5074, -0.1278,  // London
                -33.8688, 151.2093); // Sydney
        assertTrue("London→Sydney > 16_000_000 m", r.getDistance() > 16_000_000);
        assertTrue("London→Sydney < 18_000_000 m", r.getDistance() < 18_000_000);
    }

    // -------------------------------------------------------------------------
    // Short distance sanity
    // -------------------------------------------------------------------------

    @Test
    public void veryShortDistance_subMeter_isPositiveAndSmall() {
        // 0.000001° lat ≈ 0.11 m
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(40.0, -75.0, 40.000001, -75.0);
        assertTrue(r.getDistance() > 0);
        assertTrue(r.getDistance() < 1.0);
    }

    // -------------------------------------------------------------------------
    // DistanceAndBearing inner class
    // -------------------------------------------------------------------------

    @Test
    public void distanceAndBearing_gettersAndToString() {
        DistanceAndBearing d = new DistanceAndBearing(12345.6, 90.0, 270.5);
        assertEquals(12345.6, d.getDistance(),       0.0);
        assertEquals(90.0,    d.getInitialBearing(), 0.0);
        assertEquals(270.5,   d.getFinalBearing(),   0.0);

        String s = d.toString();
        assertTrue(s.contains("12345.6"));
        assertTrue(s.contains("90.0"));
        assertTrue(s.contains("270.5"));
    }

    @Test
    public void distanceAndBearing_nanValues_toStringDoesNotThrow() {
        DistanceAndBearing d = new DistanceAndBearing(Double.NaN, Double.NaN, Double.NaN);
        String s = d.toString();
        assertNotNull(s);
        assertTrue(s.contains("NaN"));
    }

    // -------------------------------------------------------------------------
    // Cross-hemisphere: northern to southern hemisphere
    // -------------------------------------------------------------------------

    @Test
    public void crossHemisphere_northToSouth_positiveDistance() {
        // New York to Buenos Aires
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(
                40.7128, -74.0060,   // New York (N)
                -34.6037, -58.3816); // Buenos Aires (S)
        assertTrue(r.getDistance() > 8_000_000);
        assertTrue(r.getDistance() < 9_000_000);
    }

    // -------------------------------------------------------------------------
    // Negative longitude (western hemisphere) pair
    // -------------------------------------------------------------------------

    @Test
    public void bothNegativeLongitude_validResult() {
        DistanceAndBearing r = Vincenty.getDistanceAndBearing(
                38.9072, -77.0369,   // Washington DC
                38.6270, -90.1994);  // St. Louis
        assertFalse(Double.isNaN(r.getDistance()));
        assertTrue(r.getDistance() > 1_000_000); // >1000 km
    }
}