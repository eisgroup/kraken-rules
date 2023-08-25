/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.runtime.engine.result;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import kraken.annotations.API;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.model.rule.payload.validation.ValidationPayload;
import kraken.runtime.utils.TemplateParameterRenderer;

/**
 * Payload result class to store results of {@link ValidationPayload} execution
 *
 * @author rimas
 * @since 1.0
 */
@API
public abstract class ValidationPayloadResult implements PayloadResult {

    private final Boolean success;

    private final String messageCode;

    private final String message;

    private final List<String> templateVariables;

    private final List<Object> rawTemplateVariables;

    private final String messageTemplate;

    private final ValidationSeverity validationSeverity;

    public ValidationPayloadResult(
        Boolean success,
        ValidationPayload payload,
        List<Object> rawTemplateVariables
    ) {
        this.success = success;
        this.validationSeverity = payload.getSeverity();
        this.rawTemplateVariables = Objects.requireNonNull(rawTemplateVariables);
        this.templateVariables = rawTemplateVariables.stream()
            .map(TemplateParameterRenderer::render)
            .collect(Collectors.toList());

        this.messageCode = payload.getErrorMessage() != null
            ? payload.getErrorMessage().getErrorCode()
            : null;

        this.message = payload.getErrorMessage() != null
            ? formatMessage(payload.getErrorMessage().getTemplateParts(), templateVariables)
            : null;

        if (payload.getErrorMessage() == null) {
            this.messageTemplate = null;
        } else {
            List<String> templateParts = payload.getErrorMessage().getTemplateParts();
            if (templateParts.isEmpty()) {
                this.messageTemplate = "";
            } else {
                StringBuilder templateBuilder = new StringBuilder();
                for (int i = 0; i < templateParts.size(); i++) {
                    templateBuilder.append(escapeMessageFormatSymbols(templateParts.get(i)));
                    if (i < templateParts.size() - 1) {
                        templateBuilder.append("{").append(i).append("}");
                    }
                }
                this.messageTemplate = templateBuilder.toString();
            }
        }
    }

    private String formatMessage(List<String> templateParts, List<String> templateVariables) {
        if(templateParts.isEmpty()) {
            return null;
        }
        StringBuilder templateBuilder = new StringBuilder();
        for(int i = 0; i < templateParts.size(); i++) {
            templateBuilder.append(templateParts.get(i));
            if(i < templateVariables.size()) {
                templateBuilder.append(templateVariables.get(i));
            }
        }
        return templateBuilder.toString();
    }

    public Boolean getSuccess() {
        return success;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getMessage() {
        return message;
    }

    public List<String> getTemplateVariables() {
        return templateVariables;
    }

    public ValidationSeverity getValidationSeverity() {
        return validationSeverity;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    private static String escapeMessageFormatSymbols(String unescaped) {
        return unescaped.replace("'", "''").replace("{", "'{'").replace("}", "'}'");
    }

    public List<Object> getRawTemplateVariables() {
        return rawTemplateVariables;
    }
}
