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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import gov.usdot.cv.msg.builder.util.ObjectPrinter;

import gov.usdot.cv.mapencoder.Position3D;

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
        private SignPriority priority1, priority2, priority3;
        private GeographicalPath[] regions1, regions2, regions3;

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
                Position3D pos = new Position3D(34.0522, -118.2437, 100, true);
                MsgId msgId = new MsgId(new RoadSignID(pos, heading));
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
                // All the fields are optional will add in later story
                regions1 = new GeographicalPath[] { new GeographicalPath() };
                regions2 = new GeographicalPath[] { new GeographicalPath(), new GeographicalPath() };
                regions3 = new GeographicalPath[] { new GeographicalPath(), new GeographicalPath(),
                                new GeographicalPath() };
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
                                (byte) 0x00, (byte) 0x1F, (byte) 0x80, (byte) 0x88, (byte) 0x00, (byte) 0xA5,
                                (byte) 0x08, (byte) 0x62,
                                (byte) 0x93, (byte) 0xE1, (byte) 0xBA, (byte) 0x20, (byte) 0x49, (byte) 0x9E,
                                (byte) 0x86, (byte) 0xEE,
                                (byte) 0x20, (byte) 0xC8, (byte) 0x00, (byte) 0x1E, (byte) 0xAA, (byte) 0xDC,
                                (byte) 0x00, (byte) 0x01,
                                (byte) 0x48, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x09, (byte) 0x81,
                                (byte) 0x0B, (byte) 0xC0,
                                (byte) 0x86, (byte) 0x00, (byte) 0x43, (byte) 0x1F, (byte) 0x52, (byte) 0xDF,
                                (byte) 0x87, (byte) 0x22,
                                (byte) 0x0A, (byte) 0xFB, (byte) 0xF9, (byte) 0x6B, (byte) 0x41, (byte) 0x07,
                                (byte) 0x46, (byte) 0x5C,
                                (byte) 0x39, (byte) 0x00, (byte) 0x40, (byte) 0x88, (byte) 0x00, (byte) 0x20,
                                (byte) 0x14, (byte) 0x52,
                                (byte) 0x7C, (byte) 0x37, (byte) 0x44, (byte) 0x09, (byte) 0x33, (byte) 0xD0,
                                (byte) 0xDD, (byte) 0xC4,
                                (byte) 0x19, (byte) 0x00, (byte) 0x03, (byte) 0xD6, (byte) 0x26, (byte) 0x00,
                                (byte) 0x01, (byte) 0xE2,
                                (byte) 0x18, (byte) 0x80, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x88,
                                (byte) 0x07, (byte) 0xD3,
                                (byte) 0x8A, (byte) 0x7C, (byte) 0x32, (byte) 0xE5, (byte) 0xC8, (byte) 0x83,
                                (byte) 0x73, (byte) 0x36,
                                (byte) 0x12, (byte) 0xDC, (byte) 0xCD, (byte) 0xA8, (byte) 0x08, (byte) 0x11,
                                (byte) 0x09, (byte) 0x24,
                                (byte) 0x03, (byte) 0x8A, (byte) 0x4F, (byte) 0x86, (byte) 0xE8, (byte) 0x81,
                                (byte) 0x26, (byte) 0x7A,
                                (byte) 0x1B, (byte) 0xB8, (byte) 0x83, (byte) 0x20, (byte) 0x00, (byte) 0x7A,
                                (byte) 0xD9, (byte) 0x6F,
                                (byte) 0x00, (byte) 0x78, (byte) 0x60, (byte) 0x20, (byte) 0x00, (byte) 0x00,
                                (byte) 0x00, (byte) 0x04,
                                (byte) 0x10, (byte) 0x04, (byte) 0x0F, (byte) 0xA3, (byte) 0x05, (byte) 0xD3,
                                (byte) 0xD9, (byte) 0xA7,
                                (byte) 0x87, (byte) 0x0C, (byte) 0xBC, (byte) 0xBC, (byte) 0xA0, (byte) 0xAF,
                                (byte) 0x97, (byte) 0xA0,
                                (byte) 0x08, (byte) 0x11, (byte) 0x39, (byte) 0x40
                };

                Assert.assertArrayEquals(
                                "Encoded TIM message doesn't match expected output",
                                expected,
                                result.getMessage());

        }



}
