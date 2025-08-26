/*
 * Copyright (C) 2025 LEIDOS.
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
package gov.usdot.cv.timencoder;
public enum SnowType {
    // TODO: replace with actual members from SnowType.h
    // Example placeholders:
    PACKED(0),
    LOOSE(1),
    SLUSH(2);

    private final int value;

    SnowType(int value) { this.value = value; }

    /** ASN.1 enumerated numeric value */
    public int intValue() { return value; }

    /** Map numeric value -> enum (throws if unknown). */
    public static SnowType fromInt(int v) {
        switch (v) {
            // TODO: update cases to match real numeric values
            case 0: return PACKED;
            case 1: return LOOSE;
            case 2: return SLUSH;
            default:
                throw new IllegalArgumentException("Unknown SnowType value: " + v);
        }
    }

    @Override
    public String toString() {
        return "SnowType{value=" + value + ", name=" + name() + '}';
    }
}
