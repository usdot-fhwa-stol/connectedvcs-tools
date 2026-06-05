/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.timencoder;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests every fromValue / fromInt branch on the timencoder enums.
 * These static factory methods have real conditional logic that OpenPojo
 * cannot exercise, so they need explicit tests.
 *
 * Covers: Extent, TravelerInfoType, DirectionOfUse, RoadSurfaceCondition,
 *         AsphaltOrTarType, PortlandCementType, CindersType, GrassType,
 *         GravelType, IceType, RockType, SnowType, MUTCDCode, DistanceUnits
 */
public class TIMEnumTest {

    // =========================================================================
    // Extent
    // =========================================================================

    @Test
    public void extent_allValuesResolve() {
        assertEquals(Extent.useInstantlyOnly,      Extent.fromValue(0));
        assertEquals(Extent.useFor3meters,         Extent.fromValue(1));
        assertEquals(Extent.useFor10meters,        Extent.fromValue(2));
        assertEquals(Extent.useFor50meters,        Extent.fromValue(3));
        assertEquals(Extent.useFor100meters,       Extent.fromValue(4));
        assertEquals(Extent.useFor500meters,       Extent.fromValue(5));
        assertEquals(Extent.useFor1000meters,      Extent.fromValue(6));
        assertEquals(Extent.useFor5000meters,      Extent.fromValue(7));
        assertEquals(Extent.useFor10000meters,     Extent.fromValue(8));
        assertEquals(Extent.useFor50000meters,     Extent.fromValue(9));
        assertEquals(Extent.useFor100000meters,    Extent.fromValue(10));
        assertEquals(Extent.useFor500000meters,    Extent.fromValue(11));
        assertEquals(Extent.useFor1000000meters,   Extent.fromValue(12));
        assertEquals(Extent.useFor5000000meters,   Extent.fromValue(13));
        assertEquals(Extent.useFor10000000meters,  Extent.fromValue(14));
        assertEquals(Extent.forever,               Extent.fromValue(15));
    }

    @Test(expected = IllegalArgumentException.class)
    public void extent_invalidValue_throwsIAE() {
        Extent.fromValue(99);
    }

    @Test
    public void extent_getValue_roundTrips() {
        for (Extent e : Extent.values()) {
            assertEquals(e, Extent.fromValue(e.getValue()));
        }
    }

    @Test
    public void extent_toString_containsValue() {
        assertTrue(Extent.forever.toString().contains("15"));
    }

    // =========================================================================
    // TravelerInfoType
    // =========================================================================

    @Test
    public void travelerInfoType_allValuesResolve() {
        assertEquals(TravelerInfoType.UNKNOWN,            TravelerInfoType.fromValue(0));
        assertEquals(TravelerInfoType.ADVISORY,           TravelerInfoType.fromValue(1));
        assertEquals(TravelerInfoType.ROAD_SIGNAGE,       TravelerInfoType.fromValue(2));
        assertEquals(TravelerInfoType.COMMERCIAL_SIGNAGE, TravelerInfoType.fromValue(3));
    }

    @Test
    public void travelerInfoType_unknownValue_returnsUnknown() {
        // TravelerInfoType.fromValue returns UNKNOWN as fallback (not throw)
        assertEquals(TravelerInfoType.UNKNOWN, TravelerInfoType.fromValue(99));
    }

    @Test
    public void travelerInfoType_toString_containsValue() {
        assertTrue(TravelerInfoType.ADVISORY.toString().contains("1"));
    }

    // =========================================================================
    // DirectionOfUse
    // =========================================================================

    @Test
    public void directionOfUse_allValuesResolve() {
        assertEquals(DirectionOfUse.unavailable, DirectionOfUse.valueOf(0));
        assertEquals(DirectionOfUse.forward,     DirectionOfUse.valueOf(1));
        assertEquals(DirectionOfUse.reverse,     DirectionOfUse.valueOf(2));
        assertEquals(DirectionOfUse.both,        DirectionOfUse.valueOf(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void directionOfUse_invalidValue_throwsIAE() {
        DirectionOfUse.valueOf(99);
    }

    @Test
    public void directionOfUse_toString_containsValue() {
        assertTrue(DirectionOfUse.forward.toString().contains("1"));
    }

    // =========================================================================
    // RoadSurfaceCondition
    // =========================================================================

    @Test
    public void roadSurfaceCondition_allValuesResolve() {
        assertEquals(RoadSurfaceCondition.DRY, RoadSurfaceCondition.fromInt(0));
        assertEquals(RoadSurfaceCondition.WET, RoadSurfaceCondition.fromInt(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void roadSurfaceCondition_invalidValue_throwsIAE() {
        RoadSurfaceCondition.fromInt(99);
    }

    @Test
    public void roadSurfaceCondition_intValue_roundTrips() {
        assertEquals(0, RoadSurfaceCondition.DRY.intValue());
        assertEquals(1, RoadSurfaceCondition.WET.intValue());
    }

    @Test
    public void roadSurfaceCondition_toString_containsName() {
        assertTrue(RoadSurfaceCondition.DRY.toString().contains("DRY"));
    }

    // =========================================================================
    // AsphaltOrTarType
    // =========================================================================

    @Test
    public void asphaltOrTarType_allValuesResolve() {
        assertEquals(AsphaltOrTarType.AsphaltOrTarType_newSharp,       AsphaltOrTarType.fromInt(0));
        assertEquals(AsphaltOrTarType.AsphaltOrTarType_traveled,       AsphaltOrTarType.fromInt(1));
        assertEquals(AsphaltOrTarType.AsphaltOrTarType_trafficPolished, AsphaltOrTarType.fromInt(2));
        assertEquals(AsphaltOrTarType.AsphaltOrTarType_excessTar,      AsphaltOrTarType.fromInt(3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void asphaltOrTarType_invalidValue_throwsIAE() {
        AsphaltOrTarType.fromInt(99);
    }

    @Test
    public void asphaltOrTarType_intValue_roundTrips() {
        for (AsphaltOrTarType t : AsphaltOrTarType.values()) {
            assertEquals(t, AsphaltOrTarType.fromInt(t.intValue()));
        }
    }

    // =========================================================================
    // PortlandCementType
    // =========================================================================

    @Test
    public void portlandCementType_allValuesResolve() {
        assertEquals(PortlandCementType.PortlandCementType_newSharp,       PortlandCementType.fromInt(0));
        assertEquals(PortlandCementType.PortlandCementType_traveled,       PortlandCementType.fromInt(1));
        assertEquals(PortlandCementType.PortlandCementType_trafficPolished, PortlandCementType.fromInt(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void portlandCementType_invalidValue_throwsIAE() {
        PortlandCementType.fromInt(99);
    }

    @Test
    public void portlandCementType_intValue_roundTrips() {
        for (PortlandCementType t : PortlandCementType.values()) {
            assertEquals(t, PortlandCementType.fromInt(t.intValue()));
        }
    }

    // =========================================================================
    // CindersType
    // =========================================================================

    @Test
    public void cindersType_validValue_resolves() {
        assertEquals(CindersType.CindersType_packed, CindersType.fromInt(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cindersType_invalidValue_throwsIAE() {
        CindersType.fromInt(99);
    }

    // =========================================================================
    // GrassType
    // =========================================================================

    @Test
    public void grassType_validValue_resolves() {
        assertEquals(GrassType.GrassType_lessThan30Mph, GrassType.fromInt(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void grassType_invalidValue_throwsIAE() {
        GrassType.fromInt(99);
    }

    // =========================================================================
    // GravelType
    // =========================================================================

    @Test
    public void gravelType_allValuesResolve() {
        assertEquals(GravelType.GravelType_packedOiled, GravelType.fromInt(0));
        assertEquals(GravelType.GravelType_loose,       GravelType.fromInt(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void gravelType_invalidValue_throwsIAE() {
        GravelType.fromInt(99);
    }

    // =========================================================================
    // IceType
    // =========================================================================

    @Test
    public void iceType_validValue_resolves() {
        assertEquals(IceType.IceType_smooth, IceType.fromInt(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void iceType_invalidValue_throwsIAE() {
        IceType.fromInt(99);
    }

    // =========================================================================
    // RockType
    // =========================================================================

    @Test
    public void rockType_validValue_resolves() {
        assertEquals(RockType.RockType_crushed, RockType.fromInt(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rockType_invalidValue_throwsIAE() {
        RockType.fromInt(99);
    }

    // =========================================================================
    // SnowType
    // =========================================================================

    @Test
    public void snowType_allValuesResolve() {
        assertEquals(SnowType.SnowType_packed, SnowType.fromInt(0));
        assertEquals(SnowType.SnowType_loose,  SnowType.fromInt(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void snowType_invalidValue_throwsIAE() {
        SnowType.fromInt(99);
    }

    // =========================================================================
    // MUTCDCode  (no fromInt - just getValue and toString)
    // =========================================================================

    @Test
    public void mutcdCode_allGetValues() {
        assertEquals(0, MUTCDCode.none.getValue());
        assertEquals(1, MUTCDCode.regulatory.getValue());
        assertEquals(2, MUTCDCode.warning.getValue());
        assertEquals(3, MUTCDCode.maintenance.getValue());
        assertEquals(4, MUTCDCode.motoristService.getValue());
        assertEquals(5, MUTCDCode.guide.getValue());
        assertEquals(6, MUTCDCode.rec.getValue());
    }

    @Test
    public void mutcdCode_toString_containsValue() {
        assertTrue(MUTCDCode.regulatory.toString().contains("1"));
    }

    // =========================================================================
    // DistanceUnits  (no fromInt - just getValue and toString)
    // =========================================================================

    @Test
    public void distanceUnits_allGetValues() {
        assertEquals(0, DistanceUnits.centimeter.getValue());
        assertEquals(1, DistanceUnits.cm2_5.getValue());
        assertEquals(2, DistanceUnits.decimeter.getValue());
        assertEquals(3, DistanceUnits.meter.getValue());
        assertEquals(4, DistanceUnits.kilometer.getValue());
        assertEquals(5, DistanceUnits.foot.getValue());
        assertEquals(6, DistanceUnits.yard.getValue());
        assertEquals(7, DistanceUnits.mile.getValue());
    }

    @Test
    public void distanceUnits_toString_containsValue() {
        assertTrue(DistanceUnits.meter.toString().contains("3"));
    }
}