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

public enum TravelerInfoType {
    UNKNOWN(0),
    ADVISORY(1),
    ROAD_SIGNAGE(2),
    COMMERCIAL_SIGNAGE(3);

    private final int value;

    TravelerInfoType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "TravelerInfoType{" +
                "value=" + value +
                '}';
    }
}
