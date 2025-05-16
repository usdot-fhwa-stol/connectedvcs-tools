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

public class PhysicalXYZNodeInfo {
    private List<IndividualXYZNodeGeometryInfo> nodeXYZGeometryNodeSet;
    private ReferencePointInfo referencePointInfo;

    public PhysicalXYZNodeInfo() {
        this.nodeXYZGeometryNodeSet = new ArrayList<>();
        this.referencePointInfo = null;
    }

    public PhysicalXYZNodeInfo(List<IndividualXYZNodeGeometryInfo> nodeXYZGeometryNodeSet, ReferencePointInfo referencePointInfo) {
        this.nodeXYZGeometryNodeSet = nodeXYZGeometryNodeSet;
        this.referencePointInfo = referencePointInfo;
    }

    public List<IndividualXYZNodeGeometryInfo> getNodeXYZGeometryNodeSet() {
        return nodeXYZGeometryNodeSet;
    }

    public void setNodeXYZGeometryNodeSet(List<IndividualXYZNodeGeometryInfo> nodeXYZGeometryNodeSet) {
        this.nodeXYZGeometryNodeSet = nodeXYZGeometryNodeSet;
    }

    public void addIndividualXYZNodeGeometryInfo(IndividualXYZNodeGeometryInfo individualXYZNodeGeometryInfo) {
        if (individualXYZNodeGeometryInfo != null) {
            this.nodeXYZGeometryNodeSet.add(individualXYZNodeGeometryInfo);
        }
    }

    public ReferencePointInfo getReferencePointInfo() {
        return referencePointInfo;
    }

    public void setReferencePointInfo(ReferencePointInfo referencePointInfo) {
        this.referencePointInfo = referencePointInfo;
    }

    @Override
    public String toString() {
        return "PhysicalXYZNodeInfo [nodeXYZGeometryNodeSet=" + nodeXYZGeometryNodeSet + ", referencePointInfo="
                + referencePointInfo + "]";
    }
}