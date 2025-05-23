/*
 * Copyright (C) 2023 LEIDOS.
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
 
package gov.usdot.cv.mapencoder;

public class NodeXY26b {
    private float x;
    private float y;

    // Constructors
    public NodeXY26b() {
    }

    public NodeXY26b(float x, float y) {
        this.x = x;
        this.y = y;
    }

     // Getters and Setters
     public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "NodeXY26b{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

}
