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

public enum DirectionOfUse {

    unavailable(0),
    forward(1),
    reverse(2),
    both(3);

    private final int value;

    DirectionOfUse(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DirectionOfUse valueOf(int value) {
        for (DirectionOfUse direction : DirectionOfUse.values()) {
            if (direction.getValue() == value) {
                return direction;
            }
        }
        throw new IllegalArgumentException("Invalid DirectionOfUse value: " + value);
    }

    @Override
    public String toString() {
        return "DirectionOfUse{" +
                "value=" + value +
                '}';
    }
}
