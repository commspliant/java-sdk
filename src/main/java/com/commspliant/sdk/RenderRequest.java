package com.commspliant.sdk;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public final class RenderRequest {
    private final String templateId;
    private final String templateVersionId;
    private final Map<String, Object> variables;

    private RenderRequest(String templateId, String templateVersionId, Map<String, Object> variables) {
        this.templateId = templateId;
        this.templateVersionId = templateVersionId;
        this.variables = variables;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTemplateId() {
        return templateId;
    }

    public String getTemplateVersionId() {
        return templateVersionId;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public static final class Builder {
        private String templateId;
        private String templateVersionId;
        private Map<String, Object> variables = Collections.emptyMap();

        public Builder templateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Builder templateVersionId(String templateVersionId) {
            this.templateVersionId = templateVersionId;
            return this;
        }

        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public RenderRequest build() {
            return new RenderRequest(templateId, templateVersionId, variables);
        }
    }
}
