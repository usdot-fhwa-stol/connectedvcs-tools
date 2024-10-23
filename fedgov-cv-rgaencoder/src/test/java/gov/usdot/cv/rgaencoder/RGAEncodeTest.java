/*
 * Copyright (C) 2024 LEIDOS.
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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.usdot.cv.mapencoder.ByteArrayObject;
import gov.usdot.cv.mapencoder.Position3D;

public class RGAEncodeTest {
    private static final Logger logger = LogManager.getLogger(RGAEncodeTest.class);
    Encoder encoder;
    RGAData mockRGA;
    BaseLayer mockBaseLayer;
    Position3D mockLocation;
    DDate mockTimeOfCalculation;
    DDateTime mockContentDateTime;
    
    @Before
    public void setup() {
        mockRGA = mock(RGAData.class); 
        mockBaseLayer = mock(BaseLayer.class);
        mockLocation = mock(Position3D.class);
        mockTimeOfCalculation = mock(DDate.class);
        mockContentDateTime = mock(DDateTime.class);

        encoder = new Encoder();

        when(mockBaseLayer.getMajorVer()).thenReturn(22);
        when(mockBaseLayer.getMinorVer()).thenReturn(11);

        // RAID
        when(mockBaseLayer.isFullRdAuthIDExists()).thenReturn(false);
        when(mockBaseLayer.getFullRdAuthID()).thenReturn(new int[]{1, 2, 83493});
        when(mockBaseLayer.isRelRdAuthIDExists()).thenReturn(true);
        when(mockBaseLayer.getRelRdAuthID()).thenReturn(new int[]{8, 4, 8571});

        when(mockBaseLayer.getRelativeToRdAuthID()).thenReturn(new int[]{1, 3, 6, 1, 4, 1, 311, 21, 20});
    
        //location
        when(mockLocation.getLatitude()).thenReturn((double)7.2);
        when(mockLocation.getLongitude()).thenReturn((double)11.1);
        when(mockLocation.isElevationExists()).thenReturn(true);
        when(mockLocation.getElevation()).thenReturn((float)13.12);

        //TimeOfCalculation
        when(mockTimeOfCalculation.getMonth()).thenReturn(8);
        when(mockTimeOfCalculation.getDay()).thenReturn(21);
        when(mockTimeOfCalculation.getYear()).thenReturn(2024);

        when(mockBaseLayer.getContentVer()).thenReturn(13);

        //ContentDateTime
        when(mockContentDateTime.getHour()).thenReturn(13);
        when(mockContentDateTime.getMinute()).thenReturn(51);
        when(mockContentDateTime.getSecond()).thenReturn(20);
        
        when(mockBaseLayer.getLocation()).thenReturn(mockLocation);
        when(mockBaseLayer.getTimeOfCalculation()).thenReturn(mockTimeOfCalculation);
        when(mockBaseLayer.getContentDateTime()).thenReturn(mockContentDateTime);

        when(mockRGA.getBaseLayer()).thenReturn(mockBaseLayer);
    }


    @Test
    public void rgaEncodeTester() {
        ByteArrayObject res = encoder.encode(mockRGA);
        byte[] expected = { 0, 43, 44, 0, 4, 4, 88, 44, -102, -46, 116, -125, -75, -92, -23, 5, 8, 6, -65, 68, 85, 65, 2, 1, 48, -98, -63, 64, 32, 96, -64, 32, -128, 48, 70, -30, -94, -127, -65, -33, -94, 42, -36, -64, 5, 26, 64 };
        Assert.assertArrayEquals(expected, res.getMessage());
    }
}