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

public class IndividualWayCnxnsManeuvers {
    private int wayID; // 0 to 255
    private List<WayCnxnManeuverInfo> cnxnManeuversSet;

    public IndividualWayCnxnsManeuvers() {
        this.wayID = 0;
        this.cnxnManeuversSet = new ArrayList<>();
    }

    public IndividualWayCnxnsManeuvers(int wayID, List<WayCnxnManeuverInfo> cnxnManeuversSet) {
        this.wayID = wayID;
        this.cnxnManeuversSet = cnxnManeuversSet;
    }

    public int getWayID() {
        return wayID;
    }

    public void setWayID(int wayID) {
        this.wayID = wayID;
    }   

    public List<WayCnxnManeuverInfo> getCnxnManeuversSet() {
        return cnxnManeuversSet;
    }

    public void setCnxnManeuversSet(List<WayCnxnManeuverInfo> cnxnManeuversSet) {
        this.cnxnManeuversSet = cnxnManeuversSet;
    }

    public void addWayCnxnManeuverInfo(WayCnxnManeuverInfo wayCnxnManeuverInfo) {
        if (wayCnxnManeuverInfo != null) {
            this.cnxnManeuversSet.add(wayCnxnManeuverInfo);
        }
    }

    @Override
    public String toString() {
        return "IndividualWayCnxnsManeuvers [wayID=" + wayID + ", cnxnManeuversSet="
                + (cnxnManeuversSet != null ? cnxnManeuversSet.toString() : "null") + "]";
    }
}
