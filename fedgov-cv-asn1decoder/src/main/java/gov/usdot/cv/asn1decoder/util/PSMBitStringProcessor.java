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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PSMBitStringProcessor {
    /**
     * PersonalDeviceUsageState — 9 bits.
     * All bits zero = unknown state.
     */
    private static final String[] PERSONAL_DEVICE_USAGE_STATE_NAMES = {
        "unavailable",      // 0 — Not specified
        "other",            // 1 — States not defined below
        "idle",             // 2 — Human not interacting with device
        "listeningToAudio", // 3 — Any audio source other than calling
        "typing",           // 4 — Texting, entering addresses, manual input
        "calling",          // 5
        "playingGames",     // 6
        "reading",          // 7
        "viewing"           // 8 — Watching dynamic content
    };

    /**
     * UserSizeAndBehaviour — 5 bits.
     */
    private static final String[] USER_SIZE_AND_BEHAVIOUR_NAMES = {
        "unavailable",   // 0
        "smallStature",  // 1 — less than 150 cm high
        "largeStature",  // 2
        "erraticMoving", // 3
        "slowMoving"     // 4 — those who move a bit slowly
    };

    /**
     * PublicSafetyAndRoadWorkerActivity — 6 bits.
     */
    private static final String[] PUBLIC_SAFETY_ACTIVITY_NAMES = {
        "unavailable",        // 0 — Not specified
        "workingOnRoad",      // 1 — Road workers on foot in or out of a closure
        "settingUpClosures",  // 2 — Setting up signs, cones, flares etc.
        "respondingToEvents", // 3 — Treating injured, fires, spills, disabled vehicles
        "directingTraffic",   // 4 — Directing traffic at signal outage, construction etc.
        "otherActivities"     // 5 — Designated by regional authorities
    };

    /**
     * PublicSafetyDirectingTrafficSubType — 7 bits.
     */
    private static final String[] DIRECTING_TRAFFIC_SUBTYPE_NAMES = {
        "unavailable",                          // 0 — Default / unknown
        "policeAndTrafficOfficers",             // 1 — Law enforcement / traffic control officers
        "trafficControlPersons",                // 2 — Road workers with special directing equipment
        "railroadCrossingGuards",               // 3 — Crossing guards at private roads/driveways
        "civilDefenseNationalGuardMilitaryPolice", // 4 — During regular duties or emergencies
        "emergencyOrganizationPersonnel",       // 5 — Fire, hospital, river rescue, ambulance
        "highwayServiceVehiclePersonnel"        // 6 — Tow trucks and road service vehicles
    };

    /**
     * PersonalAssistive — 6 bits.
     */
    private static final String[] PERSONAL_ASSISTIVE_NAMES = {
        "unavailable", // 0
        "otherType",   // 1
        "vision",      // 2
        "hearing",     // 3
        "movement",    // 4
        "cognition"    // 5
    };

    /** PSM field name → bit-name array */
    private static final Map<String, String[]> FIELD_TO_NAMES = new HashMap<>();
    static {
        FIELD_TO_NAMES.put("useState",       PERSONAL_DEVICE_USAGE_STATE_NAMES);
        FIELD_TO_NAMES.put("sizing",         USER_SIZE_AND_BEHAVIOUR_NAMES);
        FIELD_TO_NAMES.put("activityType",   PUBLIC_SAFETY_ACTIVITY_NAMES);
        FIELD_TO_NAMES.put("activitySubType",DIRECTING_TRAFFIC_SUBTYPE_NAMES);
        FIELD_TO_NAMES.put("assistType",     PERSONAL_ASSISTIVE_NAMES);
    }

    private static final Pattern BIT_STRING_PATTERN = Pattern.compile(
        "\\b(useState|sizing|activityType|activitySubType|assistType)" +
        ":\\s?([0-9A-Fa-f]{2}(?:[ \\t][0-9A-Fa-f]{2})*)" +
        "(?:\\s?\\((\\d+)\\s?bits?\\s?unused\\))?"
    );

    public static String processPSMBitStrings(String decoded) {
        if (decoded == null) {
            return null;
        }

        Matcher m = BIT_STRING_PATTERN.matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String hex       = m.group(2).trim();
            int unusedBits   = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
            String[] names   = FIELD_TO_NAMES.get(fieldName);

            if (names == null) {
                // Regex matched but dispatch table has no entry — leave unchanged.
                m.appendReplacement(out, Matcher.quoteReplacement(m.group(0)));
                continue;
            }

            String readable = BitStringUtil.decodeBitString(hex, unusedBits, names);
            m.appendReplacement(out, Matcher.quoteReplacement(fieldName + ": " + readable));
        }
        m.appendTail(out);
        return out.toString();
    }

    private PSMBitStringProcessor() { /* utility class */ }
}