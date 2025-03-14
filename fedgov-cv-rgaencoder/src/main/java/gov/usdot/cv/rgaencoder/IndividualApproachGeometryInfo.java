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

public class IndividualApproachGeometryInfo {
    private int approachID;
    private List<ApproachWayTypeIDSet> wayTypesSet;

    public IndividualApproachGeometryInfo() {
        this.approachID = 0;
        this.wayTypesSet = new ArrayList<>();
    }

    public IndividualApproachGeometryInfo(int approachID, List<ApproachWayTypeIDSet> wayTypesSet) {
        this.approachID = approachID;
        this.wayTypesSet = wayTypesSet;
    }

    public int getApproachID() {
        return approachID;
    }

    public void setApproachID(int approachID) {
        this.approachID = approachID;
    }

    public List<ApproachWayTypeIDSet> getApproachWayTypeIDSet() {
        return wayTypesSet;
    }

    public void setApproachWayTypeIDSet(List<ApproachWayTypeIDSet> wayTypesSet) {
        this.wayTypesSet = wayTypesSet;
    }

    @Override
    public String toString() {
        return "IndividualApproachGeometryInfo [approachID=" + approachID + 
        (wayTypesSet != null ? wayTypesSet.toString() : "null") + "]";
    }
}
