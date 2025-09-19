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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import gov.usdot.cv.service.rest.DecodeMessageResult.Status;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;



@Path("/decode")
public class DecodeMessageResource {

    @Context
    HttpServletRequest request;

    @Context
    UriInfo uriInfo;

    private static SemiValidator validator;
	private static final Logger log = LogManager.getLogger(DecodeMessageResource.class);
	
	@GET
	@Produces("application/json")
	public DecodeMessageResult decode(
			@QueryParam("messageVersion") String messageVersion,
			@QueryParam("encodeType") String encodingType,
			@QueryParam("encodedMsg") String encodedMsg,
			@QueryParam("messageType") String messageType) {
		try {
			
			log.debug("input message version: " + messageVersion);
			log.debug("input encoding type: " + encodingType);
			log.debug("input encoded message: " + encodedMsg);
			log.debug("input message type: " + messageType);
			/*Checking if called with empty encoded Message */
			if (StringUtils.isBlank(encodedMsg))
				throw new DecodeMessageException("Missing encoded message.");
			//initializing variable to hold converted Hex string
			byte[] encoded_ba = null;
			try{
			//Trimming white spaces and newlines
			String encodedMsgtrimmed = encodedMsg.replaceAll("\\s+", "");
	
			//Converting provided Hex String toBytes

			encoded_ba=DatatypeConverter.parseHexBinary(encodedMsgtrimmed);
			} catch (Exception e) {
				DecodeMessageResult result = new DecodeMessageResult();
				JSONObject returnStatusObject = new JSONObject();
				try {
					returnStatusObject.put("messageName", "Unknown");
					returnStatusObject.put("decodedMessage", "Error parsing input message text to bytes.  Error was: "+e.getMessage());
				} catch (JSONException je) {
					log.warn("Failed to add error details to JSON response: {}", je.getMessage());
				}
				result.setStatus(Status.Error);
				result.setMessage(returnStatusObject.toString());
				return result;
			}
			
			return decode(encoded_ba, messageType);
		} catch (DecodeMessageException e) {
			DecodeMessageResult result = new DecodeMessageResult();
			JSONObject returnStatusObject = new JSONObject();
			try {
				returnStatusObject.put("messageName", "Unknown");
				returnStatusObject.put("decodedMessage", e.getMessage());
			} catch (JSONException je) {
				log.warn("Failed to add error details to JSON response: {}", je.getMessage());
			}

			result.setStatus(Status.Error);
			result.setMessage(returnStatusObject.toString());
			return result;
		} 
	}

	private DecodeMessageResult decode( byte[] bytes, String messageType) {
		DecodeMessageResult result = new DecodeMessageResult();
		try {
			validator = new SemiValidator();
			//calling SemiValidator method to decode the encoded Message
			String resultMessage = validator.validate(bytes);
			result.setMessage(resultMessage);
			result.setStatus(Status.Success);
		} catch (SemiValidatorException ex ) {
			
			JSONObject returnStatusObject = new JSONObject();
			try {
				returnStatusObject.put("messageName", "Unknown");
				returnStatusObject.put("decodedMessage", ex.getMessage());
			} catch (JSONException je) {
				log.warn("Failed to add error details to JSON response: {}", je.getMessage());
			}
			result.setMessage(returnStatusObject.toString());
			result.setStatus(Status.Error);
		} catch (Exception ex) {

			JSONObject returnStatusObject = new JSONObject();
			try {
				returnStatusObject.put("messageName", "Unknown");
				returnStatusObject.put("decodedMessage", ex.getMessage());
			} catch (JSONException je) {
				log.warn("Failed to add error details to JSON response: {}", je.getMessage());
			}
			result.setMessage(returnStatusObject.toString());
			result.setStatus(Status.Error);
		}
		return result;
	}


}
