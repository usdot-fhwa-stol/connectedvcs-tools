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

public class GeometricProjection {
    private HeadingSlice direction;
    private Extent extent;
    private LaneWidth laneWidth;
    private Circle circle;
    private Regional regional;

    public GeometricProjection(HeadingSlice direction, Extent extent, LaneWidth laneWidth, Circle circle, Regional regional) {
        this.direction = direction;
        this.extent = extent;
        this.laneWidth = laneWidth;
        this.circle = circle;
        this.regional = regional;
    }

    public HeadingSlice getHeadingSlice() {
        return direction;
    }
    public Extent getExtent() {
        return extent;
    }
    public LaneWidth getLaneWidth() {
        return laneWidth;
    }
    public Circle getCircle() {
        return circle;
    }
    public Regional getRegional() {
        return regional;
    }

    public void setHeadingSlice(HeadingSlice direction) {
        this.direction = direction;
    }
    public void setExtent(Extent extent) {
        this.extent = extent;
    }
    public void setLaneWidth(LaneWidth laneWidth) {
        this.laneWidth = laneWidth;
    }
    public void setCircle(Circle circle) {
        this.circle = circle;
    }
    public void setRegional(Regional regional) {
        this.regional = regional;
    }
    @Override
    public String toString() {
        return "GeometricProjection {" +
                 "direction =" + direction +
                 ", extent =" + extent +
                 ", laneWidth=" + laneWidth +
                 ", circle=" + circle +
                 ", regional=" + regional +
                 '}';
    }

    // ---- Inner class ----
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
