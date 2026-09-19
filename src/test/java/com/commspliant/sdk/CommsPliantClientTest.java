package com.commspliant.sdk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommsPliantClientTest {
    private static HttpServer server;
    private static String baseUrl;
    private static volatile boolean returnHtmlError;

    @BeforeAll
    static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();

            if ("/api/v1/render/html".equals(path) && returnHtmlError) {
                write(exchange, 404, "{\"error\":\"template not found\"}", Map.of("X-Request-ID", "req-404"));
                return;
            }

            if ("/api/v1/render/html".equals(path)) {
                write(exchange, 200, "<html>ok</html>", Map.of(
                        "Content-Type", "text/html",
                        "X-Request-ID", "req-123"
                ));
                return;
            }

            if ("/api/v1/render/pdf".equals(path)) {
                write(exchange, 200, "%PDF-1.4", Map.of("Content-Type", "application/pdf"));
                return;
            }

            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    static void tearDown() {
        server.stop(0);
    }

    @Test
    void renderHtmlSuccess() throws Exception {
        CommsPliantClient client = new CommsPliantClient("ck_test", baseUrl, false, null);
        RenderResult result = client.renderHtml(RenderRequest.builder()
                .templateId("550e8400-e29b-41d4-a716-446655440000")
                .variables(Map.of("title", "Monthly Report"))
                .build());

        assertArrayEquals("<html>ok</html>".getBytes(StandardCharsets.UTF_8), result.getBody());
        assertEquals("req-123", result.getRequestId());
    }

    @Test
    void renderPdfSuccess() throws Exception {
        CommsPliantClient client = new CommsPliantClient("ck_test", baseUrl, false, null);
        RenderResult result = client.renderPdf(RenderRequest.builder()
                .templateId("550e8400-e29b-41d4-a716-446655440000")
                .variables(Map.of())
                .build());

        assertTrue(new String(result.getBody(), StandardCharsets.UTF_8).startsWith("%PDF"));
    }

    @Test
    void missingTemplateId() {
        CommsPliantClient client = new CommsPliantClient("ck_test", baseUrl, false, null);
        assertThrows(IllegalArgumentException.class, () -> client.renderHtml(RenderRequest.builder()
                .templateId("")
                .variables(Map.of())
                .build()));
    }

    @Test
    void apiError() {
        returnHtmlError = true;
        try {
            CommsPliantClient client = new CommsPliantClient("ck_test", baseUrl, false, null);
            APIException error = assertThrows(APIException.class, () -> client.renderHtml(RenderRequest.builder()
                    .templateId("550e8400-e29b-41d4-a716-446655440000")
                    .variables(Map.of())
                    .build()));
            assertEquals(404, error.getStatusCode());
            assertEquals("template not found", error.getMessage());
            assertEquals("req-404", error.getRequestId().orElse(""));
        } finally {
            returnHtmlError = false;
        }
    }

    private static void write(HttpExchange exchange, int status, String body, Map<String, String> headers)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        headers.forEach((key, value) -> exchange.getResponseHeaders().add(key, value));
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
