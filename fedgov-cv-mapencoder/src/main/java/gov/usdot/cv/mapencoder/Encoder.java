/*
 * Copyright (C) 2023 LEIDOS.
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

package gov.usdot.cv.mapencoder;

import java.nio.ByteOrder;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usdot.cv.libasn1c.NativeLoadLibrary;

public class Encoder {
	private static final Logger logger = LogManager.getLogger(Encoder.class);

	public Encoder() {
	}

	// Load the consolidated asn1c JNI library through the shared, guarded loader
	static {
		try {
			NativeLoadLibrary.load();
		} catch (Exception e) {
			logger.error("Exception trapped while trying to load the asn1c library" + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * This is the declaration for native method. It will take data from MAP message
	 * object and return an byte array with encoded information. Because of the
	 * efficiency of JNI method call, it takes fields directly instead of a
	 * single MAP object.
	 * 
	 * @return encoded BSM message
	 */
	public native byte[] encodeMap(int msgCount, long timeStamp, long layerType, long layerID,
			IntersectionGeometry[] intersections);

	//
	public ByteArrayObject encode(MapData message) {
		logger.debug("Encoding the map message " + message.toString() + "!");

		byte[] encodeMsg = encodeMap(message.getMsgCount(), message.getTimeStamp(), message.getLayerType(),
				message.getLayerId(), message.getIntersections());

		if (encodeMsg == null) {
			// cannot encode MAP message
			logger.error("Cannot encode!");
			return new ByteArrayObject("MAP", null);
		}

		ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ByteOrder.LITTLE_ENDIAN, encodeMsg);
		byte[] byteArray = new byte[buffer.readableBytes()];

		String hexString = Hex.encodeHexString(encodeMsg);
		logger.debug("Encoded hex string of the map message: " + hexString);

		return new ByteArrayObject("MAP", encodeMsg);
	}

	// Not in scope
	public MapData decode(ByteArrayObject binaryMessage) {
		return new MapData();
	}
}
