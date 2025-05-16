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

public class ApproachGeometryLayer {
    private List<IndividualApproachGeometryInfo> approachGeomApproachSet;

    public ApproachGeometryLayer() {
        this.approachGeomApproachSet = new ArrayList<>();
    }

    public ApproachGeometryLayer(List<IndividualApproachGeometryInfo> approachGeomApproachSet) {
        this.approachGeomApproachSet = approachGeomApproachSet;
    }

    public List<IndividualApproachGeometryInfo> getApproachGeomApproachSet() {
        return approachGeomApproachSet;
    }

    public void setApproachGeomApproachSet(List<IndividualApproachGeometryInfo> approachGeomApproachSet) {
        this.approachGeomApproachSet = approachGeomApproachSet;
    }

    public void addIndividualApproachGeometryInfo(IndividualApproachGeometryInfo individualApproachGeometryInfo) {
        if (individualApproachGeometryInfo != null) {
            this.approachGeomApproachSet.add(individualApproachGeometryInfo);
        }
    }

    @Override
    public String toString() {
        return "ApproachGeometryLayer [approachGeomApproachSet=" + (approachGeomApproachSet != null ? approachGeomApproachSet.toString() : "null") + "]";
    }
}
