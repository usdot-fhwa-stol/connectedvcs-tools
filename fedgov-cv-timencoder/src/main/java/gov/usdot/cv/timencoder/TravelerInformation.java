/*
 * Copyright (C) 2025 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.usdot.cv.timencoder;

public class TravelerInformation {

    private int msgCnt;
    private MinuteOfTheYear timeStamp;
    private TravelerDataFrameList dataFrames;

    /*
     * This is optional in ASN.1; will implement in a later story:
     *
     * private UniqueMSGID packetID;
     * private URLBase urlB;
     * private List<RegionalExtension> regional; // SIZE(1..4)
     */

    public TravelerInformation() {
    }

    public TravelerInformation(int msgCnt, TravelerDataFrameList dataFrames) {
        this.msgCnt = msgCnt;
        this.dataFrames = dataFrames;
    }

    public int getMsgCnt() {
        return msgCnt;
    }

    public void setMsgCnt(int msgCnt) {
        this.msgCnt = msgCnt;
    }

    public MinuteOfTheYear getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(MinuteOfTheYear timeStamp) {
        this.timeStamp = timeStamp;
    }

    public TravelerDataFrameList getDataFrames() {
        return dataFrames;
    }

    public void setDataFrames(TravelerDataFrameList dataFrames) {
        this.dataFrames = dataFrames;
    }

    @Override
    public String toString() {
        return "TravelerInformation{" +
                "msgCnt=" + msgCnt +
                ", timeStamp=" + timeStamp +
                ", dataFrames=" + dataFrames +
                '}';
    }
}
