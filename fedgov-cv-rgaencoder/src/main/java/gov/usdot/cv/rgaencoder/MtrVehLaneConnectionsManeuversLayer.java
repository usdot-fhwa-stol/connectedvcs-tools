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

public class MtrVehLaneConnectionsManeuversLayer {
    private List<IndividualWayCnxnsManeuvers> laneCnxnsManeuversLaneSet;

    public MtrVehLaneConnectionsManeuversLayer() {
        this.laneCnxnsManeuversLaneSet = null;
    }

    public MtrVehLaneConnectionsManeuversLayer(List<IndividualWayCnxnsManeuvers> laneCnxnsManeuversLaneSet) {
        this.laneCnxnsManeuversLaneSet = laneCnxnsManeuversLaneSet;
    }

    public List<IndividualWayCnxnsManeuvers> getMtrVehLaneConnectionsManeuversLayer() {
        return laneCnxnsManeuversLaneSet;
    }

    public void setMtrVehLaneConnectionsManeuversLayer(List<IndividualWayCnxnsManeuvers> laneCnxnsManeuversLaneSet) {
        this.laneCnxnsManeuversLaneSet = laneCnxnsManeuversLaneSet;
    }

    public void addMtrVehLaneConnectionsManeuversLayer(IndividualWayCnxnsManeuvers individualWayCnxnsManeuvers) {
        if (individualWayCnxnsManeuvers != null) {
            this.laneCnxnsManeuversLaneSet.add(individualWayCnxnsManeuvers);
        }
    }

    @Override
    public String toString() {
        return "MtrVehLaneConnectionsManeuversLayer [laneCnxnsManeuversLaneSet="
                + (laneCnxnsManeuversLaneSet != null ? laneCnxnsManeuversLaneSet.toString() : "null") + "]";
    }
}

