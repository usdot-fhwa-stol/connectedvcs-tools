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

public class NodeXYZOffsetInfo {
    private NodeXYZOffsetValue nodeXOffsetValue;
    private NodeXYZOffsetValue nodeYOffsetValue;
    private NodeXYZOffsetValue nodeZOffsetValue;

    public NodeXYZOffsetInfo() {
        this.nodeXOffsetValue = null;
        this.nodeYOffsetValue = null;
        this.nodeZOffsetValue = null;
    }

    public NodeXYZOffsetInfo(NodeXYZOffsetValue nodeXOffsetValue, NodeXYZOffsetValue nodeYOffsetValue,
            NodeXYZOffsetValue nodeZOffsetValue) {
        this.nodeXOffsetValue = nodeXOffsetValue;
        this.nodeYOffsetValue = nodeYOffsetValue;
        this.nodeZOffsetValue = nodeZOffsetValue;
    }

    public NodeXYZOffsetValue getNodeXOffsetValue() {
        return nodeXOffsetValue;
    }

    public void setNodeXOffsetValue(NodeXYZOffsetValue nodeXOffsetValue) {
        this.nodeXOffsetValue = nodeXOffsetValue;
    }

    public NodeXYZOffsetValue getNodeYOffsetValue() {
        return nodeYOffsetValue;
    }

    public void setNodeYOffsetValue(NodeXYZOffsetValue nodeYOffsetValue) {
        this.nodeYOffsetValue = nodeYOffsetValue;
    }

    public NodeXYZOffsetValue getNodeZOffsetValue() {
        return nodeZOffsetValue;
    }

    public void setNodeZOffsetValue(NodeXYZOffsetValue nodeZOffsetValue) {
        this.nodeZOffsetValue = nodeZOffsetValue;
    }

    @Override
    public String toString() {
        return "NodeXYZOffsetInfo [nodeXOffsetValue=" + nodeXOffsetValue + ", nodeYOffsetValue=" + nodeYOffsetValue
                + ", nodeZOffsetValue=" + nodeZOffsetValue + "]";
    }
}
