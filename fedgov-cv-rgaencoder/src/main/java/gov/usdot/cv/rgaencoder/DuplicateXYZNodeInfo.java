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

public class DuplicateXYZNodeInfo {
    private int refLaneID;

    public DuplicateXYZNodeInfo() {
        this.refLaneID = 0;
    }

    public DuplicateXYZNodeInfo(int refLaneID) {
        this.refLaneID = refLaneID;
    }

    public int getRefLaneID() {
        return refLaneID;
    }

    public void setRefLaneID(int refLaneID) {
        this.refLaneID = refLaneID;
    }

    @Override
    public String toString() {
        return "DuplicateXYZNodeInfo [refLaneID=" + refLaneID + "]";
    }
}
