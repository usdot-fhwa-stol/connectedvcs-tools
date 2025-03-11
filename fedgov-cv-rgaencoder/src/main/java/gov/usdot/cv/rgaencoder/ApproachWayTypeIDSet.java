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

import java.util.List;
import java.util.ArrayList;

public class ApproachWayTypeIDSet {
    private WayType wayType;
    private List<Long> wayIDSet;

    public ApproachWayTypeIDSet() {
        this.wayType = null;
        this.wayIDSet = new ArrayList<>();
    }

    public ApproachWayTypeIDSet(WayType wayType, List<Long> wayIDSet) {
        this.wayType = wayType;
        this.wayIDSet = wayIDSet;
    }

    public WayType getWayType() {
        return wayType;
    }

    public void setWayType(WayType wayType) {
        this.wayType = wayType;
    }

    public List<Long> getWayIDSet() {
        return wayIDSet;
    }

    public void setWayIDSet(List<Long> wayIDSet) {
        this.wayIDSet = wayIDSet;
    }

    public long getLaneID(int index) {
        return wayIDSet.get(index);
    }

    @Override
    public String toString() {
        return "ApprachWayTypeIDSet [wayType=" + wayType + 
        (wayIDSet != null ? wayIDSet.toString() : "null") + "]";
    }
}
