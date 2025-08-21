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

package gov.usdot.cv.azuremap.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TileProxyService {

    private S3Client s3Client;
    
    private RestTemplate restTemplate;
    
    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${azure.map.api.key}")
    private String apiKey;

    @Value("${azure.map.tileset.url}")
    private String tilesetUrl;

    public TileProxyService(RestTemplateBuilder restTemplateBuilder, S3Client s3Client) {
        this.restTemplate = restTemplateBuilder.build();
        this.s3Client = s3Client;
    }

    /**
     * Fetches the tile set 
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    @Cacheable("mapTilesCache")
    public byte[] fetchTileSets(String tilesetId, int z, int x, int y) {
        //Fetch the tile set from AWS S3 bucket
        byte[] s3TileBytes = fetchFromS3(tilesetId, z, x, y);
        if (s3TileBytes != null && s3TileBytes.length > 0) {
            log.info("Tile set found in S3 for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
            return s3TileBytes;
        }
        // Fetch the tile set from Azure Maps
        byte[] azureTileBytes = fetchFromAzureMap(tilesetId, z, x, y);        
        //Save to S3
        if (azureTileBytes != null && azureTileBytes.length > 0) {
            log.info("Tile set fetched from Azure Maps for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
            saveToS3(tilesetId, z, x, y, azureTileBytes);
            return azureTileBytes;
        }
        return null;
    }
    /**
     * Fetches the tile set from AWS S3.
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    private byte[] fetchFromS3(String tilesetId, int z, int x, int y) {
        String key = String.format("%s/%d/%d/%d", tilesetId, z, x, y);
        try {
            log.info("Fetching tile set from S3 for key: {}", key);
            return s3Client.getObjectAsBytes(b -> b.bucket(bucket).key(key)).asByteArray();
        } catch (Exception e) {
            log.error("Error fetching tile set from S3 for key: {}, {}", key, e.getMessage());
            return null;
        }
    }

    /**
     * Saves the tile set to AWS S3.
     * @param tilesetId The ID of the tileset.
     * @param z The zoom level.
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param tileData The tile data to save.
     */
    private void saveToS3(String tilesetId, int z, int x, int y, byte[] tileData) {
        String key = String.format("%s/%d/%d/%d", tilesetId, z, x, y);
        try {
            log.info("Saving tile set to S3 for key: {}", key);
            s3Client.putObject(b -> b.bucket(bucket).key(key).contentType("image/png"), RequestBody.fromBytes(tileData));
        } catch (Exception e) {
            log.error("Error saving tile set to S3 for key: {}", key, e.getMessage());
        }
    }

    /**
     * Fetches the tile set from Azure Maps.
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    private byte[] fetchFromAzureMap(String tilesetId, int z, int x, int y) {
        String uri = String.format(tilesetUrl, tilesetId, z, x, y, apiKey);
        log.info("Fetching tile set from Azure Maps for URI: {}", uri);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "image/png");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching tile set from Azure Maps for URI: {}, {}", uri, e.getMessage());
            return null;
        }       
    }
}
