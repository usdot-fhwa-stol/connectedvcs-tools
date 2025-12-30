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
import gov.usdot.cv.asn1decoder.Decoder;
import gov.usdot.cv.timencoder.TravelerInformation;
import gov.usdot.cv.timencoder.ByteArrayObject;
import gov.usdot.cv.libasn1decoder.DecodedResult;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class J2735TIMHelper {
		private static final Logger logger = LogManager.getLogger(J2735TIMHelper.class);


	// This constant is used to convert the given LAT/LON to J2735 format
	private static final int LAT_LONG_CONVERSION_FACTOR = 10000000;

	public static String getHexString(TravelerInformation message) 
	{

		byte[] bytes = getBytes(message);
		return Hex.encodeHexString(bytes);
		
	}

		public static String getReadbaleTIMplusFrame(TravelerInformation message) 
	{

		byte[] bytes = getBytes(message);
		Decoder decoder = new Decoder();
		DecodedResult decodedResult = decoder.decodeMsg(bytes);
		return decodedResult.decodedMessage;
	}

	public static String getReadableTIM(TravelerInformation message) {

    String decoded = getReadbaleTIMplusFrame(message);

    // Finding the location TIM payload begins
    final String TIM_START = "TravelerInformation ::= {";
    int start = decoded.indexOf(TIM_START);
    if (start < 0) {
        return decoded; 
    }

    // trim,imh the final outer '}' (MessageFrame close)
    String tim = decoded.substring(start).trim();

    int lastOuterBrace = tim.lastIndexOf('}');
    if (lastOuterBrace >= 0) {
        tim = tim.substring(0, lastOuterBrace).trim();
    }

    return tim;
}


	public static byte[] getBytes(TravelerInformation message) {
		Encoder encoder = new Encoder();
		ByteArrayObject encodedMsg = encoder.encode(message);
		return encodedMsg.getMessage();
	}
	public static byte[] getCrc(TravelerInformation message) {
		byte[] fullMessage = getBytes(message);
		int checkSum = CrcCccitt.calculateCrcCccitt(fullMessage, 0, fullMessage.length-2);
		ByteBuffer buffer = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN);
		buffer.putShort((short)checkSum);
		return buffer.array();
	}


	public static int bitWiseOr(int[] nums) {
		int result = 0;
		for (int i=0; i<nums.length; i++) {
			result|= nums[i];
		}
		return result;
	}

		/**
	 * Takes a Lat or Long as a double and converts to an int.
	 * @param point
	 * @return
	 */
	public static int convertGeoCoordinateToInt(double point) {
		double convertedPoint = point * LAT_LONG_CONVERSION_FACTOR;
		return (int)Math.round(convertedPoint);
	}

	
}
