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
package gov.usdot.cv.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import gov.usdot.cv.config.S3Config;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@Slf4j
public class S3Adapter {
    private final Optional<S3Client> s3Client;
    private final S3Config s3Config;

    public S3Adapter(Optional<S3Client> s3Client, S3Config s3Config) {
        this.s3Client = s3Client;
        this.s3Config = s3Config;
    }

    /**
     * Checks if S3 is enabled.
     * @return true if S3 is enabled, false otherwise.
     */
    public boolean isS3Enabled() {
        return s3Client.isPresent();
    }

    /**
     * Fetches the tile set from AWS S3.
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    public byte[] fetchTileSetsFromS3(String tilesetId, int z, int x, int y) {
        if (!isS3Enabled()) {
            log.info("S3 Client is not configured. Skipping S3 fetch.");
            return null;
        }
        String key = String.format("%s/%d/%d/%d", tilesetId, z, x, y);
        try {
            log.info("Fetching tile set from S3 bucket: {} for key: {}", s3Config.getBucket(), key);
            return s3Client.get().getObjectAsBytes(b -> b.bucket(s3Config.getBucket()).key(key)).asByteArray();
        } catch (S3Exception s3e) {
            if (s3e.statusCode() == 403 || s3e.statusCode() == 401) {
                log.error("Authentication error fetching tile set from S3 bucket: {} for key: {}. Please check your AWS credentials. Error: {}", s3Config.getBucket(), key, s3e.getMessage());
            } else {
                log.error("S3 error fetching tile set from S3 bucket: {} for key: {}, {}", s3Config.getBucket(), key, s3e.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Error fetching tile set from S3 bucket: {} for key: {}, {}", s3Config.getBucket(), key, e.getMessage());
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
    public void saveToS3(String tilesetId, int z, int x, int y, byte[] tileData) {
        if (!isS3Enabled()) {
            log.info("S3 Client is not configured. Skipping S3 save.");
            return;
        }
        String key = String.format("%s/%d/%d/%d", tilesetId, z, x, y);
        try {
            log.info("Saving tile set to S3 bucket: {} for key: {}", s3Config.getBucket(), key);
            s3Client.get().putObject(
                    b -> b.bucket(s3Config.getBucket())
                            .key(key)
                            .contentType("image/png"),
                             RequestBody.fromBytes(tileData));
        } catch (S3Exception s3e) {
            if (s3e.statusCode() == 403 || s3e.statusCode() == 401) {
                log.error("Authentication error saving tile set to S3 bucket: {} for key: {}. Please check your AWS credentials. Error: {}", s3Config.getBucket(), key, s3e.getMessage());
            } else {
                log.error("S3 error saving tile set to S3 bucket: {} for key: {}, {}", s3Config.getBucket(), key, s3e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error saving tile set to S3 bucket: {} for key: {}, {}", s3Config.getBucket(), key, e.getMessage());
        }
    }
}
