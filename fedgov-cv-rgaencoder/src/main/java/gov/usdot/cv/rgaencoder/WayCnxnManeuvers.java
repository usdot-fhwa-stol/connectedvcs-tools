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

public class WayCnxnManeuvers {

    private long wayCnxnManeuversValue;

    // Constants for choice field
    public static final long STRAIGHT = 0;
    public static final long LEFT_TURN = 1;
    public static final long RIGHT_TURN = 2;
    public static final long LEFT_U_TURN = 3;
    public static final long RIGHT_U_TURN = 4;

    // Constructors
    public WayCnxnManeuvers() {
    }

    public WayCnxnManeuvers(long wayCnxnManeuversValue) {
        this.wayCnxnManeuversValue = wayCnxnManeuversValue;
    }

    // Getters and Setters
    public long getWayCnxnManeuvers() {
        return wayCnxnManeuversValue;
    }

    public void setWayCnxnManeuvers(long wayCnxnManeuversValue) {
        this.wayCnxnManeuversValue = wayCnxnManeuversValue;
    }

    @Override
    public String toString() {
        return "WayCnxnManeuvers{" +
                "wayCnxnManeuversValue=" + wayCnxnManeuversValue +
                '}';
    }


}
