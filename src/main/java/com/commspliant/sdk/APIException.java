package com.commspliant.sdk;

import java.util.Map;
import java.util.Optional;

public final class APIException extends RuntimeException {
    private final int statusCode;
    private final String code;
    private final Map<String, Object> details;
    private final String requestId;

    public APIException(int statusCode, String message, String code, Map<String, Object> details, String requestId) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
        this.details = details;
        this.requestId = requestId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Optional<String> getCode() {
        return Optional.ofNullable(code);
    }

    public Optional<Map<String, Object>> getDetails() {
        return Optional.ofNullable(details);
    }

    public Optional<String> getRequestId() {
        return Optional.ofNullable(requestId);
    }
}
