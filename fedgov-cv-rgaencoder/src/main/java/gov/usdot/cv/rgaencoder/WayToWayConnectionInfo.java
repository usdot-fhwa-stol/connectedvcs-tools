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

public class WayToWayConnectionInfo {
    private int laneConnectionID; // 0 to 255
    private LaneConnectionFromInfo connectionFromInfo;
    private LaneConnectionToInfo connectionToInfo;
    private RGATimeRestrictions timeRestrictions;

    public WayToWayConnectionInfo() {
        this.laneConnectionID = 0;
        this.connectionFromInfo = null;
        this.connectionToInfo = null;
        this.timeRestrictions = null;
    }

    public WayToWayConnectionInfo(int laneConnectionID, LaneConnectionFromInfo connectionFromInfo,
            LaneConnectionToInfo connectionToInfo, RGATimeRestrictions timeRestrictions) {
        this.laneConnectionID = laneConnectionID;
        this.connectionFromInfo = connectionFromInfo;
        this.connectionToInfo = connectionToInfo;
        this.timeRestrictions = timeRestrictions;
    }

    public int getLaneConnectionID() {
        return laneConnectionID;
    }

    public void setLaneConnectionID(int laneConnectionID) {
        this.laneConnectionID = laneConnectionID;
    }

    public LaneConnectionFromInfo getConnectionFromInfo() {
        return connectionFromInfo;
    }

    public void setConnectionFromInfo(LaneConnectionFromInfo connectionFromInfo) {
        this.connectionFromInfo = connectionFromInfo;
    }

    public LaneConnectionToInfo getConnectionToInfo() {
        return connectionToInfo;
    }

    public void setConnectionToInfo(LaneConnectionToInfo connectionToInfo) {
        this.connectionToInfo = connectionToInfo;
    }

    public RGATimeRestrictions getTimeRestrictions() {
        return timeRestrictions;
    }

    public void setTimeRestrictions(RGATimeRestrictions timeRestrictions) {
        this.timeRestrictions = timeRestrictions;
    }

    @Override
    public String toString() {
        return "WayToWayConnectionInfo [laneConnectionID=" + laneConnectionID + ", connectionFromInfo="
                + connectionFromInfo + ", connectionToInfo=" + connectionToInfo + ", timeRestrictions="
                + timeRestrictions + "]";
    }
}
