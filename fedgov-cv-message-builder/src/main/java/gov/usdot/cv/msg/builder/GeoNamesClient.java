package gov.usdot.cv.msg.builder;

import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

// import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
@Path("/geonames")
public class GeoNamesClient {
	
	private static final Logger logger = LogManager.getLogger(GeoNamesClient.class);
	
	private static final String FIND_INTERSECTION_URL = "http://api.geonames.org/findNearestIntersectionJSON?lat=%s&lng=%s&username=%s";
	private static final String FIND_ELEVATION_URL = "http://api.geonames.org/gtopo30JSON?lat=%s&lng=%s&username=%s";
	
	@Path("/findNearestIntersectionJSON")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String findNearestIntersectionJSON(
			@QueryParam("lat") String lat, 
			@QueryParam("lng") String lng, 
			@QueryParam("username") String username) {
		logger.debug("calling findNearestIntersectionJSON for lat:" + lat + " lon:" + lng + " username:" + username);
		return doGet(FIND_INTERSECTION_URL, lat, lng, username);
	}
	
	@Path("/gtopo30JSON")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String gtopo30JSON(
			@QueryParam("lat") String lat, 
			@QueryParam("lng") String lng, 
			@QueryParam("username") String username) {
		logger.debug("calling gtopo30JSON for lat:" + lat + " lon:" + lng + " username:" + username);
		return doGet(FIND_ELEVATION_URL, lat, lng, username);
	}
	
	private String doGet(String baseURL, Object...params) {
		String url = null;
		InputStream is = null;
		String response = "";
		try {
			url = String.format(baseURL, params);
			is = new URL(url).openStream();
			// response = IOUtils.toString(is);
		} catch (Exception e) {
			logger.error("Error calling " + url, e);
		} 
		// finally {
		// 	IOUtils.closeQuietly(is);
		// }
		return response;
	}
}
