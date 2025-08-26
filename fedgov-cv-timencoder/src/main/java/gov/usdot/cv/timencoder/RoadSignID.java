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
public class RoadSignID {
    private Position3D position;
    private HeadingSlice viewAngle;

    public RoadSignID() { }

    public RoadSignID(Position3D position, HeadingSlice viewAngle) {
        this.position = position;
        this.viewAngle = viewAngle;
    }

    public Position3D getPosition() {
        return position;
    }

    public void setPosition(Position3D position) {
        this.position = position;
    }

    public HeadingSlice getViewAngle() {
        return viewAngle;
    }

    public void setViewAngle(HeadingSlice viewAngle) {
        this.viewAngle = viewAngle;
    }

    @Override
    public String toString() {
        return "RoadSignID{" +
                "position lat=" + position.getLatitude() +"position lon=" + position.getLongitude() +
                "position elevation=" + position.getElevation() +
                ", viewAngle=" + viewAngle +
                '}';
    }
}
