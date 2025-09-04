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

public class NodeIndexOffset {
    private long nodeIndex;
    private NodeXYZOffsetInfo nodeIndexXYZOffset;
    
    public NodeIndexOffset() {
    }

    public NodeIndexOffset(long nodeIndex, NodeXYZOffsetInfo nodeIndexXYZOffset) {
        this.nodeIndex = nodeIndex;
        this.nodeIndexXYZOffset = nodeIndexXYZOffset;
    }

    public long getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(long nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public NodeXYZOffsetInfo getNodeIndexXYZOffset() {
        return nodeIndexXYZOffset;
    }

    public void setNodeIndexXYZOffset(NodeXYZOffsetInfo nodeIndexXYZOffset) {
        this.nodeIndexXYZOffset = nodeIndexXYZOffset;
    }

    @Override
    public String toString() {
        return "NodeIndexOffset [nodeIndex=" + nodeIndex + ", nodeIndexXYZOffset=" + nodeIndexXYZOffset + "]";
    }
}
