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

public class MtrVehLaneConnectionsLayer {
    private List<IndividualWayConnections> mtrVehLaneCnxnsLaneSet;

    public MtrVehLaneConnectionsLayer() {
        this.mtrVehLaneCnxnsLaneSet = new ArrayList<>();
    }

    public MtrVehLaneConnectionsLayer(List<IndividualWayConnections> mtrVehLaneCnxnsLaneSet) {
        this.mtrVehLaneCnxnsLaneSet = mtrVehLaneCnxnsLaneSet;
    }

    public List<IndividualWayConnections> getMtrVehLaneCnxnsLaneSet() {
        return mtrVehLaneCnxnsLaneSet;
    }

    public void setMtrVehLaneCnxnsLaneSet(List<IndividualWayConnections> mtrVehLaneCnxnsLaneSet) {
        this.mtrVehLaneCnxnsLaneSet = mtrVehLaneCnxnsLaneSet;
    }

    public void addIndividualWayConnections(IndividualWayConnections individualWayConnections) {
        if (individualWayConnections != null) {
            this.mtrVehLaneCnxnsLaneSet.add(individualWayConnections);
        }
    }

    @Override
    public String toString() {
        return "MtrVehLaneConnectionsLayer [mtrVehLaneCnxnsLaneSet="
                + (mtrVehLaneCnxnsLaneSet != null ? mtrVehLaneCnxnsLaneSet.toString() : "null") + "]";
    }  
}
