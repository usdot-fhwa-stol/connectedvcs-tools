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
package gov.usdot.cv.gnis.services;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

public class GnisApiClientTest {

    @Test
    void callsOverTheLimitFailImmediatelyAndPermitsReturnAfterTimeout() throws Exception {
        // A server that accepts connections but never answers, so calls stay in flight until they time out
        try (ServerSocket silentServer = new ServerSocket(0, 10, InetAddress.getLoopbackAddress())) {
            String url = "http://127.0.0.1:" + silentServer.getLocalPort() + "/query";
            GnisApiClient client = new GnisApiClient(1, 2);

            CompletableFuture<String> first = client.fetch(url);
            CompletableFuture<String> second = client.fetch(url);
            assertEquals(0, client.availableCalls());

            CompletableFuture<String> overLimit = client.fetch(url);
            assertTrue(overLimit.isDone());
            assertNull(overLimit.get());

            assertNull(first.get());
            assertNull(second.get());
            assertEquals(2, client.availableCalls());
        }
    }

    @Test
    void awaitReturnsNullWhenTheCallNeverCompletes() {
        GnisApiClient client = new GnisApiClient(1, 2);
        assertNull(client.await(new CompletableFuture<String>()));
    }

    @Test
    void failedConnectionCompletesWithNull() throws Exception {
        int unusedPort;
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            unusedPort = socket.getLocalPort();
        }
        GnisApiClient client = new GnisApiClient(2, 2);
        assertNull(client.await(client.fetch("http://127.0.0.1:" + unusedPort + "/query")));
        assertEquals(2, client.availableCalls());
    }

    @Test
    void successfulResponseReturnsBodyAndErrorResponseReturnsNull() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/ok", exchange -> {
            byte[] body = "{\"features\":[]}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            GnisApiClient client = new GnisApiClient(2, 2);
            assertEquals("{\"features\":[]}", client.await(client.fetch(base + "/ok")));
            assertNull(client.await(client.fetch(base + "/error")));
            assertEquals(2, client.availableCalls());
        } finally {
            server.stop(0);
        }
    }
}
