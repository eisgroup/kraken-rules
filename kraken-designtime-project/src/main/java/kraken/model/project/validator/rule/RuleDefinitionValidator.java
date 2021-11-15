/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule;

import static kraken.model.project.validator.Severity.ERROR;

import kraken.model.Rule;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.namespaced.NamespacedValidator;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValidationPayload;

/**
 * @author mulevicius
 */
public class RuleDefinitionValidator {

    private final RuleNotAddedToEntryPointValidator ruleNotAddedToEntryPointValidator;
    private final RulePayloadCompatibilityValidator rulePayloadCompatibilityValidator;
    private final RuleTargetContextValidator ruleTargetContextValidator;
    private final RuleDanglingTargetContextValidator ruleDanglingTargetContextValidator;
    private final RuleDefinedOnCycleValidator ruleDefinedOnCycleValidator;

    private final RuleCrossContextCardinalityValidator ruleCrossContextCardinalityValidator;
    private final RuleCrossContextDependencyValidator ruleCrossContextDependencyValidator;

    private final RuleExpressionValidator ruleExpressionValidator;

    public RuleDefinitionValidator(KrakenProject krakenProject) {
        this.ruleNotAddedToEntryPointValidator = new RuleNotAddedToEntryPointValidator(krakenProject);
        this.rulePayloadCompatibilityValidator = new RulePayloadCompatibilityValidator(krakenProject);
        this.ruleTargetContextValidator = new RuleTargetContextValidator(krakenProject);
        this.ruleDanglingTargetContextValidator = new RuleDanglingTargetContextValidator(krakenProject);
        this.ruleDefinedOnCycleValidator = new RuleDefinedOnCycleValidator(krakenProject);

        this.ruleCrossContextCardinalityValidator = new RuleCrossContextCardinalityValidator(krakenProject);
        this.ruleCrossContextDependencyValidator = new RuleCrossContextDependencyValidator(krakenProject);

        this.ruleExpressionValidator = new RuleExpressionValidator(krakenProject);
    }

    public void validate(Rule rule, ValidationSession session) {
        ValidationSession ruleValidationSession = new ValidationSession();
        validateDefinition(rule, ruleValidationSession);

        if(!ruleValidationSession.hasRuleError()) {
            ruleTargetContextValidator.validate(rule, ruleValidationSession);
            ruleNotAddedToEntryPointValidator.validate(rule, ruleValidationSession);
        }
        if(!ruleValidationSession.hasRuleError()) {
            rulePayloadCompatibilityValidator.validate(rule, ruleValidationSession);
            ruleDanglingTargetContextValidator.validate(rule, ruleValidationSession);
            ruleDefinedOnCycleValidator.validate(rule, ruleValidationSession);
        }
        if(!ruleValidationSession.hasRuleError()) {
            ruleCrossContextCardinalityValidator.validate(rule, ruleValidationSession);
            ruleCrossContextDependencyValidator.validate(rule, ruleValidationSession);
        }
        if(!ruleValidationSession.hasRuleError()) {
            ruleExpressionValidator.validate(rule, ruleValidationSession);
        }

        session.addAll(ruleValidationSession.getValidationMessages());
    }

    public void validateDynamicRule(Rule dynamicRule, ValidationSession session) {
        ValidationSession ruleValidationSession = new ValidationSession();
        validateDefinition(dynamicRule, ruleValidationSession);

        if(!ruleValidationSession.hasRuleError()) {
            ruleTargetContextValidator.validate(dynamicRule, ruleValidationSession);
        }
        if(!ruleValidationSession.hasRuleError()) {
            rulePayloadCompatibilityValidator.validate(dynamicRule, ruleValidationSession);
            ruleDanglingTargetContextValidator.validate(dynamicRule, ruleValidationSession);
            ruleDefinedOnCycleValidator.validate(dynamicRule, ruleValidationSession);
        }

        session.addAll(ruleValidationSession.getValidationMessages());
    }

    private void validateDefinition(Rule rule, ValidationSession session) {
        if(rule.getName() == null) {
            session.add(errorMessage(rule, "name is not defined"));
        }
        session.addAll(NamespacedValidator.validate(rule));

        if(rule.getContext() == null) {
            session.add(errorMessage(rule, "context name is not defined"));
        }
        if(rule.getTargetPath() == null) {
            session.add(errorMessage(rule, "targetPath is not defined"));
        }
        if(rule.getPayload() == null) {
            session.add(errorMessage(rule, "payload is not defined"));
        } else {
            if(rule.getPayload() instanceof ValidationPayload) {
                ValidationPayload payload = (ValidationPayload) rule.getPayload();
                if(payload.getSeverity() == null) {
                    session.add(errorMessage(rule, "severity is not defined"));
                }
                if(payload.getErrorMessage() != null
                        && payload.getErrorMessage().getErrorMessage() != null
                        && payload.getErrorMessage().getErrorCode() == null) {
                    session.add(errorMessage(rule, "errorCode is not defined"));
                }
            }
            if(rule.getPayload() instanceof AssertionPayload) {
                AssertionPayload payload = (AssertionPayload) rule.getPayload();
                if(payload.getAssertionExpression() == null || payload.getAssertionExpression().getExpressionString() == null) {
                    session.add(errorMessage(rule, "assertionExpression is not defined"));
                }
            }
            if(rule.getPayload() instanceof DefaultValuePayload) {
                DefaultValuePayload payload = (DefaultValuePayload) rule.getPayload();
                if(payload.getDefaultingType() == null) {
                    session.add(errorMessage(rule, "defaultingType is not defined"));
                }
                if(payload.getValueExpression() == null || payload.getValueExpression().getExpressionString() == null) {
                    session.add(errorMessage(rule, "valueExpression is not defined"));
                }
            }
            if(rule.getPayload() instanceof SizeRangePayload) {
                SizeRangePayload payload = (SizeRangePayload) rule.getPayload();
                if(payload.getMin() < 0) {
                    session.add(errorMessage(rule, "Min must be positive"));
                }
                if(payload.getMax() < 0) {
                    session.add(errorMessage(rule, "Max must be positive"));
                }
                if(payload.getMin() > payload.getMax()) {
                    session.add(errorMessage(rule, "Min must be less than Max"));
                }
            }
            if(rule.getPayload() instanceof RegExpPayload) {
                RegExpPayload payload = (RegExpPayload) rule.getPayload();
                if(payload.getRegExp() == null) {
                    session.add(errorMessage(rule, "regExp is not defined"));
                }
            }
            if(rule.getPayload() instanceof SizePayload) {
                SizePayload payload = (SizePayload) rule.getPayload();
                if(payload.getOrientation() == null) {
                    session.add(errorMessage(rule, "orientation is not defined"));
                }
                if(payload.getSize() < 0) {
                    session.add(errorMessage(rule, "size must be positive"));
                }
            }
            if(rule.getPayload() instanceof UsagePayload) {
                UsagePayload payload = (UsagePayload) rule.getPayload();
                if(payload.getUsageType() == null) {
                    session.add(errorMessage(rule, "usageType is not defined"));
                }
            }
            if(rule.getPayload() instanceof LengthPayload) {
                LengthPayload payload = (LengthPayload) rule.getPayload();
                if(payload.getLength() < 0) {
                    session.add(errorMessage(rule, "length must be positive"));
                }
            }
        }
    }

    private ValidationMessage errorMessage(Rule rule, String message) {
        return new ValidationMessage(rule, message, ERROR);
    }
}
