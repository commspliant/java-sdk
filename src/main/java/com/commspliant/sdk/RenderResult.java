package com.commspliant.sdk;

public final class RenderResult {
    private final byte[] body;
    private final String contentType;
    private final String requestId;
    private final String contentDisposition;

    public RenderResult(byte[] body, String contentType, String requestId, String contentDisposition) {
        this.body = body;
        this.contentType = contentType;
        this.requestId = requestId;
        this.contentDisposition = contentDisposition;
    }

    public byte[] getBody() {
        return body;
    }

    public String getContentType() {
        return contentType;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }
}
