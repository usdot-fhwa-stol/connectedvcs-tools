/*
 * Copyright (C) 2026 LEIDOS.
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

import gov.usdot.cv.libasn1decoder.DecodedResult;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TIMDecoder {
	private static final Logger logger = LogManager.getLogger(TIMDecoder.class);

	static {
		try {
			System.loadLibrary("asn1c_timdecoder");
		} catch (UnsatisfiedLinkError e) {
			logger.error("Failed to load native library asn1c_timdecoder");
			throw e;
		}
	}

	/**
	 * This is the declaration for native method. It will take a decoded message
	 * object in form of binary array and return an json string with decoded information.
	 *
	 * @return JSON string decoded message
	 */
	public native DecodedResult decodeTimMsg(byte[] message);

	public DecodedResult decodeTim(ByteArrayObject binaryMessage) {
		logger.debug("Decoding the binary message...");
		DecodedResult result = decodeTimMsg(binaryMessage.getMessage());
		if (result == null || !result.success) {
			logger.error("Decoding failed or returned null.");
			// If a null object is returned assigning a object with empty decoded message and messagetype
			if (result == null) {
				result = new DecodedResult();
				result.decodedMessage = "";
				result.messageType = "";
				result.success = false;
			}
		} else {
			logger.info("Decoded Message Type: {}", result.messageType);
			logger.debug("Decoded Message: {}", result.decodedMessage);
		}

		return result;
	}
}
