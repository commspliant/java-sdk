# Render HTML

## HTTP

- **Method:** `POST`
- **Path:** `/api/v1/render/html`
- **Permission:** `render.execute`

## Description

Resolves an **approved** template version and returns rendered HTML as a streamed response.

- `templateId` is required.
- The latest approved version is always used.
- `variables` supplies values for `{{placeholder}}` syntax in the template.

## Request

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `templateId` | UUID string | Yes | Template to render (latest approved version is used) |
| `variables` | object | Yes | Values for template placeholders |

## Response

- **Status:** `200 OK`
- **Content-Type:** `text/html`
- **Body:** Rendered HTML stream
- **Headers:** `X-Request-ID` (request correlation ID), `Content-Disposition`

## Errors

| Status | Meaning |
|--------|---------|
| 400 | Invalid request body or parameters |
| 401 | Missing or invalid API key |
| 403 | Valid API key but missing `render.execute` permission |
| 404 | Template not found or not visible in the key's organization |
| 422 | Template version not approved for rendering |
| 429 | Render quota exceeded |
| 500 | Unexpected server error |

## SDK example

```java
import com.commspliant.sdk.CommsPliantClient;
import com.commspliant.sdk.RenderRequest;
import com.commspliant.sdk.RenderResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RenderHtmlExample {
    public static void main(String[] args) throws Exception {
        CommsPliantClient client = new CommsPliantClient("ck_YOUR_API_KEY");

        RenderRequest request = RenderRequest.builder()
                .templateId("550e8400-e29b-41d4-a716-446655440000")
                .variables(Map.of(
                        "title", "Monthly Report",
                        "user", Map.of("name", "Jane Doe")
                ))
                .build();

        RenderResult result = client.renderHtml(request);
        Files.write(Path.of("document.html"), result.getBody());
    }
}
```
