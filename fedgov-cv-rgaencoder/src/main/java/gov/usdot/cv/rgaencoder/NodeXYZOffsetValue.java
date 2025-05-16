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

package gov.usdot.cv.rgaencoder;

public class NodeXYZOffsetValue {
    private int choice;
    private long offsetB10;
    private long offsetB11;
    private long offsetB12;
    private long offsetB13;
    private long offsetB14;
    private long offsetB16;

    // Constants for choice field
    public static final int OFFSET_B10 = 1;
    public static final int OFFSET_B11 = 2;
    public static final int OFFSET_B12 = 3;
    public static final int OFFSET_B13 = 4;
    public static final int OFFSET_B14 = 5;
    public static final int OFFSET_B16 = 6;

    public NodeXYZOffsetValue() {
        this.choice = -1;
        this.offsetB10 = 0;
        this.offsetB11 = 0;
        this.offsetB12 = 0;
        this.offsetB13 = 0;
        this.offsetB14 = 0;
        this.offsetB16 = 0;
    }

    public NodeXYZOffsetValue(int choice,
            long offsetB10,
            long offsetB11,
            long offsetB12,
            long offsetB13,
            long offsetB14,
            long offsetB16) {
        this.choice = choice;
        this.offsetB10 = offsetB10;
        this.offsetB11 = offsetB11;
        this.offsetB12 = offsetB12;
        this.offsetB13 = offsetB13;
        this.offsetB14 = offsetB14;
        this.offsetB16 = offsetB16;
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public long getOffsetB10() {
        return offsetB10;
    }

    public void setOffsetB10(long offsetB10) {
        this.offsetB10 = offsetB10;
    }

    public long getOffsetB11() {
        return offsetB11;
    }

    public void setOffsetB11(long offsetB11) {
        this.offsetB11 = offsetB11;
    }

    public long getOffsetB12() {
        return offsetB12;
    }

    public void setOffsetB12(long offsetB12) {
        this.offsetB12 = offsetB12;
    }

    public long getOffsetB13() {
        return offsetB13;
    }

    public void setOffsetB13(long offsetB13) {
        this.offsetB13 = offsetB13;
    }

    public long getOffsetB14() {
        return offsetB14;
    }

    public void setOffsetB14(long offsetB14) {
        this.offsetB14 = offsetB14;
    }

    public long getOffsetB16() {
        return offsetB16;
    }

    public void setOffsetB16(long offsetB16) {
        this.offsetB16 = offsetB16;
    }

    @Override
    public String toString() {
        return "NodeXYZOffsetValue{" +
                "choice=" + choice +
                ", offsetB10=" + offsetB10 +
                ", offsetB11=" + offsetB11 +
                ", offsetB12=" + offsetB12 +
                ", offsetB13=" + offsetB13 +
                ", offsetB14=" + offsetB14 +
                ", offsetB16=" + offsetB16 +
                '}';
    }
}
