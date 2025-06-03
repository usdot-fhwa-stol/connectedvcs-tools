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

public class BicycleLaneConnectionsLayer {
    private List<IndividualWayConnections> bicycleLaneCnxnsLaneSet;

    public BicycleLaneConnectionsLayer() {
        this.bicycleLaneCnxnsLaneSet = null;
    }

    public BicycleLaneConnectionsLayer(List<IndividualWayConnections> bicycleLaneCnxnsLaneSet) {
        this.bicycleLaneCnxnsLaneSet = bicycleLaneCnxnsLaneSet;
    }

    public List<IndividualWayConnections> getBicycleLaneCnxnsLaneSet() {
        return bicycleLaneCnxnsLaneSet;
    }

    public void setBicycleLaneCnxnsLaneSet(List<IndividualWayConnections> bicycleLaneCnxnsLaneSet) {
        this.bicycleLaneCnxnsLaneSet = bicycleLaneCnxnsLaneSet;
    }

    public void addIndividualWayConnections(IndividualWayConnections individualWayConnections) {
        if (individualWayConnections != null) {
            this.bicycleLaneCnxnsLaneSet.add(individualWayConnections);
        }
    }

    @Override
    public String toString() {
        return "BicycleLaneConnectionsLayer [bicycleLaneCnxnsLaneSet="
                + (bicycleLaneCnxnsLaneSet != null ? bicycleLaneCnxnsLaneSet.toString() : "null") + "]";
    }
}
