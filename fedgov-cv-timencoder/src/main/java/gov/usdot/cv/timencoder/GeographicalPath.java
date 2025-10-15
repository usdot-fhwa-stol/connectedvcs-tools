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

import java.util.ArrayList;
import java.util.List;

import gov.usdot.cv.mapencoder.LaneDataAttribute;
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
    private Regional regional;

    public GeographicalPath(DescriptiveName name, RoadSegmentReferenceID id, Position3D anchor, LaneWidth laneWidth,
            DirectionOfUse directionality, boolean closedPath, HeadingSlice direction, Description description,
            Regional regional) {
        this.name = name;
        this.id = id;
        this.anchor = anchor;
        this.laneWidth = laneWidth;
        this.directionality = directionality;
        this.closedPath = closedPath;
        this.direction = direction;
        this.description = description;
        this.regional = regional;
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
    public Regional getRegional() {
        return regional;
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
    public void setRegional(Regional regional) {
        this.regional = regional;
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
                ", regional=" + regional +
                '}';
    }

    public static class Description {
        
        public enum Choice {
            path_chosen, geometry_chosen, oldRegion_chosen
        }

        private Choice choice;
        private int path_chosen;
        private int geometry_chosen;
        private int oldRegion_chosen;

        public Choice getChoice() {
            return choice;
        }

        public int getPathChosen() {
            return path_chosen;
        }

        public int getGeometryChosen() {
            return geometry_chosen;
        }

        public int getOldRegionChosen() {
            return oldRegion_chosen;
        }

        @Override
        public String toString() {
            return "Description {" +
                 "choice =" + choice;
        }

    }

    public static class Regional {
        private List<LaneDataAttribute> elements = new ArrayList<>();

        public Regional() {
        }

        public Regional(List<LaneDataAttribute> elements) {
            this.elements = elements;
        }

        public List<LaneDataAttribute> getElements() {
            return elements;
        }

        public int getElementSize() {
            return elements.size();
        }

        public void setElements(List<LaneDataAttribute> elements) {
            this.elements = elements;
        }

        public void addElement(LaneDataAttribute element) {
            if (elements == null) {
                elements = new ArrayList<>();
            }
            elements.add(element);
        }

        @Override
        public String toString() {
            return "Regional{" +
                    "elements=" + elements +
                    '}';
        }
    }
}
