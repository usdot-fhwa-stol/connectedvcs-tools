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

import java.util.Arrays;

/**
 * TravelerDataFrame ::= SEQUENCE {
 *   doNotUse1    SSPindex,
 *   frameType    TravelerInfoType,
 *   msgId        MsgId,   -- CHOICE implemented separately
 *   startTime    MinuteOfTheYear,
 *   durationTime MinutesDuration,
 *   priority     SignPriority,
 *
 *   doNotUse2    SSPindex,
 *   regions      SEQUENCE (SIZE(1..16)) OF GeographicalPath,
 *
 *   doNotUse3    SSPindex,
 *   doNotUse4    SSPindex,
 *   content      Content, -- CHOICE defined below
 *   contentNew   TravelerDataFrameNewPartIIIContent
 * }
 */
public class TravelerDataFrame {

    // ---- Part I: header ----
    private SSPindex doNotUse1;
    private TravelerInfoType frameType;
    private MsgId msgId;   // uses your standalone MsgId
    private MinuteOfTheYear startTime;
    private MinutesDuration durationTime;
    private SignPriority priority;

    // ---- Part II: regions ----
    private SSPindex doNotUse2;
    private GeographicalPath[] regions;

    // ---- Part III: content ----
    private SSPindex doNotUse3;
    private SSPindex doNotUse4;
    private Content content;
    private TravelerDataFrameNewPartIIIContent contentNew;

    // ---- CHOICE: content ----
    public static class Content {
        public enum Choice { ADVISORY, WORK_ZONE, GENERIC_SIGN, SPEED_LIMIT, EXIT_SERVICE }

        private Choice choice;
        private ITIScodesAndText advisory;
        private WorkZone workZone;
        private GenericSignage genericSign;
        private SpeedLimit speedLimit;
        private ExitService exitService;

        public Content(Choice choice) { this.choice = choice; }

        public Choice getChoice() { return choice; }
        public ITIScodesAndText getAdvisory() { return advisory; }
        public WorkZone getWorkZone() { return workZone; }
        public GenericSignage getGenericSign() { return genericSign; }
        public SpeedLimit getSpeedLimit() { return speedLimit; }
        public ExitService getExitService() { return exitService; }

        public void setAdvisory(ITIScodesAndText v) {
            this.choice = Choice.ADVISORY;
            this.advisory = v;
            clearOthers();
        }

        public void setWorkZone(WorkZone v) {
            this.choice = Choice.WORK_ZONE;
            this.workZone = v;
            clearOthers();
        }

        public void setGenericSign(GenericSignage v) {
            this.choice = Choice.GENERIC_SIGN;
            this.genericSign = v;
            clearOthers();
        }

        public void setSpeedLimit(SpeedLimit v) {
            this.choice = Choice.SPEED_LIMIT;
            this.speedLimit = v;
            clearOthers();
        }

        public void setExitService(ExitService v) {
            this.choice = Choice.EXIT_SERVICE;
            this.exitService = v;
            clearOthers();
        }

        private void clearOthers() {
            if (choice != Choice.ADVISORY) advisory = null;
            if (choice != Choice.WORK_ZONE) workZone = null;
            if (choice != Choice.GENERIC_SIGN) genericSign = null;
            if (choice != Choice.SPEED_LIMIT) speedLimit = null;
            if (choice != Choice.EXIT_SERVICE) exitService = null;
        }

        @Override
        public String toString() {
            return "Content{" +
                    "choice=" + choice +
                    ", advisory=" + advisory +
                    ", workZone=" + workZone +
                    ", genericSign=" + genericSign +
                    ", speedLimit=" + speedLimit +
                    ", exitService=" + exitService +
                    '}';
        }
    }

    // ---- Getters & Setters ----
    public SSPindex getDoNotUse1() { return doNotUse1; }
    public void setDoNotUse1(SSPindex doNotUse1) { this.doNotUse1 = doNotUse1; }

    public TravelerInfoType getFrameType() { return frameType; }
    public void setFrameType(TravelerInfoType frameType) { this.frameType = frameType; }

    public MsgId getMsgId() { return msgId; }
    public void setMsgId(MsgId msgId) { this.msgId = msgId; }

    public MinuteOfTheYear getStartTime() { return startTime; }
    public void setStartTime(MinuteOfTheYear startTime) { this.startTime = startTime; }

    public MinutesDuration getDurationTime() { return durationTime; }
    public void setDurationTime(MinutesDuration durationTime) { this.durationTime = durationTime; }

    public SignPriority getPriority() { return priority; }
    public void setPriority(SignPriority priority) { this.priority = priority; }

    public SSPindex getDoNotUse2() { return doNotUse2; }
    public void setDoNotUse2(SSPindex doNotUse2) { this.doNotUse2 = doNotUse2; }

    public GeographicalPath[] getRegions() { return regions; }
    public void setRegions(GeographicalPath[] regions) { this.regions = regions; }

    public SSPindex getDoNotUse3() { return doNotUse3; }
    public void setDoNotUse3(SSPindex doNotUse3) { this.doNotUse3 = doNotUse3; }

    public SSPindex getDoNotUse4() { return doNotUse4; }
    public void setDoNotUse4(SSPindex doNotUse4) { this.doNotUse4 = doNotUse4; }

    public Content getContent() { return content; }
    public void setContent(Content content) { this.content = content; }

    public TravelerDataFrameNewPartIIIContent getContentNew() { return contentNew; }
    public void setContentNew(TravelerDataFrameNewPartIIIContent contentNew) { this.contentNew = contentNew; }

    @Override
    public String toString() {
        return "TravelerDataFrame{" +
                "frameType=" + frameType +
                ", msgId=" + msgId +
                ", startTime=" + startTime +
                ", durationTime=" + durationTime +
                ", priority=" + priority +
                ", regions=" + (regions == null ? null : Arrays.toString(regions)) +
                ", content=" + content +
                ", contentNew=" + contentNew +
                '}';
    }
}
