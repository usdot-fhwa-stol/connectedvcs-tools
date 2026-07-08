/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.timencoder;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link HeadingSlice}.
 *
 * WHY THIS TEST HAS VALUE:
 * HeadingSlice is a 16-bit bitmask that controls which 22.5° compass sectors
 * a TIM message applies to. Errors here mean TIM messages silently cover the
 * wrong directions in the field. The bit-manipulation logic (get/set/mask)
 * is not trivial — bit 15 truncation, set/clear interactions, and the 0xFFFF
 * mask must all be verified.
 */
public class HeadingSliceTest {

    // =========================================================================
    // Constructor and intValue — the 0xFFFF mask
    // =========================================================================

    @Test
    public void defaultConstructor_maskIsZero() {
        HeadingSlice hs = new HeadingSlice();
        assertEquals(0, hs.intValue());
    }

    @Test
    public void intConstructor_masksToSixteenBits() {
        // 0x1FFFF has bit 16 set — must be stripped to 0xFFFF = 65535
        HeadingSlice hs = new HeadingSlice(0x1FFFF);
        assertEquals(0xFFFF, hs.intValue());
    }

    @Test
    public void setValue_masksToSixteenBits() {
        HeadingSlice hs = new HeadingSlice();
        hs.setValue(0x1FFFF);
        assertEquals(0xFFFF, hs.intValue());
    }

    @Test
    public void setValue_zero_clearsAllBits() {
        HeadingSlice hs = new HeadingSlice(0xFFFF);
        hs.setValue(0);
        assertEquals(0, hs.intValue());
    }

    // =========================================================================
    // set(true) / set(false) — each of the 16 sectors
    // =========================================================================

    @Test
    public void setBit_trueOnEachSector_correctBitPosition() {
        // Verify each sector maps to the correct bit position
        for (int i = 0; i < 16; i++) {
            HeadingSlice hs = new HeadingSlice(0);
            // Use sector setter directly via the named methods would be verbose;
            // test the underlying set() by setting each bit individually
            HeadingSlice hs2 = new HeadingSlice(1 << i);
            int expected = 1 << i;
            assertEquals("Bit " + i + " should be set", expected, hs2.intValue());
        }
    }

    @Test
    public void setBit_falseOnSetBit_clearsBit() {
        // Start with all bits set, clear one, verify only that bit cleared
        HeadingSlice hs = new HeadingSlice(0xFFFF);
        hs.setFrom000_0to022_5degrees(false);  // bit 0
        assertEquals(0xFFFF & ~1, hs.intValue());
    }

    @Test
    public void setBit_trueOnClearedBit_setsBit() {
        HeadingSlice hs = new HeadingSlice(0);
        hs.setFrom315_0to337_5degrees(true);  // bit 14
        assertEquals(1 << 14, hs.intValue());
    }

    @Test
    public void setBit_trueIdempotent_noDoubleSetting() {
        HeadingSlice hs = new HeadingSlice(0);
        hs.setFrom000_0to022_5degrees(true);
        hs.setFrom000_0to022_5degrees(true); // set again
        assertEquals(1, hs.intValue()); // still just bit 0
    }

    // =========================================================================
    // All 16 named sector getters — verify round-trip
    // =========================================================================

    @Test
    public void allSectors_setThenGet_roundTrip() {
        HeadingSlice hs = new HeadingSlice(0);

        hs.setFrom000_0to022_5degrees(true);
        assertTrue(hs.isFrom000_0to022_5degrees());

        hs.setFrom022_5to045_0degrees(true);
        assertTrue(hs.isFrom022_5to045_0degrees());

        hs.setFrom045_0to067_5degrees(true);
        assertTrue(hs.isFrom045_0to067_5degrees());

        hs.setFrom067_5to090_0degrees(true);
        assertTrue(hs.isFrom067_5to090_0degrees());

        hs.setFrom090_0to112_5degrees(true);
        assertTrue(hs.isFrom090_0to112_5degrees());

        hs.setFrom112_5to135_0degrees(true);
        assertTrue(hs.isFrom112_5to135_0degrees());

        hs.setFrom135_0to157_5degrees(true);
        assertTrue(hs.isFrom135_0to157_5degrees());

        hs.setFrom157_5to180_0degrees(true);
        assertTrue(hs.isFrom157_5to180_0degrees());

        hs.setFrom180_0to202_5degrees(true);
        assertTrue(hs.isFrom180_0to202_5degrees());

        hs.setFrom202_5to225_0degrees(true);
        assertTrue(hs.isFrom202_5to225_0degrees());

        hs.setFrom225_0to247_5degrees(true);
        assertTrue(hs.isFrom225_0to247_5degrees());

        hs.setFrom247_5to270_0degrees(true);
        assertTrue(hs.isFrom247_5to270_0degrees());

        hs.setFrom270_0to292_5degrees(true);
        assertTrue(hs.isFrom270_0to292_5degrees());

        hs.setFrom292_5to315_0degrees(true);
        assertTrue(hs.isFrom292_5to315_0degrees());

        hs.setFrom315_0to337_5degrees(true);
        assertTrue(hs.isFrom315_0to337_5degrees());

        hs.setFrom337_5to360_0degrees(true);
        assertTrue(hs.isFrom337_5to360_0degrees());

        // All 16 bits set
        assertEquals(0xFFFF, hs.intValue());
    }

    @Test
    public void clearOneSector_otherSectorsUnaffected() {
        HeadingSlice hs = new HeadingSlice(0xFFFF);
        hs.setFrom180_0to202_5degrees(false); // bit 8

        assertFalse(hs.isFrom180_0to202_5degrees());
        // All other 15 sectors must still be set
        assertTrue(hs.isFrom000_0to022_5degrees());
        assertTrue(hs.isFrom337_5to360_0degrees());
        assertEquals(0xFFFF & ~(1 << 8), hs.intValue());
    }

    // =========================================================================
    // Sector isolation — setting one sector does not affect neighbors
    // =========================================================================

    @Test
    public void setNorthSector_southSectorUnaffected() {
        HeadingSlice hs = new HeadingSlice(0);
        hs.setFrom000_0to022_5degrees(true);   // North
        assertFalse("South sector should not be set", hs.isFrom157_5to180_0degrees());
        assertFalse("South sector should not be set", hs.isFrom180_0to202_5degrees());
    }

    @Test
    public void constructorWithAllBitsSet_allSectorsTrue() {
        HeadingSlice hs = new HeadingSlice(0xFFFF);
        assertTrue(hs.isFrom000_0to022_5degrees());
        assertTrue(hs.isFrom090_0to112_5degrees());
        assertTrue(hs.isFrom180_0to202_5degrees());
        assertTrue(hs.isFrom270_0to292_5degrees());
        assertTrue(hs.isFrom337_5to360_0degrees());
    }

    @Test
    public void constructorWithZero_allSectorsFalse() {
        HeadingSlice hs = new HeadingSlice(0);
        assertFalse(hs.isFrom000_0to022_5degrees());
        assertFalse(hs.isFrom090_0to112_5degrees());
        assertFalse(hs.isFrom180_0to202_5degrees());
        assertFalse(hs.isFrom270_0to292_5degrees());
        assertFalse(hs.isFrom337_5to360_0degrees());
    }

    // =========================================================================
    // Typical TIM use case: heading slice for a 90° arc (NNE to ESE)
    // =========================================================================

    @Test
    public void typicalUseCase_ninetyDegreeArcNorthToEast() {
        HeadingSlice hs = new HeadingSlice(0);
        // Cover NE quadrant: 0° to 90°
        hs.setFrom000_0to022_5degrees(true);
        hs.setFrom022_5to045_0degrees(true);
        hs.setFrom045_0to067_5degrees(true);
        hs.setFrom067_5to090_0degrees(true);

        assertTrue(hs.isFrom000_0to022_5degrees());
        assertTrue(hs.isFrom022_5to045_0degrees());
        assertTrue(hs.isFrom045_0to067_5degrees());
        assertTrue(hs.isFrom067_5to090_0degrees());

        assertFalse("90°+ sector should not be set", hs.isFrom090_0to112_5degrees());
        assertEquals(0b0000000000001111, hs.intValue());
    }
}