/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.msg.builder.input;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for {@link TravelerInputData}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * GenerateType.fromType() is called on every TIM build to determine the
 * encoding path (ASD, TIM, or Frame+TIM). An unknown type throws
 * IllegalArgumentException which aborts the entire message build silently.
 * These tests verify that each valid type resolves correctly and that an
 * invalid type fails loudly with the right exception — not silently with null.
 *
 * getGenerateType() is the public API entry point used by TravelerInformationBuilder.
 */
public class TravelerInputDataTest {

    // =========================================================================
    // GenerateType.fromType — all valid values
    // =========================================================================

    @Test
    public void fromType_TIM_returnsTimEnum() {
        assertEquals(TravelerInputData.GenerateType.TIM,
                TravelerInputData.GenerateType.fromType("TIM"));
    }

    @Test
    public void fromType_ASD_returnsAsdEnum() {
        assertEquals(TravelerInputData.GenerateType.ASD,
                TravelerInputData.GenerateType.fromType("ASD"));
    }

    @Test
    public void fromType_FramePlusTIM_returnsFramePlusTIMEnum() {
        assertEquals(TravelerInputData.GenerateType.FramePlusTIM,
                TravelerInputData.GenerateType.fromType("Frame+TIM"));
    }

    // =========================================================================
    // GenerateType.fromType — invalid values throw IllegalArgumentException
    // =========================================================================

    @Test(expected = IllegalArgumentException.class)
    public void fromType_unknownString_throwsIllegalArgument() {
        TravelerInputData.GenerateType.fromType("UNKNOWN");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromType_emptyString_throwsIllegalArgument() {
        TravelerInputData.GenerateType.fromType("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fromType_lowercaseTim_throwsIllegalArgument() {
        // Matching is case-sensitive via contentEquals
        TravelerInputData.GenerateType.fromType("tim");
    }

    @Test(expected = NullPointerException.class)
    public void fromType_null_throwsNullPointerException() {
        // contentEquals(null) throws NullPointerException — documents actual behavior.
        // The point is it fails loudly rather than returning a wrong default silently.
        TravelerInputData.GenerateType.fromType(null);
    }

    // =========================================================================
    // getGenerateType — verifies the public API delegates correctly
    // =========================================================================

    @Test
    public void getGenerateType_defaultMessageType_returnsTIM() {
        TravelerInputData data = new TravelerInputData();
        // Default messageType field is "TIM"
        assertEquals(TravelerInputData.GenerateType.TIM, data.getGenerateType());
    }

    @Test
    public void getGenerateType_afterSettingASD_returnsASD() {
        TravelerInputData data = new TravelerInputData();
        data.messageType = "ASD";
        assertEquals(TravelerInputData.GenerateType.ASD, data.getGenerateType());
    }

    @Test
    public void getGenerateType_afterSettingFramePlusTIM_returnsFramePlusTIM() {
        TravelerInputData data = new TravelerInputData();
        data.messageType = "Frame+TIM";
        assertEquals(TravelerInputData.GenerateType.FramePlusTIM, data.getGenerateType());
    }
}