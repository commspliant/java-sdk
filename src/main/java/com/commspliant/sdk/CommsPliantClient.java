package com.commspliant.sdk;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class CommsPliantClient {
    public static final String DEFAULT_BASE_URL = "https://api.commspliant.com";

    private final String apiKey;
    private final String baseUrl;
    private final boolean useBearerAuth;
    private final HttpClient httpClient;

    public CommsPliantClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL, false, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build());
    }

    public CommsPliantClient(String apiKey, String baseUrl, boolean useBearerAuth, HttpClient httpClient) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("api key is required");
        }
        this.apiKey = apiKey.trim();
        this.baseUrl = baseUrl == null || baseUrl.isBlank() ? DEFAULT_BASE_URL : baseUrl.replaceAll("/$", "");
        this.useBearerAuth = useBearerAuth;
        this.httpClient = httpClient == null
                ? HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build()
                : httpClient;
    }

    public RenderResult renderHtml(RenderRequest request) throws IOException, InterruptedException {
        return postRender("/api/v1/render/html", request);
    }

    public RenderResult renderPdf(RenderRequest request) throws IOException, InterruptedException {
        return postRender("/api/v1/render/pdf", request);
    }

    private RenderResult postRender(String path, RenderRequest request) throws IOException, InterruptedException {
        validateRenderRequest(request);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("templateId", request.getTemplateId());
        if (request.getTemplateVersionId() != null && !request.getTemplateVersionId().isBlank()) {
            payload.put("templateVersionId", request.getTemplateVersionId());
        }
        payload.put("variables", request.getVariables());

        String json = JsonSupport.toJson(payload);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (useBearerAuth) {
            builder.header("Authorization", "Bearer " + apiKey);
        } else {
            builder.header("X-Api-Key", apiKey);
        }

        HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        String requestId = response.headers().firstValue("X-Request-ID").orElse("");

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw parseAPIError(response.statusCode(), response.body(), requestId);
        }

        return new RenderResult(
                response.body(),
                response.headers().firstValue("Content-Type").orElse(""),
                requestId,
                response.headers().firstValue("Content-Disposition").orElse(null)
        );
    }

    private static void validateRenderRequest(RenderRequest request) {
        Objects.requireNonNull(request, "request is required");
        if (request.getTemplateId() == null || request.getTemplateId().trim().isEmpty()) {
            throw new IllegalArgumentException("templateId is required");
        }
        if (request.getVariables() == null) {
            throw new IllegalArgumentException("variables is required");
        }
    }

    private static APIException parseAPIError(int statusCode, byte[] body, String requestId) {
        if (body == null || body.length == 0) {
            return new APIException(statusCode, "request failed", null, null, requestId);
        }

        try {
            Map<String, Object> parsed = JsonSupport.parseObject(body);
            Object errorValue = parsed.get("error");
            String message = errorValue instanceof String ? (String) errorValue : "request failed";
            Object codeValue = parsed.get("code");
            String code = codeValue instanceof String ? (String) codeValue : null;
            Object detailsValue = parsed.get("details");
            Map<String, Object> details = detailsValue instanceof Map ? castMap((Map<?, ?>) detailsValue) : null;
            return new APIException(statusCode, message, code, details, requestId);
        } catch (RuntimeException ex) {
            return new APIException(statusCode, new String(body), null, null, requestId);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> value) {
        return (Map<String, Object>) value;
    }
}
