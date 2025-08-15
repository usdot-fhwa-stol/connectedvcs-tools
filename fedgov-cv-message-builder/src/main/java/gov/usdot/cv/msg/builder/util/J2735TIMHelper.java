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
import gov.usdot.cv.timencoder.Encoder;
import gov.usdot.cv.timencoder.TIMData;
import gov.usdot.cv.timencoder.ByteArrayObject;

import org.apache.commons.codec.binary.Hex;


public class J2735TIMHelper {

	public static String getHexString(TIMData message) 
	{

		byte[] bytes = getBytes(message);
		return Hex.encodeHexString(bytes);
	}

	public static byte[] getBytes(TIMData message) {
		Encoder encoder = new Encoder();
		ByteArrayObject encodedMsg = encoder.encode(message);
		return encodedMsg.getMessage();
	}

	
}
