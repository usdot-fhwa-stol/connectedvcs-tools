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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import gov.usdot.cv.mapencoder.Position3D;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TIMEncodeTest {
    private static final Logger logger = LogManager.getLogger(TIMEncodeTest.class);

    private Encoder encoder;
    private TravelerInformation mockTimData;
    private TravelerDataFrameList mockList;
    private TravelerDataFrame mockFrame1, mockFrame2, mockFrame3;
    private SSPindex doNotUse1, doNotUse2, doNotUse3, doNotUse4;
    private MsgId msgId1, msgId2, msgId3;
    private Position3D position1, position2, position3;
    private MinuteOfTheYear startTime1, startTime2, startTime3;
    private SignPriority priority1, priority2, priority3;
    private GeographicalPath[] regions1, regions2, regions3;
    private TravelerDataFrame.Content content1, content2, content3;
    private TravelerDataFrameNewPartIIIContent contentNew1, contentNew2, contentNew3;

    @Before
    public void setUp() {
        encoder = new Encoder();

        // Mock TravelerInformation
        mockTimData = mock(TravelerInformation.class);
        when(mockTimData.getMsgCnt()).thenReturn(10);

        // Mock TravelerDataFrameList
        mockList = mock(TravelerDataFrameList.class);
        when(mockList.getframesize()).thenReturn(3);
        mockFrame1 = mock(TravelerDataFrame.class);
        mockFrame2 = mock(TravelerDataFrame.class);
        mockFrame3 = mock(TravelerDataFrame.class);
        // mockinfo type

        // start time
        startTime1 = new MinuteOfTheYear(100);
        startTime2 = new MinuteOfTheYear(200);
        startTime3 = new MinuteOfTheYear(300);

        // Optional: if encoder calls getFrames()
        java.util.List<TravelerDataFrame> mockFrames = mock(java.util.List.class);
        when(mockFrames.size()).thenReturn(3);
        when(mockFrames.get(0)).thenReturn(mockFrame1);
        when(mockFrames.get(1)).thenReturn(mockFrame2);
        when(mockFrames.get(2)).thenReturn(mockFrame3);
        when(mockList.getFrames()).thenReturn(mockFrames);

        // Mock TravelerDataFrame
        when(mockFrame1.getFrameType()).thenReturn(TravelerInfoType.ADVISORY);
        when(mockFrame2.getFrameType()).thenReturn(TravelerInfoType.ROAD_SIGNAGE);
        when(mockFrame3.getFrameType()).thenReturn(TravelerInfoType.COMMERCIAL_SIGNAGE);

        // doNotUse1
        when(mockFrame1.getDoNotUse1()).thenReturn(new SSPindex(0));
        when(mockFrame2.getDoNotUse1()).thenReturn(new SSPindex(0));
        when(mockFrame3.getDoNotUse1()).thenReturn(new SSPindex(0));
        // doNotUse2
        when(mockFrame1.getDoNotUse2()).thenReturn(new SSPindex(0));
        when(mockFrame2.getDoNotUse2()).thenReturn(new SSPindex(0));
        when(mockFrame3.getDoNotUse2()).thenReturn(new SSPindex(0));
        // doNotUse3
        when(mockFrame1.getDoNotUse3()).thenReturn(new SSPindex(0));
        when(mockFrame2.getDoNotUse3()).thenReturn(new SSPindex(0));
        when(mockFrame3.getDoNotUse3()).thenReturn(new SSPindex(0));
        // doNotUse4
        when(mockFrame1.getDoNotUse4()).thenReturn(new SSPindex(0));
        when(mockFrame2.getDoNotUse4()).thenReturn(new SSPindex(0));
        when(mockFrame3.getDoNotUse4()).thenReturn(new SSPindex(4));

        // MsgID
        byte[] bytes = new byte[] { (byte) (3003 >> 8), (byte) (3003 & 0xFF) };

        when(mockFrame1.getMsgId()).thenReturn(new MsgId(
                new RoadSignID(new Position3D(34.0522, -118.2437, 100, true), new HeadingSlice(0b0000000000001111))));
        when(mockFrame2.getMsgId()).thenReturn(new MsgId(
                new RoadSignID(new Position3D(34.0522, -118.2437, 100, true), new HeadingSlice(0b0000000000001111))));
        when(mockFrame3.getMsgId()).thenReturn(new MsgId(
                new RoadSignID(new Position3D(34.0522, -118.2437, 100, true), new HeadingSlice(0b0000000000001111))));

        // start time
        when(mockFrame1.getStartTime()).thenReturn(startTime1);
        when(mockFrame2.getStartTime()).thenReturn(startTime2);
        when(mockFrame3.getStartTime()).thenReturn(startTime3);
        // priority
        when(mockFrame1.getPriority()).thenReturn(new SignPriority(1));
        when(mockFrame2.getPriority()).thenReturn(new SignPriority(2));
        when(mockFrame3.getPriority()).thenReturn(new SignPriority(3));

        // regions
        regions1 = new GeographicalPath[] { new GeographicalPath() };
        regions2 = new GeographicalPath[] { new GeographicalPath(), new GeographicalPath() };
        regions3 = new GeographicalPath[] { new GeographicalPath(), new GeographicalPath(), new GeographicalPath() };
        when(mockFrame1.getRegions()).thenReturn(regions1);
        when(mockFrame2.getRegions()).thenReturn(regions2);
        when(mockFrame3.getRegions()).thenReturn(regions3);
        // content

        GenericSignage genericSignage = new GenericSignage(); // real object
        genericSignage.getItems().add(new GenericSignage.GenericSignageItem(new ITIScodes(1001)));
        genericSignage.getItems().add(new GenericSignage.GenericSignageItem(new ITISTextPhrase("Hello World")));
        genericSignage.getItems().add(new GenericSignage.GenericSignageItem(new ITISTextPhrase("Hello Dhaka")));
        System.out.println("items size = " + genericSignage.getItems().size()); // should be 2
    
     

        WorkZone wz = new WorkZone(); // real object

        wz.getItems().add(new WorkZone.WorkZoneItem(new ITIScodes(1071)));
        wz.getItems().add(new WorkZone.WorkZoneItem(new ITIScodes(1072)));
        wz.getItems().add(new WorkZone.WorkZoneItem(new ITIScodes(1073)));
        wz.getItems().add(new WorkZone.WorkZoneItem(new ITISTextPhrase("Road Work Ahead")));

        System.out.println("items size = " + wz.getItems().size()); // should be 4

 

        // Make sure your Content returns THIS wz
        TravelerDataFrame.Content content = mock(TravelerDataFrame.Content.class);
        when(content.getChoice()).thenReturn(TravelerDataFrame.Content.Choice.WORK_ZONE);
        when(content.getWorkZone()).thenReturn(wz);

        content1 = mock(TravelerDataFrame.Content.class);
        when(content1.getChoice()).thenReturn(TravelerDataFrame.Content.Choice.GENERIC_SIGN);
        when(content1.getGenericSign()).thenReturn(genericSignage);
       

        GenericSignage gs = mock(GenericSignage.class);
        SpeedLimit sl = mock(SpeedLimit.class);
        ExitService es = mock(ExitService.class);


        when(mockFrame1.getContent()).thenReturn(content);
        when(mockFrame2.getContent()).thenReturn(content1);
        when(mockFrame3.getContent()).thenReturn(content);
        // new content
        // ===== Part III (contentNew) mocks =====

        // Frame 1: PORTLAND_CEMENT, DRY, SMOOTH
        PortlandCement pc1 = new PortlandCement(PortlandCementType.PortlandCementType_newSharp); // adjust enum/value as
                                                                                                 // you have it
        DescriptionOfRoadSurface desc1 = new DescriptionOfRoadSurface(pc1);
        FrictionInformation fric1 = new FrictionInformation(
                desc1);
        fric1.setDryOrWet(RoadSurfaceCondition.DRY);
        contentNew1 = new TravelerDataFrameNewPartIIIContent();
        contentNew1.setFrictionInformation(fric1);
        when(mockFrame1.getContentNew()).thenReturn(contentNew1);
        // Frame 2: ASPHALT_OR_TAR, WET, ROUGH
        AsphaltOrTar at2 = new AsphaltOrTar(AsphaltOrTarType.AsphaltOrTarType_newSharp); // adjust enum/value as you
                                                                                         // have it
        DescriptionOfRoadSurface desc2 = new DescriptionOfRoadSurface(pc1);
        FrictionInformation fric2 = new FrictionInformation(
                desc2);
        fric2.setDryOrWet(RoadSurfaceCondition.WET);
        contentNew2 = new TravelerDataFrameNewPartIIIContent();
        contentNew2.setFrictionInformation(fric2);
        when(mockFrame2.getContentNew()).thenReturn(contentNew2);
        // Frame 3: GRAVEL, ICE, SNOW
        Gravel g3 = new Gravel(GravelType.GravelType_loose); // adjust enum/value as you have it
        DescriptionOfRoadSurface desc3 = new DescriptionOfRoadSurface(pc1);
        FrictionInformation fric3 = new FrictionInformation(
                desc3);
        fric3.setDryOrWet(RoadSurfaceCondition.WET);
        contentNew3 = new TravelerDataFrameNewPartIIIContent();
        contentNew3.setFrictionInformation(fric3);
        when(mockFrame3.getContentNew()).thenReturn(contentNew3);

        // Mock TravelerInfoType for each fram
        // Attach to TravelerInformation
        when(mockTimData.getDataFrames()).thenReturn(mockList);
    }

    @Test
    public void TIM_encode_test() {
        ByteArrayObject result = encoder.encode(mockTimData);

        Assert.assertNotNull("Byte array should not be null", result.getMessage());
        byte[] expected = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
        Assert.assertArrayEquals(
                "Encoded TIM message doesn't match expected output",
                expected,
                result.getMessage());
    }
}
