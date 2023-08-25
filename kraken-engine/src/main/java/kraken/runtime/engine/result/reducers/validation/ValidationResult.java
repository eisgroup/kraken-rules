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
package kraken.runtime.engine.result.reducers.validation;

import java.util.ArrayList;
import java.util.List;

import kraken.annotations.API;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.dto.ContextFieldInfo;

/**
 * Represents single error message
 *
 * @author rimas
 * @since 1.0
 */
@API
public class ValidationResult {

    /**
     * Name of the rule which generated error message
     */
    private final String ruleName;

    /**
     * Error message text.
     * If message is a template then this is already a formatted message
     * with all placeholders replaced with template variables.
     */
    private final String message;

    /**
     * Error message code
     */
    private final String messageCode;

    /**
     * If message is a template then this is an ordered list of evaluated and formatted template placeholder values.
     * Can be used as a values for indexed placeholders when formatting localized message.
     */
    private final List<String> templateVariables;

    /**
     * If message is a template then this is an ordered list of evaluated but unformatted template placeholder values.
     * Can be used as a values for indexed placeholders when formatting localized message.
     */
    private final List<Object> rawTemplateVariables;

    /**
     * MessageFormat compatible template created from default error message
     */
    private final String messageTemplate;

    /**
     * Information about field, instance containing this field
     */
    private final ContextFieldInfo contextFieldInfo;

    /**
     * Error message severity
     */
    private final ValidationSeverity severity;

    /**
     * @deprecated use {@link #ValidationResult(String, String, String, List, List, String, ValidationSeverity, ContextFieldInfo)} instead.
     */
    @Deprecated(since = "1.42.0", forRemoval = true)
    public ValidationResult(String ruleName,
                            String message,
                            String messageCode,
                            List<String> templateVariables,
                            ValidationSeverity severity,
                            ContextFieldInfo contextFieldInfo) {
        this(ruleName,
            message,
            messageCode,
            templateVariables,
            message.replace("'", "''").replace("{", "'{'").replace("}", "'}'"),
            severity,
            contextFieldInfo
        );
    }

    /**
     * @deprecated use {@link #ValidationResult(String, String, String, List, List, String, ValidationSeverity, ContextFieldInfo)} instead.
     */
    @Deprecated(since = "1.51.0", forRemoval = true)
    public ValidationResult(String ruleName,
                            String message,
                            String messageCode,
                            List<String> templateVariables,
                            String messageTemplate,
                            ValidationSeverity severity,
                            ContextFieldInfo contextFieldInfo) {
        this(ruleName, message, messageCode, templateVariables, new ArrayList<>(templateVariables), messageTemplate, severity, contextFieldInfo);
    }

    public ValidationResult(String ruleName,
                            String message,
                            String messageCode,
                            List<String> templateVariables,
                            List<Object> rawTemplateVariables,
                            String messageTemplate,
                            ValidationSeverity severity,
                            ContextFieldInfo contextFieldInfo) {
        this.ruleName = ruleName;
        this.message = message;
        this.messageCode = messageCode;
        this.templateVariables = templateVariables;
        this.rawTemplateVariables = rawTemplateVariables;
        this.messageTemplate = messageTemplate;
        this.contextFieldInfo = contextFieldInfo;
        this.severity = severity;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageTemplate() { return messageTemplate; }

    public String getMessageCode() { return messageCode; }

    public List<String> getTemplateVariables() {
        return templateVariables;
    }

    public List<Object> getRawTemplateVariables() {
        return rawTemplateVariables;
    }

    public ValidationSeverity getSeverity() {
        return severity;
    }

    public ContextFieldInfo getContextFieldInfo() {
        return contextFieldInfo;
    }

}
