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

public class LaneConnectionToInfo {
    private WayType wayType;
    private int wayID; // Represents LaneID
    private int nodeToPosition;

    // Constants for choice field
    public static final int FIRST_NODE = 0;
    public static final int LAST_NODE = 1;

    public LaneConnectionToInfo() {
        this.wayType = null;
        this.wayID = 0;
        this.nodeToPosition = 0;
    }

    public LaneConnectionToInfo(WayType wayType, int wayID, int nodeToPosition) {
        this.wayType = wayType;
        this.wayID = wayID;
        this.nodeToPosition = nodeToPosition;
    }

    public WayType getWayType() {
        return wayType;
    }

    public void setWayType(WayType wayType) {
        this.wayType = wayType;
    }

    public int getWayID() {
        return wayID;
    }

    public void setWayID(int wayID) {
        this.wayID = wayID;
    }

    public int getNodeToPosition() {
        return nodeToPosition;
    }

    public void setNodeToPosition(int nodeToPosition) {
        this.nodeToPosition = nodeToPosition;
    }

    @Override
    public String toString() {
        return "LaneConnectionToInfo [wayType=" + wayType + ", wayID=" + wayID + ", nodeToPosition=" + nodeToPosition
                + "]";
    }
}
