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

public class MovementsContainer {
    private int movementsContainerId;
    private MtrVehLaneDirectionOfTravelLayer mtrVehLaneDirectionOfTravelLayer;

    // Constants to represent the choices
    public static final int MTR_VEH_LANE_DIRECTION_OF_TRAVEL_LAYER_ID = 1;

    public MovementsContainer() {
        this.movementsContainerId = 0;
        this.mtrVehLaneDirectionOfTravelLayer = null;
    }

    public MovementsContainer(int movementsContainerId,
            MtrVehLaneDirectionOfTravelLayer mtrVehLaneDirectionOfTravelLayer) {
        this.movementsContainerId = movementsContainerId;
        this.mtrVehLaneDirectionOfTravelLayer = mtrVehLaneDirectionOfTravelLayer;
    }

    public int getMovementsContainerId() {
        System.out.println("Getting id");
        return movementsContainerId;
    }

    public void setMovementsContainerId(int movementsContainerId) {
        this.movementsContainerId = movementsContainerId;
    }

    public MtrVehLaneDirectionOfTravelLayer getMtrVehLaneDirectionOfTravelLayer() {
        return mtrVehLaneDirectionOfTravelLayer;
    }

    public void setMtrVehLaneDirectionOfTravelLayer(MtrVehLaneDirectionOfTravelLayer mtrVehLaneDirectionOfTravelLayer) {
        this.mtrVehLaneDirectionOfTravelLayer = mtrVehLaneDirectionOfTravelLayer;
    }

    @Override
    public String toString() {
        return "MovementsContainer [movementsContainerId=" + movementsContainerId
                + ", mtrVehLaneDirectionOfTravelLayer="
                + (mtrVehLaneDirectionOfTravelLayer != null ? mtrVehLaneDirectionOfTravelLayer.toString() : "null")
                + "]";
    }
}