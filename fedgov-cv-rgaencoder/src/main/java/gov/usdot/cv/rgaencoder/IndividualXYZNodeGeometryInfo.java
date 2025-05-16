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

public class IndividualXYZNodeGeometryInfo {
    private NodeXYZOffsetInfo nodeXYZOffsetInfo;
    private WayPlanarGeometryInfo nodeLocPlanarGeomInfo;

    public IndividualXYZNodeGeometryInfo() {
        this.nodeXYZOffsetInfo = null;
        this.nodeLocPlanarGeomInfo = null;
    }

    public IndividualXYZNodeGeometryInfo(NodeXYZOffsetInfo nodeXYZOffsetInfo, WayPlanarGeometryInfo nodeLocPlanarGeomInfo) {
        this.nodeXYZOffsetInfo = nodeXYZOffsetInfo;
        this.nodeLocPlanarGeomInfo = nodeLocPlanarGeomInfo;
    }

    public NodeXYZOffsetInfo getNodeXYZOffsetInfo() {
        return nodeXYZOffsetInfo;
    }

    public void setNodeXYZOffsetInfo(NodeXYZOffsetInfo nodeXYZOffsetInfo) {
        this.nodeXYZOffsetInfo = nodeXYZOffsetInfo;
    }

    public WayPlanarGeometryInfo getNodeLocPlanarGeomInfo() {
        return nodeLocPlanarGeomInfo;
    }

    public void setNodeLocPlanarGeomInfo(WayPlanarGeometryInfo nodeLocPlanarGeomInfo) {
        this.nodeLocPlanarGeomInfo = nodeLocPlanarGeomInfo;
    }

    @Override
    public String toString() {
        return "IndividualXYZNodeGeometryInfo [nodeXYZOffsetInfo=" + nodeXYZOffsetInfo + " nodeLocPlanarInfo= " + nodeLocPlanarGeomInfo + "]";
    }
}