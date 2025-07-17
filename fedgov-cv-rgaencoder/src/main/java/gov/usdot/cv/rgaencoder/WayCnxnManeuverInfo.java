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

public class WayCnxnManeuverInfo {
    private int connectionID;
    private List<CnxnManeuverInfo> maneuversSet;

    public WayCnxnManeuverInfo() {
        this.connectionID = 0;
        this.maneuversSet = new ArrayList<>();
    }

    public WayCnxnManeuverInfo(int connectionID, List<CnxnManeuverInfo> maneuversSet) {
        this.connectionID = connectionID;
        this.maneuversSet = maneuversSet;
    }  

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    public List<CnxnManeuverInfo> getManeuversSet() {
        return maneuversSet;
    }

    public void setManeuversSet(List<CnxnManeuverInfo> maneuversSet) {
        this.maneuversSet = maneuversSet;
    }  
     
    public void addManeuverInfo(CnxnManeuverInfo cnxnManeuverInfo) {
        if (cnxnManeuverInfo != null) {
            this.maneuversSet.add(cnxnManeuverInfo);
        }
    }

    @Override
    public String toString() {
        return "WayCnxnManeuverInfo [connectionID=" + connectionID + ", maneuversSet="
                + (maneuversSet != null ? maneuversSet.toString() : "null") + "]";
    }
    
}
