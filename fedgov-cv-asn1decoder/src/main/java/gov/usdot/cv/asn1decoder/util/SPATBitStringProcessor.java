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

public class SPATBitStringProcessor {

    /**
     * IntersectionStatus bit names 
     */
    private static final String[] INTERSECTION_STATUS = {
        "manualControlIsEnabled",          // 0
        "stopTimeIsActivated",                         // 1
        "failureFlash",              // 2
        "preemptIsActive",                 // 3
        "signalPriorityIsActive",             // 4
        "fixedTimeOperation",                 // 5
        "trafficDependentOperation",          // 6
        "standbyOperation",             // 7
        "failureMode",                     // 8
        "off",                              // 9
        "recentMAPmessageUpdate",          // 10
        "recentChangeInMAPassignedLanesIDsUsed",  // 11
        "noValidMAPisAvailableAtThisTime",  // 12
        "noValidSPATisAvailableAtThisTime"  // 13
    };
    
    private static final Map<String, String[]> FIELD_TO_NAMES = new HashMap<>();
    static {
        FIELD_TO_NAMES.put("status", INTERSECTION_STATUS);
    };

    private static final Pattern BIT_STRING_PATTERN = Pattern.compile(
        "\\b(status)" +
        ":\\s*([0-9A-Fa-f]{2}(?:[ \\t]++[0-9A-Fa-f]{2})*+)[ \\t]*"
    );

    public static String processSPATBitStrings(String decoded) {
        if (decoded == null) return null;
 
        Matcher m = BIT_STRING_PATTERN.matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String hex       = m.group(2).trim();
            int unusedBits   = 0;
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
 
 
    private SPATBitStringProcessor() { /* utility class */ }
}   