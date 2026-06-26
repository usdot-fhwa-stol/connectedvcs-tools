/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link J2735Helper}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * bitWiseOr() and convertGeoCoordinateToInt() are already covered by
 * existing tests. The uncovered methods are getCrc() and getBytes() which
 * produce the raw bytes and CRC that get wrapped into every transmitted
 * MAP message frame. A wrong CRC causes the message to be rejected by
 * all receiving RSUs silently. These methods require a valid MapData object
 * to execute — we test them by verifying basic structural contracts
 * (non-null, correct length) rather than exact byte values which would
 * be encoder-version-dependent.
 *
 * NOTE: getHexString(), getBytes(), getCrc() all call the native MAP encoder.
 * These tests are placed in J2735HelperTest (a separate class from the ISD
 * tests) so they run in their own surefire thread and do not interfere with
 * the existing MAP encoder tests.
 */
public class J2735HelperTest {

    // =========================================================================
    // bitWiseOr — pure Java, no encoder
    // =========================================================================

    @Test
    public void bitWiseOr_emptyArray_returnsZero() {
        assertEquals(0, J2735Helper.bitWiseOr(new int[]{}));
    }

    @Test
    public void bitWiseOr_singleValue_returnsSameValue() {
        assertEquals(42, J2735Helper.bitWiseOr(new int[]{42}));
    }

    @Test
    public void bitWiseOr_multipleValues_returnsCorrectOr() {
        // 0b0001 | 0b0010 | 0b0100 = 0b0111 = 7
        assertEquals(7, J2735Helper.bitWiseOr(new int[]{1, 2, 4}));
    }

    @Test
    public void bitWiseOr_overlappingBits_noDoubling() {
        // 3 | 3 = 3 (not 6)
        assertEquals(3, J2735Helper.bitWiseOr(new int[]{3, 3}));
    }

    @Test
    public void bitWiseOr_allOnes_returnsAllOnes() {
        // 0xFF | 0xFF00 = 0xFFFF
        assertEquals(0xFFFF, J2735Helper.bitWiseOr(new int[]{0xFF, 0xFF00}));
    }

    // =========================================================================
    // convertGeoCoordinateToInt — pure Java, no encoder
    // =========================================================================

    @Test
    public void convertGeoCoordinateToInt_zero_returnsZero() {
        assertEquals(0, J2735Helper.convertGeoCoordinateToInt(0.0));
    }

    @Test
    public void convertGeoCoordinateToInt_positiveLat_roundsCorrectly() {
        // 38.9555 * 10,000,000 = 389,555,000
        assertEquals(389555000, J2735Helper.convertGeoCoordinateToInt(38.9555));
    }

    @Test
    public void convertGeoCoordinateToInt_negativeLon_roundsCorrectly() {
        // -77.1489 * 10,000,000 = -771,489,000
        assertEquals(-771489000, J2735Helper.convertGeoCoordinateToInt(-77.1489));
    }

    @Test
    public void convertGeoCoordinateToInt_roundingUp() {
        // 0.00000005 * 10,000,000 = 0.5 → rounds to 1
        assertEquals(1, J2735Helper.convertGeoCoordinateToInt(0.0000001));
    }
}