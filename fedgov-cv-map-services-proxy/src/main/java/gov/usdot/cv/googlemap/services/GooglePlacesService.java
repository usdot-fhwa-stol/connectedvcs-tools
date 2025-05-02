/*
 * Copyright (C) 2025 LEIDOS.
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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.usdot.cv.googlemap.models.GooglePlaceSuggestions;
import gov.usdot.cv.googlemap.models.GoogleSearchTextResponse;
import gov.usdot.cv.utils.RequestComposer;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class GooglePlacesService {
    private final Logger logger = LogManager.getLogger(GooglePlacesService.class);
    private final String AUTOCOMPLETE_URL = "https://places.googleapis.com/v1/places:autocomplete";
    private final String SEARCHTEXT_URL="https://places.googleapis.com/v1/places:searchText";
    private final String CONTENT_TYPE="application/json";
    private final String X_GOOG_FIELD_MASK_AUTOCOMPLETE = "suggestions.placePrediction.text";
    private final String X_GOOG_FIELD_MASK_SEARCHTEXT= "places.formattedAddress,places.location";
    private final OkHttpClient client = new OkHttpClient();

    public GooglePlaceSuggestions requestAutoComplete(String searchPlace, String googleAPIKey){
        Map<String, String> fieldMap = new HashMap<>();
        String searchPlaceFieldKey = "input";
        fieldMap.put(searchPlaceFieldKey, searchPlace);
        String data = new RequestComposer().composeRequestBodyData(fieldMap);
        if(data != null){
            RequestBody body = RequestBody.create(data, MediaType.parse(CONTENT_TYPE));
            Request req = new Request.Builder()
                .url(AUTOCOMPLETE_URL)
                .addHeader("X-Goog-Api-Key", googleAPIKey)
                .addHeader("X-Goog-FieldMask", X_GOOG_FIELD_MASK_AUTOCOMPLETE)
                .addHeader("Content-Type", CONTENT_TYPE)
                .post(body)
                .build();
            try{
                logger.info("Send request [{}={}] to Google places API autocomplete: {}",searchPlaceFieldKey ,searchPlace, AUTOCOMPLETE_URL);
                Response response = client.newCall(req).execute();
                return parseAutoCompletePlacesResponse(response.body().string());
            }catch(IOException | IllegalStateException ex){
                logger.error("Failed to call Google places API autocomplete: Detail error message: "+ ex.getMessage());
            }
        }
        return null;
    }


    public GoogleSearchTextResponse requestSearchText(String searchText, String googleAPIKey){
        Map<String, String> fieldMap = new HashMap<>();
        String searchPlaceFieldKey="textQuery";
        fieldMap.put(searchPlaceFieldKey, searchText);
        String data = new RequestComposer().composeRequestBodyData(fieldMap);
        if(data != null){
            RequestBody body = RequestBody.create(data, MediaType.parse(CONTENT_TYPE));
            Request req = new Request.Builder()
                .url(SEARCHTEXT_URL)
                .addHeader("X-Goog-Api-Key", googleAPIKey)
                .addHeader("X-Goog-FieldMask", X_GOOG_FIELD_MASK_SEARCHTEXT)
                .addHeader("Content-Type", CONTENT_TYPE)
                .post(body)
                .build();
            try{
                logger.info("Send request [{}={}] to Google places API searchText: {}", searchPlaceFieldKey, searchText, SEARCHTEXT_URL);
                Response response = client.newCall(req).execute();
                return parseSearchTextResponse(response.body().string());
            }catch(IOException | IllegalStateException ex){
                logger.error("Failed to call Google places API searchText: Detail error message: "+ ex.getMessage());
            }
        }
        return null;
    }

    public GooglePlaceSuggestions parseAutoCompletePlacesResponse(String response){
        ObjectMapper objMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objMapper.readTree(response);
            boolean isError = rootNode.has("error");
            if(!isError){
                logger.info("Response: "+response);
                GooglePlaceSuggestions suggestions =  objMapper.readValue(rootNode.toString(), GooglePlaceSuggestions.class);
                return suggestions;
            }else{
                logger.error("Failure: "+response);
            }
        } catch(IllegalArgumentException | NullPointerException ex){
            logger.error("Error response: "+response+". Detailed error: "+ ex.getMessage());
        }  catch (JsonMappingException ex) {
            logger.error("Cannot map JSON response to self-defined places suggestions. Response content: "+response+ ". Detailed error: "+ ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        }
        return null;
    }

    public GoogleSearchTextResponse parseSearchTextResponse(String response){
        ObjectMapper objMapper = new ObjectMapper();
        JsonNode rootNode;
        try {
            rootNode = objMapper.readTree(response);
            boolean isError = rootNode.has("error");
            if(!isError){
                logger.info("Response: "+response);
                GoogleSearchTextResponse result =  objMapper.readValue(rootNode.get("places").get(0).toString(), GoogleSearchTextResponse.class);
                return result;
            }else{
                logger.error("Failure: "+response);
            }
        } catch(IllegalArgumentException | NullPointerException ex){
            logger.error("Error response: "+response+". Detailed error: "+ ex.getMessage());
        }  catch (JsonMappingException ex) {
            logger.error("Cannot map JSON response to self-defined searchText result. Response content: "+response+ ". Detailed error: "+ ex.getMessage());
        } catch (JsonProcessingException ex) {
            logger.error("Failed to parse response: "+response+". Detailed error: "+ ex.getMessage());
        }
        return null;
    }
}
