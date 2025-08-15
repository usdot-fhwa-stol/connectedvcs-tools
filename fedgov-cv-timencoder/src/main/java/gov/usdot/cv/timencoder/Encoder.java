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

package gov.usdot.cv.timencoder;
import java.nio.ByteOrder;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Encoder {
    private static final Logger logger = LogManager.getLogger(Encoder.class);

    public Encoder() {
    }

    // Load native library for TIM encoding
    static {
        try {
            System.loadLibrary("asn1c_timencoder");
        } catch (Exception e) {
            logger.error("Exception while loading the asn1c_timencoder library: " + e.toString(), e);
        }
    }

    /**
     * Native method that encodes a TIM message using the ASN.1 C library.
     * 
     * @return encoded TIM message as byte array, or null if encoding fails
     */
    public native byte[] encodeTIM(TIMData message);

    public ByteArrayObject encode(TIMData message) {
        System.out.println("Calling Encoder Java API");
		logger.debug("Starting TIM encoding process...");

        byte[] encodeMsg = encodeTIM(message);

        if (encodeMsg == null) {
            logger.error("Encoding failed. Returned byte array is null.");
            return new ByteArrayObject("TIM", null);
        }

        ChannelBuffer buffer = ChannelBuffers.copiedBuffer(ByteOrder.LITTLE_ENDIAN, encodeMsg);
        byte[] byteArray = new byte[buffer.readableBytes()];
        buffer.readBytes(byteArray);
        
        String hexString = Hex.encodeHexString(byteArray);
        logger.debug("Encoded hex string of the TIM message: " + hexString);

        return new ByteArrayObject("TIM", byteArray);
    }
}