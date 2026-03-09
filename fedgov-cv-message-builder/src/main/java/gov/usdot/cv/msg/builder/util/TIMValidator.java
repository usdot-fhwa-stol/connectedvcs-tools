/*

Copyright (C) 2026 LEIDOS.
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
*/
package gov.usdot.cv.msg.builder.util;

import org.apache.commons.lang.StringUtils;
import gov.usdot.cv.asn1decoder.TIMDecoder;
import gov.usdot.cv.asn1decoder.ByteArrayObject;
import gov.usdot.cv.libasn1decoder.DecodedResult;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class TIMValidator {

    private TIMDecoder messageDecoder; // JNI wrapper object to make a native call to asn1c based decoder

    public TIMValidator() {
        messageDecoder = new TIMDecoder(); // Initialize the Decoder object
    }

    public synchronized String validateTIM(byte[] bytes) throws SemiValidatorException {
        /*
         * variable to hold decoded message and message name
         */
        DecodedResult decodedResult;
        try {
            // Using Decoder class to decode the message directly using byte[]
            ByteArrayObject byteArrayObject = new ByteArrayObject("", bytes);
            decodedResult = messageDecoder.decodeTim(byteArrayObject);

            if (!decodedResult.success) {
                throw new SemiValidatorException("Couldn't decode message using Decoder");
            }

            return decodedResult.decodedMessage;
        } catch (Exception ex) {
            throw new SemiValidatorException("Error during decoding: " + ex.getMessage());
        }
    }
}
