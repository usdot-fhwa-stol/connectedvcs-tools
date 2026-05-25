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

public class MsgId {

    public enum Choice {
        FURTHER_INFO_ID,
        ROAD_SIGN_ID
    }

    private Choice choice;
    private FurtherInfoID furtherInfoID;
    private RoadSignID roadSignID;

    public MsgId() {
    }

    public MsgId(FurtherInfoID furtherInfoID) {
        this.choice = Choice.FURTHER_INFO_ID;
        this.furtherInfoID = furtherInfoID;
    }

    public MsgId(RoadSignID roadSignID) {
        this.choice = Choice.ROAD_SIGN_ID;
        this.roadSignID = roadSignID;
    }

    public Choice getChoice() {
        return choice;
    }

    public FurtherInfoID getFurtherInfoID() {
        return furtherInfoID;
    }

    public void setFurtherInfoID(FurtherInfoID furtherInfoID) {
        this.choice = Choice.FURTHER_INFO_ID;
        this.furtherInfoID = furtherInfoID;
        this.roadSignID = null;
    }

    public RoadSignID getRoadSignID() {
        return roadSignID;
    }

    public void setRoadSignID(RoadSignID roadSignID) {
        this.choice = Choice.ROAD_SIGN_ID;
        this.roadSignID = roadSignID;
        this.furtherInfoID = null;
    }

    @Override
    public String toString() {
        return "MsgId{" +
                "choice=" + choice +
                ", furtherInfoID=" + furtherInfoID +
                ", roadSignID=" + roadSignID +
                '}';
    }
}
