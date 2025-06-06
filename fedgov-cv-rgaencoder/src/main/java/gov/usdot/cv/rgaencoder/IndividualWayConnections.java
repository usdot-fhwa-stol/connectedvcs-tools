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

public class IndividualWayConnections {
    private int wayID; // Represents LaneID
    private List<WayToWayConnectionInfo> connectionsSet;

    public IndividualWayConnections() {
        this.wayID = 0;
        this.connectionsSet = null;
    }

    public IndividualWayConnections(int wayID, List<WayToWayConnectionInfo> connectionsSet) {
        this.wayID = wayID;
        this.connectionsSet = connectionsSet;
    }

    public int getWayID() {
        return wayID;
    }

    public void setWayID(int wayID) {
        this.wayID = wayID;
    }

    public List<WayToWayConnectionInfo> getConnectionsSet() {
        return connectionsSet;
    }

    public void setConnectionsSet(List<WayToWayConnectionInfo> connectionsSet) {
        this.connectionsSet = connectionsSet;
    }

    public void addWayToWayConnectionInfo(WayToWayConnectionInfo wayToWayConnectionInfo) {
        if (wayToWayConnectionInfo != null) {
            this.connectionsSet.add(wayToWayConnectionInfo);
        }
    }

    @Override
    public String toString() {
        return "IndividualWayConnections [wayID=" + wayID + ", connectionsSet="
                + (connectionsSet != null ? connectionsSet.toString() : "null") + "]";
    }
}
