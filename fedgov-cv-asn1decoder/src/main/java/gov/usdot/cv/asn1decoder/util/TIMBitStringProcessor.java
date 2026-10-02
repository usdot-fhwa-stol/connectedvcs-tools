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

/**
 * Post-processes the raw ASN1c text output of TIM (TravelerInformation) messages
 * to replace hex BIT STRING values with human-readable named-flag lists.
 *
 * Only the readability layer is changed; the original ASN1c output is preserved
 * in its entirety before this method is called).
 *
 */
public class TIMBitStringProcessor {

    // -----------------------------------------------------------------------
    // Named-bit array
    // -----------------------------------------------------------------------

    /**
     * HeadingSlice — 16 named bits covering 360° in 22.5° increments,
     * starting at North and moving clockwise (eastward).
     */
    private static final String[] HEADING_SLICE_NAMES = {
        "from000-0to022-5degrees",   //  0 — North
        "from022-5to045-0degrees",   //  1
        "from045-0to067-5degrees",   //  2
        "from067-5to090-0degrees",   //  3
        "from090-0to112-5degrees",   //  4 — East
        "from112-5to135-0degrees",   //  5
        "from135-0to157-5degrees",   //  6
        "from157-5to180-0degrees",   //  7
        "from180-0to202-5degrees",   //  8 — South
        "from202-5to225-0degrees",   //  9
        "from225-0to247-5degrees",   // 10
        "from247-5to270-0degrees",   // 11
        "from270-0to292-5degrees",   // 12 — West
        "from292-5to315-0degrees",   // 13
        "from315-0to337-5degrees",   // 14
        "from337-5to360-0degrees"    // 15
    };

    private static final Map<String, String[]> FIELD_TO_NAMES = new HashMap<>();
    static {
        FIELD_TO_NAMES.put("viewAngle",  HEADING_SLICE_NAMES);
        FIELD_TO_NAMES.put("direction",  HEADING_SLICE_NAMES);
        FIELD_TO_NAMES.put("heading",    HEADING_SLICE_NAMES);
        FIELD_TO_NAMES.put("directions", HEADING_SLICE_NAMES);
    }

    private static final Pattern TIM_BIT_STRING_PATTERN = Pattern.compile(
        "\\b(viewAngle|direction|heading|directions)" +
        ":\\s*([0-9A-Fa-f]{2}(?:[ \\t]++[0-9A-Fa-f]{2})*+)" +
        "(?:\\s*\\((\\d+)\\s*bits?\\s*unused\\))?"
    );

    public static String processTIMBitStrings(String decoded) {
        if (decoded == null) {
            return null;
        }

        Matcher m = TIM_BIT_STRING_PATTERN.matcher(decoded);
        StringBuffer out = new StringBuffer();
        while (m.find()) {
            String fieldName = m.group(1);
            String hex       = m.group(2).trim();
            int unusedBits = (m.group(3) != null) ? Integer.parseInt(m.group(3)) : 0;
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

    private TIMBitStringProcessor() { /* utility class */ }
}