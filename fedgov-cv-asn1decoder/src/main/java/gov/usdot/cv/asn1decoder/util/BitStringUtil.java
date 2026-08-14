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

/**
 * Shared decoding logic for ASN1c BIT STRING post-processing.
 */
class BitStringUtil {

    /**
     * Converts a space-separated hex byte string with a trailing unused-bit
     * count into a flag list, MSB-first.
     */
    static String decodeBitString(String hex, int unusedBits, String[] names) {
        if (hex.isEmpty()) {
            return "{ }";
        }

        String[] bytesStr = hex.split("\\s+");
        byte[] bytes = new byte[bytesStr.length];
        for (int i = 0; i < bytesStr.length; i++) {
            bytes[i] = (byte) Integer.parseInt(bytesStr[i], 16);
        }

        int totalBits = bytes.length * 8 - unusedBits;

        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < totalBits && i < names.length; i++) {
            int byteIndex = i / 8;
            int bitIndex  = 7 - (i % 8); // MSB-first
            if (((bytes[byteIndex] >> bitIndex) & 1) == 1) {
                sb.append(names[i]).append(' ');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    private BitStringUtil() { /* utility class */ }
}