/*
 * Copyright (C) 2025 LEIDOS.
 * Apache License 2.0
 */
package gov.usdot.cv.azuremap.services;

import gov.usdot.cv.adapter.S3Adapter;
import gov.usdot.cv.config.AzureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link TileProxyService}.
 *
 * WHY THESE TESTS HAVE VALUE:
 * fetchTileSets() implements a two-level cache: S3 first, then Azure Maps.
 * The branching logic determines whether tiles come from S3 (fast, cheap),
 * Azure (slower, expensive), or null (error state). A bug here means the
 * UI either gets stale tiles, incurs unnecessary Azure API costs, or shows
 * a blank map. Tests verify each branch without making real network calls.
 *
 * INFRASTRUCTURE REQUIRED:
 * Mockito is already a test dependency in fedgov-cv-map-services-proxy/pom.xml.
 * No additional dependencies needed.
 *
 * NOTE: TileProxyService constructor takes RestTemplateBuilder which is
 * framework-managed. We use Mockito to mock the RestTemplateBuilder chain
 * and inject the RestTemplate directly via reflection.
 */
@ExtendWith(MockitoExtension.class)
public class TileProxyServiceTest {

    @Mock private S3Adapter s3Adapter;
    @Mock private AzureConfig azureConfig;
    @Mock private RestTemplate restTemplate;

    private TileProxyService service;
    private final Executor syncExecutor = Runnable::run; // synchronous for testing

    @BeforeEach
    public void setUp() throws Exception {
        // Build service using mocked RestTemplateBuilder chain
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.setConnectTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.setReadTimeout(any(Duration.class))).thenReturn(builder);
        when(builder.build()).thenReturn(restTemplate);

        service = new TileProxyService(builder, s3Adapter, azureConfig, syncExecutor);
    }

    // =========================================================================
    // fetchTileSets — S3 hit path
    // =========================================================================

    @Test
    public void fetchTileSets_s3Hit_returnsS3Bytes() {
        byte[] s3Bytes = new byte[]{1, 2, 3};
        when(s3Adapter.fetchTileSetsFromS3("tileset1", 5, 10, 20)).thenReturn(s3Bytes);

        byte[] result = service.fetchTileSets("tileset1", 5, 10, 20);

        assertArrayEquals(s3Bytes, result);
        // Azure should NOT be called
        verifyNoInteractions(restTemplate);
    }

    // =========================================================================
    // fetchTileSets — S3 miss, Azure hit path
    // =========================================================================

    @Test
    public void fetchTileSets_s3Miss_azureHit_returnsAzureBytes() {
        byte[] azureBytes = new byte[]{4, 5, 6};
        when(s3Adapter.fetchTileSetsFromS3(any(), anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(azureConfig.getTilesetUrl()).thenReturn("https://azure.example.com/%s/%d/%d/%d?key=%s");
        when(azureConfig.getApiKey()).thenReturn("azure-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(ResponseEntity.ok(azureBytes));

        byte[] result = service.fetchTileSets("tileset1", 5, 10, 20);

        assertArrayEquals(azureBytes, result);
        // Should save to S3 asynchronously (sync executor here)
        verify(s3Adapter).saveToS3("tileset1", 5, 10, 20, azureBytes);
    }

    @Test
    public void fetchTileSets_s3EmptyArray_azureHit_returnsAzureBytes() {
        // Empty array (not null) is also treated as cache miss
        byte[] azureBytes = new byte[]{7, 8, 9};
        when(s3Adapter.fetchTileSetsFromS3(any(), anyInt(), anyInt(), anyInt())).thenReturn(new byte[0]);
        when(azureConfig.getTilesetUrl()).thenReturn("https://azure.example.com/%s/%d/%d/%d?key=%s");
        when(azureConfig.getApiKey()).thenReturn("azure-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(ResponseEntity.ok(azureBytes));

        byte[] result = service.fetchTileSets("tileset1", 5, 10, 20);

        assertArrayEquals(azureBytes, result);
    }

    // =========================================================================
    // fetchTileSets — S3 miss, Azure miss path
    // =========================================================================

    @Test
    public void fetchTileSets_s3Miss_azureMiss_returnsNull() {
        when(s3Adapter.fetchTileSetsFromS3(any(), anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(azureConfig.getTilesetUrl()).thenReturn("https://azure.example.com/%s/%d/%d/%d?key=%s");
        when(azureConfig.getApiKey()).thenReturn("azure-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(ResponseEntity.ok(null));

        byte[] result = service.fetchTileSets("tileset1", 5, 10, 20);

        assertNull(result);
        verify(s3Adapter, never()).saveToS3(any(), anyInt(), anyInt(), anyInt(), any());
    }

    @Test
    public void fetchTileSets_s3Miss_azureThrows_returnsNull() {
        when(s3Adapter.fetchTileSetsFromS3(any(), anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(azureConfig.getTilesetUrl()).thenReturn("https://azure.example.com/%s/%d/%d/%d?key=%s");
        when(azureConfig.getApiKey()).thenReturn("azure-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenThrow(new RuntimeException("Connection refused"));

        byte[] result = service.fetchTileSets("tileset1", 5, 10, 20);

        assertNull(result);
    }

    // =========================================================================
    // fetchTileSets — S3 save failure does not affect return value
    // =========================================================================

    @Test
    public void fetchTileSets_s3SaveFails_stillReturnsAzureBytes() throws Exception {
        byte[] azureBytes = new byte[]{10, 11, 12};
        when(s3Adapter.fetchTileSetsFromS3(any(), anyInt(), anyInt(), anyInt())).thenReturn(null);
        when(azureConfig.getTilesetUrl()).thenReturn("https://azure.example.com/%s/%d/%d/%d?key=%s");
        when(azureConfig.getApiKey()).thenReturn("azure-key");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
            .thenReturn(ResponseEntity.ok(azureBytes));
        doThrow(new RuntimeException("S3 unavailable"))
            .when(s3Adapter).saveToS3(any(), anyInt(), anyInt(), anyInt(), any());

        byte[] result = service.fetchTileSets("tileset1", 5, 10, 20);

        // Azure bytes still returned despite S3 save failure
        assertArrayEquals(azureBytes, result);
    }
}