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

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import gov.usdot.cv.adapter.S3Adapter;
import gov.usdot.cv.config.AzureConfig;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TileProxyService {

    private S3Adapter s3Adapter;
    private RestTemplate restTemplate;
    private AzureConfig azureConfig;

    public TileProxyService(RestTemplateBuilder restTemplateBuilder, S3Adapter s3Depositor, AzureConfig azureConfig) {
        this.restTemplate = restTemplateBuilder.build();
        this.s3Adapter = s3Depositor;
        this.azureConfig = azureConfig;
    }

    /**
     * Fetches the tile set 
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    @Cacheable("mapTilesCache")
    public byte[] fetchTileSets(String tilesetId, int z, int x, int y) {
        if (s3Adapter.isS3Enabled()) {
            //Fetch the tile set from AWS S3 bucket
            byte[] s3TileBytes = s3Adapter.fetchTileSetsFromS3(tilesetId, z, x, y);
            if (s3TileBytes != null && s3TileBytes.length > 0) {
                log.info("Tile set found in S3 for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
                return s3TileBytes;
            }
        } else {
            log.warn("S3 Bucket is not configured. Skipping S3 fetch.");
        }
        
        // Fetch the tile set from Azure Maps
        byte[] azureTileBytes = fetchTileSetsFromAzureMap(tilesetId, z, x, y);
        //Save to S3
        if (azureTileBytes != null && azureTileBytes.length > 0) {
            log.info("Tile set fetched from Azure Maps for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
            s3Adapter.saveToS3(tilesetId, z, x, y, azureTileBytes);
            return azureTileBytes;
        } else {
            log.error("Tile set not found in Azure Maps for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
        }
        return null;
    }
  
    /**
     * Fetches the tile set from Azure Maps.
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    private byte[] fetchTileSetsFromAzureMap(String tilesetId, int z, int x, int y) {
        String uri = String.format(azureConfig.getTilesetUrl(), tilesetId, z, x, y, azureConfig.getApiKey());
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
