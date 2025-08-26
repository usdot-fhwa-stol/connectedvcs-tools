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

public class TravelerDataFrameList {

    private List<TravelerDataFrame> frames = new ArrayList<>();

    public TravelerDataFrameList() {
    }

    public TravelerDataFrameList(List<TravelerDataFrame> frames) {
        this.frames = frames;
    }

    public List<TravelerDataFrame> getFrames() {
        return frames;
    }

    public void setFrames(List<TravelerDataFrame> frames) {
        this.frames = frames;
    }

    public void addFrame(TravelerDataFrame frame) {
        if (frames == null) {
            frames = new ArrayList<>();
        }
        frames.add(frame);
    }

    @Override
    public String toString() {
        return "TravelerDataFrameList{" +
                "frames=" + frames +
                '}';
    }
}
