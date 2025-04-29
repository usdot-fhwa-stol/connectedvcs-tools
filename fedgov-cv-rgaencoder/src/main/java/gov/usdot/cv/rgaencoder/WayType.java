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

public class WayType {
    private long wayTypeValue;

    // Constants for choice field
    public static final long MOTOR_VEHICLE_LANE = 0;
    public static final long BICYCLE_LANE = 1;
    public static final long CROSSWALK_LANE = 2;

    // Constructors
    public WayType() {
    }

    public WayType(long wayTypeValue) {
        this.wayTypeValue = wayTypeValue;
    }

    // Getters and Setters
    public long getWayTypeValue() {
        return wayTypeValue;
    }

    public void setWayTypeValue(long wayTypeValue) {
        this.wayTypeValue = wayTypeValue;
    }

    @Override
    public String toString() {
        return "WayType{" +
                "wayTypeValue=" + wayTypeValue +
                '}';
    }
    
}
