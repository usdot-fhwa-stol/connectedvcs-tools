package gov.usdot.cv.adapter;

import org.springframework.stereotype.Component;

import gov.usdot.cv.config.S3Config;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@Slf4j
public class S3Adapter {
    private S3Client s3Client;
    private S3Config s3Config;

    public S3Adapter(S3Client s3Client, S3Config s3Config) {
        this.s3Client = s3Client;
        this.s3Config = s3Config;
    }

    /**
     * Checks if S3 integration is enabled.
     * @return true if S3 is enabled, false otherwise.
     */
    public boolean isS3Enabled() {
        return s3Config.getBucket() != null && !s3Config.getBucket().isEmpty() && s3Config.getAccessKey() != null
                && !s3Config.getAccessKey().isEmpty() && s3Config.getSecretKey() != null
                && !s3Config.getSecretKey().isEmpty();
    }
    
    /**
     * Fetches the tile set from AWS S3.
     * @param uri The URI to fetch the tile set from.
     * @return The byte array representing the tile set.
     */
    public byte[] fetchTileSetsFromS3(String tilesetId, int z, int x, int y) {
        String key = String.format("%s/%d/%d/%d", tilesetId, z, x, y);
        try {
            log.info("Fetching tile set from S3 bucket: {} for key: {}", s3Config.getBucket(), key);
            return s3Client.getObjectAsBytes(b -> b.bucket(s3Config.getBucket()).key(key)).asByteArray();
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
        String key = String.format("%s/%d/%d/%d", tilesetId, z, x, y);
        try {
            log.info("Saving tile set to S3 bucket: {} for key: {}", s3Config.getBucket(), key);
            s3Client.putObject(
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
