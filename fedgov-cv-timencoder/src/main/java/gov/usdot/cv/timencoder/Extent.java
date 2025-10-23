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

public enum Extent {

    useInstantlyOnly(0),
    useFor3meters(1),
    useFor10meters(2),
    useFor50meters(3),
    useFor100meters(4),
    useFor500meters(5),
    useFor1000meters(6),
    useFor5000meters(7),
    useFor10000meters(8),
    useFor50000meters(9),
    useFor100000meters(10),
    useFor500000meters(11),
    useFor1000000meters(12),
    useFor5000000meters(13),
    useFor10000000meters(14),
    forever(15);

    private final int value;

    Extent(int value) {
        this.value = value;
    }    

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Extent{" +
                "value=" + value +
                '}';
    }
}
