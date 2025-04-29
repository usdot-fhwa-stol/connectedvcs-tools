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

public class WayWidth {
    private byte choice;
    private int fullWidth; // For FULL_WIDTH choice
    private int deltaWidth; // For DELTA_WIDTH choice

    // Constants to represent the choices
    public static final byte FULL_WIDTH = 0;
    public static final byte DELTA_WIDTH = 1;

    public WayWidth() {
        this.choice = -1;
        this.fullWidth = 0;
        this.deltaWidth = 0;
    }

    public byte getChoice() {
        return choice;
    }

    public void setChoice(byte choice) {
        this.choice = choice;
    }

    public int getFullWidth() {
        return fullWidth;
    }

    public void setFullWidth(int fullWidth) {
        this.fullWidth = fullWidth;
    }

    public int getDeltaWidth() {
        return deltaWidth;
    }

    public void setDeltaWidth(int deltaWidth) {
        this.deltaWidth = deltaWidth;
    }

    @Override
    public String toString() {
        if (choice == FULL_WIDTH) {
            return "WayWidth [choice=FULL_WIDTH, fullWidth=" + fullWidth + "]";
        } else if (choice == DELTA_WIDTH) {
            return "WayWidth [choice=DELTA_WIDTH, deltaWidth=" + deltaWidth + "]";
        } else {
            return "WayWidth [choice=UNKNOWN]";
        }
    }
}