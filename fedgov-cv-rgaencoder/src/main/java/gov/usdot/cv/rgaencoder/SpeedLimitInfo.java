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

public class SpeedLimitInfo {
    private List<IndividualSpeedLimitSettings> maxSpeedLimitSettingsSet;
    private List<IndividualSpeedLimitSettings> minSpeedLimitSettingsSet;

    public SpeedLimitInfo() {
        this.maxSpeedLimitSettingsSet = new ArrayList<>();
        this.minSpeedLimitSettingsSet = new ArrayList<>();
    }

    public SpeedLimitInfo(List<IndividualSpeedLimitSettings> maxSpeedLimitSettingsSet,
            List<IndividualSpeedLimitSettings> minSpeedLimitSettingsSet) {
        this.maxSpeedLimitSettingsSet = maxSpeedLimitSettingsSet;
        this.minSpeedLimitSettingsSet = minSpeedLimitSettingsSet;
    }

    public List<IndividualSpeedLimitSettings> getMaxSpeedLimitSettingsSet() {
        return maxSpeedLimitSettingsSet;
    }

    public void setMaxSpeedLimitSettingsSet(List<IndividualSpeedLimitSettings> maxSpeedLimitSettingsSet) {
        this.maxSpeedLimitSettingsSet = maxSpeedLimitSettingsSet;
    }

    public List<IndividualSpeedLimitSettings> getMinSpeedLimitSettingsSet() {
        return minSpeedLimitSettingsSet;
    }

    public void setMinSpeedLimitSettingsSet(List<IndividualSpeedLimitSettings> minSpeedLimitSettingsSet) {
        this.minSpeedLimitSettingsSet = minSpeedLimitSettingsSet;
    }

    public void addMaxSpeedLimitSettingsSet(IndividualSpeedLimitSettings maxSpeedLimitSettingsSet) {
        if (maxSpeedLimitSettingsSet != null) {
            this.maxSpeedLimitSettingsSet.add(maxSpeedLimitSettingsSet);
        }
    }

    public void addMinSpeedLimitSettingsSet(IndividualSpeedLimitSettings minSpeedLimitSettingsSet) {
        if (minSpeedLimitSettingsSet != null) {
            this.minSpeedLimitSettingsSet.add(minSpeedLimitSettingsSet);
        }
    }

    @Override
    public String toString() {
        return "SpeedLimitInfo [maxSpeedLimitSettingsSet=" + maxSpeedLimitSettingsSet + ", minSpeedLimitSettingsSet="
                + minSpeedLimitSettingsSet + "]";
    } 
}
