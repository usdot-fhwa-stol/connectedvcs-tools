/*
 * Copyright (C) 2026 LEIDOS.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class TileProxyService {

    private S3Adapter s3Adapter;
    private RestTemplate restTemplate;
    private AzureConfig azureConfig;

    private final Logger logger = LogManager.getLogger(TileProxyService.class);
    private final Executor s3Executor;

    public TileProxyService(RestTemplateBuilder restTemplateBuilder, S3Adapter s3Depositor, AzureConfig azureConfig, @Qualifier("s3Executor") Executor s3Executor) {
        this.restTemplate = restTemplateBuilder.setConnectTimeout(Duration.ofSeconds(5)).setReadTimeout(Duration.ofSeconds(10)).build();
        this.s3Adapter = s3Depositor;
        this.azureConfig = azureConfig;
        this.s3Executor = s3Executor;
    }

    /**
     * Fetches the tile set 
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    @Cacheable("mapTilesCache")
    public byte[] fetchTileSets(String tilesetId, int z, int x, int y) {
        //Fetch the tile set from AWS S3 bucket
        byte[] s3TileBytes = s3Adapter.fetchTileSetsFromS3(tilesetId, z, x, y);
        if (s3TileBytes != null && s3TileBytes.length > 0) {
            logger.info("Tile set found in S3 for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
            return s3TileBytes;
        }
        
        // Fetch the tile set from Azure Maps
        byte[] azureTileBytes = fetchTileSetsFromAzureMap(tilesetId, z, x, y);
        //Save to S3
        if (azureTileBytes != null && azureTileBytes.length > 0) {
            logger.info("Tile set fetched from Azure Maps for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
            s3Executor.execute(() -> {
                try {
                    s3Adapter.saveToS3(tilesetId, z, x, y, azureTileBytes);
                    logger.info("Successfully saved tile set to S3 for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
                } catch (Exception e) {
                    logger.error("Failed to save tile set to S3 for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y, e);
                }
            });
            return azureTileBytes;
        } else {
            logger.warn("Tile set not found in Azure Maps for tilesetId: {}, z: {}, x: {}, y: {}", tilesetId, z, x, y);
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
        logger.info("Fetching tile set from Azure Maps for URI: {}", uri);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "image/png");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching tile set from Azure Maps for URI: {}, {}", uri, e.getMessage());
            return null;
        }       
    }
}
