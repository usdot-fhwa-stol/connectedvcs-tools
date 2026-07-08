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
 * reference point, producing physically incorrect lane geometry sent to vehicles.
 *
 * The elevation guard (J2735 valid range -409.5m to 6143.9m) silently returns 0
 * for out-of-range values. The validateShortRange overflow guard throws
 * IllegalArgumentException for offsets exceeding Short.MAX_VALUE (32,767 cm).
 *
 * COORDINATE DELTA NOTE:
 * All test deltas use 0.00005 degrees (~5 meters), keeping computed offsets
 * well within Short range (32,767 cm). The overflow test uses 0.003 degrees
 * (~333 meters = ~33,300 cm) which reliably exceeds Short.MAX_VALUE.
 */
public class GeoPointTest {

    // Reference point near Washington DC, used across all tests
    private static final double BASE_LAT = 38.9555;
    private static final double BASE_LON = -77.1489;

    // Small safe delta: ~5.5 meters = ~555 cm — well within Short.MAX_VALUE
    private static final double SMALL_DELTA = 0.00005;

    // Latitude overflow delta: 0.003 deg = ~33,300 cm > Short.MAX_VALUE (32,767)
    private static final double OVERFLOW_DELTA_LAT = 0.003;

    // Longitude overflow delta: longitude compresses by cos(lat)=0.778 at this latitude,
    // so 0.003 deg lon = only ~25,900 cm which does NOT overflow. Use 0.004 deg = ~34,527 cm.
    private static final double OVERFLOW_DELTA_LON = 0.004;

    // =========================================================================
    // Constructor and getters
    // =========================================================================

    @Test
    public void constructor_twoArg_setsLatLon() {
        GeoPoint p = new GeoPoint(BASE_LAT, BASE_LON);
        assertEquals(BASE_LAT, p.getLat(), 1e-7);
        assertEquals(BASE_LON, p.getLon(), 1e-7);
        assertEquals(0.0, p.getElevation(), 1e-7);
    }

    @Test
    public void constructor_threeArg_setsElevation() {
        GeoPoint p = new GeoPoint(BASE_LAT, BASE_LON, 42.0);
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
    // getElevationOffsetInCentimeters — J2735 range guard
    // =========================================================================

    @Test
    public void getElevationOffset_validRange_returnsOffset() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON, 10.0);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON, 15.0);
        // 5.0m difference = 500cm
        assertEquals(500, to.getElevationOffsetInCentimeters(from));
    }

    @Test
    public void getElevationOffset_negativeOffset_returnsNegative() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON, 20.0);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON, 15.0);
        assertEquals(-500, to.getElevationOffsetInCentimeters(from));
    }

    @Test
    public void getElevationOffset_toElevationAboveMax_returnsZero() {
        // J2735 max elevation is 6143.9m
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON, 10.0);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON, 6200.0); // > 6143.9
        assertEquals("Elevation above J2735 max must return 0", 0,
                to.getElevationOffsetInCentimeters(from));
    }

    @Test
    public void getElevationOffset_fromElevationBelowMin_returnsZero() {
        // J2735 min elevation is -409.5m
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON, -500.0); // < -409.5
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON, 10.0);
        assertEquals("Elevation below J2735 min must return 0", 0,
                to.getElevationOffsetInCentimeters(from));
    }

    @Test
    public void getElevationOffset_sameElevation_returnsZero() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON, 100.0);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON, 100.0);
        assertEquals(0, to.getElevationOffsetInCentimeters(from));
    }

    // =========================================================================
    // getLatOffsetInCentimeters — bearing-based sign logic
    // SMALL_DELTA keeps offsets ~555 cm, safely within Short.MAX_VALUE
    // =========================================================================

    @Test
    public void getLatOffsetInCentimeters_northward_returnsPositive() {
        // Moving north: bearing ~0°, NOT in range (90°, 270°) → positive offset
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT + SMALL_DELTA, BASE_LON);
        short result = to.getLatOffsetInCentimeters(from);
        assertTrue("Northward lat offset must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLatOffsetInCentimeters_southward_returnsNegative() {
        // Moving south: bearing ~180°, IS in range (90°, 270°) → negative offset
        GeoPoint from = new GeoPoint(BASE_LAT + SMALL_DELTA, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON);
        short result = to.getLatOffsetInCentimeters(from);
        assertTrue("Southward lat offset must be negative, got: " + result, result < 0);
    }

    @Test
    public void getLatOffsetInCentimeters_samePoint_returnsZero() {
        GeoPoint p = new GeoPoint(BASE_LAT, BASE_LON);
        assertEquals(0, p.getLatOffsetInCentimeters(p));
    }

    // =========================================================================
    // getLonOffsetInCentimeters — bearing-based sign logic
    // =========================================================================

    @Test
    public void getLonOffsetInCentimeters_eastward_returnsPositive() {
        // Moving east: bearing ~90°, NOT in range (180°, 360°) → positive offset
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON + SMALL_DELTA);
        short result = to.getLonOffsetInCentimeters(from);
        assertTrue("Eastward lon offset must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLonOffsetInCentimeters_westward_returnsNegative() {
        // Moving west: bearing ~270°, IS in range (180°, 360°) → negative offset
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON + SMALL_DELTA);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON);
        short result = to.getLonOffsetInCentimeters(from);
        assertTrue("Westward lon offset must be negative, got: " + result, result < 0);
    }

    @Test
    public void getLonOffsetInCentimeters_samePoint_returnsZero() {
        GeoPoint p = new GeoPoint(BASE_LAT, BASE_LON);
        assertEquals(0, p.getLonOffsetInCentimeters(p));
    }

    // =========================================================================
    // getDistanceInCentimeters
    // =========================================================================

    @Test
    public void getDistanceInCentimeters_smallOffset_returnsPositive() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT + SMALL_DELTA, BASE_LON + SMALL_DELTA);
        short result = to.getDistanceInCentimeters(from);
        assertTrue("Distance must be positive, got: " + result, result > 0);
    }

    @Test
    public void getDistanceInCentimeters_samePoint_returnsZero() {
        GeoPoint p = new GeoPoint(BASE_LAT, BASE_LON);
        assertEquals(0, p.getDistanceInCentimeters(p));
    }

    // =========================================================================
    // getLatOffsetInMeters / getLonOffsetInMeters / getDistanceInMeters
    // =========================================================================

    @Test
    public void getLatOffsetInMeters_northward_returnsPositive() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT + SMALL_DELTA, BASE_LON);
        short result = to.getLatOffsetInMeters(from);
        assertTrue("Northward lat offset in meters must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLatOffsetInMeters_southward_returnsNegative() {
        GeoPoint from = new GeoPoint(BASE_LAT + SMALL_DELTA, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON);
        short result = to.getLatOffsetInMeters(from);
        assertTrue("Southward lat offset in meters must be negative, got: " + result, result < 0);
    }

    @Test
    public void getLonOffsetInMeters_eastward_returnsPositive() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON + SMALL_DELTA);
        short result = to.getLonOffsetInMeters(from);
        assertTrue("Eastward lon offset in meters must be positive, got: " + result, result > 0);
    }

    @Test
    public void getLonOffsetInMeters_westward_returnsNegative() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON + SMALL_DELTA);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON);
        short result = to.getLonOffsetInMeters(from);
        assertTrue("Westward lon offset in meters must be negative, got: " + result, result < 0);
    }

    @Test
    public void getDistanceInMeters_smallOffset_returnsPositive() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT + SMALL_DELTA, BASE_LON + SMALL_DELTA);
        short result = to.getDistanceInMeters(from);
        assertTrue("Distance in meters must be positive, got: " + result, result > 0);
    }

    // =========================================================================
    // validateShortRange — overflow guard throws IllegalArgumentException
    // OVERFLOW_DELTA_LAT (~33,300 cm) and OVERFLOW_DELTA_LON (~34,527 cm) both exceed Short.MAX_VALUE (32,767)
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void getLatOffsetInCentimeters_overflowDelta_throwsIllegalArgument() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT + OVERFLOW_DELTA_LAT, BASE_LON);
        to.getLatOffsetInCentimeters(from);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLonOffsetInCentimeters_overflowDelta_throwsIllegalArgument() {
        GeoPoint from = new GeoPoint(BASE_LAT, BASE_LON);
        GeoPoint to   = new GeoPoint(BASE_LAT, BASE_LON + OVERFLOW_DELTA_LON);
        to.getLonOffsetInCentimeters(from);
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    public void toString_containsLatAndLon() {
        GeoPoint p = new GeoPoint(BASE_LAT, BASE_LON);
        String s = p.toString();
        assertTrue(s.contains(String.valueOf(BASE_LAT)));
        assertTrue(s.contains(String.valueOf(BASE_LON)));
    }
}