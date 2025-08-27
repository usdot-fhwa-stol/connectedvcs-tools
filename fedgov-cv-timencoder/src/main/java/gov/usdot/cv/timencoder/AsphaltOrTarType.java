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
public enum AsphaltOrTarType {
    AsphaltOrTarType_newSharp(0),
    AsphaltOrTarType_traveled(1),
    AsphaltOrTarType_trafficPolished(2),
    AsphaltOrTarType_excessTar	(3);

    private final int value;

    AsphaltOrTarType(int value) {
        this.value = value;
    }

    /** Get the integer value of this enumerated type. */
    public int intValue() {
        return value;
    }

    /** Map numeric value -> enum (throws if unknown). */
    public static AsphaltOrTarType fromInt(int v) {
        switch (v) {
            case 0: return AsphaltOrTarType_newSharp;
            case 1: return AsphaltOrTarType_traveled;
            case 2: return AsphaltOrTarType_trafficPolished;
            case 3: return AsphaltOrTarType_excessTar;
            default:
                throw new IllegalArgumentException("Unknown AsphaltOrTarType value: " + v);
        }
    }

    @Override
    public String toString() {
        return "AsphaltOrTarType{" +
                "value=" + value +
                ", name=" + name() +
                '}';
    }
}
