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

public class SRMBitStringProcessor {
    /**
     * TransitVehicleStatus — SIZE(8), 6 named bits (bits 6-7 unused).
     */
    private static final String[] TRANSIT_VEHICLE_STATUS_NAMES = {
        "loading",    // 0 — parking and unable to move at this time
        "anADAuse",   // 1 — ADA access in progress (wheelchairs, kneeling, etc.)
        "aBikeLoad",  // 2 — loading of a bicycle is in progress
        "doorOpen",   // 3 — a vehicle door is open for passenger access
        "charging",   // 4 — vehicle is connected to a charging point
        "atStopLine"  // 5 — vehicle is at the stop line for the lane it is in
    };

    /** SRM field name → bit-name array */
    private static final Map<String, String[]> FIELD_TO_NAMES = new HashMap<>();
    static {
        FIELD_TO_NAMES.put("transitStatus", TRANSIT_VEHICLE_STATUS_NAMES);
    }

    private static final Pattern BIT_STRING_PATTERN = Pattern.compile(
        "\\b(transitStatus)" +
        ":\\s?([0-9A-Fa-f]{2}(?:[ \\t][0-9A-Fa-f]{2})*)" +
        "(?:\\s?\\((\\d+)\\s?bits?\\s?unused\\))?"
    );


    public static String processSRMBitStrings(String decoded) {
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
                m.appendReplacement(out, Matcher.quoteReplacement(m.group(0)));
                continue;
            }

            String readable = BitStringUtil.decodeBitString(hex, unusedBits, names);
            m.appendReplacement(out, Matcher.quoteReplacement(fieldName + ": " + readable));
        }
        m.appendTail(out);
        return out.toString();
    }

    private SRMBitStringProcessor() { /* utility class */ }
}