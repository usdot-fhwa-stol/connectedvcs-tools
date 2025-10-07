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

public class ValidRegion {
    private HeadingSlice direction;
    private Extent extent;
    private AreaChoice area;

    public ValidRegion(HeadingSlice direction, Extent extent, AreaChoice area) {
        this.direction = direction;
        this.extent = extent;
        this.area = area;
    }

    public HeadingSlice getHeadingSlice() {
        return direction;
    }

    public Extent getExtent() {
        return extent;
    }

    public AreaChoice getAreaChoice() {
        return area;
    }

    @Override
    public String toString() {
        return "ValidRegion {" +
                 "direction =" + direction +
                 ", extent =" + extent +
                 ", area=" + area +
                 '}';
    }

    // ---- Inner class ----
    public static class AreaChoice {
        
        public enum Choice {
            shapePointSet, circle, regionPointSet
        }

        private Choice choice;
        private ShapePointSet shapePointSet;
        private Circle circle;
        private RegionPointSet regionPointSet;

        public Choice getChoice() {
            return choice;
        }

        public ShapePointSet getShapePointSet() {
            return shapePointSet;
        }

        public Circle getCircle() {
            return circle;
        }

        public RegionPointSet getRegionPointSet() {
            return regionPointSet;
        }

        @Override
        public String toString() {
            return "AreaChoice {" +
                 "choice =" + choice;
        }

    }
    
}
