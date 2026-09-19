package com.commspliant.sdk;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

final class JsonSupport {
    private JsonSupport() {
    }

    static String toJson(Map<String, Object> payload) {
        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append("\":");
            builder.append(encodeValue(entry.getValue()));
        }
        builder.append('}');
        return builder.toString();
    }

    static Map<String, Object> parseObject(byte[] body) {
        String json = new String(body, StandardCharsets.UTF_8).trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("invalid json object");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        String inner = json.substring(1, json.length() - 1).trim();
        if (inner.isEmpty()) {
            return result;
        }

        for (String part : splitTopLevel(inner)) {
            int colon = part.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = unquote(part.substring(0, colon).trim());
            String value = part.substring(colon + 1).trim();
            result.put(key, decodeValue(value));
        }
        return result;
    }

    private static String encodeValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return '"' + escape((String) value) + '"';
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?>) {
            Map<?, ?> map = (Map<?, ?>) value;
            Map<String, Object> cast = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                cast.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            return toJson(cast);
        }
        return '"' + escape(String.valueOf(value)) + '"';
    }

    private static Object decodeValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return unquote(value);
        }
        if ("null".equals(value)) {
            return null;
        }
        if ("true".equals(value) || "false".equals(value)) {
            return Boolean.parseBoolean(value);
        }
        return value;
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1).replace("\\\"", "\"");
        }
        return trimmed;
    }

    private static String[] splitTopLevel(String inner) {
        return inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
    }
}
