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
package gov.usdot.cv.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.http.apache.ApacheHttpClient;

@Configuration
public class S3Config {
    @Value("${aws.s3.accessKey:}")
    private String accessKey;
    
    @Value("${aws.s3.secretKey:}")
    private String secretKey;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.bucket:}")
    public String bucket;

    @Bean
    @ConditionalOnProperty(name = {
        "aws.s3.accessKey",
        "aws.s3.secretKey",
        "aws.s3.region",
        "aws.s3.bucket"
    }, matchIfMissing = false)
    public S3Client s3Client() {
        if (accessKey == null || accessKey.isEmpty() ||
            secretKey == null || secretKey.isEmpty() ||
            region == null || region.isEmpty() ||
            bucket == null || bucket.isEmpty()) {
            return null;
        }
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .httpClientBuilder(
                        ApacheHttpClient.builder()
                                .maxConnections(100)
                                .connectionTimeout(Duration.ofSeconds(10))
                                .socketTimeout(Duration.ofSeconds(30)))
                .build();
    }

    public String getBucket() {
        return bucket;
    }
}
