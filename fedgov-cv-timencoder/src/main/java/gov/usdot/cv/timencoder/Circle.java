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

public class Circle {
    private Position3D center;
    private Radius_B12 radius;
    private DistanceUnits units;

    public Circle(Position3D center, Radius_B12 radius, DistanceUnits units) {
        this.center = center;
        this.radius = radius;
        this.units = units;
    }

    public Position3D getCenter() {
        return center;
    }

    public Radius_B12 getRadius() {
        return radius;
    }

    public DistanceUnits getUnits() {
        return units;
    }

    public void setCenter(Position3D center) {
        this.center = center;
    }

    public void setRadius(Radius_B12 radius) {
        this.radius = radius;
    }

    public void setUnits(DistanceUnits units) {
        this.units = units;
    }

    @Override
    public String toString() {
        return "Circle{" +
                "center=" + center +
                ", radius=" + radius +
                ", units=" + units +
                '}';
    }
    
}
