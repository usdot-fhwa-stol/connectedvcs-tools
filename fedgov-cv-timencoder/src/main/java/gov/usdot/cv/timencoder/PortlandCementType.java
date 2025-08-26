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
public enum PortlandCementType {
    PortlandCementType_newSharp	(0),
    PortlandCementType_traveled	(1),
    PortlandCementType_trafficPolished(2);

    private final int value;

    PortlandCementType(int value) {
        this.value = value;
    }

    /** ASN.1 enumerated numeric value */
    public int intValue() {
        return value;
    }

    /** Map numeric value -> enum (throws if unknown) */
    public static PortlandCementType fromInt(int v) {
        switch (v) {
            case 0: return PortlandCementType_newSharp;
            case 1: return PortlandCementType_traveled;
            case 2: return PortlandCementType_trafficPolished;
            default:
                throw new IllegalArgumentException("Unknown PortlandCementType value: " + v);
        }
    }

    @Override
    public String toString() {
        return "PortlandCementType{" +
                "value=" + value +
                ", name=" + name() +
                '}';
    }
}
