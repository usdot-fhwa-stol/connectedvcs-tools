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

public class WayUseContainer {
    private int waUseContainerId;
    private MtrVehLaneSpeedLimitsLayer mtrVehLaneSpeedLimitsLayer;

    // Constants to represent the choices
    public static final int MTR_VEH_LANE_SPEED_LIMITS_LAYER_ID = 1;

    public WayUseContainer() {
    }

    public WayUseContainer(int waUseContainerId, MtrVehLaneSpeedLimitsLayer mtrVehLaneSpeedLimitsLayer) {
        this.waUseContainerId = waUseContainerId;
        this.mtrVehLaneSpeedLimitsLayer = mtrVehLaneSpeedLimitsLayer;
    }

    public int getWaUseContainerId() {
        return waUseContainerId;
    }

    public void setWaUseContainerId(int waUseContainerId) {
        this.waUseContainerId = waUseContainerId;
    }

    public MtrVehLaneSpeedLimitsLayer getMtrVehLaneSpeedLimitsLayer() {
        return mtrVehLaneSpeedLimitsLayer;
    }

    public void setMtrVehLaneSpeedLimitsLayer(MtrVehLaneSpeedLimitsLayer mtrVehLaneSpeedLimitsLayer) {
        this.mtrVehLaneSpeedLimitsLayer = mtrVehLaneSpeedLimitsLayer;
    }

    @Override
    public String toString() {
        return "WayUseContainer [waUseContainerId=" + waUseContainerId + ", mtrVehLaneSpeedLimitsLayer="
                + mtrVehLaneSpeedLimitsLayer + "]";
    }    
}
