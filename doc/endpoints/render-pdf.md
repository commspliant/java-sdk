# Render PDF

## HTTP

- **Method:** `POST`
- **Path:** `/api/v1/render/pdf`
- **Permission:** `render.execute`

## Description

Resolves an **approved** template version, renders HTML with `variables`, converts to PDF, and returns the PDF as a streamed response.

Same auth rules as `POST /api/v1/render/html`.

## Request

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `templateId` | UUID string | Yes | Template to render (latest approved version is used) |
| `variables` | object | Yes | Values for template placeholders |

## Response

- **Status:** `200 OK`
- **Content-Type:** `application/pdf`
- **Body:** Rendered PDF stream
- **Headers:** `X-Request-ID` (request correlation ID), `Content-Disposition`

## Errors

| Status | Meaning |
|--------|---------|
| 400 | Invalid request body or parameters. When required sample-data fields are missing from `variables`, includes `code: validation_failed` and `details.missingFields`. |
| 401 | Missing or invalid API key |
| 403 | Valid API key but missing `render.execute` permission |
| 404 | Template not found or not visible in the key's organization |
| 422 | Template version not approved for rendering |
| 429 | Render quota exceeded |
| 500 | Unexpected server error |

### Missing required variables

```json
{
  "error": "Required variables are missing",
  "code": "validation_failed",
  "details": {
    "missingFields": ["firstName", "policies.0.endDate"]
  }
}
```

## SDK example

```java
import com.commspliant.sdk.APIException;
import com.commspliant.sdk.CommsPliantClient;
import com.commspliant.sdk.RenderRequest;
import com.commspliant.sdk.RenderResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class RenderPdfExample {
    public static void main(String[] args) throws Exception {
        CommsPliantClient client = new CommsPliantClient("ck_YOUR_API_KEY");

        RenderRequest request = RenderRequest.builder()
                .templateId("550e8400-e29b-41d4-a716-446655440000")
                .variables(Map.of(
                        "title", "Invoice",
                        "amount", "99.00"
                ))
                .build();

        try {
            RenderResult result = client.renderPdf(request);
            Files.write(Path.of("document.pdf"), result.getBody());
        } catch (APIException err) {
            if ("validation_failed".equals(err.getCode().orElse(null))) {
                // err.getDetails() may contain missingFields
            }
            throw err;
        }
    }
}
```
