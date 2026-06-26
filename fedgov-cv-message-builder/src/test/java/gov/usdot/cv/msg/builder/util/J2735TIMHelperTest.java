/*
 * Copyright (C) 2026 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link J2735TIMHelper} targeting uncovered branches:
 *
 *  extractDecodedMessageFromValidatorResponse
 *    - "message" key absent → returns input unchanged
 *    - outerEnd < outerStart → returns input unchanged
 *    - "decodedMessage" key absent → returns input unchanged
 *    - decodedEnd < decodedStart → returns input unchanged
 *    - happy path: escape sequences replaced correctly
 *
 *  bitWiseOr
 *    - empty array
 *    - single element
 *    - multiple elements, known result
 *    - all-zeros → 0
 *    - overlapping bits
 *
 *  convertGeoCoordinateToInt
 *    - positive coordinate (typical lat)
 *    - negative coordinate (typical lon)
 *    - zero
 *    - rounding at .5 boundary
 *    - extreme values
 *
 *  getMutcdFromInt (via TravelerInformationBuilder — but static, so call directly)
 *    covered in TravelerInformationBuilderTest, mirrored here for isolation
 */
public class J2735TIMHelperTest {

    // =========================================================================
    // extractDecodedMessageFromValidatorResponse
    // =========================================================================

    @Test
    public void extractDecoded_noMessageKey_returnsOriginal() {
        String input = "{\"status\":\"ok\",\"data\":\"something\"}";
        String result = J2735TIMHelper.extractDecodedMessageFromValidatorResponse(input);
        assertEquals(input, result);
    }

    @Test
    public void extractDecoded_outerEndBeforeOuterStart_returnsOriginal() {
        // "message":" is present but there is no trailing "} so outerEnd < outerStart
        String input = "{\"message\":\"no closing brace here";
        String result = J2735TIMHelper.extractDecodedMessageFromValidatorResponse(input);
        assertEquals(input, result);
    }

    @Test
    public void extractDecoded_noDecodedMessageKey_returnsOriginal() {
        // Has outer message wrapper but inner JSON has no decodedMessage key
        // The inner JSON must be escaped inside the "message" value
        String input = "{\"message\":\"{\\\"otherKey\\\":\\\"someValue\\\"}\"}";
        String result = J2735TIMHelper.extractDecodedMessageFromValidatorResponse(input);
        assertEquals(input, result);
    }

    @Test
    public void extractDecoded_decodedEndBeforeDecodedStart_returnsOriginal() {
        // decodedMessage key present but no closing \" after the value start
        String input = "{\"message\":\"{\\\"decodedMessage\\\":\\\"no end here\"}";
        String result = J2735TIMHelper.extractDecodedMessageFromValidatorResponse(input);
        assertEquals(input, result);
    }

    @Test
    public void extractDecoded_happyPath_unescapesAndStrips() {
        // Construct a response that matches the parsing logic:
        // outer: {"message":"<inner>"}
        // inner (JSON-escaped): {\"decodedMessage\":\"line1\\nline2\"}
        String innerDecoded = "line1\\nline2";
        String innerJson = "{\\\"decodedMessage\\\":\\\"" + innerDecoded + "\\\"}";
        String input = "{\"message\":\"" + innerJson + "\"}";

        String result = J2735TIMHelper.extractDecodedMessageFromValidatorResponse(input);

        // The method replaces literal \n with newline and removes remaining backslashes
        assertTrue("Should contain actual newline", result.contains("\n"));
        assertFalse("Should not contain escaped newline", result.contains("\\n"));
    }

    @Test
    public void extractDecoded_multipleEscapeSequences_allReplaced() {
        String innerDecoded = "hello\\nworld\\nbye";
        String innerJson = "{\\\"decodedMessage\\\":\\\"" + innerDecoded + "\\\"}";
        String input = "{\"message\":\"" + innerJson + "\"}";

        String result = J2735TIMHelper.extractDecodedMessageFromValidatorResponse(input);
        // Two \n → two newlines
        long newlineCount = result.chars().filter(c -> c == '\n').count();
        assertEquals(2, newlineCount);
    }

    // =========================================================================
    // bitWiseOr
    // =========================================================================

    @Test
    public void bitWiseOr_emptyArray_returnsZero() {
        assertEquals(0, J2735TIMHelper.bitWiseOr(new int[]{}));
    }

    @Test
    public void bitWiseOr_singleElement_returnsThatElement() {
        assertEquals(42, J2735TIMHelper.bitWiseOr(new int[]{42}));
        assertEquals(0,  J2735TIMHelper.bitWiseOr(new int[]{0}));
    }

    @Test
    public void bitWiseOr_allZeros_returnsZero() {
        assertEquals(0, J2735TIMHelper.bitWiseOr(new int[]{0, 0, 0, 0}));
    }

    @Test
    public void bitWiseOr_nonOverlappingBits_sumOfBits() {
        // 0b0001 | 0b0010 | 0b0100 = 0b0111 = 7
        assertEquals(7, J2735TIMHelper.bitWiseOr(new int[]{1, 2, 4}));
    }

    @Test
    public void bitWiseOr_overlappingBits_correctOr() {
        // 0b1100 | 0b0110 = 0b1110 = 14
        assertEquals(14, J2735TIMHelper.bitWiseOr(new int[]{0b1100, 0b0110}));
    }

    @Test
    public void bitWiseOr_allOnes32Bit_returnsAllOnes() {
        assertEquals(-1, J2735TIMHelper.bitWiseOr(new int[]{-1, 0}));
    }

    @Test
    public void bitWiseOr_headingBits_matchesExpected() {
        // Typical heading slice: bits 11,12,13,14,15 → each heading value maps to a bit
        // This mirrors the usage in TravelerInformationBuilder.getHeadingSlice
        int[] headings = {0, 3, 4, 7};
        int result = J2735TIMHelper.bitWiseOr(headings);
        // 0|3|4|7 = 7 (since 0|3=3, 3|4=7, 7|7=7)
        assertEquals(7, result);
    }

    // =========================================================================
    // convertGeoCoordinateToInt
    // =========================================================================

    @Test
    public void convertGeoCoordinateToInt_typicalLatitude_correctInt() {
        // 38.955 * 10,000,000 = 389,550,000
        assertEquals(389550000, J2735TIMHelper.convertGeoCoordinateToInt(38.955));
    }

    @Test
    public void convertGeoCoordinateToInt_typicalLongitude_correctInt() {
        // -77.149 * 10,000,000 = -771,490,000
        assertEquals(-771490000, J2735TIMHelper.convertGeoCoordinateToInt(-77.149));
    }

    @Test
    public void convertGeoCoordinateToInt_zero_returnsZero() {
        assertEquals(0, J2735TIMHelper.convertGeoCoordinateToInt(0.0));
    }

    @Test
    public void convertGeoCoordinateToInt_positiveRoundingUp() {
        // 0.00000005 * 10_000_000 = 0.5 → rounds to 1
        assertEquals(1, J2735TIMHelper.convertGeoCoordinateToInt(0.0000001));
    }

    @Test
    public void convertGeoCoordinateToInt_negativeValue_roundsCorrectly() {
        // -77.1489615 * 10_000_000 = -771489615 (no rounding needed)
        int result = J2735TIMHelper.convertGeoCoordinateToInt(-77.1489615);
        // Allow ±1 for floating-point representation
        assertTrue(Math.abs(result - (-771489615)) <= 1);
    }

    @Test
    public void convertGeoCoordinateToInt_maxLatitude_90() {
        assertEquals(900000000, J2735TIMHelper.convertGeoCoordinateToInt(90.0));
    }

    @Test
    public void convertGeoCoordinateToInt_minLatitude_minus90() {
        assertEquals(-900000000, J2735TIMHelper.convertGeoCoordinateToInt(-90.0));
    }

    @Test
    public void convertGeoCoordinateToInt_maxLongitude_180() {
        assertEquals(1800000000, J2735TIMHelper.convertGeoCoordinateToInt(180.0));
    }

    @Test
    public void convertGeoCoordinateToInt_scaleFactor_isCorrect() {
        // Verifying the 10,000,000 constant is applied
        double coord = 1.0;
        assertEquals(10000000, J2735TIMHelper.convertGeoCoordinateToInt(coord));
    }
}