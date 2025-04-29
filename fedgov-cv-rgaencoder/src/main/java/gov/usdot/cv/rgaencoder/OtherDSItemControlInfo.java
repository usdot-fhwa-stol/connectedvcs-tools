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

package gov.usdot.cv.rgaencoder;

public class OtherDSItemControlInfo {
    private long messageID;
    private long enaAttributeID;

    public OtherDSItemControlInfo() {
        this.messageID = 0;
        this.enaAttributeID = 0;
    }

    public OtherDSItemControlInfo(long messageID, long enaAttributeID) {
        this.messageID = messageID;
        this.enaAttributeID = enaAttributeID;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public long getEnaAttributeID() {
        return enaAttributeID;
    }
    
    public void setEnaAttributeID(long enaAttributeID) {
        this.enaAttributeID = enaAttributeID;
    }

    @Override
    public String toString() {
        return "OtherDSItemControlInfo [messageID= " + messageID + ", enaAttributeID= " + enaAttributeID + "]";
    }
    
}