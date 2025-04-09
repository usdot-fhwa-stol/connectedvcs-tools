package gov.usdot.cv.service.rest;

import gov.usdot.cv.service.rest.DecodeMessageResult.Status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.jersey.core.util.Base64;

@Path("/decode")
public class DecodeMessageResource {

	@Context
	HttpServletRequest request;

	@Context
	UriInfo uriInfo;
	
	private static final Logger log = Logger.getLogger(DecodeMessageResource.class);
	
	private static Map<EncodeVersion, SemiValidator> validators;
	
	private static Configuration configuration;
	
	static {

		try {
			configuration = new Configuration();
			
			log.info("Initializing validators map");
			SemiValidator v23validator = new SemiValidator();
			validators = new HashMap<EncodeVersion, SemiValidator>();
			validators.put(EncodeVersion.v23, v23validator);
			
			String version = EncodeVersion.v23.getValue();
			//@SuppressWarnings("unchecked")
			//List<String> messages = (List<String>)v23validator.getMessageTypes();
			//configuration.addConfiguration(version, messages);
			//log.debug("Registered validator version: " + version);
			// add more validator configurations here 
			// and optionally set default configuration index
			/* uncomment to produce two artificial cases for testing
			validators.put(EncodeVersion.v24, v23validator);
			java.util.Collections.sort(messages, java.util.Collections.reverseOrder());
			configuration.addConfiguration(EncodeVersion.v24.getValue(), messages);
			configuration.setDefaultIndex(1);
			 */
		} catch (Exception ex) {
			log.error("Initialization error: " + ex.getMessage());
		}
	}

	private enum EncodeType {
		HEX("Hex"), BASE64("Base64"), BER("BER");

		private String representation;

		private EncodeType(String s) {
			this.representation = s;
		}

		public static EncodeType getType(String r) {
			for (EncodeType v : EncodeType.values()) {
				if (v.representation.equalsIgnoreCase(r))
					return v;
			}
			return null;
		}
	}

	enum EncodeVersion {
		v20("2.0"), v21("2.1"), v22("2.2"), v23("2.3"), v24("2.4");

		private String representation;

		private EncodeVersion(String s) {
			this.representation = s;
		}

		public static EncodeVersion getType(String r) {
			for (EncodeVersion v : EncodeVersion.values()) {
				if (v.representation.equalsIgnoreCase(r))
					return v;
			}
			return null;
		}
		
		public String getValue() {
			return representation;
		}
	}
	
	@GET
	@Path("/getConfiguration")
	@Produces("application/json")
	public DecodeMessageResult getConfiguration() {
		DecodeMessageResult result = new DecodeMessageResult();
		result.setMessage(configuration.toString());
		result.setStatus(Status.Success);
		return result;
	}
	
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
			if (StringUtils.isBlank(messageVersion))
				throw new DecodeMessageException(
						"Missing message version parameter.");
			if (StringUtils.isBlank(encodedMsg))
				throw new DecodeMessageException("Missing encoded message.");
			if (StringUtils.isBlank(encodingType))
				throw new DecodeMessageException("Missing encoding type.");

			EncodeType eType = EncodeType.getType(encodingType);
			if (eType == null)
				throw new DecodeMessageException(
						"Error determining encoding type.");

			EncodeVersion eVersion = EncodeVersion.getType(messageVersion);
			if (eVersion == null)
				throw new DecodeMessageException(
						"Error determining encoding type.");

			byte[] encoded_ba = null;
			try{
				switch (eType) {
				case BASE64:
					encoded_ba = Base64.decode(encodedMsg);
					break;
				case HEX:
					//if (encodedMsg.length()/2 != 0) {
					//	throw new DecodeMessageException("Invalid hex string, hex string should have an even number of digits");
					//}
					//encoded_ba = HexTool.parseHex(encodedMsg, false);
					/*Use tool to convert Hex to Binary */
					break;
				case BER:
					log.error("Unexpected type BER");
				}
			} catch (Exception e) {
				DecodeMessageResult result = new DecodeMessageResult();
				JSONObject returnStatusObject = new JSONObject();
				try {
					returnStatusObject.put("messageName", "Unknown");
					returnStatusObject.put("decodedMessage", "Error parsing input message text to bytes.  Error was: "+e.getMessage());
				} catch (JSONException je) {
					// ignore
				}
				result.setStatus(Status.Error);
				result.setMessage(returnStatusObject.toString());
				return result;
			}
			return decode(eVersion, encoded_ba, messageType);
		} catch (DecodeMessageException e) {
			DecodeMessageResult result = new DecodeMessageResult();
			JSONObject returnStatusObject = new JSONObject();
			try {
				returnStatusObject.put("messageName", "Unknown");
				returnStatusObject.put("decodedMessage", e.getMessage());
			} catch (JSONException je) {
				// ignore
			}

			result.setStatus(Status.Error);
			result.setMessage(returnStatusObject.toString());
			return result;
		} 
	}

	private DecodeMessageResult decode(EncodeVersion eVersion, byte[] bytes, String messageType) {
		DecodeMessageResult result = new DecodeMessageResult();
		try {
			SemiValidator validator = validators.get(eVersion);
			String resultMessage = validator.validate(bytes);
			result.setMessage(resultMessage);
			result.setStatus(Status.Success);
		} catch (SemiValidatorException ex ) {
			result.setMessage(ex.getMessage());
			result.setStatus(Status.Error);
		} catch (Exception ex) {
			result.setMessage(ex.getMessage());
			result.setStatus(Status.Error);
		}
		return result;
	}

}