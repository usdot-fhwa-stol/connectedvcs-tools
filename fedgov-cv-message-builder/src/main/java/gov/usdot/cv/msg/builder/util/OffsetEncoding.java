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

package gov.usdot.cv.msg.builder.util;

import gov.usdot.cv.mapencoder.NodeOffsetPointXY;
import gov.usdot.cv.mapencoder.NodeLLmD64b;
import gov.usdot.cv.mapencoder.NodeXY20b;
import gov.usdot.cv.mapencoder.NodeXY22b;
import gov.usdot.cv.mapencoder.NodeXY24b;
import gov.usdot.cv.mapencoder.NodeXY26b;
import gov.usdot.cv.mapencoder.NodeXY28b;
import gov.usdot.cv.mapencoder.NodeXY32b;
import gov.usdot.cv.rgaencoder.NodeXYZOffsetInfo;
import gov.usdot.cv.rgaencoder.NodeXYZOffsetValue;

public class OffsetEncoding {
    public OffsetEncodingType type = OffsetEncodingType.Standard;
    public OffsetEncodingSize size = OffsetEncodingSize.Offset32Bit;

    public OffsetEncoding(OffsetEncodingType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "OffsetEncoding [type=" + type + " size=" + size + "]";
    }

    public enum OffsetEncodingType {
        Standard, // 32 Bit
        Explicit, // 64 Bit
        Compact, // Automatic @ map level
        Tight; // Automatic @ node-to-node level
    }

    public enum OffsetEncodingSize {
        // Note the ordering of the enums based on size is important here!
        Offset20Bit(511), Offset22Bit(1023),
        Offset24Bit(2047), Offset26Bit(4095),
        Offset28Bit(8191), Offset32Bit(32767),
        Explicit64Bit(Integer.MAX_VALUE);

        private int maxSize;

        private OffsetEncodingSize(int maxSize) {
            this.maxSize = maxSize;
        }

        public static OffsetEncodingSize getOffsetEncodingSize(int offsetLength) {
            for (OffsetEncodingSize offsetEncodingSize : values()) {
                if (offsetLength <= offsetEncodingSize.maxSize) {
                    return offsetEncodingSize;
                }
            }
            return Explicit64Bit;
        }
    }

    public OffsetEncodingSize getOffsetEncodingSize(GeoPoint gp1, GeoPoint gp2) {
        switch (type) {
            case Compact: // Currently, this type is unsupported
            case Tight:
                int longestOffsetInCm = getLongerOffset(gp1, gp2);
                return OffsetEncodingSize.getOffsetEncodingSize(longestOffsetInCm);
            case Explicit:
                return OffsetEncodingSize.Explicit64Bit;
            case Standard: // Currently, this type is unsupported
            default:
                return OffsetEncodingSize.Offset32Bit;
        }
    }

    public static int getLongerOffset(GeoPoint gp1, GeoPoint gp2) {
        int latOffset = Math.abs(gp2.getLatOffsetInCentimeters(gp1));
        int lonOffset = Math.abs(gp2.getLonOffsetInCentimeters(gp1));

        return (latOffset > lonOffset) ? (latOffset) : (lonOffset);
    }

    public NodeOffsetPointXY encodeOffset(GeoPoint gp1, GeoPoint gp2) {
        NodeOffsetPointXY nodeOffsetPointXY = new NodeOffsetPointXY();
        switch (size) { 
            case Offset20Bit:
                NodeXY20b nodeXY20b = new NodeXY20b();
                nodeXY20b.setX(gp2.getLonOffsetInCentimeters(gp1));
                nodeXY20b.setY(gp2.getLatOffsetInCentimeters(gp1));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_XY1);
                nodeOffsetPointXY.setNodeXY1(nodeXY20b);
                break;
            case Offset22Bit:
                NodeXY22b nodeXY22b = new NodeXY22b();
                nodeXY22b.setX(gp2.getLonOffsetInCentimeters(gp1));
                nodeXY22b.setY(gp2.getLatOffsetInCentimeters(gp1));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_XY2);
                nodeOffsetPointXY.setNodeXY2(nodeXY22b);
                break;
            case Offset24Bit:
                NodeXY24b nodeXY24b = new NodeXY24b();
                nodeXY24b.setX(gp2.getLonOffsetInCentimeters(gp1));
                nodeXY24b.setY(gp2.getLatOffsetInCentimeters(gp1));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_XY3);
                nodeOffsetPointXY.setNodeXY3(nodeXY24b);
                break;
            case Offset26Bit:
                NodeXY26b nodeXY26b = new NodeXY26b();
                nodeXY26b.setX(gp2.getLonOffsetInCentimeters(gp1));
                nodeXY26b.setY(gp2.getLatOffsetInCentimeters(gp1));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_XY4);
                nodeOffsetPointXY.setNodeXY4(nodeXY26b);
                break;
            case Offset28Bit:
                NodeXY28b nodeXY28b = new NodeXY28b();
                nodeXY28b.setX(gp2.getLonOffsetInCentimeters(gp1));
                nodeXY28b.setY(gp2.getLatOffsetInCentimeters(gp1));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_XY5);
                nodeOffsetPointXY.setNodeXY5(nodeXY28b);
                break;
            case Offset32Bit:
                NodeXY32b nodeXY32b = new NodeXY32b();
                nodeXY32b.setX(gp2.getLonOffsetInCentimeters(gp1));
                nodeXY32b.setY(gp2.getLatOffsetInCentimeters(gp1));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_XY6);
                nodeOffsetPointXY.setNodeXY6(nodeXY32b);
                break;
            case Explicit64Bit:
            default:
                NodeLLmD64b nodeLLmD64b = new NodeLLmD64b();
                nodeLLmD64b.setLongitude(J2735Helper.convertGeoCoordinateToInt(gp2.getLon()));
                nodeLLmD64b.setLatitude(J2735Helper.convertGeoCoordinateToInt(gp2.getLat()));
                nodeOffsetPointXY.setChoice(NodeOffsetPointXY.NODE_LAT_LON);
                nodeOffsetPointXY.setNodeLatLon(nodeLLmD64b);
                break;
        }
        return nodeOffsetPointXY;
    }

    public NodeXYZOffsetInfo encodeRGAOffset(GeoPoint gp1, GeoPoint gp2) {
        NodeXYZOffsetInfo nodeXYZOffsetInfo = new NodeXYZOffsetInfo();
        NodeXYZOffsetValue nodeXOffsetValue = new NodeXYZOffsetValue();
        NodeXYZOffsetValue nodeYOffsetValue = new NodeXYZOffsetValue();
        NodeXYZOffsetValue nodeZOffsetValue = new NodeXYZOffsetValue();
        switch (size) {
            case Offset20Bit:
                nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B10);
                nodeXOffsetValue.setOffsetB10(gp2.getLonOffsetInCentimeters(gp1));
                nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B10);
                nodeYOffsetValue.setOffsetB10(gp2.getLatOffsetInCentimeters(gp1));
                nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B10);
                nodeZOffsetValue.setOffsetB10(gp2.getElevationOffsetInCentimeters(gp1));
                break;
            case Offset22Bit:
                nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B11);
                nodeXOffsetValue.setOffsetB11(gp2.getLonOffsetInCentimeters(gp1));
                nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B11);
                nodeYOffsetValue.setOffsetB11(gp2.getLatOffsetInCentimeters(gp1));
                nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B11);
                nodeZOffsetValue.setOffsetB11(gp2.getElevationOffsetInCentimeters(gp1));
                break;
            case Offset24Bit:
                nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B12);
                nodeXOffsetValue.setOffsetB12(gp2.getLonOffsetInCentimeters(gp1));
                nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B12);
                nodeYOffsetValue.setOffsetB12(gp2.getLatOffsetInCentimeters(gp1));
                nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B12);
                nodeZOffsetValue.setOffsetB12(gp2.getElevationOffsetInCentimeters(gp1));
                break;
            case Offset26Bit:
                nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B13);
                nodeXOffsetValue.setOffsetB13(gp2.getLonOffsetInCentimeters(gp1));
                nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B13);
                nodeYOffsetValue.setOffsetB13(gp2.getLatOffsetInCentimeters(gp1));
                nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B13);
                nodeZOffsetValue.setOffsetB13(gp2.getElevationOffsetInCentimeters(gp1));
                break;
            case Offset28Bit:
                nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B14);
                nodeXOffsetValue.setOffsetB14(gp2.getLonOffsetInCentimeters(gp1));
                nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B14);
                nodeYOffsetValue.setOffsetB14(gp2.getLatOffsetInCentimeters(gp1));
                nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B14);
                nodeZOffsetValue.setOffsetB14(gp2.getElevationOffsetInCentimeters(gp1));
                break;
            case Offset32Bit:
                nodeXOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B16);
                nodeXOffsetValue.setOffsetB16(gp2.getLonOffsetInCentimeters(gp1));
                nodeYOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B16);
                nodeYOffsetValue.setOffsetB16(gp2.getLatOffsetInCentimeters(gp1));
                nodeZOffsetValue.setChoice(NodeXYZOffsetValue.OFFSET_B16);
                nodeZOffsetValue.setOffsetB16(gp2.getElevationOffsetInCentimeters(gp1));
                break;
            default:
                break;
        }
        nodeXYZOffsetInfo.setNodeXOffsetValue(nodeXOffsetValue);
        nodeXYZOffsetInfo.setNodeYOffsetValue(nodeYOffsetValue);
        nodeXYZOffsetInfo.setNodeZOffsetValue(nodeZOffsetValue);
        return nodeXYZOffsetInfo;
    }
}
