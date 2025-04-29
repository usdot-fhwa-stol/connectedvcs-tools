/*

Copyright (C) 2025 LEIDOS.
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
package gov.usdot.cv.service.rest;
import gov.usdot.cv.asn1decoder.Decoder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.apache.commons.lang.StringUtils;

public class SemiValidator {

    private Decoder messageDecoder; // JNI wrapper object to make a native call to asn1c based decoder
    
    public SemiValidator() {
        messageDecoder = new Decoder(); // Initialize the Decoder object
    }
    public synchronized String validate(byte[] bytes) throws SemiValidatorException {
        /*
        variable to hold decoded message and message name 
        */

        String messageName = null;
    	String decodedMessage = null;
        try {
            // Using Decoder class to decode the message directly using byte[]
            decodedMessage = messageDecoder.decodeMsg(bytes);
          
            // TODO: Once asn1C Decoder returns a message type that name will be used instead of Hardcoded messagename below

            messageName="messageName"; 
            if (decodedMessage.isEmpty()) {
               
                throw new SemiValidatorException("Couldn't decode message using Decoder");
            }
            
            return formatResult(messageName, decodedMessage);
        } catch (Exception ex) {
            throw new SemiValidatorException("Error during decoding: " + ex.getMessage());
        }
    }

    /*Functions to validate if message type is given and will be implemented later 
    when native C decoder will be able to decode bytes given message type */

     public synchronized String validate( byte[] bytes, String name ) throws SemiValidatorException {
    	String messageName = null;
    	String decodedMessage = null;
    	try {
    		
            /*need to rewrite native C decoder to decode with given message type */
	    	//decodedMessage = name != null ? messageDecoder.decodeMsg( bytes, name ) : messageDecoder.decodeMsg( bytes )			
            if (decodedMessage == null || decodedMessage.isEmpty()) {
                throw new SemiValidatorException(
    				"Couldn't decode message using the given message type");
            }
            return formatResult(messageName, decodedMessage);
   
    	} catch (Exception ex) {
    		throw new SemiValidatorException(formatResult("Unknown", ex.getMessage()));
    	}
    }

    /*function to format  */
    private String formatResult(String messageName, String decodedMessage) {
        JSONObject result = new JSONObject();
        try {
            result.put("messageName", !StringUtils.isBlank(messageName) ? messageName : "Unknown");
            result.put("decodedMessage", decodedMessage);
        } catch (JSONException ignored) { }
        return result.toString();
    }

}
