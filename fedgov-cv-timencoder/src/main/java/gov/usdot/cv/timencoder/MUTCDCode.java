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

public class MUTCDCode {
    private long value;

	public static final long none = 0;
	public static final long regulatory = 1;
	public static final long warning = 2;
	public static final long maintenance = 3;
	public static final long motoristService = 4;
	public static final long guide = 5;
	public static final long rec = 6;

    public MUTCDCode(long value) {
        this.value = value;
    }    

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MUTCDCode{" +
                "value=" + value +
                '}';
    }
}
