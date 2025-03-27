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

package gov.usdot.cv.asn1decoder;

import java.nio.ByteOrder;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Decoder {
	private static final Logger logger = LogManager.getLogger(Decoder.class);

	public Decoder() {
	}

	// Load libasn1c_decoder.so external C library
	static {
		try {
			System.loadLibrary("asn1c_decoder");
		} catch (Exception e) {
			logger.error("Exception trapped while trying to load the asn1c library" + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * This is the declaration for native method. It will take a decoded message
	 * object in form of binary array and return an json string with decoded information.
	 *
	 * @return JSON string decoded message
	 */
	public native String decodeMsg(byte[] message);


	public String decode(ByteArrayObject binaryMessage) {
		logger.debug("Decoding the binary message :");

		// TODO The following lines are assumption and may not be correct
		/*Checking with Bytes */
		String decodedMsg = decodeMsg(binaryMessage.getMessage());

		if (decodedMsg == null) {
			// cannot decode message
			logger.error("Cannot decode!");
			return "";
		}

		return decodedMsg;
	}

}
