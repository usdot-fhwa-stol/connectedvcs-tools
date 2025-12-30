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

import gov.usdot.cv.mapencoder.Position3D;
import gov.usdot.cv.mapencoder.NodeListXY;

public class ShapePointSet {
    private Position3D anchor;
    private LaneWidth laneWidth;
    private DirectionOfUse directionality;
    private NodeListXY nodeListXY;

    public ShapePointSet(Position3D anchor, LaneWidth laneWidth, DirectionOfUse directionality, NodeListXY nodeListXY) {
        this.anchor = anchor;
        this.laneWidth = laneWidth;
        this.directionality = directionality;
        this.nodeListXY = nodeListXY;
    }

    public Position3D getAnchor() {
        return anchor;
    }

    public LaneWidth getLaneWidth() {
        return laneWidth;
    }

    public DirectionOfUse getDirectionality() {
        return directionality;
    }

    public NodeListXY getNodeListXY() {
        return nodeListXY;
    }

    public void setAnchor(Position3D anchor) {
        this.anchor = anchor;
    }

    public void setLaneWidth(LaneWidth laneWidth) {
        this.laneWidth = laneWidth;
    }

    public void setDirectionality(DirectionOfUse directionality) {
        this.directionality = directionality;
    }

    public void setNodeListXY(NodeListXY nodeListXY) {
        this.nodeListXY = nodeListXY;
    }

    @Override
    public String toString() {
        return "ShapePointSet{" +
                "anchor=" + anchor +
                ", laneWidth=" + laneWidth +
                ", directionality=" + directionality +
                ", nodeListXY=" + nodeListXY +
                '}';
    }
}
