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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_ASSERTION_EXPRESSION_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_CONDITION_EXPRESSION_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_CONTEXT_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_DECIMAL64_PRECISION_LOSS;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_DEFAULT_EXPRESSION_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_DEFAULT_TYPE_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_LENGTH_IS_NOT_POSITIVE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_NAME_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_NUMBER_SET_MIN_AND_MAX_NOT_SET;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_NUMBER_SET_MIN_NOT_SMALLER_THAN_MAX;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_NUMBER_SET_STEP_NOT_MORE_THAN_ZERO;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_PAYLOAD_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_PRIORITY_NOT_IN_DEFAULT;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_REGEXP_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_SIZE_IS_NOT_POSITIVE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_SIZE_MAX_IS_NOT_POSITIVE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_SIZE_MIN_IS_NOT_POSITIVE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_SIZE_MIN_NOT_LESS_THAN_MAX;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_SIZE_ORIENTATION_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TARGET_PATH_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_USAGE_TYPE_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_VALIDATION_CODE_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_VALIDATION_SEVERITY_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_VALUE_LIST_IS_EMPTY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_VALUE_LIST_IS_NULL;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_VALUE_LIST_TYPE_IS_NULL;

import java.math.BigDecimal;

import kraken.el.math.Numbers;
import kraken.model.Rule;
import kraken.model.ValueList;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.namespaced.NamespacedValidator;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.NumberSetPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValidationPayload;
import kraken.model.validation.ValueListPayload;

/**
 * Validates Kraken Rule model. Ensures that mandatory model properties are set and that model is consistent.
 *
 * @author mulevicius
 */
public class RuleModelValidator implements RuleValidator {

    @Override
    public void validate(Rule rule, ValidationSession session) {
        if(rule.getName() == null) {
            session.add(ValidationMessageBuilder.create(RULE_NAME_IS_NULL, rule).build());
        }
        session.addAll(NamespacedValidator.validate(rule));

        if(rule.getContext() == null) {
            session.add(ValidationMessageBuilder.create(RULE_CONTEXT_IS_NULL, rule).build());
        }
        if(rule.getTargetPath() == null) {
            session.add(ValidationMessageBuilder.create(RULE_TARGET_PATH_IS_NULL, rule).build());
        }
        if(rule.getCondition() != null) {
            if(rule.getCondition().getExpression() == null
                || rule.getCondition().getExpression().getExpressionString() == null) {
                session.add(ValidationMessageBuilder.create(RULE_CONDITION_EXPRESSION_IS_NULL, rule).build());
            }
        }
        if(rule.getMetadata() != null) {
            rule.getMetadata().asMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof BigDecimal)
                .forEach(e -> validatePrecision("Dimension '" + e.getKey() + "'", (BigDecimal) e.getValue(), rule, session));
        }
        if(rule.getPayload() == null) {
            session.add(ValidationMessageBuilder.create(RULE_PAYLOAD_IS_NULL, rule).build());
        } else {
            if(rule.getPayload() instanceof ValidationPayload) {
                ValidationPayload payload = (ValidationPayload) rule.getPayload();
                if(payload.getSeverity() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_VALIDATION_SEVERITY_IS_NULL, rule).build());
                }
                if(payload.getErrorMessage() != null
                    && payload.getErrorMessage().getErrorMessage() != null
                    && payload.getErrorMessage().getErrorCode() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_VALIDATION_CODE_IS_NULL, rule).build());
                }
            }
            if(rule.getPayload() instanceof AssertionPayload) {
                AssertionPayload payload = (AssertionPayload) rule.getPayload();
                if(payload.getAssertionExpression() == null || payload.getAssertionExpression().getExpressionString() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_ASSERTION_EXPRESSION_IS_NULL, rule).build());
                }
            }
            if(rule.getPayload() instanceof DefaultValuePayload) {
                DefaultValuePayload payload = (DefaultValuePayload) rule.getPayload();
                if(payload.getDefaultingType() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_DEFAULT_TYPE_IS_NULL, rule).build());
                }
                if(payload.getValueExpression() == null || payload.getValueExpression().getExpressionString() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_DEFAULT_EXPRESSION_IS_NULL, rule).build());
                }
            }
            if(rule.getPayload() instanceof SizeRangePayload) {
                SizeRangePayload payload = (SizeRangePayload) rule.getPayload();
                if(payload.getMin() < 0) {
                    session.add(ValidationMessageBuilder.create(RULE_SIZE_MIN_IS_NOT_POSITIVE, rule).build());
                }
                if(payload.getMax() < 0) {
                    session.add(ValidationMessageBuilder.create(RULE_SIZE_MAX_IS_NOT_POSITIVE, rule).build());
                }
                if(payload.getMin() > payload.getMax()) {
                    session.add(ValidationMessageBuilder.create(RULE_SIZE_MIN_NOT_LESS_THAN_MAX, rule).build());
                }
            }
            if(rule.getPayload() instanceof RegExpPayload) {
                RegExpPayload payload = (RegExpPayload) rule.getPayload();
                if(payload.getRegExp() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_REGEXP_IS_NULL, rule).build());
                }
            }
            if(rule.getPayload() instanceof SizePayload) {
                SizePayload payload = (SizePayload) rule.getPayload();
                if(payload.getOrientation() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_SIZE_ORIENTATION_IS_NULL, rule).build());
                }
                if(payload.getSize() < 0) {
                    session.add(ValidationMessageBuilder.create(RULE_SIZE_IS_NOT_POSITIVE, rule).build());
                }
            }
            if(rule.getPayload() instanceof UsagePayload) {
                UsagePayload payload = (UsagePayload) rule.getPayload();
                if(payload.getUsageType() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_USAGE_TYPE_IS_NULL, rule).build());
                }
            }
            if(rule.getPayload() instanceof LengthPayload) {
                LengthPayload payload = (LengthPayload) rule.getPayload();
                if(payload.getLength() < 0) {
                    session.add(ValidationMessageBuilder.create(RULE_LENGTH_IS_NOT_POSITIVE, rule).build());
                }
            }
            if(rule.getPayload() instanceof NumberSetPayload) {
                var payload = (NumberSetPayload) rule.getPayload();
                if(payload.getMin() == null && payload.getMax() == null) {
                    session.add(ValidationMessageBuilder.create(RULE_NUMBER_SET_MIN_AND_MAX_NOT_SET, rule).build());
                }
                if(payload.getMin() != null && payload.getMax() != null) {
                    if(payload.getMin().compareTo(payload.getMax()) >= 0) {
                        session.add(ValidationMessageBuilder.create(RULE_NUMBER_SET_MIN_NOT_SMALLER_THAN_MAX, rule).build());
                    }
                }
                if(payload.getStep() != null) {
                    if(payload.getStep().compareTo(BigDecimal.ZERO) <= 0) {
                        session.add(ValidationMessageBuilder.create(RULE_NUMBER_SET_STEP_NOT_MORE_THAN_ZERO, rule).build());
                    }
                }
                validatePrecision("Min", payload.getMin(), rule, session);
                validatePrecision("Max", payload.getMax(), rule, session);
                validatePrecision("Step", payload.getStep(), rule, session);
            }
            if(!(rule.getPayload() instanceof DefaultValuePayload)) {
                if(rule.getPriority() != null) {
                    session.add(ValidationMessageBuilder.create(RULE_PRIORITY_NOT_IN_DEFAULT, rule)
                        .parameters(rule.getPayload().getPayloadType().getTypeName())
                        .build());
                }
            }
            if (rule.getPayload() instanceof ValueListPayload) {
                ValueListPayload valueListPayload = (ValueListPayload) rule.getPayload();
                ValueList valueList = valueListPayload.getValueList();

                if (valueList == null) {
                    session.add(ValidationMessageBuilder.create(RULE_VALUE_LIST_IS_NULL, rule).build());
                } else {
                    if (valueList.getValueType() == null) {
                        session.add(ValidationMessageBuilder.create(RULE_VALUE_LIST_TYPE_IS_NULL, rule).build());
                    }

                    if (valueList.getValues() == null || valueList.getValues().isEmpty()) {
                        session.add(ValidationMessageBuilder.create(RULE_VALUE_LIST_IS_EMPTY, rule).build());
                    }

                    if(valueList.getValues() != null) {
                        valueList.getValues().stream()
                            .filter(v -> v instanceof BigDecimal)
                            .map(v -> (BigDecimal) v)
                            .forEach(bigDecimalValue -> validatePrecision("Value list", bigDecimalValue, rule, session));
                    }
                }
            }
        }
    }

    private void validatePrecision(String fieldName, BigDecimal number, Rule rule, ValidationSession session) {
        if(number != null && number.stripTrailingZeros().precision() > Numbers.DEFAULT_MATH_CONTEXT.getPrecision()) {
            var m = ValidationMessageBuilder.create(RULE_DECIMAL64_PRECISION_LOSS, rule)
                .parameters(fieldName, number.toPlainString(), Numbers.normalized(number).toPlainString())
                .build();
            session.add(m);
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return true;
    }
}
