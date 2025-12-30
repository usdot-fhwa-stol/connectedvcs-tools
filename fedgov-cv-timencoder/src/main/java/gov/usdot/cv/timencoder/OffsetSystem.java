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

import gov.usdot.cv.mapencoder.NodeListXY;

public class OffsetSystem {
    private Zoom scale;
    private Offset offset;

    public OffsetSystem() {}
    public OffsetSystem(Zoom scale, Offset offset) {
        this.scale = scale;
        this.offset = offset;
    }
    public Zoom getScale() {
        return scale;
    }
    public void setScale(Zoom scale) {
        this.scale = scale;
    }
    public Offset getOffset() {
        return offset;
    }
    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "OffsetSystem{" +
                "scale=" + scale +
                ", offset=" + offset +
                '}';
    }

    // Inner class
    public static class Offset {

        public enum Choice {
            xy_chosen,
            ll_chosen
        }

        private Choice choice;
        private NodeListXY xy_chosen;
        private NodeListLL ll_chosen;

        public Choice getChoice() {
            return choice;
        }
        public void setChoice(Choice choice) {
            this.choice = choice;
        }
        public NodeListXY getXy_chosen() {
            return xy_chosen;
        }
        public void setXy_chosen(NodeListXY xy_chosen) {
            this.xy_chosen = xy_chosen;
        }
        public NodeListLL getLl_chosen() {
            return ll_chosen;
        }
        public void setLl_chosen(NodeListLL ll_chosen) {
            this.ll_chosen = ll_chosen;
        }
        @Override
        public String toString() {
            return "Offset{" +
                    "choice=" + choice +
                    '}';
        }

    }
}
