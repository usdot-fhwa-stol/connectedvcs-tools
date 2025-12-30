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

import java.util.ArrayList;
import java.util.List;

public class IndividualWaySpeedLimits {
    private int wayID; // Represents LaneID
    private List<LocationSpeedLimits> locationSpeedLimitSet;

    public IndividualWaySpeedLimits() {
        this.locationSpeedLimitSet = new ArrayList<>();
    }

    public IndividualWaySpeedLimits(int wayID, List<LocationSpeedLimits> locationSpeedLimitSet) {
        this.wayID = wayID;
        this.locationSpeedLimitSet = locationSpeedLimitSet;
    }

    public int getWayID() {
        return wayID;
    }

    public void setWayID(int wayID) {
        this.wayID = wayID;
    }

    public List<LocationSpeedLimits> getLocationSpeedLimitSet() {
        return locationSpeedLimitSet;
    }

    public void setLocationSpeedLimitSet(List<LocationSpeedLimits> locationSpeedLimitSet) {
        this.locationSpeedLimitSet = locationSpeedLimitSet;
    }

    public void addLocationSpeedLimits(LocationSpeedLimits locationSpeedLimits) {
        if (locationSpeedLimits != null) {
            this.locationSpeedLimitSet.add(locationSpeedLimits);
        }
    }

    @Override
    public String toString() {
        return "IndividualWaySpeedLimits [wayID=" + wayID + ", locationSpeedLimitSet=" + locationSpeedLimitSet + "]";
    } 
}
