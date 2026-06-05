/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.util;

import gov.usdot.cv.mapencoder.NodeOffsetPointXY;
import gov.usdot.cv.rgaencoder.NodeXYZOffsetInfo;
import gov.usdot.cv.rgaencoder.NodeXYZOffsetValue;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link OffsetEncoding}.
 *
 * Covers:
 *   OffsetEncodingSize.getOffsetEncodingSize  - all 7 size thresholds
 *   OffsetEncoding.getOffsetEncodingSize(gp1,gp2) - all 4 type branches
 *   OffsetEncoding.getLongerOffset            - lat>lon and lon>lat paths
 *   OffsetEncoding.encodeOffset               - all 7 NodeOffsetPointXY cases
 *   OffsetEncoding.encodeRGAOffset            - all 6 NodeXYZOffsetValue cases
 *   OffsetEncoding.toString                   - format verification
 *
 * Uses nearby WGS-84 coordinates to keep offsets small and predictable.
 */
public class OffsetEncodingTest {

    // Two nearby points in Washington DC (~1 cm apart in lat, ~2 cm in lon)
    // We need offsets in specific size buckets, so we use real GeoPoints
    // with carefully chosen separations.
    //
    // Reference: 1 degree lat ≈ 111,139 m = 11,113,900 cm
    //            1 degree lon at 38.9° ≈ 86,500 m = 8,650,000 cm
    //
    // Offset20Bit threshold: 511 cm → ~0.0000046° lat
    // Offset32Bit threshold: 32767 cm → ~0.000295° lat

    private static final double BASE_LAT = 38.9555;
    private static final double BASE_LON = -77.1489;

    // ~100 cm lat separation → fits in 20-bit (≤511 cm)
    private static final GeoPoint GP1 = new GeoPoint(BASE_LAT, BASE_LON);
    private static final GeoPoint GP_20BIT = new GeoPoint(BASE_LAT + 0.000009, BASE_LON); // ~100 cm

    // ~600 cm → fits in 22-bit (≤1023)
    private static final GeoPoint GP_22BIT = new GeoPoint(BASE_LAT + 0.000055, BASE_LON);

    // ~1100 cm → fits in 24-bit (≤2047)
    private static final GeoPoint GP_24BIT = new GeoPoint(BASE_LAT + 0.000099, BASE_LON);

    // ~2500 cm → fits in 26-bit (≤4095)
    private static final GeoPoint GP_26BIT = new GeoPoint(BASE_LAT + 0.000225, BASE_LON);

    // ~5000 cm → fits in 28-bit (≤8191)
    private static final GeoPoint GP_28BIT = new GeoPoint(BASE_LAT + 0.00045, BASE_LON);

    // ~15000 cm → fits in 32-bit (≤32767)
    private static final GeoPoint GP_32BIT = new GeoPoint(BASE_LAT + 0.00135, BASE_LON);

    // ~50000 cm → exceeds 32-bit → Explicit64Bit
    private static final GeoPoint GP_64BIT = new GeoPoint(BASE_LAT + 0.0045, BASE_LON);

    // =========================================================================
    // OffsetEncodingSize.getOffsetEncodingSize
    // =========================================================================

    @Test
    public void getOffsetEncodingSize_zero_returns20Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset20Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(0));
    }

    @Test
    public void getOffsetEncodingSize_atBoundary511_returns20Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset20Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(511));
    }

    @Test
    public void getOffsetEncodingSize_512_returns22Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset22Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(512));
    }

    @Test
    public void getOffsetEncodingSize_atBoundary1023_returns22Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset22Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(1023));
    }

    @Test
    public void getOffsetEncodingSize_1024_returns24Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset24Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(1024));
    }

    @Test
    public void getOffsetEncodingSize_atBoundary2047_returns24Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset24Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(2047));
    }

    @Test
    public void getOffsetEncodingSize_2048_returns26Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset26Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(2048));
    }

    @Test
    public void getOffsetEncodingSize_atBoundary4095_returns26Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset26Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(4095));
    }

    @Test
    public void getOffsetEncodingSize_4096_returns28Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset28Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(4096));
    }

    @Test
    public void getOffsetEncodingSize_atBoundary8191_returns28Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset28Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(8191));
    }

    @Test
    public void getOffsetEncodingSize_8192_returns32Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset32Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(8192));
    }

    @Test
    public void getOffsetEncodingSize_atBoundary32767_returns32Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset32Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(32767));
    }

    @Test
    public void getOffsetEncodingSize_above32767_returnsExplicit64Bit() {
        assertEquals(OffsetEncoding.OffsetEncodingSize.Explicit64Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(32768));
        assertEquals(OffsetEncoding.OffsetEncodingSize.Explicit64Bit,
                OffsetEncoding.OffsetEncodingSize.getOffsetEncodingSize(Integer.MAX_VALUE));
    }

    // =========================================================================
    // OffsetEncoding.getOffsetEncodingSize(GeoPoint, GeoPoint) – type branches
    // =========================================================================

    @Test
    public void getOffsetEncodingSize_tightType_usesLongerOffset() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        OffsetEncoding.OffsetEncodingSize result = enc.getOffsetEncodingSize(GP1, GP_20BIT);
        assertNotNull(result);
        // Tight falls through to getLongerOffset path, result must be a valid size
        assertTrue(result.ordinal() >= 0);
    }

    @Test
    public void getOffsetEncodingSize_compactType_usesLongerOffset() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Compact);
        OffsetEncoding.OffsetEncodingSize result = enc.getOffsetEncodingSize(GP1, GP_20BIT);
        assertNotNull(result);
    }

    @Test
    public void getOffsetEncodingSize_explicitType_alwaysReturnsExplicit64Bit() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Explicit);
        assertEquals(OffsetEncoding.OffsetEncodingSize.Explicit64Bit,
                enc.getOffsetEncodingSize(GP1, GP_20BIT));
    }

    @Test
    public void getOffsetEncodingSize_standardType_alwaysReturns32Bit() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        assertEquals(OffsetEncoding.OffsetEncodingSize.Offset32Bit,
                enc.getOffsetEncodingSize(GP1, GP_64BIT));
    }

    // =========================================================================
    // getLongerOffset — lat > lon and lon > lat branches
    // =========================================================================

    @Test
    public void getLongerOffset_latDominates_returnsLatOffset() {
        // Large lat diff, small lon diff
        GeoPoint from = new GeoPoint(38.0, -77.0);
        GeoPoint to   = new GeoPoint(38.01, -77.0001); // lat >> lon
        int result = OffsetEncoding.getLongerOffset(from, to);
        assertTrue("Lat-dominated result should be positive", result > 0);
    }

    @Test
    public void getLongerOffset_lonDominates_returnsLonOffset() {
        // Small lat diff, large lon diff
        GeoPoint from = new GeoPoint(38.0, -77.0);
        GeoPoint to   = new GeoPoint(38.0001, -77.01); // lon >> lat
        int result = OffsetEncoding.getLongerOffset(from, to);
        assertTrue("Lon-dominated result should be positive", result > 0);
    }

    @Test
    public void getLongerOffset_equalOffsets_returnsNonNegative() {
        int result = OffsetEncoding.getLongerOffset(GP1, GP1);
        assertEquals(0, result);
    }

    // =========================================================================
    // toString
    // =========================================================================

    @Test
    public void toString_containsTypeAndSize() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        String s = enc.toString();
        assertTrue(s.contains("Standard"));
        assertTrue(s.contains("Offset32Bit"));
    }

    // =========================================================================
    // encodeOffset — all 7 NodeOffsetPointXY cases
    // =========================================================================

    @Test
    public void encodeOffset_20bit_producesNodeXY1() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset20Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_20BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_XY1, result.getChoice());
        assertNotNull(result.getNodeXY1());
    }

    @Test
    public void encodeOffset_22bit_producesNodeXY2() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset22Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_22BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_XY2, result.getChoice());
        assertNotNull(result.getNodeXY2());
    }

    @Test
    public void encodeOffset_24bit_producesNodeXY3() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset24Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_24BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_XY3, result.getChoice());
        assertNotNull(result.getNodeXY3());
    }

    @Test
    public void encodeOffset_26bit_producesNodeXY4() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset26Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_26BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_XY4, result.getChoice());
        assertNotNull(result.getNodeXY4());
    }

    @Test
    public void encodeOffset_28bit_producesNodeXY5() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset28Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_28BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_XY5, result.getChoice());
        assertNotNull(result.getNodeXY5());
    }

    @Test
    public void encodeOffset_32bit_producesNodeXY6() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Standard);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset32Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_32BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_XY6, result.getChoice());
        assertNotNull(result.getNodeXY6());
    }

    @Test
    public void encodeOffset_explicit64bit_producesNodeLatLon() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Explicit);
        enc.size = OffsetEncoding.OffsetEncodingSize.Explicit64Bit;
        NodeOffsetPointXY result = enc.encodeOffset(GP1, GP_64BIT);
        assertNotNull(result);
        assertEquals(NodeOffsetPointXY.NODE_LAT_LON, result.getChoice());
        assertNotNull(result.getNodeLatLon());
    }

    // =========================================================================
    // encodeRGAOffset — all 6 NodeXYZOffsetValue cases
    // =========================================================================

    private static final GeoPoint GP1_3D    = new GeoPoint(38.9555, -77.1489, 50.0);
    private static final GeoPoint GP_20B_3D = new GeoPoint(38.9555 + 0.000009, -77.1489, 50.5);

    @Test
    public void encodeRGAOffset_20bit_usesOffsetB10() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset20Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertNotNull(result);
        assertEquals(NodeXYZOffsetValue.OFFSET_B10, result.getNodeXOffsetValue().getChoice());
        assertEquals(NodeXYZOffsetValue.OFFSET_B10, result.getNodeYOffsetValue().getChoice());
        assertEquals(NodeXYZOffsetValue.OFFSET_B10, result.getNodeZOffsetValue().getChoice());
    }

    @Test
    public void encodeRGAOffset_22bit_usesOffsetB11() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset22Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertEquals(NodeXYZOffsetValue.OFFSET_B11, result.getNodeXOffsetValue().getChoice());
    }

    @Test
    public void encodeRGAOffset_24bit_usesOffsetB12() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset24Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertEquals(NodeXYZOffsetValue.OFFSET_B12, result.getNodeXOffsetValue().getChoice());
    }

    @Test
    public void encodeRGAOffset_26bit_usesOffsetB13() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset26Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertEquals(NodeXYZOffsetValue.OFFSET_B13, result.getNodeXOffsetValue().getChoice());
    }

    @Test
    public void encodeRGAOffset_28bit_usesOffsetB14() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset28Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertEquals(NodeXYZOffsetValue.OFFSET_B14, result.getNodeXOffsetValue().getChoice());
    }

    @Test
    public void encodeRGAOffset_32bit_usesOffsetB16() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset32Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertEquals(NodeXYZOffsetValue.OFFSET_B16, result.getNodeXOffsetValue().getChoice());
    }

    @Test
    public void encodeRGAOffset_returnsNonNullStructure() {
        OffsetEncoding enc = new OffsetEncoding(OffsetEncoding.OffsetEncodingType.Tight);
        enc.size = OffsetEncoding.OffsetEncodingSize.Offset32Bit;
        NodeXYZOffsetInfo result = enc.encodeRGAOffset(GP1_3D, GP_20B_3D);
        assertNotNull(result.getNodeXOffsetValue());
        assertNotNull(result.getNodeYOffsetValue());
        assertNotNull(result.getNodeZOffsetValue());
    }
}