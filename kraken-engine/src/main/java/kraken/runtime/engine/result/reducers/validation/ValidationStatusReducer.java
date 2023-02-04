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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.BooleanUtils;

import kraken.annotations.API;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationStatus;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.ValueListPayloadResult;
import kraken.runtime.engine.result.reducers.EntryPointResultReducer;
import kraken.runtime.engine.result.reducers.validation.ValidationMessageProvider.ValidationMessage;
import kraken.runtime.engine.result.reducers.validation.trace.EntryPointResultReducingOperation;
import kraken.runtime.engine.result.reducers.validation.trace.RuleResultOverriddenOperation;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.tracer.Tracer;

/**
 * Processes rule evaluation information from {@link EntryPointResult}, reducing it to
 * {@link ValidationStatus} which contains all failing validation results separated by {@link ValidationSeverity}
 *
 * @author rimas
 * @author psurinin
 * @since 1.0
 */
@API
public class ValidationStatusReducer implements EntryPointResultReducer<ValidationStatus> {

    private final RuleOverrideStatusResolver overrideStatusResolver;
    private final PayloadMessageProvider messageProvider;
    private final ValidationMessageProvider validationMessageProvider;

    public ValidationStatusReducer() {
        this((RuleOverrideStatusResolver) null);
    }

    public ValidationStatusReducer(RuleOverrideStatusResolver overrideStatusResolver) {
        this(overrideStatusResolver, new DefaultValidationMessageProvider());
    }

    public ValidationStatusReducer(ValidationMessageProvider validationMessageProvider) {
        this(null, validationMessageProvider);
    }

    /**
     * ValidationStatusReducer that care about overridden rules and those rules should be filtered out as valid.
     * Provided {@link RuleOverrideStatusResolver} should know what rules on current context are overridden.
     *
     * @deprecated deprecated because uses {@link PayloadMessageProvider} which does not support localization of
     * default validation messages.
     * Use {@link #ValidationStatusReducer(RuleOverrideStatusResolver, ValidationMessageProvider)} instead.
     */
    @Deprecated(since = "1.24.0", forRemoval = true)
    public ValidationStatusReducer(RuleOverrideStatusResolver overrideStatusResolver,
                                   PayloadMessageProvider messageProvider) {
        this.overrideStatusResolver = overrideStatusResolver;
        this.messageProvider = Objects.requireNonNull(messageProvider);
        this.validationMessageProvider = null;
    }

    /**
     * ValidationStatusReducer that care about overridden rules and those rules should be filtered out as valid.
     * Provided {@link RuleOverrideStatusResolver} should know what rules on current context are overridden.
     */
    public ValidationStatusReducer(RuleOverrideStatusResolver overrideStatusResolver,
                                   ValidationMessageProvider validationMessageProvider) {
        this.overrideStatusResolver = overrideStatusResolver;
        this.messageProvider = null;
        this.validationMessageProvider = Objects.requireNonNull(validationMessageProvider);
    }

    /**
     * Reduces {@link EntryPointResult} to list of error messages
     *
     * @param entryPointResult entry point validation result tree
     * @return list of error messages, in case validation fails, if validation passes will
     * return empty list
     */
    @Override
    public ValidationStatus reduce(EntryPointResult entryPointResult) {
        return Tracer.doOperation(new EntryPointResultReducingOperation(entryPointResult), () -> {
            List<ValidationResult> validationResults = entryPointResult.getFieldResults().values().stream()
                .flatMap(result -> buildValidationResults(result).stream())
                .collect(Collectors.toList());

            return buildValidationStatus(validationResults);
        });
    }

    private List<ValidationResult> buildValidationResults(FieldEvaluationResult fieldResult) {
        return getEvalsValidationResults(fieldResult).stream()
            .map(result -> buildValidationResult(result, fieldResult))
            .collect(Collectors.toList());
    }

    private ValidationStatus buildValidationStatus(List<ValidationResult> validationResults){
        var validationStatus = new ValidationStatus(
                getResultsBySeverity(validationResults, ValidationSeverity.critical),
                getResultsBySeverity(validationResults, ValidationSeverity.warning),
                getResultsBySeverity(validationResults, ValidationSeverity.info)
        );

        return validationStatus;
    }

    private List<ValidationResult> getResultsBySeverity(List<ValidationResult> messages, ValidationSeverity severity){
        return messages.stream()
                .filter(message -> message.getSeverity().equals(severity))
                .collect(Collectors.toList());
    }

    private List<RuleEvaluationResult> getEvalsValidationResults(FieldEvaluationResult result) {
        return result.getRuleResults().stream()
            .filter(rr -> rr.getRuleEvaluationStatus() == RuleEvaluationStatus.APPLIED)
            .filter(rr -> rr.getPayloadResult() instanceof ValidationPayloadResult)
            .filter(rr -> BooleanUtils.isFalse(((ValidationPayloadResult) rr.getPayloadResult()).getSuccess()))
            .filter(rr -> !isRuleOverridden(rr))
            .collect(Collectors.toList());
    }

    private boolean isRuleOverridden(RuleEvaluationResult ruleEvaluationResult) {
        RuleInfo ruleInfo = ruleEvaluationResult.getRuleInfo();
        if (ruleEvaluationResult.getOverrideInfo() == null || !ruleEvaluationResult.getOverrideInfo().isOverridable()) {
            return false;
        }

        var isOverridden = overrideStatusResolver != null
            && ruleEvaluationResult.getOverrideInfo().getOverridableRuleContextInfo() != null
            && overrideStatusResolver.isRuleOverridden(ruleInfo,
            ruleEvaluationResult.getOverrideInfo().getOverridableRuleContextInfo());

        if (isOverridden) {
            Tracer.doOperation(new RuleResultOverriddenOperation(ruleEvaluationResult));
        }

        return isOverridden;
    }

    private ValidationResult buildValidationResult(RuleEvaluationResult ruleResult, FieldEvaluationResult fieldEvaluationResult) {
        RenderedValidationMessage message = resolveMessage((ValidationPayloadResult) ruleResult.getPayloadResult());
        return new ValidationResult(
            ruleResult.getRuleInfo().getRuleName(),
            message.getMessage(),
            message.getCode(),
            message.getParameters(),
            message.getMessageTemplate(),
            ((ValidationPayloadResult)ruleResult.getPayloadResult()).getValidationSeverity(),
            fieldEvaluationResult.getContextFieldInfo()
        );
    }

    private RenderedValidationMessage resolveMessage(ValidationPayloadResult payloadResult) {
        ValidationMessage defaultMessage = resolveDefaultMessage(payloadResult);
        return new RenderedValidationMessage(
            payloadResult.getMessageCode() != null
                ? payloadResult.getMessageCode()
                : defaultMessage.getCode(),
            payloadResult.getMessage() != null
                ? payloadResult.getMessage()
                : defaultMessage.getMessage(),
            payloadResult.getMessage() != null
                ? payloadResult.getTemplateVariables()
                : renderParameters(defaultMessage.getParameters()),
            payloadResult.getMessageTemplate() != null
                ? payloadResult.getMessageTemplate()
                : defaultMessage.getMessage()
        );
    }

    private List<String> renderParameters(List<Object> parameters) {
        return parameters.stream()
            .map(TemplateParameterRenderer::render)
            .collect(Collectors.toList());
    }

    private ValidationMessage resolveDefaultMessage(ValidationPayloadResult payloadResult) {
        if(messageProvider != null) {
            DefaultMessage message = messageProvider.resolveByPayloadResult(payloadResult);
            return new ValidationMessage(message.getCode(), message.getMessage());
        }

        if(validationMessageProvider != null) {
            if (payloadResult instanceof AssertionPayloadResult) {
                return validationMessageProvider.assertionErrorMessage((AssertionPayloadResult) payloadResult);
            }
            if (payloadResult instanceof SizePayloadResult) {
                return validationMessageProvider.sizeErrorMessage((SizePayloadResult) payloadResult);
            }
            if (payloadResult instanceof SizeRangePayloadResult) {
                return validationMessageProvider.sizeRangeErrorMessage((SizeRangePayloadResult) payloadResult);
            }
            if (payloadResult instanceof LengthPayloadResult) {
                return validationMessageProvider.lengthErrorMessage((LengthPayloadResult) payloadResult);
            }
            if (payloadResult instanceof RegExpPayloadResult) {
                return validationMessageProvider.regExpErrorMessage((RegExpPayloadResult) payloadResult);
            }
            if (payloadResult instanceof UsagePayloadResult) {
                return validationMessageProvider.usageErrorMessage((UsagePayloadResult) payloadResult);
            }
            if (payloadResult instanceof NumberSetPayloadResult) {
                return validationMessageProvider.numberSetErrorMessage((NumberSetPayloadResult) payloadResult);
            }
            if (payloadResult instanceof ValueListPayloadResult) {
                return validationMessageProvider.valueListErrorMessage((ValueListPayloadResult) payloadResult);
            }
            throw new IllegalStateException("Unknown payload result type encountered: " + payloadResult.getClass());
        }
        throw new IllegalStateException(
            "ValidationStatusReducer is incorrectly initialized. Message provider is not set.");
    }

    static class RenderedValidationMessage {
        private final String code;
        private final String message;
        private final List<String> parameters;
        private final String messageTemplate;

        public RenderedValidationMessage(@Nonnull String code, @Nonnull String message, @Nonnull List<String> parameters, @Nonnull String messageTemplate) {
            this.code = code;
            this.message = message;
            this.parameters = parameters;
            this.messageTemplate = messageTemplate;
        }

        @Nonnull
        public String getCode() {
            return code;
        }

        @Nonnull
        public String getMessage() {
            return message;
        }

        @Nonnull
        public List<String> getParameters() {
            return parameters;
        }

        @Nonnull
        public String getMessageTemplate() {
            return messageTemplate;
        }
    }
}
