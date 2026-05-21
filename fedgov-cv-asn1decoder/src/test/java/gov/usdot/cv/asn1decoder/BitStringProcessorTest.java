/*
 * Copyright (C) 2026 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.usdot.cv.asn1decoder.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for the BIT STRING post-processors
 * ({@link MAPBitStringProcessor} and {@link BSMBitStringProcessor}).
 *
 * Both processors are pure Java (regex + dispatch table + {@link BitStringUtil}),
 * so these tests run without the native asn1c library. Expected values are
 * hand-derived from the MSB-first decoding in {@code BitStringUtil}: bit i
 * (0-based, MSB-first) maps to the i-th name in the field's array, only set bits
 * are emitted inside {@code "{ ... }"}, and the scan is bounded by both the live
 * bit count (bytes*8 - unusedBits) and the array length.
 */
public class BitStringProcessorTest {

    // MAPBitStringProcessor

    @Test
    public void mapNullInputReturnsNull() {
        Assert.assertNull(MAPBitStringProcessor.processMapBitStrings(null));
    }

    @Test
    public void mapEmptyStringReturnsEmptyString() {
        Assert.assertEquals("", MAPBitStringProcessor.processMapBitStrings(""));
    }

    @Test
    public void mapTextWithoutBitStringIsUnchanged() {
        String input = "MapData ::= { intersections : ... } no bit strings here";
        Assert.assertEquals(input, MAPBitStringProcessor.processMapBitStrings(input));
    }

    @Test
    public void mapUnrecognizedFieldNameIsLeftUnchanged() {
        String input = "speed: 80 (4 bits unused)";
        Assert.assertEquals(input, MAPBitStringProcessor.processMapBitStrings(input));
    }

    @Test
    public void mapManeuversSingleBit() {
        // 0x80 = 1000 0000 -> bit 0 set; 4 unused bits -> only first 4 bits considered.
        Assert.assertEquals(
            "maneuvers: { maneuverStraightAllowed }",
            MAPBitStringProcessor.processMapBitStrings("maneuvers: 80 (4 bits unused)"));
    }

    @Test
    public void mapManeuversMultipleBits() {
        // 0xC0 = 1100 0000 -> bits 0 and 1 set.
        Assert.assertEquals(
            "maneuvers: { maneuverStraightAllowed maneuverLeftAllowed }",
            MAPBitStringProcessor.processMapBitStrings("maneuvers: C0 (4 bits unused)"));
    }

    @Test
    public void mapManeuversAllEightBitsSet() {
        // 0xFF, 0 unused -> first 8 maneuver names (array has 12 entries).
        Assert.assertEquals(
            "maneuvers: { maneuverStraightAllowed maneuverLeftAllowed maneuverRightAllowed "
                + "maneuverUTurnAllowed maneuverLeftTurnOnRedAllowed maneuverRightTurnOnRedAllowed "
                + "maneuverLaneChangeAllowed maneuverNoStoppingAllowed }",
            MAPBitStringProcessor.processMapBitStrings("maneuvers: FF (0 bits unused)"));
    }

    @Test
    public void mapNoBitsSetProducesEmptyBraces() {
        Assert.assertEquals(
            "maneuvers: { }",
            MAPBitStringProcessor.processMapBitStrings("maneuvers: 00 (0 bits unused)"));
    }

    @Test
    public void mapManeuverSingularAliasUsesSameNames() {
        // Dispatch table maps both "maneuvers" and "maneuver" to MANEUVER_NAMES.
        Assert.assertEquals(
            "maneuver: { maneuverStraightAllowed }",
            MAPBitStringProcessor.processMapBitStrings("maneuver: 80 (4 bits unused)"));
    }

    @Test
    public void mapDirectionalUseSingleBit() {
        // LANE_DIRECTION_NAMES = { ingressPath, egressPath }; loop capped at names.length.
        Assert.assertEquals(
            "directionalUse: { ingressPath }",
            MAPBitStringProcessor.processMapBitStrings("directionalUse: 80 (6 bits unused)"));
    }

    @Test
    public void mapDirectionalUseBothBits() {
        Assert.assertEquals(
            "directionalUse: { ingressPath egressPath }",
            MAPBitStringProcessor.processMapBitStrings("directionalUse: C0 (6 bits unused)"));
    }

    @Test
    public void mapVehicleFieldDecodes() {
        Assert.assertEquals(
            "vehicle: { isVehicleRevocableLane }",
            MAPBitStringProcessor.processMapBitStrings("vehicle: 80 (0 bits unused)"));
    }

    @Test
    public void mapLowercaseHexIsParsed() {
        Assert.assertEquals(
            "vehicle: { isVehicleRevocableLane isVehicleFlyOverLane hovLaneUseOnly "
                + "restrictedToBusUse restrictedToTaxiUse restrictedFromPublicUse "
                + "hasIRbeaconCoverage permissionOnRequest }",
            MAPBitStringProcessor.processMapBitStrings("vehicle: ff (0 bits unused)"));
    }

    @Test
    public void mapSingularBitWordIsAccepted() {
        // 0x80 on BARRIER_NAMES (median) -> first name only.
        Assert.assertEquals(
            "median: { median-RevocableLane }",
            MAPBitStringProcessor.processMapBitStrings("median: 80 (1 bit unused)"));
    }

    @Test
    public void mapMultiByteHexSpansBytes() {
        // sharedWith -> LANE_SHARING_NAMES (9 names). "80 80" with 7 unused -> 9 live bits.
        // bit 0 (byte0 MSB) = overlappingLaneDescriptionProvided, bit 8 (byte1 MSB) = reserved.
        Assert.assertEquals(
            "sharedWith: { overlappingLaneDescriptionProvided reserved }",
            MAPBitStringProcessor.processMapBitStrings("sharedWith: 80 80 (7 bits unused)"));
    }

    @Test
    public void mapSurroundingTextIsPreserved() {
        Assert.assertEquals(
            "prefix maneuvers: { maneuverStraightAllowed } suffix",
            MAPBitStringProcessor.processMapBitStrings(
                "prefix maneuvers: 80 (4 bits unused) suffix"));
    }

    @Test
    public void mapMultipleFieldsInOneStringAreAllReplaced() {
        String input = "maneuvers: 80 (4 bits unused) and directionalUse: C0 (6 bits unused)";
        String expected =
            "maneuvers: { maneuverStraightAllowed } and "
                + "directionalUse: { ingressPath egressPath }";
        Assert.assertEquals(expected, MAPBitStringProcessor.processMapBitStrings(input));
    }

    @Test
    public void mapRecognizedAndUnrecognizedFieldsMixed() {
        String input = "speed: 80 (4 bits unused); maneuvers: 80 (4 bits unused)";
        String expected = "speed: 80 (4 bits unused); maneuvers: { maneuverStraightAllowed }";
        Assert.assertEquals(expected, MAPBitStringProcessor.processMapBitStrings(input));
    }

    // BSMBitStringProcessor

    @Test
    public void bsmNullInputReturnsNull() {
        Assert.assertNull(BSMBitStringProcessor.processBSMBitStrings(null));
    }

    @Test
    public void bsmEmptyStringReturnsEmptyString() {
        Assert.assertEquals("", BSMBitStringProcessor.processBSMBitStrings(""));
    }

    @Test
    public void bsmTextWithoutBitStringIsUnchanged() {
        String input = "BasicSafetyMessage ::= { coreData ... } nothing to decode here";
        Assert.assertEquals(input, BSMBitStringProcessor.processBSMBitStrings(input));
    }

    @Test
    public void bsmUnrecognizedFieldNameIsLeftUnchanged() {
        String input = "brakes: 80 (4 bits unused)";
        Assert.assertEquals(input, BSMBitStringProcessor.processBSMBitStrings(input));
    }

    @Test
    public void bsmExteriorLightsSingleBit() {
        // 0x80, 7 unused -> 1 live bit -> bit 0 only.
        Assert.assertEquals(
            "lights: { lowBeamHeadlightsOn }",
            BSMBitStringProcessor.processBSMBitStrings("lights: 80 (7 bits unused)"));
    }

    @Test
    public void bsmExteriorLightsMultipleBits() {
        // 0xA0 = 1010 0000 -> bits 0 and 2.
        Assert.assertEquals(
            "lights: { lowBeamHeadlightsOn leftTurnSignalOn }",
            BSMBitStringProcessor.processBSMBitStrings("lights: A0 (0 bits unused)"));
    }

    @Test
    public void bsmExteriorLightsNinthBitSpansSecondByte() {
        // 9 names. "80 80" with 7 unused -> 9 live bits.
        // bit 0 (byte0 MSB) = lowBeamHeadlightsOn, bit 8 (byte1 MSB) = parkingLightsOn (index 8).
        Assert.assertEquals(
            "lights: { lowBeamHeadlightsOn parkingLightsOn }",
            BSMBitStringProcessor.processBSMBitStrings("lights: 80 80 (7 bits unused)"));
    }

    @Test
    public void bsmHeadingSliceFirstBit() {
        Assert.assertEquals(
            "headingSlice: { from000-0to022-5degrees }",
            BSMBitStringProcessor.processBSMBitStrings("headingSlice: 80 00 (0 bits unused)"));
    }

    @Test
    public void bsmHeadingSliceFirstAndLastBit() {
        // 0x80 0x01 -> bit 0 (byte0 MSB) and bit 15 (byte1 LSB).
        Assert.assertEquals(
            "headingSlice: { from000-0to022-5degrees from337-5to360-0degrees }",
            BSMBitStringProcessor.processBSMBitStrings("headingSlice: 80 01 (0 bits unused)"));
    }

    @Test
    public void bsmVehicleEventFlagsSingleBit() {
        Assert.assertEquals(
            "events: { eventHazardLights }",
            BSMBitStringProcessor.processBSMBitStrings("events: 80 (0 bits unused)"));
    }

    @Test
    public void bsmVehicleEventFlagsAllFourteenBits() {
        // 14 names across two bytes. "FF FC" with 2 unused -> 14 live bits, all set:
        // byte0 = 0xFF (bits 0-7), byte1 = 0xFC = 1111 1100 (bits 8-13 set, 14-15 unused).
        Assert.assertEquals(
            "events: { eventHazardLights eventStopLineViolation eventABSactivated "
                + "eventTractionControlLoss eventStabilityControlactivated eventHazardousMaterials "
                + "eventReserved1 eventHardBraking eventLightsChanged eventWipersChanged "
                + "eventFlatTire eventDisabledVehicle eventAirBagDeployment eventJackKnife }",
            BSMBitStringProcessor.processBSMBitStrings("events: FF FC (2 bits unused)"));
    }

    @Test
    public void bsmNoBitsSetProducesEmptyBraces() {
        Assert.assertEquals(
            "events: { }",
            BSMBitStringProcessor.processBSMBitStrings("events: 00 00 (2 bits unused)"));
    }

    @Test
    public void bsmLowercaseHexIsParsed() {
        // Lowercase hex 'ff', 1 unused -> 7 live bits (indices 0-6). Field name stays mixed-case.
        Assert.assertEquals(
            "lights: { lowBeamHeadlightsOn highBeamHeadlightsOn leftTurnSignalOn "
                + "rightTurnSignalOn hazardSignalOn automaticLightControlOn daytimeRunningLightsOn }",
            BSMBitStringProcessor.processBSMBitStrings("lights: ff (1 bit unused)"));
    }

    @Test
    public void bsmSingularBitWordIsAccepted() {
        Assert.assertEquals(
            "lights: { lowBeamHeadlightsOn }",
            BSMBitStringProcessor.processBSMBitStrings("lights: 80 (1 bit unused)"));
    }

    @Test
    public void bsmSurroundingTextIsPreserved() {
        Assert.assertEquals(
            "prefix lights: { lowBeamHeadlightsOn } suffix",
            BSMBitStringProcessor.processBSMBitStrings(
                "prefix lights: 80 (7 bits unused) suffix"));
    }

    @Test
    public void bsmMultipleFieldsInOneStringAreAllReplaced() {
        String input = "lights: 80 (7 bits unused), events: 80 (0 bits unused)";
        String expected =
            "lights: { lowBeamHeadlightsOn }, events: { eventHazardLights }";
        Assert.assertEquals(expected, BSMBitStringProcessor.processBSMBitStrings(input));
    }

    @Test
    public void bsmRecognizedAndUnrecognizedFieldsMixed() {
        String input = "brakes: 80 (4 bits unused); lights: 80 (7 bits unused)";
        String expected = "brakes: 80 (4 bits unused); lights: { lowBeamHeadlightsOn }";
        Assert.assertEquals(expected, BSMBitStringProcessor.processBSMBitStrings(input));
    }
}