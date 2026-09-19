package com.commspliant.sdk;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class RenderRequest {
    private final String templateId;
    private final Map<String, Object> variables;

    private RenderRequest(String templateId, Map<String, Object> variables) {
        this.templateId = templateId;
        this.variables = variables;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateId() {
        return templateId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public static final class Builder {
        private String templateId;
        private Map<String, Object> variables = Collections.emptyMap();

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public RenderRequest build() {
            return new RenderRequest(templateId, variables);
        }
    }
}
