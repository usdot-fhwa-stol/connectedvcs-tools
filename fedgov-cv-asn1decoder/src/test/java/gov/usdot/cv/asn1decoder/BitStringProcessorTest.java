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
import gov.usdot.cv.asn1decoder.util.SRMBitStringProcessor;
import gov.usdot.cv.asn1decoder.util.PSMBitStringProcessor;
import gov.usdot.cv.asn1decoder.util.SPATBitStringProcessor;
import gov.usdot.cv.asn1decoder.util.TIMBitStringProcessor;

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

    // PSMBitStringProcessor
 
    @Test
    public void psmNullInputReturnsNull() {
        Assert.assertNull(PSMBitStringProcessor.processPSMBitStrings(null));
    }
 
    @Test
    public void psmEmptyStringReturnsEmptyString() {
        Assert.assertEquals("", PSMBitStringProcessor.processPSMBitStrings(""));
    }
 
    @Test
    public void psmTextWithoutBitStringIsUnchanged() {
        String input = "PersonalSafetyMessage ::= { msgCnt: 1 } nothing to decode here";
        Assert.assertEquals(input, PSMBitStringProcessor.processPSMBitStrings(input));
    }
 
    @Test
    public void psmUnrecognizedFieldNameIsLeftUnchanged() {
        String input = "speed: 80 (4 bits unused)";
        Assert.assertEquals(input, PSMBitStringProcessor.processPSMBitStrings(input));
    }
 
    // useState — PersonalDeviceUsageState (9 bits)
 
    @Test
    public void psmUseStateUnavailable() {
        // 0x80 = 1000 0000 -> bit 0 -> unavailable
        Assert.assertEquals(
            "useState: { unavailable }",
            PSMBitStringProcessor.processPSMBitStrings("useState: 80 (0 bits unused)"));
    }
 
    @Test
    public void psmUseStateOther() {
        // 0x40 = 0100 0000 -> bit 1 -> other
        Assert.assertEquals(
            "useState: { other }",
            PSMBitStringProcessor.processPSMBitStrings("useState: 40 (0 bits unused)"));
    }
 
    @Test
    public void psmUseStateCalling() {
        // 0x04 = 0000 0100 -> bit 5 -> calling
        Assert.assertEquals(
            "useState: { calling }",
            PSMBitStringProcessor.processPSMBitStrings("useState: 04 (0 bits unused)"));
    }
 
    @Test
    public void psmUseStateViewing() {
        // bit 8 = MSB of second byte -> 0x80 in byte1 -> viewing
        Assert.assertEquals(
            "useState: { viewing }",
            PSMBitStringProcessor.processPSMBitStrings("useState: 00 80 (0 bits unused)"));
    }
 
    @Test
    public void psmUseStateAllNineBitsSet() {
        // FF 80 -> byte0 all set (bits 0-7), byte1 MSB set (bit 8) -> all 9 names
        Assert.assertEquals(
            "useState: { unavailable other idle listeningToAudio typing calling "
                + "playingGames reading viewing }",
            PSMBitStringProcessor.processPSMBitStrings("useState: FF 80 (0 bits unused)"));
    }
 
    @Test
    public void psmUseStateNoBitsSet() {
        Assert.assertEquals(
            "useState: { }",
            PSMBitStringProcessor.processPSMBitStrings("useState: 00 00 (0 bits unused)"));
    }
 
    @Test
    public void psmUseStateNoUnusedBitsSuffix() {
        // ASN1c may omit the unused-bits clause when all bits are used
        Assert.assertEquals(
            "useState: { unavailable }",
            PSMBitStringProcessor.processPSMBitStrings("useState: 80"));
    }
 
    // sizing — UserSizeAndBehaviour (5 bits)
    @Test
    public void psmSizingSmallStature() {
        // 0x40 = 0100 0000 -> bit 1 -> smallStature
        Assert.assertEquals(
            "sizing: { smallStature }",
            PSMBitStringProcessor.processPSMBitStrings("sizing: 40 (0 bits unused)"));
    }
 
    @Test
    public void psmSizingErraticMoving() {
        // 0x10 = 0001 0000 -> bit 3 -> erraticMoving
        Assert.assertEquals(
            "sizing: { erraticMoving }",
            PSMBitStringProcessor.processPSMBitStrings("sizing: 10 (0 bits unused)"));
    }
 
    @Test
    public void psmSizingAllFiveBitsSet() {
        // 0xF8 = 1111 1000 -> bits 0-4 -> all 5 names
        Assert.assertEquals(
            "sizing: { unavailable smallStature largeStature erraticMoving slowMoving }",
            PSMBitStringProcessor.processPSMBitStrings("sizing: F8 (0 bits unused)"));
    }
 
    @Test
    public void psmSizingNoBitsSet() {
        Assert.assertEquals(
            "sizing: { }",
            PSMBitStringProcessor.processPSMBitStrings("sizing: 00 (0 bits unused)"));
    }
 
    // activityType — PublicSafetyAndRoadWorkerActivity (6 bits)
 
    @Test
    public void psmActivityTypeWorkingOnRoad() {
        // 0x40 = 0100 0000 -> bit 1 -> workingOnRoad
        Assert.assertEquals(
            "activityType: { workingOnRoad }",
            PSMBitStringProcessor.processPSMBitStrings("activityType: 40 (0 bits unused)"));
    }
 
    @Test
    public void psmActivityTypeDirectingTraffic() {
        // 0x08 = 0000 1000 -> bit 4 -> directingTraffic
        Assert.assertEquals(
            "activityType: { directingTraffic }",
            PSMBitStringProcessor.processPSMBitStrings("activityType: 08 (0 bits unused)"));
    }
 
    @Test
    public void psmActivityTypeOtherActivities() {
        // 0x04 = 0000 0100 -> bit 5 -> otherActivities
        Assert.assertEquals(
            "activityType: { otherActivities }",
            PSMBitStringProcessor.processPSMBitStrings("activityType: 04 (0 bits unused)"));
    }
 
    @Test
    public void psmActivityTypeNoBitsSet() {
        Assert.assertEquals(
            "activityType: { }",
            PSMBitStringProcessor.processPSMBitStrings("activityType: 00 (0 bits unused)"));
    }
 
    // activitySubType — PublicSafetyDirectingTrafficSubType (7 bits)
 
    @Test
    public void psmActivitySubTypePolice() {
        // 0x40 = 0100 0000 -> bit 1 -> policeAndTrafficOfficers
        Assert.assertEquals(
            "activitySubType: { policeAndTrafficOfficers }",
            PSMBitStringProcessor.processPSMBitStrings("activitySubType: 40 (0 bits unused)"));
    }
 
    @Test
    public void psmActivitySubTypeHighwayService() {
        // 0x02 = 0000 0010 -> bit 6 -> highwayServiceVehiclePersonnel
        Assert.assertEquals(
            "activitySubType: { highwayServiceVehiclePersonnel }",
            PSMBitStringProcessor.processPSMBitStrings("activitySubType: 02 (0 bits unused)"));
    }
 
    @Test
    public void psmActivitySubTypeNoBitsSet() {
        Assert.assertEquals(
            "activitySubType: { }",
            PSMBitStringProcessor.processPSMBitStrings("activitySubType: 00 (0 bits unused)"));
    }
 
    // assistType — PersonalAssistive (6 bits)
 
    @Test
    public void psmAssistTypeVision() {
        // 0x20 = 0010 0000 -> bit 2 -> vision
        Assert.assertEquals(
            "assistType: { vision }",
            PSMBitStringProcessor.processPSMBitStrings("assistType: 20 (0 bits unused)"));
    }
 
    @Test
    public void psmAssistTypeCognition() {
        // 0x04 = 0000 0100 -> bit 5 -> cognition
        Assert.assertEquals(
            "assistType: { cognition }",
            PSMBitStringProcessor.processPSMBitStrings("assistType: 04 (0 bits unused)"));
    }
 
    @Test
    public void psmAssistTypeHearingAndMovement() {
        // 0x18 = 0001 1000 -> bits 3,4 -> hearing, movement
        Assert.assertEquals(
            "assistType: { hearing movement }",
            PSMBitStringProcessor.processPSMBitStrings("assistType: 18 (0 bits unused)"));
    }
 
    @Test
    public void psmAssistTypeNoBitsSet() {
        Assert.assertEquals(
            "assistType: { }",
            PSMBitStringProcessor.processPSMBitStrings("assistType: 00 (0 bits unused)"));
    }
 
    @Test
    public void psmLowercaseHexIsParsed() {
        // Lowercase hex 'f8' -> bits 0-4 set -> all 5 sizing names
        Assert.assertEquals(
            "sizing: { unavailable smallStature largeStature erraticMoving slowMoving }",
            PSMBitStringProcessor.processPSMBitStrings("sizing: f8 (0 bits unused)"));
    }
 
    @Test
    public void psmSingularBitWordIsAccepted() {
        Assert.assertEquals(
            "assistType: { vision }",
            PSMBitStringProcessor.processPSMBitStrings("assistType: 20 (1 bit unused)"));
    }
 
    @Test
    public void psmSurroundingTextIsPreserved() {
        Assert.assertEquals(
            "prefix assistType: { vision } suffix",
            PSMBitStringProcessor.processPSMBitStrings(
                "prefix assistType: 20 (0 bits unused) suffix"));
    }
 
    @Test
    public void psmAllFiveFieldsInOneString() {
        String input =
            "useState: 40 (0 bits unused)\n" +
            "sizing: 40 (0 bits unused)\n" +
            "activityType: 40 (0 bits unused)\n" +
            "activitySubType: 40 (0 bits unused)\n" +
            "assistType: 20 (0 bits unused)";
        String result = PSMBitStringProcessor.processPSMBitStrings(input);
        Assert.assertTrue(result.contains("useState: { other }"));
        Assert.assertTrue(result.contains("sizing: { smallStature }"));
        Assert.assertTrue(result.contains("activityType: { workingOnRoad }"));
        Assert.assertTrue(result.contains("activitySubType: { policeAndTrafficOfficers }"));
        Assert.assertTrue(result.contains("assistType: { vision }"));
    }
 
    @Test
    public void psmNonBitStringFieldsAreUntouched() {
        // basicType is ENUMERATED, crossRequest is BOOLEAN — must not be altered
        String input =
            "basicType: 1 (aPEDESTRIAN)\n" +
            "crossRequest: TRUE\n" +
            "assistType: 20 (0 bits unused)";
        String result = PSMBitStringProcessor.processPSMBitStrings(input);
        Assert.assertTrue(result.contains("basicType: 1 (aPEDESTRIAN)"));
        Assert.assertTrue(result.contains("crossRequest: TRUE"));
        Assert.assertTrue(result.contains("assistType: { vision }"));
    }
 
    @Test
    public void psmRecognizedAndUnrecognizedFieldsMixed() {
        String input = "speed: 80 (4 bits unused); assistType: 20 (0 bits unused)";
        String expected = "speed: 80 (4 bits unused); assistType: { vision }";
        Assert.assertEquals(expected, PSMBitStringProcessor.processPSMBitStrings(input));
    }

    // TIMBitStringProcessor
 
    @Test
    public void timNullInputReturnsNull() {
        Assert.assertNull(TIMBitStringProcessor.processTIMBitStrings(null));
    }
 
    @Test
    public void timEmptyStringReturnsEmptyString() {
        Assert.assertEquals("", TIMBitStringProcessor.processTIMBitStrings(""));
    }
 
    @Test
    public void timTextWithoutBitStringIsUnchanged() {
        String input = "TravelerInformation ::= { msgCnt: 1 } nothing to decode here";
        Assert.assertEquals(input, TIMBitStringProcessor.processTIMBitStrings(input));
    }
 
    @Test
    public void timUnrecognizedFieldNameIsLeftUnchanged() {
        String input = "speed: 80 (4 bits unused)";
        Assert.assertEquals(input, TIMBitStringProcessor.processTIMBitStrings(input));
    }
 
    // viewAngle — HeadingSlice (16 bits), RoadSignID
    @Test
    public void timViewAngleNorthSector() {
        // 0x80 0x00 -> bit 0 -> from000-0to022-5degrees
        Assert.assertEquals(
            "viewAngle: { from000-0to022-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: 80 00 (0 bits unused)"));
    }
 
    @Test
    public void timViewAngleTwoSectors() {
        // 0xC0 0x00 -> bits 0,1 -> from000-0to022-5 and from022-5to045-0
        Assert.assertEquals(
            "viewAngle: { from000-0to022-5degrees from022-5to045-0degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: C0 00 (0 bits unused)"));
    }
 
    @Test
    public void timViewAngleLastSector() {
        // 0x00 0x01 -> bit 15 -> from337-5to360-0degrees
        Assert.assertEquals(
            "viewAngle: { from337-5to360-0degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: 00 01 (0 bits unused)"));
    }
 
    @Test
    public void timViewAngleAllSixteenSectors() {
        // FF FF -> all 16 bits set
        String result = TIMBitStringProcessor.processTIMBitStrings(
            "viewAngle: FF FF (0 bits unused)");
        Assert.assertTrue(result.contains("from000-0to022-5degrees"));
        Assert.assertTrue(result.contains("from337-5to360-0degrees"));
        Assert.assertEquals(16, countOccurrences(result, "degrees"));
    }
 
    @Test
    public void timViewAngleNoBitsSet() {
        Assert.assertEquals(
            "viewAngle: { }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: 00 00 (0 bits unused)"));
    }
 
    @Test
    public void timViewAngleNoUnusedBitsSuffix() {
        // ASN1c omits the unused-bits clause when SIZE is exact (HeadingSlice is SIZE 16)
        Assert.assertEquals(
            "viewAngle: { from000-0to022-5degrees from022-5to045-0degrees "
                + "from045-0to067-5degrees from067-5to090-0degrees "
                + "from090-0to112-5degrees from112-5to135-0degrees "
                + "from337-5to360-0degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: FC 01"));
    }
 
    @Test
    public void timViewAngleWithUnusedBits() {
        // 0x80 0x00 (1 bit unused) -> 15 live bits; only bit 0 set
        Assert.assertEquals(
            "viewAngle: { from000-0to022-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: 80 00 (1 bit unused)"));
    }
 
    // direction — HeadingSlice (16 bits), GeographicalPath / ValidRegion
    @Test
    public void timDirectionNorthSector() {
        Assert.assertEquals(
            "direction: { from000-0to022-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("direction: 80 00 (0 bits unused)"));
    }
 
    @Test
    public void timDirectionSouthSector() {
        // 0x00 0x80 -> bit 8 -> from180-0to202-5degrees
        Assert.assertEquals(
            "direction: { from180-0to202-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("direction: 00 80 (0 bits unused)"));
    }
 
    @Test
    public void timDirectionWestSector() {
        // 0x00 0x08 -> bit 12 -> from270-0to292-5degrees
        Assert.assertEquals(
            "direction: { from270-0to292-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("direction: 00 08 (0 bits unused)"));
    }
 
    @Test
    public void timDirectionNoBitsSet() {
        Assert.assertEquals(
            "direction: { }",
            TIMBitStringProcessor.processTIMBitStrings("direction: 00 00 (0 bits unused)"));
    }
 
    @Test
    public void timDirectionNoUnusedBitsSuffix() {
        // Mirrors the real decoded output observed from ASN1c
        Assert.assertEquals(
            "direction: { from000-0to022-5degrees from022-5to045-0degrees "
                + "from045-0to067-5degrees from067-5to090-0degrees "
                + "from090-0to112-5degrees from112-5to135-0degrees "
                + "from337-5to360-0degrees }",
            TIMBitStringProcessor.processTIMBitStrings("direction: FC 01"));
    }
 
    // heading — HeadingSlice (16 bits), EventDescription
    @Test
    public void timHeadingNorthSector() {
        Assert.assertEquals(
            "heading: { from000-0to022-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("heading: 80 00 (0 bits unused)"));
    }
 
    @Test
    public void timHeadingLastSector() {
        Assert.assertEquals(
            "heading: { from337-5to360-0degrees }",
            TIMBitStringProcessor.processTIMBitStrings("heading: 00 01 (0 bits unused)"));
    }
 
    @Test
    public void timHeadingNoBitsSet() {
        Assert.assertEquals(
            "heading: { }",
            TIMBitStringProcessor.processTIMBitStrings("heading: 00 00 (0 bits unused)"));
    }
 
    // directions — HeadingSlice (16 bits), ProbeDataManagement
    @Test
    public void timDirectionsTwoSectors() {
        Assert.assertEquals(
            "directions: { from000-0to022-5degrees from022-5to045-0degrees }",
            TIMBitStringProcessor.processTIMBitStrings("directions: C0 00 (0 bits unused)"));
    }
 
    @Test
    public void timDirectionsNoBitsSet() {
        Assert.assertEquals(
            "directions: { }",
            TIMBitStringProcessor.processTIMBitStrings("directions: 00 00 (0 bits unused)"));
    }
 
    @Test
    public void timLowercaseHexIsParsed() {
        Assert.assertEquals(
            "viewAngle: { from000-0to022-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: 80 00 (0 bits unused)"));
    }
 
    @Test
    public void timSingularBitWordIsAccepted() {
        Assert.assertEquals(
            "viewAngle: { from000-0to022-5degrees }",
            TIMBitStringProcessor.processTIMBitStrings("viewAngle: 80 00 (1 bit unused)"));
    }
 
    @Test
    public void timSurroundingTextIsPreserved() {
        Assert.assertEquals(
            "prefix viewAngle: { from000-0to022-5degrees } suffix",
            TIMBitStringProcessor.processTIMBitStrings(
                "prefix viewAngle: 80 00 (0 bits unused) suffix"));
    }
 
    @Test
    public void timAllFourFieldsInOneString() {
        String input =
            "viewAngle: C0 00 (0 bits unused)\n" +
            "direction: 80 00 (0 bits unused)\n" +
            "heading: 00 80 (0 bits unused)\n" +
            "directions: FF FF (0 bits unused)";
        String result = TIMBitStringProcessor.processTIMBitStrings(input);
        Assert.assertTrue(result.contains(
            "viewAngle: { from000-0to022-5degrees from022-5to045-0degrees }"));
        Assert.assertTrue(result.contains(
            "direction: { from000-0to022-5degrees }"));
        Assert.assertTrue(result.contains(
            "heading: { from180-0to202-5degrees }"));
        Assert.assertEquals(16, countOccurrences(
            result.substring(result.indexOf("directions:")), "degrees"));
    }
 
    @Test
    public void timDirectionalityEnumeratedIsNotTouched() {
        // directionality is DirectionOfUse (ENUMERATED) — must not be altered
        String input = "directionality: 1 (forward)";
        Assert.assertEquals(input, TIMBitStringProcessor.processTIMBitStrings(input));
    }
 
    @Test
    public void timNonBitStringContentIsUntouched() {
        String input =
            "TravelerInformation ::= {\n" +
            "  msgCnt: 42\n" +
            "  viewAngle: C0 00 (0 bits unused)\n" +
            "  url: \"http://example.com\"\n" +
            "}";
        String result = TIMBitStringProcessor.processTIMBitStrings(input);
        Assert.assertTrue(result.contains("msgCnt: 42"));
        Assert.assertTrue(result.contains("url: \"http://example.com\""));
        Assert.assertTrue(result.contains(
            "viewAngle: { from000-0to022-5degrees from022-5to045-0degrees }"));
    }
 
    @Test
    public void timRecognizedAndUnrecognizedFieldsMixed() {
        String input = "speed: 80 (4 bits unused); viewAngle: 80 00 (0 bits unused)";
        String expected = "speed: 80 (4 bits unused); viewAngle: { from000-0to022-5degrees }";
        Assert.assertEquals(expected, TIMBitStringProcessor.processTIMBitStrings(input));
    }
 
    // =========================================================================
    // Helper
    // =========================================================================
 
    private static int countOccurrences(String text, String token) {
        int count = 0;
        int idx = 0;

        while ((idx = text.indexOf(token, idx)) != -1) {
            count++;
            idx += token.length();
        }

        return count;
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