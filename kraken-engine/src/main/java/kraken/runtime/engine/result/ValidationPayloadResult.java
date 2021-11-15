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

import kraken.annotations.API;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.model.rule.payload.validation.ValidationPayload;

/**
 * Payload result class to store results of {@link ValidationPayload} execution
 *
 * @author rimas
 * @since 1.0
 */
@API
public abstract class ValidationPayloadResult implements PayloadResult {

    private Boolean success;

    private String messageCode;

    private String message;

    private List<String> templateVariables;

    private ValidationSeverity validationSeverity;

    /**
     * @deprecated use {@link #ValidationPayloadResult(Boolean, ValidationPayload, List)} instead.
     */
    @Deprecated(since = "1.14.0", forRemoval = true)
    public ValidationPayloadResult(Boolean success, ValidationPayload payload) {
        this(success, payload, List.of());
    }

    public ValidationPayloadResult(Boolean success, ValidationPayload payload, List<String> templateVariables) {
        this.success = success;
        this.validationSeverity = payload.getSeverity();
        this.templateVariables = Objects.requireNonNull(templateVariables);

        this.messageCode = payload.getErrorMessage() != null
            ? payload.getErrorMessage().getErrorCode()
            : null;

        this.message = payload.getErrorMessage() != null
            ? formatMessage(payload.getErrorMessage().getTemplateParts(), templateVariables)
            : null;
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

}
