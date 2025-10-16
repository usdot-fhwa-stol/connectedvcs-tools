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

public class NodeSetLL {
    private NodeLL[] nodes;

    public NodeSetLL() {
        
    }
    
    public NodeSetLL(NodeLL[] nodes) {
        this.nodes = nodes;
    }
    public NodeLL[] getNodes() {
        return nodes;
    }
    public void setNodes(NodeLL[] nodes) {
        this.nodes = nodes;
    }
    @Override
    public String toString() {
        return "NodeSetLL{" +
                "nodes=" + java.util.Arrays.toString(nodes) +
                '}';
    }
}
