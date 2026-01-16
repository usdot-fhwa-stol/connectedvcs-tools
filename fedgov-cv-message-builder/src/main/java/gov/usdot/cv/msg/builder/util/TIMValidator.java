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
