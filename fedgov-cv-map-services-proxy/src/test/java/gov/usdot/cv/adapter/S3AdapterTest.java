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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import gov.usdot.cv.config.S3Config;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
  "azure.map.api.key=fake-api-key",
  "azure.map.tileset.url=https://atlas.microsoft.com/map/tile?api-version=2.1&tilesetId=microsoft.imagery&zoom=%d&x=%d&y=%d&subscription-key=%s",
  "aws.s3.accessKey=unknown",
  "aws.s3.secretKey=unknown",
  "aws.s3.bucket=unknown"
})
public class S3AdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Config s3Config;

    @Test
    void isS3Enabled_returnsTrue_whenAllConfigPresent() {
        when(s3Config.getBucket()).thenReturn("my-bucket");
        when(s3Config.getAccessKey()).thenReturn("AKIA");
        when(s3Config.getSecretKey()).thenReturn("SECRET");
        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        assertTrue(s3Adapter.isS3Enabled());
    }

    @Test
    void isS3Enabled_returnsFalse_whenBucketMissing() {
        when(s3Config.getBucket()).thenReturn(null);
        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        assertFalse(s3Adapter.isS3Enabled());
    }

    @Test
    void fetchTileSetsFromS3_returnsBytes_whenObjectExists() {
        byte[] expected = new byte[] {1,2,3};
        ResponseBytes<GetObjectResponse> respBytes =
                ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), expected);
        when(s3Client.getObjectAsBytes(any(Consumer.class))).thenReturn(respBytes);
        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        byte[] result = s3Adapter.fetchTileSetsFromS3("tileset", 1, 2, 3);
        assertArrayEquals(expected, result);
    }

    @Test
    void fetchTileSetsFromS3_returnsNull_onAuthS3Exception() {
        AwsServiceException authEx = S3Exception.builder().statusCode(403).message("Forbidden").build();
        when(s3Client.getObjectAsBytes(any(Consumer.class)))
            .thenThrow(authEx);

        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        byte[] result = s3Adapter.fetchTileSetsFromS3("tileset", 0, 0, 0);
        assertNull(result);
    }

    @Test
    void fetchTileSetsFromS3_returnsNull_onOtherS3Exception() {
        AwsServiceException other = S3Exception.builder().statusCode(500).message("ServerError").build();
        when(s3Client.getObjectAsBytes(any(Consumer.class)))
            .thenThrow(other);

        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        assertNull(s3Adapter.fetchTileSetsFromS3("tileset", 0, 0, 0));
    }

    @Test
    void fetchTileSetsFromS3_returnsNull_onRuntimeException() {
        when(s3Client.getObjectAsBytes(any(Consumer.class)))
            .thenThrow(new RuntimeException("boom"));

        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        assertNull(s3Adapter.fetchTileSetsFromS3("tileset", 0, 0, 0));
    }

    @Test
    void saveToS3_invokesPutObject_withRequestBody() {
        PutObjectResponse putResp = PutObjectResponse.builder().eTag("etag").build();
        when(s3Client.putObject(any(Consumer.class), any(RequestBody.class)))
            .thenReturn(putResp);

        byte[] data = new byte[] {9,8,7};
        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        s3Adapter.saveToS3("tileset", 4, 5, 6, data);
        verify(s3Client).putObject(any(Consumer.class), any(RequestBody.class));
    }

    @Test
    void saveToS3_handlesAuthS3Exception_withoutThrowing() {
        when(s3Client.putObject(any(Consumer.class), any(RequestBody.class)))
            .thenThrow(S3Exception.builder().statusCode(403).message("Forbidden").build());

        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        assertDoesNotThrow(() -> s3Adapter.saveToS3("tileset", 1, 1, 1, new byte[] {1}));
    }

    @Test
    void saveToS3_handlesRuntimeException_withoutThrowing() {
        when(s3Client.putObject(any(Consumer.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("boom"));

        S3Adapter s3Adapter = new S3Adapter(s3Client, s3Config);
        assertDoesNotThrow(() -> s3Adapter.saveToS3("tileset", 1, 1, 1, new byte[] {2}));
    }
}
