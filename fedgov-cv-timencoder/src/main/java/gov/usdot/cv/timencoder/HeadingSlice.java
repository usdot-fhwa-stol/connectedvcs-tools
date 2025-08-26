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

public class HeadingSlice {
    private short headingSliceValue;

    // Constants for choice field (matches ASN.1 e_HeadingSlice values 0..15)
    public static final short FROM_000_0_TO_022_5_DEGREES = 0;
    public static final short FROM_022_5_TO_045_0_DEGREES = 1;
    public static final short FROM_045_0_TO_067_5_DEGREES = 2;
    public static final short FROM_067_5_TO_090_0_DEGREES = 3;
    public static final short FROM_090_0_TO_112_5_DEGREES = 4;
    public static final short FROM_112_5_TO_135_0_DEGREES = 5;
    public static final short FROM_135_0_TO_157_5_DEGREES = 6;
    public static final short FROM_157_5_TO_180_0_DEGREES = 7;
    public static final short FROM_180_0_TO_202_5_DEGREES = 8;
    public static final short FROM_202_5_TO_225_0_DEGREES = 9;
    public static final short FROM_225_0_TO_247_5_DEGREES = 10;
    public static final short FROM_247_5_TO_270_0_DEGREES = 11;
    public static final short FROM_270_0_TO_292_5_DEGREES = 12;
    public static final short FROM_292_5_TO_315_0_DEGREES = 13;
    public static final short FROM_315_0_TO_337_5_DEGREES = 14;
    public static final short FROM_337_5_TO_360_0_DEGREES = 15;

    public HeadingSlice() {
    }

    public HeadingSlice(short headingSliceValue) {
        this.headingSliceValue = headingSliceValue;
    }

    public short getHeadingSliceValue() {
        return headingSliceValue;
    }

    public void setHeadingSliceValue(short headingSliceValue) {
        this.headingSliceValue = headingSliceValue;
    }

    @Override
    public String toString() {
        return "HeadingSlice{" +
                "headingSliceValue=" + headingSliceValue +
                '}';
    }
}
