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

public class MtrVehLaneSpeedLimitsLayer {
    private List<IndividualWaySpeedLimits> laneSpeedLimitLaneSet;

    public MtrVehLaneSpeedLimitsLayer() {
        this.laneSpeedLimitLaneSet = new ArrayList<>();
    }

    public MtrVehLaneSpeedLimitsLayer(List<IndividualWaySpeedLimits> laneSpeedLimitLaneSet) {
        this.laneSpeedLimitLaneSet = laneSpeedLimitLaneSet;
    }

    public List<IndividualWaySpeedLimits> getLaneSpeedLimitLaneSet() {
        return laneSpeedLimitLaneSet;
    }

    public void setLaneSpeedLimitLaneSet(List<IndividualWaySpeedLimits> laneSpeedLimitLaneSet) {
        this.laneSpeedLimitLaneSet = laneSpeedLimitLaneSet;
    }

    public void addIndividualWaySpeedLimits(IndividualWaySpeedLimits individualWaySpeedLimits) {
        if (individualWaySpeedLimits != null) {
            this.laneSpeedLimitLaneSet.add(individualWaySpeedLimits);
        }
    }

    @Override
    public String toString() {
        return "MtrVehLaneSpeedLimitsLayer [laneSpeedLimitLaneSet=" + laneSpeedLimitLaneSet + "]";
    }
}
