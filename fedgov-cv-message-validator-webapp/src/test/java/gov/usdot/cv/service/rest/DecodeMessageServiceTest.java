// package gov.usdot.cv.service.rest;

// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertNotNull;
// import static org.junit.Assert.assertTrue;

// import gov.usdot.cv.service.rest.DecodeMessageResult.Status;

// import org.codehaus.jettison.json.JSONException;
// import org.codehaus.jettison.json.JSONObject;
// import org.junit.Test;

// import com.sun.jersey.api.client.WebResource;
// import com.sun.jersey.test.framework.JerseyTest;

// public class DecodeMessageServiceTest extends JerseyTest  {

// 	public DecodeMessageServiceTest() throws Exception {
// 		super("gov.usdot.cv.service.rest");
// 	}
	
// 	class Message {
// 		String version, messageName, encoding, message, messageType;
// 		boolean isValid;
		
// 		Message( String version, String messageName, String encoding, boolean isValid, String message) {
// 			this.version = version;
// 			this.messageName = messageName;
// 			this.encoding = encoding;
// 			this.isValid = isValid;
// 			this.message = message;
// 			this.messageType = null;
// 		}
		
// 		Message( String version, String messageName, String encoding, String messageType, boolean isValid, String message) {
// 			this.version = version;
// 			this.messageName = messageName;
// 			this.encoding = encoding;
// 			this.messageType = messageType;
// 			this.isValid = isValid;
// 			this.message = message;
// 		}
		
// 		@Override
// 		public String toString() {
// 			return "Message [version=" + version + ", messageName=" + messageName + ", encoding=" + 
// 							encoding + ", messageType=" + messageType + ", valid?=" + isValid + ", " + "message=" + message +"]";
// 		}
// 	}
	
// 	Message[] test_messages = {
			
// 		//Valid hex
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.AdvisorySituationData", "Hex", true, "c4400000000000003e9000003e9227a318401c4fecbf89c2a070074d339fe00000ad9a01038a8d0d2e640d2e640c2dc40c2c8ecd2e6dee4f240dacae6e6c2ceca5c0"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataConfirmation", "Hex", true, "10c000000008004f85800000004000000000000000000000000000000000000000000000000000000000"),		
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "Hex", true, "0a4000000000404040402625a100"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionResponse", "Hex", true, "85000000000020202026264666800000"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.ServiceResponse", "Hex", true, "8420000000002020203fdf81ef2e80055a409df92e6c72fab25627757bc41cd046b2800000008000000000000000000000000000000000000000000000000000000000"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.IntersectionSituationDataAcceptance", "Hex", true, "4140000000000000fa403180"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionRequest", "Hex", true, "84e0000000002020203017e07bd3913bf25cd8e5f564ac4eeaf78839a08d65"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.AdvisorySituationDataDistribution", "Hex", true, "1900000000000000fa4061900c4c8ccd0a626466680317ddc4a5e7ddc4a68021223934bb34b733903634b5b2903a3432b932c939903737903a37b6b7b93937bb9034b9903634b5b2b63c903a3790383937b23ab1b2903a3430ba103932b9bab63a174c4c8ccd0062fbb894bcfbb894d0013436c69636b206974206f72207469636b65742e9899199a00c5f7712979f77129a003a9adeecca40deeccae440dee440cecae840e0ead8d8cac840deeccae45c"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.ServiceResponse", "Hex", true, "80200000000000007d3fdf81ef2e80055a409df92e6c72fab25627757bc41cd046b2800000008000000000000000000000000000000000000000000000000000000000"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataRequest", "Hex", true, "0c400000000000007d2296cdbc01d8119bf89fbd8e007997edfe00"),
		
// 		//valid base64
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.AdvisorySituationData", "base64", true, "xEAAAAAAAAA+kAAAPpInoxhAHE/sv4nCoHAHTTOf4AAArZoBA4qNDS5kDS5kDC3EDCyOzS5t7k8kDayubmws7KXA"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataConfirmation", "base64", true, "EMAAAAAIAE+FgAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),		
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "base64", true, "CkAAAAAAQEBAQCYloQA="),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionResponse", "base64", true, "hQAAAAAAICAgJiZGZoAAAA=="),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.ServiceResponse", "base64", true, "hCAAAAAAICAgP9+B7y6ABVpAnfkubHL6slYndXvEHNBGsoAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=="),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.IntersectionSituationDataAcceptance", "base64", true, "QUAAAAAAAAD6QDGA"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionRequest", "base64", true, "hOAAAAAAICAgMBfge9ORO/Jc2OX1ZKxO6veIOaCNZQ=="),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.AdvisorySituationDataDistribution", "base64", true, "GQAAAAAAAAD6QGGQDEyMzQpiZGZoAxfdxKXn3cSmgCEiOTS7NLczkDY0tbKQOjQyuTLJOZA3N5A6N7a3uTk3u5A0uZA2NLWytjyQOjeQODk3sjqxspA6NDC6EDkyubq2OhdMTIzNAGL7uJS8+7iU0AE0NsaWNrIGl0IG9yIHRpY2tldC6YmRmaAMX3cSl593EpoAOpre7MpA3uzK5EDe5EDOyuhA4OrY2MrIQN7syuRc"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.ServiceResponse", "base64", true, "gCAAAAAAAAB9P9+B7y6ABVpAnfkubHL6slYndXvEHNBGsoAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=="),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataRequest", "base64", true, "DEAAAAAAAAB9IpbNvAHYEZv4n72OAHmX7f4A"),

// 		//valid with whitespace
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "Hex", true, "    0a4000000000404040402625a100"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "Hex", true, "0a4000000000404040402625a100    "),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "Hex", true, "			0a4000000000404040402625a100"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "Hex", true, "0a400   0000   000404040		402625a100    "),
		
// 		//Valid hex with MessageType
// 		new Message("2.3", "AdvisorySituationData", "Hex", "AdvisorySituationData", true, "c4400000000000003e9000003e9227a318401c4fecbf89c2a070074d339fe00000ad9a01038a8d0d2e640d2e640c2dc40c2c8ecd2e6dee4f240dacae6e6c2ceca5c0"),
// 		new Message("2.3", "DataConfirmation", "Hex", "DataConfirmation", true, "10c000000008004f85800000004000000000000000000000000000000000000000000000000000000000"),		
// 		new Message("2.3", "DataSubscriptionCancel", "Hex", "DataSubscriptionCancel", true, "0a4000000000404040402625a100"),
// 		new Message("2.3", "DataSubscriptionResponse", "Hex", "DataSubscriptionResponse", true, "85000000000020202026264666800000"),
// 		new Message("2.3", "ServiceResponse", "Hex", "ServiceResponse", true, "8420000000002020203fdf81ef2e80055a409df92e6c72fab25627757bc41cd046b2800000008000000000000000000000000000000000000000000000000000000000"),
// 		new Message("2.3", "IntersectionSituationDataAcceptance", "Hex", "IntersectionSituationDataAcceptance", true, "4140000000000000fa403180"),
// 		new Message("2.3", "DataSubscriptionRequest", "Hex", "DataSubscriptionRequest", true, "84e0000000002020203017e07bd3913bf25cd8e5f564ac4eeaf78839a08d65"),
// 		new Message("2.3", "AdvisorySituationDataDistribution", "Hex", "AdvisorySituationDataDistribution", true, "1900000000000000fa4061900c4c8ccd0a626466680317ddc4a5e7ddc4a68021223934bb34b733903634b5b2903a3432b932c939903737903a37b6b7b93937bb9034b9903634b5b2b63c903a3790383937b23ab1b2903a3430ba103932b9bab63a174c4c8ccd0062fbb894bcfbb894d0013436c69636b206974206f72207469636b65742e9899199a00c5f7712979f77129a003a9adeecca40deeccae440dee440cecae840e0ead8d8cac840deeccae45c"),
// 		new Message("2.3", "ServiceResponse", "Hex", "ServiceResponse", true, "80200000000000007d3fdf81ef2e80055a409df92e6c72fab25627757bc41cd046b2800000008000000000000000000000000000000000000000000000000000000000"),
// 		new Message("2.3", "DataRequest", "Hex", "DataRequest", true, "0c400000000000007d2296cdbc01d8119bf89fbd8e007997edfe00"),
		
// 		//valid base64 with MessageType
// 		new Message("2.3", "AdvisorySituationData", "base64", "AdvisorySituationData", true, "xEAAAAAAAAA+kAAAPpInoxhAHE/sv4nCoHAHTTOf4AAArZoBA4qNDS5kDS5kDC3EDCyOzS5t7k8kDayubmws7KXA"),
// 		new Message("2.3", "DataConfirmation", "base64",  "DataConfirmation",true, "EMAAAAAIAE+FgAAAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),		
// 		new Message("2.3", "DataSubscriptionCancel", "base64", "DataSubscriptionCancel", true, "CkAAAAAAQEBAQCYloQA="),
// 		new Message("2.3", "DataSubscriptionResponse", "base64", "DataSubscriptionResponse", true, "hQAAAAAAICAgJiZGZoAAAA=="),
// 		new Message("2.3", "ServiceResponse",  "base64", "ServiceResponse",true, "hCAAAAAAICAgP9+B7y6ABVpAnfkubHL6slYndXvEHNBGsoAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=="),
// 		new Message("2.3", "IntersectionSituationDataAcceptance", "base64", "IntersectionSituationDataAcceptance", true, "QUAAAAAAAAD6QDGA"),
// 		new Message("2.3", "DataSubscriptionRequest", "base64", "DataSubscriptionRequest", true, "hOAAAAAAICAgMBfge9ORO/Jc2OX1ZKxO6veIOaCNZQ=="),
// 		new Message("2.3", "AdvisorySituationDataDistribution", "base64", "AdvisorySituationDataDistribution", true, "GQAAAAAAAAD6QGGQDEyMzQpiZGZoAxfdxKXn3cSmgCEiOTS7NLczkDY0tbKQOjQyuTLJOZA3N5A6N7a3uTk3u5A0uZA2NLWytjyQOjeQODk3sjqxspA6NDC6EDkyubq2OhdMTIzNAGL7uJS8+7iU0AE0NsaWNrIGl0IG9yIHRpY2tldC6YmRmaAMX3cSl593EpoAOpre7MpA3uzK5EDe5EDOyuhA4OrY2MrIQN7syuRc"),
// 		new Message("2.3", "ServiceResponse", "base64", "ServiceResponse", true, "gCAAAAAAAAB9P9+B7y6ABVpAnfkubHL6slYndXvEHNBGsoAAAACAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=="),
// 		new Message("2.3", "DataRequest", "base64", "DataRequest", true, "DEAAAAAAAAB9IpbNvAHYEZv4n72OAHmX7f4A"),

// 		//valid with whitespace with MessageType
// 		new Message("2.3", "DataSubscriptionCancel", "Hex", "DataSubscriptionCancel", true, "    0a4000000000404040402625a100"),
// 		new Message("2.3", "DataSubscriptionCancel", "Hex", "DataSubscriptionCancel", true, "0a4000000000404040402625a100    "),
// 		new Message("2.3", "DataSubscriptionCancel", "Hex", "DataSubscriptionCancel", true, "			0a4000000000404040402625a100"),
// 		new Message("2.3", "DataSubscriptionCancel", "Hex", "DataSubscriptionCancel", true, "0a400   0000   000404040		402625a100    "),
		
// 		//invalid due to extra internal values
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.AdvisorySituationData", "Hex", false, "c4400000000000003e9000003 12 e9227a318401c4fecbf89c2a070074d339fe00000ad9a01038a8d0d2e640d2e640c2dc40c2c8ecd2e6dee4f240dacae6e6c2ceca5c0"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataConfirmation", "Hex", false, "10c000000008004f85800000004000 1 000000000000000000000000000000000000000000000000000000"),		
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionCancel", "Hex", false, "0a4000000000404040402 123 625a100"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.DataSubscriptionResponse", "Hex", false, "850000000000202 123 02026264666800000"),
// 		new Message("2.3", "gov.usdot.asn1.generated.j2735.semi.ServiceResponse", "Hex", false, "8420000000002020203fdf81ef2e8 123 0055a409df92e6c72fab25627757bc41cd046b2800000008000000000000000000000000000000000000000000000000000000000"),
		
// 	};
	
// 	@Test
// 	public void testMessages() throws JSONException {	
// 		for( Message message : test_messages ) {
// 			System.out.println("Testing message: " + message.toString());
// 			testMessage( message );
// 		}
// 	}
	
// 	private void testMessage( Message message) throws JSONException {	
// 		WebResource webResource = buildWebResource(message.version, message.message, message.encoding, message.messageType);
// 		DecodeMessageResult result = webResource.get(DecodeMessageResult.class);
		
// 		if( message.isValid ) {
// 			JSONObject res = new JSONObject(result.getMessage());
// 			assertNotNull(res);
// 			if ( message.messageName != null ) {
// 				boolean match = 
// 						res.getString("messageName").equals(message.messageName) ||
// 						res.getString("messageName").endsWith("." + message.messageName);
// 				assertTrue(match);
// 			}
//     		assertEquals(Status.Success, result.getStatus());
// 		} else 
// 			assertEquals(Status.Error, result.getStatus());
// 	}	
	
// 	private WebResource buildWebResource(String messageVersion, String encodedMsg, String encoding, String messageType) {
// 		WebResource webResource = resource().path("decode");
// 		webResource = (messageVersion != null) ? webResource.queryParam("messageVersion", messageVersion) : webResource;
// 		webResource = (encodedMsg != null) ? webResource.queryParam("encodedMsg", encodedMsg) : webResource;
// 		webResource = (encoding != null) ? webResource.queryParam("encodeType", encoding) : webResource;
// 		webResource = (messageType != null) ? webResource.queryParam("messageType", messageType) : webResource;
// 		return webResource;
// 	}
// }