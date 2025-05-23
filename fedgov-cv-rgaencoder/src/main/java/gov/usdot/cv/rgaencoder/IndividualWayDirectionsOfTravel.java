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

public class IndividualWayDirectionsOfTravel {
    private int wayID; // Represents LaneID
    private List<WayDirectionOfTravelInfo> directionsOfTravelSet;

    public IndividualWayDirectionsOfTravel() {
        this.wayID = 0;
        this.directionsOfTravelSet = null;
    }

    public IndividualWayDirectionsOfTravel(int wayID, List<WayDirectionOfTravelInfo> directionsOfTravelSet) {
        this.wayID = wayID;
        this.directionsOfTravelSet = directionsOfTravelSet;
    }

    public int getWayID() {
        return wayID;
    }

    public void setWayID(int wayID) {
        this.wayID = wayID;
    }

    public List<WayDirectionOfTravelInfo> getDirectionsOfTravelSet() {
        return directionsOfTravelSet;
    }

    public void setDirectionsOfTravelSet(List<WayDirectionOfTravelInfo> directionsOfTravelSet) {
        this.directionsOfTravelSet = directionsOfTravelSet;
    }

    public void addWayDirectionOfTravelInfo(WayDirectionOfTravelInfo wayDirectionOfTravelInfo) {
        if (wayDirectionOfTravelInfo != null) {
            this.directionsOfTravelSet.add(wayDirectionOfTravelInfo);
        }
    }

    @Override
    public String toString() {
        return "IndividualWayDirectionsOfTravel [wayID=" + wayID + ", directionsOfTravelSet="
                + (directionsOfTravelSet != null ? directionsOfTravelSet.toString() : "null")
                + "]";
    }
}
