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
import static kraken.model.project.validator.Severity.WARNING;

import java.math.BigDecimal;

import kraken.el.ast.builder.Literals;
import kraken.el.math.Numbers;
import kraken.model.Rule;
import kraken.model.ValueList;
import kraken.model.ValueList.DataType;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.validator.ValidationMessage;
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
            session.add(errorMessage(rule, "name is not defined"));
        }
        session.addAll(NamespacedValidator.validate(rule));

        if(rule.getContext() == null) {
            session.add(errorMessage(rule, "context name is not defined"));
        }
        if(rule.getTargetPath() == null) {
            session.add(errorMessage(rule, "targetPath is not defined"));
        }
        if(rule.getCondition() != null) {
            if(rule.getCondition().getExpression() == null
                || rule.getCondition().getExpression().getExpressionString() == null) {
                session.add(errorMessage(rule, "condition exists but condition expression is not defined"));
            }
        }
        if(rule.getMetadata() != null) {
            rule.getMetadata().asMap().entrySet().stream()
                .filter(e -> e.getValue() instanceof BigDecimal)
                .forEach(e -> validatePrecision("dimension '" + e.getKey() + "'", (BigDecimal) e.getValue(), rule, session));
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
            if(rule.getPayload() instanceof NumberSetPayload) {
                var payload = (NumberSetPayload) rule.getPayload();
                if(payload.getMin() == null && payload.getMax() == null) {
                    session.add(errorMessage(rule, "min or max must be set"));
                }
                if(payload.getMin() != null && payload.getMax() != null) {
                    if(payload.getMin().compareTo(payload.getMax()) >= 0) {
                        session.add(errorMessage(rule, "min must be smaller than max"));
                    }
                }
                if(payload.getStep() != null) {
                    if(payload.getStep().compareTo(BigDecimal.ZERO) <= 0) {
                        session.add(errorMessage(rule, "step must be more than zero"));
                    }
                }
                validatePrecision("min", payload.getMin(), rule, session);
                validatePrecision("max", payload.getMax(), rule, session);
                validatePrecision("step", payload.getStep(), rule, session);
            }
            if(!(rule.getPayload() instanceof DefaultValuePayload)) {
                if(rule.getPriority() != null) {
                    String payloadType = rule.getPayload().getPayloadType().getTypeName();
                    session.add(errorMessage(rule, "priority cannot be set because rule payload type is " + payloadType
                        + " - priority is supported only for defaulting rules."));
                }
            }
            if (rule.getPayload() instanceof ValueListPayload) {
                ValueListPayload valueListPayload = (ValueListPayload) rule.getPayload();
                ValueList valueList = valueListPayload.getValueList();

                if (valueList == null) {
                    session.add(errorMessage(rule, "ValueList must be set"));
                } else {
                    if (valueList.getValueType() == null) {
                        session.add(errorMessage(rule, "ValueList data type must be set"));
                    }

                    if (valueList.getValues() == null || valueList.getValues().isEmpty()) {
                        session.add(errorMessage(rule, "ValueList should contain at least one value"));
                    }

                    if(valueList.getValues() != null) {
                        valueList.getValues().stream()
                            .filter(v -> v instanceof BigDecimal)
                            .map(v -> (BigDecimal) v)
                            .forEach(bigDecimalValue -> validatePrecision("ValueList", bigDecimalValue, rule, session));
                    }
                }
            }
        }
    }

    private void validatePrecision(String fieldName, BigDecimal number, Rule rule, ValidationSession session) {
        if(number != null && number.stripTrailingZeros().precision() > Numbers.DEFAULT_MATH_CONTEXT.getPrecision()) {
            session.add(warningMessage(
                rule,
                String.format(
                    "%s value '%s' cannot be encoded as a decimal64 without a loss of precision. Actual number at runtime would be rounded to '%s'",
                    fieldName,
                    number.toPlainString(),
                    Numbers.normalized(number).toPlainString()
                )
            ));
        }
    }

    private ValidationMessage errorMessage(Rule rule, String message) {
        return new ValidationMessage(rule, message, ERROR);
    }

    private ValidationMessage warningMessage(Rule rule, String message) {
        return new ValidationMessage(rule, message, WARNING);
    }

    @Override
    public boolean canValidate(Rule rule) {
        return true;
    }
}
