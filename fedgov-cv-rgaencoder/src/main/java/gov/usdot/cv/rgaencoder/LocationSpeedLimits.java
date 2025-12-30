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

public class LocationSpeedLimits {
    private NodeIndexOffset location;
    private SpeedLimitInfo speedLimitInfo;

    public LocationSpeedLimits() {
    }

    public LocationSpeedLimits(NodeIndexOffset location, SpeedLimitInfo speedLimitInfo) {
        this.location = location;
        this.speedLimitInfo = speedLimitInfo;
    }

    public NodeIndexOffset getLocation() {
        return location;
    }

    public void setLocation(NodeIndexOffset location) {
        this.location = location;
    }

    public SpeedLimitInfo getSpeedLimitInfo() {
        return speedLimitInfo;
    }

    public void setSpeedLimitInfo(SpeedLimitInfo speedLimitInfo) {
        this.speedLimitInfo = speedLimitInfo;
    }

    @Override
    public String toString() {
        return "LocationSpeedLimits [location=" + location + ", speedLimitInfo=" + speedLimitInfo + "]";
    }
}
