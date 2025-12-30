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
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.usdot.cv.timencoder;

import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import gov.usdot.cv.mapencoder.Position3D;
import gov.usdot.cv.timencoder.GeographicalPath.Description;
import gov.usdot.cv.timencoder.GeographicalPath.Description.Choice;
import gov.usdot.cv.mapencoder.NodeListXY;
import gov.usdot.cv.mapencoder.NodeSetXY;
import gov.usdot.cv.mapencoder.NodeXY;
import gov.usdot.cv.mapencoder.NodeOffsetPointXY;
import gov.usdot.cv.mapencoder.NodeXY20b;
import gov.usdot.cv.mapencoder.NodeXY22b;
import gov.usdot.cv.mapencoder.NodeXY24b;
import gov.usdot.cv.mapencoder.NodeXY26b;
import gov.usdot.cv.mapencoder.NodeXY28b;
import gov.usdot.cv.mapencoder.NodeXY32b;
import gov.usdot.cv.mapencoder.NodeLLmD64b;
import gov.usdot.cv.mapencoder.NodeAttributeSetXY;
public class TIMEncodeTest {

        private static final Logger logger = LogManager.getLogger(TIMEncodeTest.class);

        // System under test
        private Encoder encoder;

        // Top-level TIM and frames
        private TravelerInformation mockTimData;
        private TravelerDataFrameList mockList;
        private TravelerDataFrame mockFrame1, mockFrame2, mockFrame3;

        // Per-frame fields
        private MinuteOfTheYear startTime1, startTime2, startTime3;
        private DYear startDYear1, startDYear2, startDYear3;
        private SignPriority priority1, priority2, priority3;
        private List<GeographicalPath> regions1, regions2, regions3;
        private GeographicalPath mockRegion1, mockRegion2, mockRegion3;
        private HeadingSlice headingSlice1, headingSlice2, headingSlice3;
        private Description mockDescription1, mockDescription2, mockDescription3;
        private GeometricProjection mockGeometricProjection1;
        private Circle mockCircle1;
        NodeListXY mockNodeListXY;
        NodeSetXY mockNodeSetXY;
        NodeXY mockNodeXY1;
        NodeOffsetPointXY mockNodeOffsetPointXY1;
        NodeXY20b mockNodeXY20b;
        NodeXY mockNodeXY2;
        NodeOffsetPointXY mockNodeOffsetPointXY2;
        NodeXY22b mockNodeXY22b;
        NodeXY mockNodeXY3;
        NodeOffsetPointXY mockNodeOffsetPointXY3;
        NodeXY24b mockNodeXY24b;
        NodeXY mockNodeXY4;
        NodeOffsetPointXY mockNodeOffsetPointXY4;
        NodeXY26b mockNodeXY26b;
        NodeXY mockNodeXY5;
        NodeOffsetPointXY mockNodeOffsetPointXY5;
        NodeXY28b mockNodeXY28b;
        NodeXY mockNodeXY6;
        NodeOffsetPointXY mockNodeOffsetPointXY6;
        NodeXY32b mockNodeXY32b;
        NodeXY mockNodeXY7;
        NodeOffsetPointXY mockNodeOffsetPointXY7;
        NodeLLmD64b mockNodeLLmD64b;
        OffsetSystem mockOffsetSystem;
        OffsetSystem.Offset mockOffset;
        NodeAttributeSetXY mockNodeAttributeSetXY;

        // Content (Part I) + ContentNew (Part III)
        private TravelerDataFrame.Content content1, content2, content3;
        private TravelerDataFrameNewPartIIIContent contentNew1, contentNew2, contentNew3;
        private SSPindex donotUse1, donotUse2, donotUse3, donotUse4;
        private MinutesDuration duration1, duration2, duration3;
        private HeadingSlice heading;

        @Before
        public void setUp() {
                // === SUT ===
                encoder = new Encoder();

                // === TravelerInformation ===
                mockTimData = mock(TravelerInformation.class);
                when(mockTimData.getMsgCnt()).thenReturn(10);

                long packetIdValue = 376597980L;

                ByteBuffer buf = ByteBuffer.allocate(9);
                buf.put((byte) 0); 
                buf.putLong(packetIdValue); 

                when(mockTimData.getPacketID()).thenReturn(new UniqueMSGID(buf.array()));

                // === TravelerDataFrameList & frames ===
                mockList = mock(TravelerDataFrameList.class);
                when(mockList.getFrameSize()).thenReturn(3);

                mockFrame1 = mock(TravelerDataFrame.class);
                mockFrame2 = mock(TravelerDataFrame.class);
                mockFrame3 = mock(TravelerDataFrame.class);

                List<TravelerDataFrame> mockFrames = mock(List.class);
                when(mockFrames.size()).thenReturn(3);
                when(mockFrames.get(0)).thenReturn(mockFrame1);
                when(mockFrames.get(1)).thenReturn(mockFrame2);
                when(mockFrames.get(2)).thenReturn(mockFrame3);
                when(mockList.getFrames()).thenReturn(mockFrames);

                when(mockTimData.getDataFrames()).thenReturn(mockList);

                // === Frame types ===
                when(mockFrame1.getFrameType()).thenReturn(TravelerInfoType.ADVISORY);
                when(mockFrame2.getFrameType()).thenReturn(TravelerInfoType.ROAD_SIGNAGE);
                when(mockFrame3.getFrameType()).thenReturn(TravelerInfoType.COMMERCIAL_SIGNAGE);
                
                // === Start Year ===
                startDYear1 = new DYear(2024);
                startDYear2 = new DYear(2025);
                startDYear3 = new DYear(2026);
                when(mockFrame1.getStartYear()).thenReturn(startDYear1);
                when(mockFrame2.getStartYear()).thenReturn(startDYear2);
                when(mockFrame3.getStartYear()).thenReturn(startDYear3);

                // === Start times ===
                startTime1 = new MinuteOfTheYear(349_920);
                startTime2 = new MinuteOfTheYear(362_880);
                startTime3 = new MinuteOfTheYear(373_470);
                when(mockFrame1.getStartTime()).thenReturn(startTime1);
                when(mockFrame2.getStartTime()).thenReturn(startTime2);
                when(mockFrame3.getStartTime()).thenReturn(startTime3);

                // === Priorities (init to avoid NPE) ===
                priority1 = new SignPriority();
                priority2 = new SignPriority();
                priority3 = new SignPriority();
                priority1.setValue(1);
                priority2.setValue(2);
                priority3.setValue(3);
                when(mockFrame1.getPriority()).thenReturn(priority1);
                when(mockFrame2.getPriority()).thenReturn(priority2);
                when(mockFrame3.getPriority()).thenReturn(priority3);
                // HeadingSLice
                heading = new HeadingSlice(0b0000000000001111);
                // === MsgId (RoadSignID w/ position + heading) ===
                Position3D pos = new Position3D(4.23369794E8, -8.30508547E8, 100, true);
                MUTCDCode mutcd = MUTCDCode.regulatory; 
                RoadSignID rsId = new RoadSignID(pos, heading);
                rsId.setMutcdCode(mutcd);

                MsgId msgId = new MsgId(rsId);
                when(mockFrame1.getMsgId()).thenReturn(msgId);
                when(mockFrame2.getMsgId()).thenReturn(msgId);
                when(mockFrame3.getMsgId()).thenReturn(msgId);

                // === Duration ===
                duration1 = new MinutesDuration(5);
                duration2 = new MinutesDuration(60);
                duration3 = new MinutesDuration(120);
                when(mockFrame1.getDurationTime()).thenReturn(duration1);
                when(mockFrame2.getDurationTime()).thenReturn(duration2);
                when(mockFrame3.getDurationTime()).thenReturn(duration3);

                // SSPindex
                donotUse1 = new SSPindex(4);
                donotUse2 = new SSPindex(3);
                donotUse3 = new SSPindex(2);
                donotUse4 = new SSPindex(1);
                when(mockFrame1.getDoNotUse1()).thenReturn(donotUse1);
                when(mockFrame2.getDoNotUse2()).thenReturn(donotUse2);
                when(mockFrame3.getDoNotUse3()).thenReturn(donotUse3);
                when(mockFrame3.getDoNotUse4()).thenReturn(donotUse4);

                // === Regions ===
                mockRegion1 = mock(GeographicalPath.class);
                mockRegion2 = mock(GeographicalPath.class);
                mockRegion3 = mock(GeographicalPath.class);

                when(mockRegion1.getAnchor()).thenReturn(new Position3D(4.13369794E8, -8.30508547E8, 100, true));
                when(mockRegion2.getAnchor()).thenReturn(new Position3D(4.23369794E8, -8.40508547E8, 200, true));
                when(mockRegion3.getAnchor()).thenReturn(new Position3D(4.33369794E8, -8.50508547E8, 300, true));

                when(mockRegion1.getLaneWidth()).thenReturn(360);
                when(mockRegion2.getLaneWidth()).thenReturn(361);
                when(mockRegion3.getLaneWidth()).thenReturn(362);

                when(mockRegion1.getDirectionality()).thenReturn(DirectionOfUse.forward);
                when(mockRegion2.getDirectionality()).thenReturn(DirectionOfUse.reverse);
                when(mockRegion3.getDirectionality()).thenReturn(DirectionOfUse.both);

                when(mockRegion1.isClosedPath()).thenReturn(true);
                when(mockRegion2.isClosedPath()).thenReturn(false);
                when(mockRegion3.isClosedPath()).thenReturn(true);

                headingSlice1 = new HeadingSlice(0b1010000000000000);
                headingSlice2 = new HeadingSlice(0b1110000000000000);
                headingSlice3 = new HeadingSlice(0b1111000000000000);

                when(mockRegion1.getDirection()).thenReturn(headingSlice1);
                when(mockRegion2.getDirection()).thenReturn(headingSlice2);
                when(mockRegion3.getDirection()).thenReturn(headingSlice3);

                mockDescription1 = mock(Description.class);
                when(mockDescription1.getChoice()).thenReturn(Choice.geometry_chosen);
                mockGeometricProjection1 = mock(GeometricProjection.class);
                mockCircle1 = mock(Circle.class);
                HeadingSlice headingSlice4 = new HeadingSlice(0b1111101000000000);
                when(mockGeometricProjection1.getHeadingSlice()).thenReturn(headingSlice4);
                when(mockGeometricProjection1.getExtent()).thenReturn(Extent.useFor3meters);
                when(mockGeometricProjection1.getLaneWidth()).thenReturn(370);
                when(mockCircle1.getCenter()).thenReturn(new Position3D(4.53369794E8, -8.30508547E8, 50, true));
                when(mockCircle1.getRadius()).thenReturn(new Radius_B12(30));
                when(mockCircle1.getUnits()).thenReturn(DistanceUnits.meter);
                when(mockGeometricProjection1.getCircle()).thenReturn(mockCircle1);
                when(mockDescription1.getGeometryChosen()).thenReturn(mockGeometricProjection1);
                when(mockRegion1.getDescription()).thenReturn(mockDescription1);

                NodeListXY mockNodeListXY = mock(NodeListXY.class);
                NodeSetXY mockNodeSetXY = mock(NodeSetXY.class);

                NodeXY mockNodeXY1 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY1 = mock(NodeOffsetPointXY.class);
                NodeXY20b mockNodeXY20b = mock(NodeXY20b.class);

                NodeXY mockNodeXY2 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY2 = mock(NodeOffsetPointXY.class);
                NodeXY22b mockNodeXY22b = mock(NodeXY22b.class);

                NodeXY mockNodeXY3 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY3 = mock(NodeOffsetPointXY.class);
                NodeXY24b mockNodeXY24b = mock(NodeXY24b.class);

                NodeXY mockNodeXY4 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY4 = mock(NodeOffsetPointXY.class);
                NodeXY26b mockNodeXY26b = mock(NodeXY26b.class);

                NodeXY mockNodeXY5 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY5 = mock(NodeOffsetPointXY.class);
                NodeXY28b mockNodeXY28b = mock(NodeXY28b.class);

                NodeXY mockNodeXY6 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY6 = mock(NodeOffsetPointXY.class);
                NodeXY32b mockNodeXY32b = mock(NodeXY32b.class);

                NodeXY mockNodeXY7 = mock(NodeXY.class);
                NodeOffsetPointXY mockNodeOffsetPointXY7 = mock(NodeOffsetPointXY.class);
                NodeLLmD64b mockNodeLLmD64b = mock(NodeLLmD64b.class);

                mockNodeAttributeSetXY = mock(NodeAttributeSetXY.class);
                mockOffsetSystem = mock(OffsetSystem.class);
                mockOffset = mock(OffsetSystem.Offset.class);

                // === Delta encodings for each node ===
                when(mockNodeXY20b.getX()).thenReturn(2F);
                when(mockNodeXY20b.getY()).thenReturn(3F);
                when(mockNodeOffsetPointXY1.getChoice()).thenReturn((byte) 1);
                when(mockNodeOffsetPointXY1.getNodeXY1()).thenReturn(mockNodeXY20b);
                when(mockNodeXY1.getDelta()).thenReturn(mockNodeOffsetPointXY1);
                when(mockNodeXY1.isAttributesExists()).thenReturn(false);

                when(mockNodeXY22b.getX()).thenReturn(4F);
                when(mockNodeXY22b.getY()).thenReturn(5F);
                when(mockNodeOffsetPointXY2.getChoice()).thenReturn((byte) 2);
                when(mockNodeOffsetPointXY2.getNodeXY2()).thenReturn(mockNodeXY22b);
                when(mockNodeXY2.getDelta()).thenReturn(mockNodeOffsetPointXY2);
                when(mockNodeXY2.isAttributesExists()).thenReturn(false);

                when(mockNodeXY24b.getX()).thenReturn((short) 6);
                when(mockNodeXY24b.getY()).thenReturn((short) 7);
                when(mockNodeOffsetPointXY3.getChoice()).thenReturn((byte) 3);
                when(mockNodeOffsetPointXY3.getNodeXY3()).thenReturn(mockNodeXY24b);
                when(mockNodeXY3.getDelta()).thenReturn(mockNodeOffsetPointXY3);
                when(mockNodeXY3.isAttributesExists()).thenReturn(false);

                when(mockNodeXY26b.getX()).thenReturn(8F);
                when(mockNodeXY26b.getY()).thenReturn(9F);
                when(mockNodeOffsetPointXY4.getChoice()).thenReturn((byte) 4);
                when(mockNodeOffsetPointXY4.getNodeXY4()).thenReturn(mockNodeXY26b);
                when(mockNodeXY4.getDelta()).thenReturn(mockNodeOffsetPointXY4);
                when(mockNodeXY4.isAttributesExists()).thenReturn(false);
               
                when(mockNodeXY28b.getX()).thenReturn(10F);
                when(mockNodeXY28b.getY()).thenReturn(11F);
                when(mockNodeOffsetPointXY5.getChoice()).thenReturn((byte) 5);
                when(mockNodeOffsetPointXY5.getNodeXY5()).thenReturn(mockNodeXY28b);
                when(mockNodeXY5.getDelta()).thenReturn(mockNodeOffsetPointXY5);
                when(mockNodeXY5.isAttributesExists()).thenReturn(false);

                when(mockNodeXY32b.getX()).thenReturn(12F);
                when(mockNodeXY32b.getY()).thenReturn(13F);
                when(mockNodeOffsetPointXY6.getChoice()).thenReturn((byte) 6);
                when(mockNodeOffsetPointXY6.getNodeXY6()).thenReturn(mockNodeXY32b);
                when(mockNodeXY6.getDelta()).thenReturn(mockNodeOffsetPointXY6);
                when(mockNodeXY6.isAttributesExists()).thenReturn(false);

                when(mockNodeLLmD64b.getLatitude()).thenReturn(14);
                when(mockNodeLLmD64b.getLongitude()).thenReturn(15);
                when(mockNodeOffsetPointXY7.getChoice()).thenReturn((byte) 7);
                when(mockNodeOffsetPointXY7.getNodeLatLon()).thenReturn(mockNodeLLmD64b);
                when(mockNodeXY7.getDelta()).thenReturn(mockNodeOffsetPointXY7);
                when(mockNodeXY7.isAttributesExists()).thenReturn(true);
                when(mockNodeAttributeSetXY.isDWidthExists()).thenReturn(true);
                when(mockNodeAttributeSetXY.getDWidth()).thenReturn((float)12);
                when(mockNodeAttributeSetXY.isDElevationExists()).thenReturn(true);
                when(mockNodeAttributeSetXY.getDElevation()).thenReturn((float)50);
                
                when(mockNodeXY7.getAttributes()).thenReturn(mockNodeAttributeSetXY);

                // === NodeSetXY and NodeListXY ===
                when(mockNodeSetXY.getNodeSetXY()).thenReturn(new NodeXY[] {
                                mockNodeXY1, mockNodeXY2, mockNodeXY3, mockNodeXY4,
                                mockNodeXY5, mockNodeXY6, mockNodeXY7
                });
                when(mockNodeListXY.getNodes()).thenReturn(mockNodeSetXY);
                when(mockOffset.getXy_chosen()).thenReturn(mockNodeListXY);
                when(mockOffsetSystem.getOffset()).thenReturn(mockOffset);
                // === Hook NodeListXY into Description ===
                mockDescription2 = mock(Description.class);
                when(mockDescription2.getChoice()).thenReturn(Choice.path_chosen);
                when(mockDescription2.getPathChosen()).thenReturn(mockOffsetSystem);

                // === Hook Description into Region 2 ===
                when(mockRegion2.getDescription()).thenReturn(mockDescription2);

                regions1 = Arrays.asList(mockRegion1);
                regions2 = Arrays.asList(mockRegion2);
                regions3 = Arrays.asList(mockRegion3);

                when(mockFrame1.getRegions()).thenReturn(regions1);
                when(mockFrame2.getRegions()).thenReturn(regions2);
                when(mockFrame3.getRegions()).thenReturn(regions3);

                // === Content (Part I) ===
                // WorkZone (real object)
                WorkZone wz = new WorkZone();
                wz.getItems().add(new WorkZone.WorkZoneItem(new ITIScodes(1071)));
                wz.getItems().add(new WorkZone.WorkZoneItem(new ITIScodes(1072)));
                wz.getItems().add(new WorkZone.WorkZoneItem(new ITIScodes(1073)));
                wz.getItems().add(new WorkZone.WorkZoneItem(new ITISTextPhrase("Road Work Ahead")));
                logger.debug("WorkZone items: {}", wz.getItems().size()); // 4

                // GenericSignage (real object)
                GenericSignage genericSignage = new GenericSignage();
                genericSignage.getItems().add(new GenericSignage.GenericSignageItem(new ITIScodes(1001)));
                genericSignage.getItems().add(new GenericSignage.GenericSignageItem(new ITISTextPhrase("Speed n30")));
                genericSignage.getItems().add(new GenericSignage.GenericSignageItem(new ITISTextPhrase("n35")));
                logger.debug("GenericSignage items: {}", genericSignage.getItems().size()); // 3

                // Advisory
                ITIScodesAndText advisory = new ITIScodesAndText();
                advisory.getItems().add(new ITIScodesAndText.Item(new ITIScodes(2001)));
                advisory.getItems().add(new ITIScodesAndText.Item(new ITISTextPhrase("Slippery Wet")));

                // Content for frame1 (WorkZone)
                content1 = mock(TravelerDataFrame.Content.class);
                when(content1.getChoice()).thenReturn(TravelerDataFrame.Content.Choice.WORK_ZONE);
                when(content1.getWorkZone()).thenReturn(wz);

                // Content for frame2 (Generic Sign)
                content2 = mock(TravelerDataFrame.Content.class);
                when(content2.getChoice()).thenReturn(TravelerDataFrame.Content.Choice.GENERIC_SIGN);
                when(content2.getGenericSign()).thenReturn(genericSignage);

                // Content for frame3 (WorkZone)
                content3 = mock(TravelerDataFrame.Content.class);
                when(content3.getChoice()).thenReturn(TravelerDataFrame.Content.Choice.ADVISORY);
                when(content3.getAdvisory()).thenReturn(advisory);

                // Attach content to frames
                when(mockFrame1.getContent()).thenReturn(content1);
                when(mockFrame2.getContent()).thenReturn(content2);
                when(mockFrame3.getContent()).thenReturn(content3);

                // === ContentNew (Part III) ===
                // Frame 1: Portland Cement, DRY
                PortlandCement pc1 = new PortlandCement(PortlandCementType.PortlandCementType_newSharp);
                DescriptionOfRoadSurface desc1 = new DescriptionOfRoadSurface(pc1);
                FrictionInformation fric1 = new FrictionInformation(desc1);
                fric1.setDryOrWet(RoadSurfaceCondition.DRY);
                contentNew1 = new TravelerDataFrameNewPartIIIContent();
                contentNew1.setFrictionInformation(fric1);
                when(mockFrame1.getContentNew()).thenReturn(contentNew1);

                // Frame 2: Asphalt/Tar, WET
                AsphaltOrTar at2 = new AsphaltOrTar(AsphaltOrTarType.AsphaltOrTarType_trafficPolished);
                DescriptionOfRoadSurface desc2 = new DescriptionOfRoadSurface(at2);
                FrictionInformation fric2 = new FrictionInformation(desc2);
                fric2.setDryOrWet(RoadSurfaceCondition.WET);
                contentNew2 = new TravelerDataFrameNewPartIIIContent();
                contentNew2.setFrictionInformation(fric2);
                when(mockFrame2.getContentNew()).thenReturn(contentNew2);

                // Frame 3: Snow (WET) — note: gravel created but friction uses snow desc per
                // original
                Snow snow = new Snow(SnowType.SnowType_loose);
                DescriptionOfRoadSurface descSnow = new DescriptionOfRoadSurface(snow);
                FrictionInformation fric3 = new FrictionInformation(descSnow);
                fric3.setDryOrWet(RoadSurfaceCondition.WET);
                contentNew3 = new TravelerDataFrameNewPartIIIContent();
                contentNew3.setFrictionInformation(fric3);
                when(mockFrame3.getContentNew()).thenReturn(contentNew3);

        }

        @Test
        public void TIM_encode_test() {
                ByteArrayObject result = encoder.encode(mockTimData);
                Assert.assertNotNull("Byte array should not be null", result.getMessage());
                byte[] expected = new byte[] {
                        0x00, 0x1F, (byte) 0x80, (byte) 0xF5, 0x20, (byte) 0xA0, 0x00, 0x00, 0x00, 0x00,
                        0x01, 0x67, 0x26, (byte) 0xDD, (byte) 0xC5, (byte) 0x88, 0x72, (byte) 0x9D, (byte) 0xC2, 0x0A,
                        (byte) 0x84, 0x73, (byte) 0x92, (byte) 0x87, (byte) 0xF8, 0x20, (byte) 0xC8, 0x00, 0x1E, 0x2F,
                        (byte) 0xD0, (byte) 0xAA, (byte) 0xDC, 0x00, 0x01, 0x48, 0x00, 0x7E, 0x53, (byte) 0x92,
                        0x1B, (byte) 0xB0, (byte) 0x8E, 0x72, 0x50, (byte) 0xFF, 0x04, 0x19, 0x00, (byte) 0xB4,
                        0x3A, 0x00, 0x02, (byte) 0xDF, 0x40, 0x02, 0x05, (byte) 0xC9, 0x50, (byte) 0xAA,
                        (byte) 0xC8, (byte) 0xC2, 0x39, (byte) 0xC9, 0x43, (byte) 0xFC, 0x10, 0x32, 0x01, (byte) 0xE6,
                        0x00, 0x13, 0x02, 0x17, (byte) 0x81, 0x0C, 0x00, (byte) 0x86, 0x3E, (byte) 0xA5,
                        (byte) 0xBF, 0x0E, 0x44, 0x15, (byte) 0xF7, (byte) 0xF2, (byte) 0xD6, (byte) 0x82, 0x0E,
                        (byte) 0x8C, (byte) 0xB8, 0x72, 0x00, (byte) 0x81, 0x10, 0x00, 0x60, 0x2C, (byte) 0xA7,
                        0x70, (byte) 0x82, (byte) 0xA1, 0x1C, (byte) 0xE4, (byte) 0xA1, (byte) 0xFE, 0x08, 0x32,
                        0x00, 0x07, (byte) 0x8B, (byte) 0xF4, (byte) 0xAC, 0x4C, 0x00, 0x03, (byte) 0xC4, 0x30,
                        0x1F, (byte) 0x94, (byte) 0xEE, 0x10, 0x54, 0x23, (byte) 0x93, 0x0A, (byte) 0xD7, (byte) 0xC1,
                        0x0C, (byte) 0x80, 0x2D, 0x33, (byte) 0x80, 0x00, 0x00, (byte) 0xA0, (byte) 0x80, (byte) 0xA0,
                        0x30, (byte) 0xC0, 0x48, 0x0A, 0x28, 0x06, (byte) 0x80, 0x71, (byte) 0xC0, 0x22,
                        0x01, 0x24, (byte) 0x80, 0x2A, 0x00, (byte) 0xB2, (byte) 0xC0, 0x06, 0x40, 0x06,
                        (byte) 0xB9, (byte) 0xAD, 0x27, 0x48, 0x39, (byte) 0xAD, 0x27, 0x48, 0x70, 0x34,
                        0x19, 0x19, 0x00, 0x08, (byte) 0x80, 0x7D, 0x38, (byte) 0xA7, (byte) 0xC3, 0x2E,
                        0x5C, (byte) 0x88, 0x37, 0x33, 0x61, 0x2D, (byte) 0xCC, (byte) 0xDA, (byte) 0x80,
                        (byte) 0x81, 0x10, (byte) 0x92, 0x60, 0x3C, (byte) 0xA7, 0x70, (byte) 0x82, (byte) 0xA1,
                        0x1C, (byte) 0xE4, (byte) 0xA1, (byte) 0xFE, 0x08, 0x32, 0x00, 0x07, (byte) 0x8B,
                        (byte) 0xF5, 0x2D, (byte) 0x96, (byte) 0xF0, 0x07, (byte) 0x86, 0x00, 0x1F, 0x14,
                        (byte) 0xF7, (byte) 0x99, (byte) 0xBC, 0x23, (byte) 0x89, (byte) 0x81, 0x6F, (byte) 0xC1,
                        0x12, (byte) 0xC0, 0x2D, 0x5F, (byte) 0xC0, 0x00, 0x41, 0x00, 0x40, (byte) 0xFA,
                        0x30, 0x5D, 0x3D, (byte) 0x9A, 0x78, 0x70, (byte) 0xCB, (byte) 0xCB, (byte) 0xCA,
                        0x0A, (byte) 0xF9, 0x7A, 0x00, (byte) 0x81, 0x13, (byte) 0x94, 0x00
                    };
                    

                Assert.assertArrayEquals(
                "Encoded TIM message doesn't match expected output",
                expected,
                result.getMessage());
        }

}
