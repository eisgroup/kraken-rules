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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import kraken.annotations.API;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.result.ExceptionAwarePayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.reducers.EntryPointResultReducer;
import org.apache.commons.lang3.BooleanUtils;


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

    private RuleOverrideStatusResolver overrideStatusResolver;

    private PayloadMessageProvider messageProvider;


    public ValidationStatusReducer() {
        this(null, new DefaultPayloadMessageProvider());
    }

    public ValidationStatusReducer(RuleOverrideStatusResolver overrideStatusResolver) {
        this(overrideStatusResolver, new DefaultPayloadMessageProvider());
    }

    /**
     * ValidationStatusReducer that care about overridden rules and those rules should be filtered out as valid.
     * Provided {@link RuleOverrideStatusResolver} should know what rules on current context are overridden.
     */
    public ValidationStatusReducer(RuleOverrideStatusResolver overrideStatusResolver, PayloadMessageProvider messageProvider) {
        this.overrideStatusResolver = overrideStatusResolver;
        this.messageProvider = Objects.requireNonNull(messageProvider);
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
        List<ValidationResult> validationResults = entryPointResult.getFieldResults().values().stream()
                .flatMap(result -> buildValidationResults(result).stream())
                .collect(Collectors.toList());
        return buildValidationStatus(validationResults);
    }

    private List<ValidationResult> buildValidationResults(FieldEvaluationResult fieldResult){
        return getEvalsValidationResults(fieldResult).stream()
                .map(result -> buildValidationResult(result, fieldResult))
                .collect(Collectors.toList());
    }

    private ValidationStatus buildValidationStatus(List<ValidationResult> validationResults){
        return new ValidationStatus(
                getResultsBySeverity(validationResults, ValidationSeverity.critical),
                getResultsBySeverity(validationResults, ValidationSeverity.warning),
                getResultsBySeverity(validationResults, ValidationSeverity.info)
        );
    }

    private List<ValidationResult> getResultsBySeverity(List<ValidationResult> messages, ValidationSeverity severity){
        return messages.stream()
                .filter(message -> message.getSeverity().equals(severity))
                .collect(Collectors.toList());
    }

    private List<RuleEvaluationResult> getEvalsValidationResults(FieldEvaluationResult result) {
        return result.getRuleResults().stream()
                .filter(rr -> rr.getPayloadResult() instanceof ValidationPayloadResult)
                .filter(isNotEvaluatedWithError())
                .filter(rr -> rr.getConditionEvaluationResult().isApplicable())
                .filter(rr -> BooleanUtils.isFalse(((ValidationPayloadResult) rr.getPayloadResult()).getSuccess()))
                .filter(rr -> !isRuleOverridden(rr))
                .collect(Collectors.toList());
    }

    private boolean isRuleOverridden(RuleEvaluationResult ruleEvaluationResult) {
        RuleInfo ruleInfo = ruleEvaluationResult.getRuleInfo();
        if (ruleEvaluationResult.getOverrideInfo() == null || !ruleEvaluationResult.getOverrideInfo().isOverridable()) {
            return false;
        }
        return overrideStatusResolver != null
                && ruleEvaluationResult.getOverrideInfo().getOverridableRuleContextInfo() != null
                && overrideStatusResolver.isRuleOverridden(ruleInfo, ruleEvaluationResult.getOverrideInfo().getOverridableRuleContextInfo());
    }

    private Predicate<RuleEvaluationResult> isNotEvaluatedWithError() {
        return ruleEvaluationResult -> {
            if (ruleEvaluationResult.getPayloadResult() instanceof ExceptionAwarePayloadResult) {
                return !((ExceptionAwarePayloadResult) ruleEvaluationResult.getPayloadResult()).getException().isPresent();
            }
            return true;
        };
    }

    private ValidationResult buildValidationResult(RuleEvaluationResult ruleResult, FieldEvaluationResult fieldEvaluationResult) {
        ValidationResult validationResult = new ValidationResult(
                ruleResult.getRuleInfo().getRuleName(),
                getMessage((ValidationPayloadResult) ruleResult.getPayloadResult()),
                getMessageCode((ValidationPayloadResult) ruleResult.getPayloadResult()),
                ((ValidationPayloadResult) ruleResult.getPayloadResult()).getTemplateVariables(),
                ((ValidationPayloadResult)ruleResult.getPayloadResult()).getValidationSeverity(),
                fieldEvaluationResult.getContextFieldInfo()
        );
        return validationResult;
    }

    private String getMessageCode(ValidationPayloadResult validationPayloadResult) {
        return validationPayloadResult.getMessageCode() != null
                ? validationPayloadResult.getMessageCode()
                : messageProvider.resolveByPayloadResult(validationPayloadResult).getCode();
    }

    private String getMessage(ValidationPayloadResult validationPayloadResult) {
        return validationPayloadResult.getMessage() != null
                ? validationPayloadResult.getMessage()
                : messageProvider.resolveByPayloadResult(validationPayloadResult).getMessage();
    }

}
