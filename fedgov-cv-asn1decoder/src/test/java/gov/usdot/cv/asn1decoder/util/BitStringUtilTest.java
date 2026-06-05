/*
 * Copyright (C) 2026 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.asn1decoder.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the package-private {@link BitStringUtil} class.
 * Must be in the same package (gov.usdot.cv.asn1decoder.util) to access
 * the package-private class and static method.
 *
 * Covers all branches of decodeBitString:
 *   - Single byte, some bits set
 *   - Single byte, no bits set → empty result
 *   - Single byte, all bits set
 *   - Multi-byte input
 *   - unusedBits > 0 trims the last N bits
 *   - names array shorter than totalBits → loop terminates at names.length
 *   - names array longer than totalBits → loop terminates at totalBits
 */
public class BitStringUtilTest {

    private static final String[] FOUR_NAMES = {"a", "b", "c", "d"};
    private static final String[] EIGHT_NAMES = {"b0","b1","b2","b3","b4","b5","b6","b7"};

    // =========================================================================
    // Basic decoding
    // =========================================================================

    @Test
    public void decodeBitString_allBitsSet_oneByteNoUnused() {
        // 0xFF = 11111111 → all 8 bits set, 0 unused
        String result = BitStringUtil.decodeBitString("FF", 0, EIGHT_NAMES);
        for (String name : EIGHT_NAMES) {
            assertTrue("Expected " + name + " in result: " + result, result.contains(name));
        }
    }

    @Test
    public void decodeBitString_noBitsSet_returnsEmptyBraces() {
        // 0x00 = 00000000 → no bits set
        String result = BitStringUtil.decodeBitString("00", 0, EIGHT_NAMES);
        assertEquals("{ }", result);
    }

    @Test
    public void decodeBitString_highestBitSet_firstNamePresent() {
        // 0x80 = 10000000 → MSB (bit index 0) set → names[0]
        String result = BitStringUtil.decodeBitString("80", 0, EIGHT_NAMES);
        assertTrue(result.contains("b0"));
        assertFalse(result.contains("b1"));
    }

    @Test
    public void decodeBitString_lowestBitSet_lastNamePresent() {
        // 0x01 = 00000001 → LSB (bit index 7) set → names[7]
        String result = BitStringUtil.decodeBitString("01", 0, EIGHT_NAMES);
        assertTrue(result.contains("b7"));
        assertFalse(result.contains("b0"));
    }

    @Test
    public void decodeBitString_fourBitsSet_correctNames() {
        // 0xAA = 10101010 → bits 0,2,4,6 set → names[0],[2],[4],[6]
        String result = BitStringUtil.decodeBitString("AA", 0, EIGHT_NAMES);
        assertTrue(result.contains("b0"));
        assertFalse(result.contains("b1"));
        assertTrue(result.contains("b2"));
        assertFalse(result.contains("b3"));
        assertTrue(result.contains("b4"));
        assertFalse(result.contains("b5"));
        assertTrue(result.contains("b6"));
        assertFalse(result.contains("b7"));
    }

    // =========================================================================
    // unusedBits trims trailing bits
    // =========================================================================

    @Test
    public void decodeBitString_unusedBits4_trimsFourBits() {
        // 0xFF with 4 unused bits → only first 4 bits are valid
        String result = BitStringUtil.decodeBitString("FF", 4, EIGHT_NAMES);
        // Only names[0..3] can appear (bits 0-3 of the byte are MSB-first)
        assertTrue(result.contains("b0"));
        assertTrue(result.contains("b1"));
        assertTrue(result.contains("b2"));
        assertTrue(result.contains("b3"));
        // bits 4-7 trimmed → names[4..7] must not appear
        assertFalse(result.contains("b4"));
        assertFalse(result.contains("b5"));
        assertFalse(result.contains("b6"));
        assertFalse(result.contains("b7"));
    }

    @Test
    public void decodeBitString_unusedBits7_onlyFirstBitValid() {
        // 0x80 = 10000000 with 7 unused → totalBits = 1 → only bit 0 valid
        String result = BitStringUtil.decodeBitString("80", 7, EIGHT_NAMES);
        assertTrue(result.contains("b0"));
    }

    @Test
    public void decodeBitString_unusedBits7_clearByte_noNames() {
        // 0x00 with 7 unused → totalBits = 1 → bit 0 not set → empty
        String result = BitStringUtil.decodeBitString("00", 7, EIGHT_NAMES);
        assertEquals("{ }", result);
    }

    // =========================================================================
    // names array shorter than totalBits → loop stops at names.length
    // =========================================================================

    @Test
    public void decodeBitString_namesArrayShorterThanBits_usesAvailableNames() {
        // 0xFF, 0 unused → 8 bits, but only 4 names → loop runs 4 times
        String result = BitStringUtil.decodeBitString("FF", 0, FOUR_NAMES);
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
        assertTrue(result.contains("d"));
        // No 5th name to appear → no ArrayIndexOutOfBoundsException
    }

    // =========================================================================
    // Multi-byte input
    // =========================================================================

    @Test
    public void decodeBitString_twoBytes_secondByteContributes() {
        // "80 80" = 10000000 10000000 → bits 0 and 8 set
        String[] sixteenNames = new String[16];
        for (int i = 0; i < 16; i++) sixteenNames[i] = "n" + i;

        String result = BitStringUtil.decodeBitString("80 80", 0, sixteenNames);
        assertTrue("Bit 0 (byte 0 MSB) should be set", result.contains("n0"));
        assertTrue("Bit 8 (byte 1 MSB) should be set", result.contains("n8"));
        assertFalse("Bit 1 should not be set", result.contains("n1"));
    }

    @Test
    public void decodeBitString_twoBytes_unusedBits4_trimsFourBitsFromSecondByte() {
        // "FF FF" = all bits, 4 unused → 12 valid bits
        String[] sixteenNames = new String[16];
        for (int i = 0; i < 16; i++) sixteenNames[i] = "n" + i;

        String result = BitStringUtil.decodeBitString("FF FF", 4, sixteenNames);
        // bits 0-11 valid, bits 12-15 trimmed
        assertTrue(result.contains("n0"));
        assertTrue(result.contains("n11"));
        assertFalse(result.contains("n12"));
        assertFalse(result.contains("n15"));
    }

    // =========================================================================
    // Result format
    // =========================================================================

    @Test
    public void decodeBitString_resultStartsWithOpenBrace() {
        String result = BitStringUtil.decodeBitString("00", 0, EIGHT_NAMES);
        assertTrue(result.startsWith("{ "));
    }

    @Test
    public void decodeBitString_resultEndsWithCloseBrace() {
        String result = BitStringUtil.decodeBitString("FF", 0, EIGHT_NAMES);
        assertTrue(result.endsWith("}"));
    }
}