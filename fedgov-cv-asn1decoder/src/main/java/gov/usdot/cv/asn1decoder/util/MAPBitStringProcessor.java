/*
 * Copyright (C) 2026 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package gov.usdot.cv.asn1decoder.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

public class MAPBitStringProcessor {

    /**
     * AllowedManeuvers bit names (index = bit position)
     */
    private static final String[] MANEUVER_NAMES = {
        "maneuverStraightAllowed",          // 0
        "maneuverLeftAllowed",              // 1
        "maneuverRightAllowed",             // 2
        "maneuverUTurnAllowed",             // 3
        "maneuverLeftTurnOnRedAllowed",     // 4
        "maneuverRightTurnOnRedAllowed",    // 5
        "maneuverLaneChangeAllowed",        // 6
        "maneuverNoStoppingAllowed",        // 7
        "yieldAllwaysRequired",             // 8
        "goWithHalt",                       // 9
        "caution",                          // 10
        "reserved1"                         // 11
    };

    /**
     * Barrier bit names 
     */
    private static final String[] BARRIER_NAMES = {
        "median-RevocableLane",          // 0
        "median",                         // 1
        "whiteLineHashing",              // 2
        "stripedLines",                 // 3
        "doubleStripedLines",             // 4
        "trafficCones",                 // 5
        "constructionBarrier",          // 6
        "trafficChannels",             // 7
        "lowCurbs",                      // 8
        "highCurbs"                   // 9
    };

    /**
     * Bike bit names
     */
    private static final String[] BIKE_NAMES = {
        "bikeRevocableLane",                      // 0
        "pedestrianUseAllowed",                      // 1
        "isBikeFlyOverLane",                    // 2
        "fixedCycleTime",                      // 3
        "biDirectionalCycleTimes",                      // 4
        "isolatedByBarrier",                     // 5
        "unsignalizedSegmentsPresent"                         // 6
    };

    private static final String[] CROSSWALK_NAMES = {
        "crosswalkRevocableLane",                      // 0
        "bicycleUseAllowed",                      // 1
        "isXwalkFlyOverLane",                    // 2
        "fixedCycleTime",                      // 3
        "biDirectionalCycleTimes",                      // 4
        "hasPushToWalkButton",                     // 5
        "audioSupport",                         // 6
        "rfSignalRequestPresent",                         // 7
        "unsignalizedSegmentsPresent"                         // 8
    };

    private static final String[] PARKING_NAMES = {
        "parkingRevocableLane",                      // 0
        "parallelParkingInUse",                    // 1
        "headInParkingInUse",                      // 2
        "doNotParkZone",                      // 3
        "parkingForBusUse",                         // 4
        "parkingForTaxiUse",                         // 5
        "noPublicParkingUse"                  // 6
    };

    private static final String[] SIDEWALK_NAMES = {
        "sidewalk-RevocableLane",                      // 0
        "bicycleUseAllowed",                      // 1
        "isSidewalkFlyOverLane",                    // 2
        "walkBikes"                     // 3
    };

    private static final String[] STRIPING_NAMES = {
        "stripeToConnectingLanesRevocableLane",
        "stripeDrawOnLeft",
        "stripeDrawOnRight",
        "stripeToConnectingLanesLeft",
        "stripeToConnectingLanesRight",
        "stripeToConnectingLanesAhead",
    };

    private static final String[] TRACKED_VEHICLE_NAMES = {
        "spec-RevocableLane",                      // 0
        "spec-commuterRailRoadTrack",                    // 1
        "spec-lightRailRoadTrack",                      // 2
        "spec-heavyRailRoadTrack",                      // 3
        "spec-otherRailType"                         // 4
    };

    private static final String[] VEHICLE_NAMES = {
        "isVehicleRevocableLane",                      // 0
        "isVehicleFlyOverLane",                    // 1
        "hovLaneUseOnly",                      // 2
        "restrictedToBusUse",                      // 3
        "restrictedToTaxiUse",                      // 4
        "restrictedFromPublicUse",                         // 5
        "hasIRbeaconCoverage",                         // 6
        "permissionOnRequest"                         // 7
    };

    private static final String[] LANE_DIRECTION_NAMES = {
        "ingressPath",
        "egressPath"
    };

    private static final String[] LANE_SHARING_NAMES = {
        "overlappingLaneDescriptionProvided",
        "multipleLanesTreatedAsOneLane",
        "otherNonMoterizedTrafficTypes",
        "busVehicleTraffic",
        "taxiVehicleTraffic",
        "pedestriansTraffic",
        "cyclistVehicleTraffic",
        "trackedVehicleTraffic",
        "reserved"
    };

    private static final Map<String, String[]> FIELD_TO_NAMES = new HashMap<>();
    static {
        FIELD_TO_NAMES.put("maneuvers",      MANEUVER_NAMES);
        FIELD_TO_NAMES.put("maneuver",       MANEUVER_NAMES);
        FIELD_TO_NAMES.put("directionalUse", LANE_DIRECTION_NAMES);
        FIELD_TO_NAMES.put("sharedWith",     LANE_SHARING_NAMES);
        FIELD_TO_NAMES.put("vehicle",        VEHICLE_NAMES);
        FIELD_TO_NAMES.put("crosswalk",      CROSSWALK_NAMES);
        FIELD_TO_NAMES.put("bikeLane",       BIKE_NAMES);
        FIELD_TO_NAMES.put("sidewalk",       SIDEWALK_NAMES);
        FIELD_TO_NAMES.put("median",         BARRIER_NAMES);
        FIELD_TO_NAMES.put("striping",       STRIPING_NAMES);
        FIELD_TO_NAMES.put("trackedVehicle", TRACKED_VEHICLE_NAMES);
        FIELD_TO_NAMES.put("parking",        PARKING_NAMES);
    };

    private static final Pattern BIT_STRING_PATTERN = Pattern.compile(
        "\\b(maneuvers|maneuver|directionalUse|sharedWith|vehicle|crosswalk|bikeLane|" +
        "sidewalk|median|striping|trackedVehicle|parking)" +
        ":\\s*([0-9A-Fa-f]{2}(?:[ \\t]++[0-9A-Fa-f]{2})*+)" +
        "(?:\\s*\\((\\d+)\\s*bits?\\s*unused\\))?"
    );

    public static String processMapBitStrings(String decoded) {
        if (decoded == null) return null;

        Matcher m = BIT_STRING_PATTERN.matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String hex       = m.group(2).trim();
            int unusedBits   = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
            String[] names   = FIELD_TO_NAMES.get(fieldName);
 
            if (names == null) {
                // Regex said yes but dispatch table says no — leave unchanged.
                m.appendReplacement(out, Matcher.quoteReplacement(m.group(0)));
                continue;
            }
 
            String readable = BitStringUtil.decodeBitString(hex, unusedBits, names);
            m.appendReplacement(out, Matcher.quoteReplacement(fieldName + ": " + readable));
        }
        m.appendTail(out);
        return out.toString();
    }
 
 
    private MAPBitStringProcessor() { /* utility class */ }
}   