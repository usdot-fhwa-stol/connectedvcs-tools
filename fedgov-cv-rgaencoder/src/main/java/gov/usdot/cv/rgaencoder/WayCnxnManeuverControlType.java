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

public class WayCnxnManeuverControlType {

    private int choice;
    private int signalizedControl;
    private UnsignalizedMovementStates unsignalizedControl;
    private int uncontrolled;

    // Constants to represent the choices
    public static final int SIGNALIZED_CONTROL = 1;
    public static final int UNSIGNALIZED_CONTROL = 2;
    public static final int UNCONTROLLED = 3;

    public WayCnxnManeuverControlType() {
    }

    public WayCnxnManeuverControlType(int choice, int signalizedControl,
    UnsignalizedMovementStates unsignalizedControl, int uncontrolled) {
        this.choice = choice;
        this.signalizedControl = signalizedControl;
        this.unsignalizedControl = unsignalizedControl;
        this.uncontrolled = uncontrolled;
    }

    public int getChoice() {
        return choice;
    }

    public void setChoice(int choice) {
        this.choice = choice;
    }

    public int getSignalizedControl() {
        return signalizedControl;
    }

    public void setSignalizedControl(int signalizedControl) {
        this.signalizedControl = signalizedControl;
    }

    public UnsignalizedMovementStates getUnsignalizedMovementStates() {
        return unsignalizedControl;
    }

    public void setUnsignalizedMovementStates(UnsignalizedMovementStates unsignalizedControl) {
        this.unsignalizedControl = unsignalizedControl;
    }

    public int getUncontrolled() {
        return uncontrolled;
    }

    public void setUncontrolled(int uncontrolled) {
        this.uncontrolled = uncontrolled;
    }

    @Override
    public String toString() {
        return "WayCnxnManeuverControlType [signalizedControl=" + signalizedControl + ", unsignalizedControl="
                + unsignalizedControl + ", uncontrolled=" + uncontrolled + "]";
    }
    
    
}
