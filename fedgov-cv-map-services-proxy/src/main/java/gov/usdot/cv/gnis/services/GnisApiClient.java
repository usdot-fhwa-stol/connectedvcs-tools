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

package gov.usdot.cv.gnis.services;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Makes the outside calls to the Census and USGS GNIS services. Calls time out after a few seconds,
 * and only a limited number may be in flight at once across all users, so a burst of GNIS requests
 * cannot tie up the proxy or flood those services; calls over the limit fail immediately.
 */
@Component
public class GnisApiClient {
    static final long DEFAULT_TIMEOUT_SECONDS = 5;
    static final int DEFAULT_MAX_CONCURRENT_CALLS = 50;
    private static final Logger logger = LogManager.getLogger(GnisApiClient.class);

    private final OkHttpClient client;
    private final Semaphore permits;
    private final long timeoutSeconds;

    public GnisApiClient() {
        this(DEFAULT_TIMEOUT_SECONDS, DEFAULT_MAX_CONCURRENT_CALLS);
    }

    GnisApiClient(long timeoutSeconds, int maxConcurrentCalls) {
        this.timeoutSeconds = timeoutSeconds;
        this.permits = new Semaphore(maxConcurrentCalls);
        this.client = new OkHttpClient.Builder().callTimeout(timeoutSeconds, TimeUnit.SECONDS).build();
    }

    /**
     * @return a future completed with the response body, or with null if the call failed or the
     *         limit on calls in flight was reached
     */
    public CompletableFuture<String> fetch(String url) {
        if (!permits.tryAcquire()) {
            logger.warn("GNIS lookup call skipped, too many calls in flight: {}", url);
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<String> future = new CompletableFuture<>();
        future.whenComplete((body, ex) -> permits.release());
        logger.info("GNIS lookup call: {}", url);
        client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException ex) {
                logger.error("IO error calling GNIS lookup API {}. Detailed error: {}", url, ex.getMessage());
                future.complete(null);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    if (response.isSuccessful() && body != null) {
                        future.complete(body.string());
                    } else {
                        logger.error("Error response from GNIS lookup API {}. Detailed error: {}", url, response);
                        future.complete(null);
                    }
                } catch (IOException ex) {
                    logger.error("IO error reading GNIS lookup response {}. Detailed error: {}", url, ex.getMessage());
                    future.complete(null);
                }
            }
        });
        return future;
    }

    /**
     * @return the future's value, or null if it did not complete within the timeout
     */
    public <T> T await(CompletableFuture<T> future) {
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException ex) {
            logger.error("GNIS lookup did not complete. Detailed error: {}", ex.getMessage());
        }
        return null;
    }

    int availableCalls() {
        return permits.availablePermits();
    }
}
