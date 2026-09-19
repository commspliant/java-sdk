# CommsPliant Java SDK

Official Java client for the [CommsPliant Customer Integration API](https://developer.commspliant.com/).

Requires Java 11 or later.

## Installation

```bash
mvn install
```

Or clone this repository and add it as a Maven dependency from your local build.

## Quickstart

```java
import com.commspliant.sdk.CommsPliantClient;
import com.commspliant.sdk.RenderRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class Example {
    public static void main(String[] args) throws Exception {
        CommsPliantClient client = new CommsPliantClient("ck_YOUR_API_KEY");

        var result = client.renderHtml(RenderRequest.builder()
                .templateId("550e8400-e29b-41d4-a716-446655440000")
                .variables(Map.of(
                        "title", "Monthly Report",
                        "user", Map.of("name", "Jane Doe")
                ))
                .build());

        Files.write(Path.of("document.html"), result.getBody());
    }
}
```

## Authentication

By default the SDK sends `X-Api-Key: ck_...`.

```java
CommsPliantClient client = new CommsPliantClient("ck_YOUR_API_KEY", "https://api.commspliant.com", true, null);
```

## Errors

Non-success API responses throw `APIException` with `statusCode`, message, and `requestId`.

## Documentation

Endpoint guides and SDK usage examples: [doc/README.md](doc/README.md)

## Links

- [Developer Portal](https://developer.commspliant.com/)
- [About CommsPliant](https://commspliant.com/)
