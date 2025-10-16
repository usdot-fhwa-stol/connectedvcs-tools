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
import gov.usdot.cv.mapencoder.RoadSegmentReferenceID;

public class GeographicalPath {

    private DescriptiveName name;
    private RoadSegmentReferenceID id;
    private Position3D anchor;
    private LaneWidth laneWidth;
    private DirectionOfUse directionality;
    private boolean closedPath;
    private HeadingSlice direction;
    private Description description;

    public GeographicalPath() {
        
    }

    public GeographicalPath(DescriptiveName name, RoadSegmentReferenceID id, Position3D anchor, LaneWidth laneWidth,
            DirectionOfUse directionality, boolean closedPath, HeadingSlice direction, Description description) {
        this.name = name;
        this.id = id;
        this.anchor = anchor;
        this.laneWidth = laneWidth;
        this.directionality = directionality;
        this.closedPath = closedPath;
        this.direction = direction;
        this.description = description;
    }

    public DescriptiveName getName() {
        return name;
    }
    public RoadSegmentReferenceID getId() {
        return id;
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
    public boolean isClosedPath() {
        return closedPath;
    }
    public HeadingSlice getDirection() {
        return direction;
    }
    public Description getDescription() {
        return description;
    }

    public void setName(DescriptiveName name) {
        this.name = name;
    }
    public void setId(RoadSegmentReferenceID id) {
        this.id = id;
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
    public void setClosedPath(boolean closedPath) {
        this.closedPath = closedPath;
    }
    public void setDirection(HeadingSlice direction) {
        this.direction = direction;
    }
    public void setDescription(Description description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "GeographicalPath{" +
                "name=" + name +
                ", id=" + id +
                ", anchor=" + anchor +
                ", laneWidth=" + laneWidth +
                ", directionality=" + directionality +
                ", closedPath=" + closedPath +
                ", direction=" + direction +
                ", description=" + description +
                '}';
    }    

    public static class Description {
        
        public enum Choice {
            path_chosen, geometry_chosen, oldRegion_chosen
        }

        private Choice choice;
        private OffsetSystem path_chosen;
        private GeometricProjection geometry_chosen;
        private ValidRegion oldRegion_chosen;

        public Choice getChoice() {
            return choice;
        }

        public OffsetSystem getPathChosen() {
            return path_chosen;
        }

        public GeometricProjection getGeometryChosen() {
            return geometry_chosen;
        }

        public ValidRegion getOldRegionChosen() {
            return oldRegion_chosen;
        }

        @Override
        public String toString() {
            return "Description {" +
                 "choice =" + choice +
                 "}";
        }

    }
}
