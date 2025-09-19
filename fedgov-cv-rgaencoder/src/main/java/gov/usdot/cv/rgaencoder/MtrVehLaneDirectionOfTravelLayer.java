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

public class MtrVehLaneDirectionOfTravelLayer {
    private List<IndividualWayDirectionsOfTravel> laneDirOfTravelLaneSet;

    public MtrVehLaneDirectionOfTravelLayer() {
        this.laneDirOfTravelLaneSet = new ArrayList<>();
    }

    public MtrVehLaneDirectionOfTravelLayer(List<IndividualWayDirectionsOfTravel> laneDirOfTravelLaneSet) {
        this.laneDirOfTravelLaneSet = laneDirOfTravelLaneSet;
    }

    public List<IndividualWayDirectionsOfTravel> getLaneDirOfTravelLaneSet() {
        return laneDirOfTravelLaneSet;
    }

    public void setLaneDirOfTravelLaneSet(List<IndividualWayDirectionsOfTravel> laneDirOfTravelLaneSet) {
        this.laneDirOfTravelLaneSet = laneDirOfTravelLaneSet;
    }

    public void addIndividualWayDirectionsOfTravel(IndividualWayDirectionsOfTravel individualWayDirectionsOfTravel) {
        if (individualWayDirectionsOfTravel != null) {
            this.laneDirOfTravelLaneSet.add(individualWayDirectionsOfTravel);
        }
    }

    @Override
    public String toString() {
        return "MtrVehLaneDirectionOfTravelLayer [laneDirOfTravelLaneSet="
                + (laneDirOfTravelLaneSet != null ? laneDirOfTravelLaneSet.toString() : "null") + "]";
    }
}