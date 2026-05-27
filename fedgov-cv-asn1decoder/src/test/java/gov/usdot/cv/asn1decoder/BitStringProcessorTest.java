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

import gov.usdot.cv.asn1decoder.util.BSMBitStringProcessor;
import gov.usdot.cv.asn1decoder.util.MAPBitStringProcessor;
import gov.usdot.cv.asn1decoder.util.SPATBitStringProcessor;
import gov.usdot.cv.asn1decoder.util.SRMBitStringProcessor;

/**
 * Unit tests for the BIT STRING post-processors
 * ({@link MAPBitStringProcessor}, {@link BSMBitStringProcessor}, {@link SPATBitStringProcessor}).
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

    // SPATBitStringProcessor

    @Test
    public void spatNullInputReturnsNull() {
        Assert.assertNull(SPATBitStringProcessor.processSPATBitStrings(null));
    }

    @Test
    public void spatEmptyStringReturnsEmptyString() {
        Assert.assertEquals("", SPATBitStringProcessor.processSPATBitStrings(""));
    }
 
    @Test
    public void unrecognizedFieldNameIsLeftUnchanged() {
        // "revision" is not in the regex alternation, so it passes through verbatim.
        String input = "revision: 80 00";
        Assert.assertEquals(input, SPATBitStringProcessor.processSPATBitStrings(input));
    }

    @Test
    public void statusSingleBit() {
        // 0x80 0x00 -> bit 0 only.
        Assert.assertEquals(
            "status: { manualControlIsEnabled }",
            SPATBitStringProcessor.processSPATBitStrings("status: 80 00"));
    }
 
    @Test
    public void statusMultipleBitsFirstByte() {
        // 0xA0 = 1010 0000 -> bits 0 and 2.
        Assert.assertEquals(
            "status: { manualControlIsEnabled failureFlash }",
            SPATBitStringProcessor.processSPATBitStrings("status: A0 00"));
    }
 
    @Test
    public void statusAllEightBitsOfFirstByte() {
        // 0xFF 0x00 -> first 8 names (manualControlIsEnabled .. standbyOperation).
        Assert.assertEquals(
            "status: { manualControlIsEnabled stopTimeIsActivated failureFlash preemptIsActive "
                + "signalPriorityIsActive fixedTimeOperation trafficDependentOperation "
                + "standbyOperation }",
            SPATBitStringProcessor.processSPATBitStrings("status: FF 00"));
    }
 
    @Test
    public void statusAllFourteenNamedBits() {
        // 14 named bits. "FF FC" = byte0 0xFF (bits 0-7),
        // byte1 0xFC = 1111 1100 (bits 8-13 set, 14-15 reserved -> 2 unused).
        Assert.assertEquals(
            "status: { manualControlIsEnabled stopTimeIsActivated failureFlash preemptIsActive "
                + "signalPriorityIsActive fixedTimeOperation trafficDependentOperation "
                + "standbyOperation failureMode off recentMAPmessageUpdate "
                + "recentChangeInMAPassignedLanesIDsUsed noValidMAPisAvailableAtThisTime "
                + "noValidSPATisAvailableAtThisTime }",
            SPATBitStringProcessor.processSPATBitStrings("status: FF FC"));
    }
 
    @Test
    public void reservedBitsBeyondNamesAreIgnored() {
        // "00 03" -> bits 14 and 15 set (reserved). 14 names cap the scan, so nothing emitted.
        Assert.assertEquals(
            "status: { }",
            SPATBitStringProcessor.processSPATBitStrings("status: 00 03"));
    }
 
    @Test
    public void noBitsSetProducesEmptyBraces() {
        Assert.assertEquals(
            "status: { }",
            SPATBitStringProcessor.processSPATBitStrings("status: 00 00"));
    }
 
    @Test
    public void lastNamedBitOnly() {
        // bit 13 = noValidSPATisAvailableAtThisTime. byte1 = 0x04 = 0000 0100 -> bit 13.
        Assert.assertEquals(
            "status: { noValidSPATisAvailableAtThisTime }",
            SPATBitStringProcessor.processSPATBitStrings("status: 00 04"));
    }

    @Test
    public void lowercaseHexIsParsed() {
        // Lowercase 'ff', byte0 set -> first 8 names.
        Assert.assertEquals(
            "status: { manualControlIsEnabled stopTimeIsActivated failureFlash preemptIsActive "
                + "signalPriorityIsActive fixedTimeOperation trafficDependentOperation "
                + "standbyOperation }",
            SPATBitStringProcessor.processSPATBitStrings("status: ff 00"));
    }
 
    @Test
    public void singularBitWordIsAccepted() {
        // "1 bit unused" (singular) must match the same as "bits".
        Assert.assertEquals(
            "status: { manualControlIsEnabled }",
            SPATBitStringProcessor.processSPATBitStrings("status: 80 00"));
    }

    // SRMBitStringProcessor

    @Test
    public void srmNullInputReturnsNull() {
        Assert.assertNull(SRMBitStringProcessor.processSRMBitStrings(null));
    }

    @Test
    public void srmEmptyStringReturnsEmptyString() {
        Assert.assertEquals("", SRMBitStringProcessor.processSRMBitStrings(""));
    }

    @Test
    public void srmTextWithoutBitStringIsUnchanged() {
        String input = "SignalRequestMessage ::= { second: 100 } nothing to decode here";
        Assert.assertEquals(input, SRMBitStringProcessor.processSRMBitStrings(input));
    }

    @Test
    public void srmUnrecognizedFieldNameIsLeftUnchanged() {
        String input = "speed: 80 (2 bits unused)";
        Assert.assertEquals(input, SRMBitStringProcessor.processSRMBitStrings(input));
    }

    @Test
    public void srmTransitStatusLoading() {
        // 0x80 = 1000 0000, 6 live bits -> bit 0 -> loading
        Assert.assertEquals(
            "transitStatus: { loading }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 80 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusAnADAuse() {
        // 0x40 = 0100 0000 -> bit 1 -> anADAuse
        Assert.assertEquals(
            "transitStatus: { anADAuse }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 40 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusABikeLoad() {
        // 0x20 = 0010 0000 -> bit 2 -> aBikeLoad
        Assert.assertEquals(
            "transitStatus: { aBikeLoad }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 20 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusDoorOpen() {
        // 0x10 = 0001 0000 -> bit 3 -> doorOpen
        Assert.assertEquals(
            "transitStatus: { doorOpen }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 10 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusCharging() {
        // 0x08 = 0000 1000 -> bit 4 -> charging
        Assert.assertEquals(
            "transitStatus: { charging }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 08 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusAtStopLine() {
        // 0x04 = 0000 0100 -> bit 5 -> atStopLine
        Assert.assertEquals(
            "transitStatus: { atStopLine }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 04 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusAllSixBitsSet() {
        // 0xFC = 1111 1100 -> bits 0-5 -> all 6 names
        Assert.assertEquals(
            "transitStatus: { loading anADAuse aBikeLoad doorOpen charging atStopLine }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: FC (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusNoBitsSet() {
        Assert.assertEquals(
            "transitStatus: { }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 00 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusMultipleBitsSet() {
        // 0xC0 = 1100 0000 -> bits 0,1 -> loading, anADAuse
        Assert.assertEquals(
            "transitStatus: { loading anADAuse }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: C0 (2 bits unused)"));
    }

    @Test
    public void srmTransitStatusReservedBitsBeyondNamesAreIgnored() {
        // bits 6,7 are beyond the 6 named bits -> empty set
        Assert.assertEquals(
            "transitStatus: { }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 03 (0 bits unused)"));
    }

    @Test
    public void srmTransitStatusSingularBitKeywordAccepted() {
        Assert.assertEquals(
            "transitStatus: { loading }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: 80 (1 bit unused)"));
    }

    @Test
    public void srmTransitStatusNoUnusedBitsSuffix() {
        Assert.assertEquals(
            "transitStatus: { loading }",
            SRMBitStringProcessor.processSRMBitStrings("transitStatus: 80"));
    }

    @Test
    public void srmTransitStatusLowercaseHexIsParsed() {
        Assert.assertEquals(
            "transitStatus: { loading anADAuse aBikeLoad doorOpen charging atStopLine }",
            SRMBitStringProcessor.processSRMBitStrings(
                "transitStatus: fc (2 bits unused)"));
    }

    @Test
    public void srmSurroundingTextIsPreserved() {
        Assert.assertEquals(
            "prefix transitStatus: { doorOpen } suffix",
            SRMBitStringProcessor.processSRMBitStrings(
                "prefix transitStatus: 10 (2 bits unused) suffix"));
    }

    @Test
    public void srmNonBitStringFieldsAreUntouched() {
        // role is BasicVehicleRole (ENUMERATED), requestType is PriorityRequestType (ENUMERATED)
        String input =
            "SignalRequestMessage ::= {\n" +
            "  role: 9 (transit)\n" +
            "  requestType: 1 (priorityRequest)\n" +
            "  transitStatus: 80 (2 bits unused)\n" +
            "}";
        String result = SRMBitStringProcessor.processSRMBitStrings(input);
        Assert.assertTrue(result.contains("role: 9 (transit)"));
        Assert.assertTrue(result.contains("requestType: 1 (priorityRequest)"));
        Assert.assertTrue(result.contains("transitStatus: { loading }"));
    }

    @Test
    public void srmRecognizedAndUnrecognizedFieldsMixed() {
        String input = "speed: 80 (2 bits unused); transitStatus: 80 (2 bits unused)";
        String expected = "speed: 80 (2 bits unused); transitStatus: { loading }";
        Assert.assertEquals(expected, SRMBitStringProcessor.processSRMBitStrings(input));
    }

    @Test
    public void srmFullMessageContext() {
        String input =
            "MessageFrame ::= {\n" +
            "  messageId: 29\n" +
            "  value: SignalRequestMessage ::= {\n" +
            "    second: 1000\n" +
            "    requestor: RequestorDescription ::= {\n" +
            "      id: 12 34 56 78\n" +
            "      transitStatus: C0 (2 bits unused)\n" +
            "    }\n" +
            "  }\n" +
            "}";
        String result = SRMBitStringProcessor.processSRMBitStrings(input);
        Assert.assertTrue(result.contains("transitStatus: { loading anADAuse }"));
        Assert.assertTrue(result.contains("second: 1000"));
        Assert.assertTrue(result.contains("id: 12 34 56 78"));
    }
}