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
package gov.usdot.cv.bingmap.services;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usdot.cv.bingmap.models.BingImageryMetadata;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Service
public class BingImageryServices {

    private static final String URL_BASE ="https://dev.virtualearth.net/REST/v1/Imagery/Metadata/Aerial/";
    private static final String SCHEMA="https";
    private Logger logger = LogManager.getLogger(BingImageryServices.class);
    private final OkHttpClient client = new OkHttpClient();

    public String composeFullURL(String latitude, String longitude, String zoomLevel, String api_key){
        String fullURL = URL_BASE + latitude + "," + longitude + "?uriScheme="+SCHEMA+"&zl=" + zoomLevel;
        fullURL += "&key=" + api_key;
        return fullURL;
    }

    public BingImageryMetadata getImageryMetadata(String latitude, String longitude, String zoomLevel, String api_key){
        String fullURL = composeFullURL(latitude, longitude, zoomLevel, api_key);
        Request req = new Request.Builder()
        .url(fullURL)
        .build();
        try {
            logger.info("Imagery Metadata call: " + fullURL);
            Response response = client.newCall(req).execute();
            if(response.isSuccessful()){
                BingImageryMetadata data = parseImageryMetadataResponse(response.body().string());
                return data;
            }else{
                logger.error("Error response from BingMap Imagery Metadata API. Detailed error: "+ response);
            }
        } catch (IOException ex) {
            logger.error("IO error calling BingMap Imagery Metadata API " + fullURL + ". Detailed error: "+ ex.getMessage());
        }
        return null;

    }
    public BingImageryMetadata parseImageryMetadataResponse(String response){
        try {
            ObjectMapper objMapper = new ObjectMapper();
            JsonNode rootNode = objMapper.readTree(response);
            JsonNode resultsNode = rootNode.get("resourceSets").get(0).get("resources").get(0);
            BingImageryMetadata data =  objMapper.readValue(resultsNode.toString(), BingImageryMetadata.class);
            logger.info("Response: "+ data.toString());
            return data;
        }catch(IllegalArgumentException ex){
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        } catch(NullPointerException ex){
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        } catch (JsonMappingException ex) {
            logger.error("Cannot map JSON response to self-defined imagery metadata. Response content: "+response+ ". Detailed error: "+ ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        }
        return null;
    }
}
