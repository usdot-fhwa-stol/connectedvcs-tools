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

public class BSMBitStringProcessor {

    /**
     * ExteriorLights bit names 
     */
    private static final String[] EXTERIOR_LIGHTS = {
        "lowBeamHeadlightsOn",          // 0
        "highBeamHeadlightsOn",                         // 1
        "leftTurnSignalOn",              // 2
        "rightTurnSignalOn",                 // 3
        "hazardSignalOn",             // 4
        "automaticLightControlOn",                 // 5
        "daytimeRunningLightsOn",          // 6
        "fogLightsOn",             // 7
        "parkingLightsOn"                     // 8
    };

    /**
     * HeadingSlice bit names
     */
    private static final String[] HEADING_SLICE = {
        "from000-0to022-5degrees",                      // 0
        "from022-5to045-0degrees",                      // 1
        "from045-0to067-5degrees",                    // 2
        "from067-5to090-0degrees",                      // 3
        "from090-0to112-5degrees",                      // 4
        "from112-5to135-0degrees",                     // 5
        "from135-0to157-5degrees",                         // 6
        "from157-5to180-0degrees",                      // 7
        "from180-0to202-5degrees",                      // 8
        "from202-5to225-0degrees",                      // 9
        "from225-0to247-5degrees",                      // 10
        "from247-5to270-0degrees",                      // 11
        "from270-0to292-5degrees",                      // 12
        "from292-5to315-0degrees",                      // 13
        "from315-0to337-5degrees",                      // 14
        "from337-5to360-0degrees"                       // 15
    };

    private static final String[] VEHICLE_EVENT_FLAGS = {
        "eventHazardLights",                      // 0
        "eventStopLineViolation",                      // 1
        "eventABSactivated",                    // 2
        "eventTractionControlLoss",                      // 3
        "eventStabilityControlactivated",                      // 4
        "eventHazardousMaterials",                     // 5
        "eventReserved1",                         // 6
        "eventHardBraking",                         // 7
        "eventLightsChanged",                         // 8
        "eventWipersChanged",                         // 9
        "eventFlatTire",                         // 10
        "eventDisabledVehicle",                         // 11
        "eventAirBagDeployment",                         // 12
        "eventJackKnife"
    };

    private static final String[] BRAKE_APPLIED_STATUS = {
        "unavailable",
        "leftFront",
        "leftRear",
        "rightFront",
        "rightRear"
    };

    

    private static final Map<String, String[]> FIELD_TO_NAMES = new HashMap<>();
    static {
        FIELD_TO_NAMES.put("lights",      EXTERIOR_LIGHTS);
        FIELD_TO_NAMES.put("headingSlice",       HEADING_SLICE);
        FIELD_TO_NAMES.put("events", VEHICLE_EVENT_FLAGS);
        FIELD_TO_NAMES.put("wheelBrakes", BRAKE_APPLIED_STATUS);
    };

    private static final Pattern BIT_STRING_PATTERN = Pattern.compile(
        "\\b(lights|headingSlice|events|wheelBrakes)" +
        ":\\s*([0-9A-Fa-f]{1,2}(?:\\s++[0-9A-Fa-f]{1,2})*+)\\s*" +
        "\\((\\d+)\\s*bits?\\s*unused\\)"
    );

    public static String processBSMBitStrings(String decoded) {
        if (decoded == null) return null;
 
        Matcher m = BIT_STRING_PATTERN.matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String hex       = m.group(2).trim();
            int unusedBits   = Integer.parseInt(m.group(3));
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
 
 
    private BSMBitStringProcessor() { /* utility class */ }
}   