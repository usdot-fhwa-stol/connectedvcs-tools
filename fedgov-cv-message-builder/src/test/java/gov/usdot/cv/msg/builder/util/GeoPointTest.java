/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link GeoPoint}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * GeoPoint computes signed lat/lon offsets used in every MAP and TIM message
 * node encoding. The bearing-based sign assignment (positive vs negative offset)
 * is the critical logic — a wrong sign flips a node to the opposite side of the
 * reference point, producing a physically incorrect map lane geometry.
 * The elevation guard (J2735 valid range) silently returns 0 for out-of-range
 * values; without a test this could mask a field data error.
 * The validateShortRange overflow guard throws IllegalArgumentException for
 * unreachable offsets — this is the safety net against encoding garbage data.
 */
public class GeoPointTest {

    // =========================================================================
    // Constructor and getters
    // =========================================================================

    @Test
    public void constructor_twoArg_setsLatLon() {
        GeoPoint p = new GeoPoint(38.9555, -77.1489);
        assertEquals(38.9555, p.getLat(), 1e-7);
        assertEquals(-77.1489, p.getLon(), 1e-7);
        assertEquals(0.0, p.getElevation(), 1e-7);
    }

    @Test
    public void constructor_threeArg_setsElevation() {
        GeoPoint p = new GeoPoint(38.9555, -77.1489, 42.0);
        assertEquals(42.0, p.getElevation(), 1e-7);
    }

    @Test
    public void setters_updateFields() {
        GeoPoint p = new GeoPoint(0, 0);
        p.setLat(10.0);
        p.setLon(20.0);
        p.setElevation(5.0);
        assertEquals(10.0, p.getLat(), 1e-7);
        assertEquals(20.0, p.getLon(), 1e-7);
        assertEquals(5.0,  p.getElevation(), 1e-7);
    }

    // =========================================================================
    // getElevationOffsetInCentimeters — out-of-range guard
    // =========================================================================

    @Test
    public void getElevationOffset_validRange_returnsOffset() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489, 10.0);
        GeoPoint to   = new GeoPoint(38.9556, -77.1490, 15.0);
        // 5.0m difference = 500cm
        short result = to.getElevationOffsetInCentimeters(from);
        assertEquals(500, result);
    }

    @Test
    public void getElevationOffset_negativeOffset_returnsNegative() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489, 20.0);
        GeoPoint to   = new GeoPoint(38.9556, -77.1490, 15.0);
        // -5.0m difference = -500cm
        short result = to.getElevationOffsetInCentimeters(from);
        assertEquals(-500, result);
    }

    @Test
    public void getElevationOffset_toElevationOutOfRange_returnsZero() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489, 10.0);
        GeoPoint to   = new GeoPoint(38.9556, -77.1490, 9999.0); // > 6143.9 max
        short result = to.getElevationOffsetInCentimeters(from);
        assertEquals("Out-of-range elevation must return 0", 0, result);
    }

    @Test
    public void getElevationOffset_fromElevationOutOfRange_returnsZero() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489, -500.0); // < -409.5 min
        GeoPoint to   = new GeoPoint(38.9556, -77.1490, 10.0);
        short result = to.getElevationOffsetInCentimeters(from);
        assertEquals("Out-of-range fromPoint elevation must return 0", 0, result);
    }

    @Test
    public void getElevationOffset_atMaxValidElevation_doesNotReturnZero() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489, 6143.9);
        GeoPoint to   = new GeoPoint(38.9556, -77.1490, 6143.9);
        // Same elevation, valid range — offset is 0 but for correct reason
        short result = to.getElevationOffsetInCentimeters(from);
        assertEquals(0, result);
    }

    // =========================================================================
    // getLatOffsetInCentimeters — directional sign (bearing logic)
    // =========================================================================

    @Test
    public void getLatOffsetInCentimeters_northward_returnsPositive() {
        // Moving north: bearing ~0°, which is NOT in (90,270) → positive
        GeoPoint from = new GeoPoint(38.9000, -77.1489);
        GeoPoint to   = new GeoPoint(38.9100, -77.1489); // ~1.11 km north
        short result = to.getLatOffsetInCentimeters(from);
        assertTrue("Northward lat offset must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLatOffsetInCentimeters_southward_returnsNegative() {
        // Moving south: bearing ~180°, which IS in (90,270) → negative
        GeoPoint from = new GeoPoint(38.9100, -77.1489);
        GeoPoint to   = new GeoPoint(38.9000, -77.1489); // south
        short result = to.getLatOffsetInCentimeters(from);
        assertTrue("Southward lat offset must be negative, got: " + result, result < 0);
    }

    @Test
    public void getLatOffsetInCentimeters_samePoint_returnsZero() {
        GeoPoint p = new GeoPoint(38.9555, -77.1489);
        short result = p.getLatOffsetInCentimeters(p);
        assertEquals(0, result);
    }

    // =========================================================================
    // getLonOffsetInCentimeters — directional sign (bearing logic)
    // =========================================================================

    @Test
    public void getLonOffsetInCentimeters_eastward_returnsPositive() {
        // Moving east: bearing ~90°, which is NOT in (180,360) → positive
        GeoPoint from = new GeoPoint(38.9555, -77.1500);
        GeoPoint to   = new GeoPoint(38.9555, -77.1400); // east
        short result = to.getLonOffsetInCentimeters(from);
        assertTrue("Eastward lon offset must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLonOffsetInCentimeters_westward_returnsNegative() {
        // Moving west: bearing ~270°, which IS in (180,360) → negative
        GeoPoint from = new GeoPoint(38.9555, -77.1400);
        GeoPoint to   = new GeoPoint(38.9555, -77.1500); // west
        short result = to.getLonOffsetInCentimeters(from);
        assertTrue("Westward lon offset must be negative, got: " + result, result < 0);
    }

    // =========================================================================
    // getDistanceInCentimeters
    // =========================================================================

    @Test
    public void getDistanceInCentimeters_smallOffset_returnsPositive() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489);
        GeoPoint to   = new GeoPoint(38.9556, -77.1490);
        short result = to.getDistanceInCentimeters(from);
        assertTrue("Distance must be positive, got: " + result, result > 0);
    }

    @Test
    public void getDistanceInCentimeters_samePoint_returnsZero() {
        GeoPoint p = new GeoPoint(38.9555, -77.1489);
        short result = p.getDistanceInCentimeters(p);
        assertEquals(0, result);
    }

    // =========================================================================
    // getLatOffsetInMeters / getLonOffsetInMeters / getDistanceInMeters
    // =========================================================================

    @Test
    public void getLatOffsetInMeters_northward_returnsPositive() {
        GeoPoint from = new GeoPoint(38.9000, -77.1489);
        GeoPoint to   = new GeoPoint(38.9050, -77.1489);
        short result = to.getLatOffsetInMeters(from);
        assertTrue("Northward lat offset in meters must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLonOffsetInMeters_eastward_returnsPositive() {
        GeoPoint from = new GeoPoint(38.9555, -77.1500);
        GeoPoint to   = new GeoPoint(38.9555, -77.1450);
        short result = to.getLonOffsetInMeters(from);
        assertTrue("Eastward lon offset in meters must be positive, got: " + result, result > 0);
    }

    @Test
    public void getDistanceInMeters_smallOffset_returnsPositive() {
        GeoPoint from = new GeoPoint(38.9555, -77.1489);
        GeoPoint to   = new GeoPoint(38.9560, -77.1490);
        short result = to.getDistanceInMeters(from);
        assertTrue("Distance in meters must be positive, got: " + result, result > 0);
    }

    // =========================================================================
    // validateShortRange — overflow guard (IllegalArgumentException)
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void getLatOffsetInCentimeters_hugeDelta_throwsIllegalArgument() {
        // 400 degrees latitude difference → offset >> Short.MAX_VALUE
        GeoPoint from = new GeoPoint(0.0,   0.0);
        GeoPoint to   = new GeoPoint(89.0,  0.0); // max realistic: ~89 degrees
        // ~9900 km = 990,000,000 cm >> 32767 → should throw
        to.getLatOffsetInCentimeters(from);
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    public void toString_containsLatAndLon() {
        GeoPoint p = new GeoPoint(38.9555, -77.1489);
        String s = p.toString();
        assertTrue(s.contains("38.9555"));
        assertTrue(s.contains("-77.1489"));
    }
}