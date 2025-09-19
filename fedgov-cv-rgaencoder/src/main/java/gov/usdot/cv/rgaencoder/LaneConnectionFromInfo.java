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

public class LaneConnectionFromInfo {
    private int nodeFromPosition;

    // Constants for choice field
    public static final int FIRST_NODE = 0;
    public static final int LAST_NODE = 1;

    public LaneConnectionFromInfo() {
        this.nodeFromPosition = 0;
    }

    public LaneConnectionFromInfo(int nodeFromPosition) {
        this.nodeFromPosition = nodeFromPosition;
    }

    public int getNodeFromPosition() {
        return nodeFromPosition;
    }

    public void setNodeFromPosition(int nodeFromPosition) {
        this.nodeFromPosition = nodeFromPosition;
    }

    @Override
    public String toString() {
        return "LaneConnectionFromInfo [nodeFromPosition=" + nodeFromPosition + "]";
    }
}
