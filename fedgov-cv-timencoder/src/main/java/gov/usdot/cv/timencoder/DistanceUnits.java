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

public class DistanceUnits {
    private int value;

    public static final long centimeter = 0;
	public static final long cm2_5 = 1;
	public static final long decimeter = 2;
	public static final long meter = 3;
	public static final long kilometer = 4;
	public static final long foot = 5;
	public static final long yard = 6;
	public static final long mile = 7;

    public DistanceUnits(int value) {
        this.value = value;
    }    

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DistanceUnits{" +
                "value=" + value +
                '}';
    }
}
