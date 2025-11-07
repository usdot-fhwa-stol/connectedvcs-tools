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

import gov.usdot.cv.mapencoder.NodeOffsetPointXY;

public class NodeLL {
    private NodeOffsetPointXY delta;
    private NodeAttributeSetLL attributes;

    public NodeLL() {
        
    }
    
    public NodeLL(NodeOffsetPointXY delta, NodeAttributeSetLL attributes) {
        this.delta = delta;
        this.attributes = attributes;
    }

    public NodeOffsetPointXY getDelta() {
        return delta;
    }
    public void setDelta(NodeOffsetPointXY delta) {
        this.delta = delta;
    }
    public NodeAttributeSetLL getAttributes() {
        return attributes;
    }
    public void setAttributes(NodeAttributeSetLL attributes) {
        this.attributes = attributes;
    }
    @Override
    public String toString() {
        return "NodeLL{" +
                "delta=" + delta +
                ", attributes=" + attributes +
                '}';
    }
    
}
