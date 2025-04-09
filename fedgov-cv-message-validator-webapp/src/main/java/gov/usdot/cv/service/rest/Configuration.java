package gov.usdot.cv.service.rest;

import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Configuration {
	
	final public static String title = "Connected Vehicle Message Validator";
	final public static String subtitle = "Unified Model Architecture Using J2735 3/2016";
	final public static String version = "1.5";
	final public static String release = "June 14, 2022";	
	final public static String copyright = "2014-2022";
	
	private JSONObject configuraton = new JSONObject();

	public Configuration() throws JSONException {
		JSONObject application = new JSONObject();
		application.put("title", title);
		application.put("subtitle", subtitle);
		application.put("version", version);
		application.put("release", release);		
		application.put("copyright", copyright);
		configuraton.put("application", application);
		configuraton.put("defaultIndex", 0);
		JSONArray validators = new JSONArray();
		configuraton.put("validators", validators);
	}
	
	void setDefaultIndex(int index) throws JSONException {
		if ( index >= 0 ) {
			configuraton.put("defaultIndex", index);
		}
	}
	
	void addConfiguration(String version, List<String> messages) throws JSONException {
		JSONObject validator = new JSONObject();
		validator.put("version", version);
		validator.put("messages", messages);
		JSONArray validators = configuraton.getJSONArray("validators");
		validators.put(validator);
	}
	
	@Override
	public String toString() {
		return configuraton.toString();
	}
	
	public String toString(int indentFactor) throws JSONException {
		return configuraton.toString(indentFactor);
	}

}
