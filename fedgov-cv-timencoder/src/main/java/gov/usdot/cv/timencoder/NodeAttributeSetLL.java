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

package gov.usdot.cv.timencoder;

public class NodeAttributeSetLL {
    private NodeAttributeLLList localNode;
    private SegmentAttributeLLList disabled;
    private SegmentAttributeLLList enabled;
    private LaneDataAttributeList data;
    private Offset_B10 dWidth;
    private Offset_B10 dElevation;
    
    public NodeAttributeSetLL() {

    }

    public NodeAttributeSetLL(NodeAttributeLLList localNode, SegmentAttributeLLList disabled,
            SegmentAttributeLLList enabled, LaneDataAttributeList data, Offset_B10 dWidth, Offset_B10 dElevation) {
        this.localNode = localNode;
        this.disabled = disabled;
        this.enabled = enabled;
        this.data = data;
        this.dWidth = dWidth;
        this.dElevation = dElevation;
    }

    public NodeAttributeLLList getLocalNode() {
        return localNode;
    }
    public SegmentAttributeLLList getDisabled() {
        return disabled;
    }
    public SegmentAttributeLLList getEnabled() {
        return enabled;
    }
    public LaneDataAttributeList getData() {
        return data;
    }
    public Offset_B10 getDWidth() {
        return dWidth;
    }
    public Offset_B10 getDElevation() {
        return dElevation;
    }

    public void setLocalNode(NodeAttributeLLList localNode) {
        this.localNode = localNode;
    }
    public void setDisabled(SegmentAttributeLLList disabled) {
        this.disabled = disabled;
    }
    public void setEnabled(SegmentAttributeLLList enabled) {
        this.enabled = enabled;
    }
    public void setData(LaneDataAttributeList data) {
        this.data = data;
    }
    public void setDWidth(Offset_B10 dWidth) {
        this.dWidth = dWidth;
    }
    public void setDElevation(Offset_B10 dElevation) {
        this.dElevation = dElevation;
    }

    @Override
    public String toString() {
        return "NodeAttributeSetLL{" +
                "localNode=" + localNode +
                ", disabled=" + disabled +
                ", enabled=" + enabled +
                ", data=" + data +
                ", dWidth=" + dWidth +
                ", dElevation=" + dElevation +
                '}';
    }
}
