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

package gov.usdot.cv.msg.builder;

import static org.junit.Assert.assertEquals;
import gov.usdot.cv.timencoder.*;
import gov.usdot.cv.msg.builder.exception.MessageEncodeException;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TravelerInformationBuilderTest {

	@Test
	public void testBuildTravelerInformation() throws IOException {
		testBuildTravelerInformation("sample_tim.json","20100000000001bd08b5c0807099ba1ebc7a9ba564003e2fd2ca38e2d008006e133743d78f5374ac80b70007c00445d3451824dd8f4c927147bcc929a3fec00100f9a8"); 
		testBuildTravelerInformation("sample_timplusframe.json","001f4320100000000001bd08b5c0807099ba1ebc7a9ba564003e2fd2ca38e2d008006e133743d78f5374ac80b70007c00445d3451824dd8f4c927147bcc929a3fec00100f9a8");
		testBuildTravelerInformation("tim_Circle.json","20100000000000372d6a5082729dc23852739262402e6000002fc26fb882d030406a53b8470a4e724c4805cc00000b00000816e0d693a401ad2747ff56404200004300");
		testBuildTravelerInformation("tim_Circle2.json","20100000000000372d6a5082729dc23852739262402e6000002fc26fb882d030406a53b8470a4e724c4805cc00000b00000816e0d693a401ad2747fc1f504200004300");
		testBuildTravelerInformation("sample_tim_lane.json","001f4d201000000000035ec53d4080b292837b0e4adfe1d21f8830006fcea64a3f4020007e52506f61c95bfc3a43f100b7218000003895bfc36e4a0debd81450e25700a2992837bc2050a0000007cd80"); 
		testBuildTravelerInformation("sample_tim_poly.json","001f7520100000000001254d1b10807299ba56787a9b892622f9fffe4fce7c1a3f4028007e53374acf0f537124c45f03e87ffff00535e5a5d7000a0a2c4e2cb7495fdd60f90afabd053ed6a4688c3027b0b01f1045ad92da78dc02850b08e6fbd6581ff7deb2c0973ede96005dee44aeb82d174000001712"); 
		testBuildTravelerInformation("sample_tim_tight.json","001f74201000000000012571e37080b299b9ec767a9ba76a230dfffe6fce7c1a3f4038007e53373d8ecf5374ed446180b77ffff006a80969f40a0a0b8c2f02da16f656e23a9014143c44bd785fe50462cd085d165cc5c8b59e3a02e33610857daf0670e7920cb4d017518cc2e813ec233b897000807cdc"); 
		testBuildTravelerInformation("tim_road_roughness.json","2010000000000152cb81b182728b654080763c939a1fed33322fc0ea84016819006e516ca8100ec7927343fd80b726664002a1dc276048154d1649e0902aa0d44ac1205188061a948043062470cd3e1972e441b34edd3d1074de8336fd9b37f758837336031018079f41a3e008110020"); 

	}

	public void testBuildTravelerInformation(String timName, String expectedString) throws IOException {
		TravelerInformationBuilder timBuilder = new TravelerInformationBuilder();
		String json = FileUtils.readFileToString(new File("src/test/resources/" + timName));
		TravelerInformationMessage timMessage = (TravelerInformationMessage) timBuilder.build(json);
		System.out.println("TIM Message Name: " + timMessage.getMessageName());
		assertEquals(expectedString, timMessage.getHexString());
	}

}