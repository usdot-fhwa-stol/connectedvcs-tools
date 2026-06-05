/*
 * Copyright (C) 2024 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link BitStringHelper}.
 *
 * Covers all three public static methods and every branch:
 *   getBitString  - empty array (no-op), non-empty array with/without matches
 *   checkIfIndexPresent - value found (break), value not found, empty array
 *   setBit        - set=true branch, set=false branch, boundary positions
 */
public class BitStringHelperTest {

    // =========================================================================
    // checkIfIndexPresent
    // =========================================================================

    @Test
    public void checkIfIndexPresent_valueFound_returnsTrue() {
        assertTrue(BitStringHelper.checkIfIndexPresent(new int[]{3, 7, 11}, 7));
    }

    @Test
    public void checkIfIndexPresent_valueAtFirstIndex_returnsTrue() {
        assertTrue(BitStringHelper.checkIfIndexPresent(new int[]{5, 10, 15}, 5));
    }

    @Test
    public void checkIfIndexPresent_valueAtLastIndex_returnsTrue() {
        assertTrue(BitStringHelper.checkIfIndexPresent(new int[]{1, 2, 3}, 3));
    }

    @Test
    public void checkIfIndexPresent_valueNotInArray_returnsFalse() {
        assertFalse(BitStringHelper.checkIfIndexPresent(new int[]{1, 2, 4}, 3));
    }

    @Test
    public void checkIfIndexPresent_emptyArray_returnsFalse() {
        assertFalse(BitStringHelper.checkIfIndexPresent(new int[]{}, 5));
    }

    @Test
    public void checkIfIndexPresent_singleElementMatch_returnsTrue() {
        assertTrue(BitStringHelper.checkIfIndexPresent(new int[]{42}, 42));
    }

    @Test
    public void checkIfIndexPresent_singleElementNoMatch_returnsFalse() {
        assertFalse(BitStringHelper.checkIfIndexPresent(new int[]{42}, 1));
    }

    // =========================================================================
    // setBit
    // =========================================================================

    @Test
    public void setBit_setTrue_setsBit() {
        // Set bit at position 0 of 0 → 1
        assertEquals(1, BitStringHelper.setBit(0, 0, true));
    }

    @Test
    public void setBit_setTrue_highPosition() {
        // Set bit at position 3 of 0 → 0b1000 = 8
        assertEquals(8, BitStringHelper.setBit(0, 3, true));
    }

    @Test
    public void setBit_setFalse_clearsBit() {
        // Clear bit at position 0 of 1 → 0
        assertEquals(0, BitStringHelper.setBit(1, 0, false));
    }

    @Test
    public void setBit_setFalse_clearsBitHighPosition() {
        // Clear bit 3 of 0b1111 = 15 → 0b0111 = 7
        assertEquals(7, BitStringHelper.setBit(15, 3, false));
    }

    @Test
    public void setBit_setTrue_idempotent() {
        // Setting an already-set bit returns same value
        assertEquals(1, BitStringHelper.setBit(1, 0, true));
    }

    @Test
    public void setBit_setFalse_onAlreadyClearedBit_returnsUnchanged() {
        // Clearing an already-clear bit returns same value
        assertEquals(0, BitStringHelper.setBit(0, 2, false));
    }

    // =========================================================================
    // getBitString
    // =========================================================================

    @Test
    public void getBitString_emptyAttributesArray_returnsUnchangedBitString() {
        // Empty array → inner loop doesn't execute → original bitString returned
        int result = BitStringHelper.getBitString(0b1010, 4, new int[]{});
        assertEquals(0b1010, result);
    }

    @Test
    public void getBitString_setsMatchingBits() {
        // 16-bit string, indices 0 and 3 present
        // bit 0 → position 15 (16-0-1), bit 3 → position 12 (16-3-1)
        int result = BitStringHelper.getBitString(0, 16, new int[]{0, 3});
        // bit 15 = 0x8000, bit 12 = 0x1000
        int expected = (1 << 15) | (1 << 12);
        assertEquals(expected, result);
    }

    @Test
    public void getBitString_noMatchingIndices_returnsOriginal() {
        // attributesArray has values outside the bitStringLength range → no bits set
        int result = BitStringHelper.getBitString(0, 4, new int[]{10, 20, 30});
        assertEquals(0, result);
    }

    @Test
    public void getBitString_allIndicesPresent_allBitsSet() {
        // 4-bit string, all 4 indices present
        int result = BitStringHelper.getBitString(0, 4, new int[]{0, 1, 2, 3});
        // positions: 3,2,1,0 → all set = 0b1111 = 15
        assertEquals(0b1111, result);
    }

    @Test
    public void getBitString_headingSliceUsage_matchesExpected() {
        // Mirrors the usage in TravelerInformationBuilder.getHeadingSlice
        // heading = [11, 12, 13, 14, 15], bitStringLength = 16
        // each index i → set bit at (16 - i - 1)
        // index 11 → bit 4, 12→3, 13→2, 14→1, 15→0
        int result = BitStringHelper.getBitString(0, 16, new int[]{11, 12, 13, 14, 15});
        int expected = (1 << 4) | (1 << 3) | (1 << 2) | (1 << 1) | (1 << 0);
        assertEquals(expected, result);
    }

    @Test
    public void getBitString_startsWithNonZeroBitString_onlyAddsNewBits() {
        // Starting with bit 0 already set, add bit 1
        int start = BitStringHelper.setBit(0, 0, true); // = 1
        int result = BitStringHelper.getBitString(start, 2, new int[]{0}); // bit at position (2-0-1)=1
        assertTrue("Bit 1 should be set", (result & 2) != 0);
        assertTrue("Bit 0 from original should still be set", (result & 1) != 0);
    }
}