/*
 * Copyright (C) 2024 LEIDOS.
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
package gov.usdot.cv.googlemap.services;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usdot.cv.googlemap.models.GoogleElevationResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class GoogleElevationsService {

    private static final String URL_BASE ="https://maps.googleapis.com/maps/api/elevation";
    private static final String TYPE="json";
    private Logger logger = LogManager.getLogger(GoogleElevationsService.class);
    private final OkHttpClient client = new OkHttpClient();

    public String composeFullURL(String latitude, String longitude, String api_key){
        String fullURL = URL_BASE+"/"+ TYPE +"?locations="+ latitude + "," + longitude;
        fullURL += "&key=" + api_key;
        return fullURL;
    }

    public GoogleElevationResponse getElevation(String latitude, String longitude, String api_key){
        String fullURL = composeFullURL(latitude, longitude, api_key);
        Request req = new Request.Builder()
        .url(fullURL)
        .build();
        try {
            logger.info("Elevation call: " + fullURL);
            Response response = client.newCall(req).execute();
            if(response.isSuccessful()){
                GoogleElevationResponse elevationData = parseElevationResponse(response.body().string());
                return elevationData;
            }else{
                logger.error("Error response from Google Elevation API. Detailed error: "+ response);
            }
        } catch (IOException ex) {
            logger.error("IO error calling Google Elevation API " + fullURL + ". Detailed error: "+ ex.getMessage());
        }
        return null;
    }

    public GoogleElevationResponse parseElevationResponse(String response){
        try {
            ObjectMapper objMapper = new ObjectMapper();
            JsonNode rootNode = objMapper.readTree(response);
            if(rootNode.has("results") && !rootNode.get("results").get(0).isNull()){
                JsonNode resultsNode = rootNode.get("results").get(0);
                GoogleElevationResponse elevationData =  objMapper.readValue(resultsNode.toString(), GoogleElevationResponse.class);
                logger.info("Response: "+ elevationData.toString());
                return elevationData;
            }else{
                logger.info("Empty results from response: "+ response);
            }
        } catch(IllegalArgumentException ex){
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        } catch(NullPointerException ex){
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        } catch (JsonMappingException ex) {
            logger.error("Cannot map JSON response to self-defined elevation. Response content: "+response+ ". Detailed error: "+ ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        }
        return null;
    }

}
