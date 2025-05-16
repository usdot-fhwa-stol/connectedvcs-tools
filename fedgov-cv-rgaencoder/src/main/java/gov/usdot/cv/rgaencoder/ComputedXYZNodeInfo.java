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

public class ComputedXYZNodeInfo {
    private int refLaneID;
    private NodeXYZOffsetInfo laneCenterLineXYZOffset;
    private WayPlanarGeometryInfo lanePlanarGeomInfo;

    public ComputedXYZNodeInfo() {
        this.refLaneID = 0;
        this.laneCenterLineXYZOffset = null;
        this.lanePlanarGeomInfo = null;
    }

    public ComputedXYZNodeInfo(int refLaneID, NodeXYZOffsetInfo laneCenterLineXYZOffset, WayPlanarGeometryInfo lanePlanarGeomInfo) {
        this.refLaneID = refLaneID;
        this.laneCenterLineXYZOffset = laneCenterLineXYZOffset;
        this.lanePlanarGeomInfo = lanePlanarGeomInfo;
    }

    public int getRefLaneID() {
        return refLaneID;
    }

    public void setRefLaneID(int refLaneID) {
        this.refLaneID = refLaneID;
    }

    public NodeXYZOffsetInfo getLaneCenterLineXYZOffset() {
        return laneCenterLineXYZOffset;
    }

    public void setLaneCenterLineXYZOffset(NodeXYZOffsetInfo laneCenterLineXYZOffset) {
        this.laneCenterLineXYZOffset = laneCenterLineXYZOffset;
    }

    public WayPlanarGeometryInfo getLanePlanarGeomInfo() {
        return lanePlanarGeomInfo;
    }

    public void setLanePlanarGeomInfo(WayPlanarGeometryInfo lanePlanarGeomInfo) {
        this.lanePlanarGeomInfo = lanePlanarGeomInfo;
    }

    @Override
    public String toString() {
        return "ComputedXYZNodeInfo [refLaneID=" + refLaneID + ", laneCenterLineXYZOffset=" + laneCenterLineXYZOffset + ", lanePlanarGeomInfo=" + lanePlanarGeomInfo + "]";
    }
}
