/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl;

import com.google.common.collect.ImmutableList;
import kraken.model.Condition;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.Metadata;
import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultingType;
import kraken.model.dsl.model.DSLAccessibilityPayload;
import kraken.model.dsl.model.DSLAssertionValidationPayload;
import kraken.model.dsl.model.DSLDefaultValuePayload;
import kraken.model.dsl.model.DSLDefaultingType;
import kraken.model.dsl.model.DSLExpression;
import kraken.model.dsl.model.DSLLengthValidationPayload;
import kraken.model.dsl.model.DSLModel;
import kraken.model.dsl.model.DSLPayload;
import kraken.model.dsl.model.DSLRegExpValidationPayload;
import kraken.model.dsl.model.DSLRule;
import kraken.model.dsl.model.DSLRules;
import kraken.model.dsl.model.DSLSeverity;
import kraken.model.dsl.model.DSLSizeOrientation;
import kraken.model.dsl.model.DSLSizePayload;
import kraken.model.dsl.model.DSLSizeRangePayload;
import kraken.model.dsl.model.DSLUsageType;
import kraken.model.dsl.model.DSLUsageValidationPayload;
import kraken.model.dsl.model.DSLVisibilityPayload;
import kraken.model.factory.RulesModelFactory;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizeOrientation;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.UsageType;
import kraken.model.validation.ValidationSeverity;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static kraken.model.dsl.KrakenDSLModelMetadataConverter.convertMetadata;
import static kraken.model.dsl.KrakenDSLModelMetadataConverter.merge;
import static kraken.model.dsl.KrakenDSLModelMetadataConverter.withParentMetadata;

/**
 * @author mulevicius
 */
class KrakenDSLModelRuleConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelRuleConverter() {
    }

    static List<Rule> convertRules(DSLModel dsl) {
        List<Rule> rules = convertRuleBlocks(dsl.getNamespace(), dsl.getRuleBlocks(), null);
        List<Rule> otherRules = convertRules(dsl.getNamespace(), dsl.getRules(), null);
        return ImmutableList.<Rule>builder()
                .addAll(rules)
                .addAll(otherRules)
                .build();
    }

    private static List<Rule> convertRuleBlocks(String namespace, Collection<DSLRules> ruleBlocks, Metadata parentMetadata) {
        return ruleBlocks.stream()
                .flatMap(ruleBlock -> convertRuleBlock(namespace, ruleBlock, parentMetadata).stream())
                .collect(Collectors.toList());
    }

    private static List<Rule> convertRuleBlock(String namespace, DSLRules rules, Metadata parentMetadata) {
        parentMetadata = merge(parentMetadata, convertMetadata(rules.getMetadata()));

        return ImmutableList.<Rule>builder()
                .addAll(convertRuleBlocks(namespace, rules.getRuleBlocks(), parentMetadata))
                .addAll(convertRules(namespace, rules.getRules(), parentMetadata))
                .build();
    }

    private static List<Rule> convertRules(String namespace, Collection<DSLRule> rules, Metadata parentMetadata) {
        return rules.stream()
                .map(rule -> convert(namespace, rule))
                .map(rule -> withParentMetadata(rule, parentMetadata))
                .collect(Collectors.toList());
    }

    private static Rule convert(String namespace, DSLRule dslRule) {
        Rule rule = factory.createRule();
        rule.setName(dslRule.getName());
        rule.setDescription(dslRule.getDescription());
        rule.setContext(dslRule.getContextName());
        rule.setTargetPath(dslRule.getFieldName());
        rule.setPhysicalNamespace(namespace);
        rule.setRuleVariationId(UUID.randomUUID().toString());

        if (dslRule.getCondition() != null) {
            Condition condition = factory.createCondition();
            condition.setExpression(convert(dslRule.getCondition()));
            rule.setCondition(condition);
        }

        rule.setMetadata(convertMetadata(dslRule.getMetadata()));
        rule.setPayload(convert(dslRule.getPayload()));
        return rule;
    }

    private static Payload convert(DSLPayload dslPayload) {
        if (dslPayload instanceof DSLDefaultValuePayload) {
            DSLDefaultValuePayload p = (DSLDefaultValuePayload) dslPayload;
            DefaultValuePayload payload = factory.createDefaultValuePayload();
            payload.setValueExpression(p.getDefaultValueExpression() != null ? convert(p.getDefaultValueExpression()) : null);
            payload.setDefaultingType(convert(p.getDefaultingType()));
            return payload;
        }
        if (dslPayload instanceof DSLAccessibilityPayload) {
            DSLAccessibilityPayload p = (DSLAccessibilityPayload) dslPayload;
            AccessibilityPayload payload = factory.createAccessibilityPayload();
            payload.setAccessible(!p.isDisabled());
            return payload;
        }
        if (dslPayload instanceof DSLVisibilityPayload) {
            DSLVisibilityPayload p = (DSLVisibilityPayload) dslPayload;
            VisibilityPayload payload = factory.createVisibilityPayload();
            payload.setVisible(!p.isHidden());
            return payload;
        }
        if (dslPayload instanceof DSLUsageValidationPayload) {
            DSLUsageValidationPayload p = (DSLUsageValidationPayload) dslPayload;
            UsagePayload payload = factory.createUsagePayload();
            payload.setUsageType(convert(p.getUsageType()));
            payload.setSeverity(convert(p.getSeverity()));
            payload.setErrorMessage(convert(p.getCode(), p.getMessage()));
            payload.setOverridable(p.isOverridable());
            payload.setOverrideGroup(p.getOverrideGroup());
            return payload;
        }
        if (dslPayload instanceof DSLRegExpValidationPayload) {
            DSLRegExpValidationPayload p = (DSLRegExpValidationPayload) dslPayload;
            RegExpPayload payload = factory.createRegExpPayload();
            payload.setRegExp(p.getRegExp());
            payload.setSeverity(convert(p.getSeverity()));
            payload.setErrorMessage(convert(p.getCode(), p.getMessage()));
            payload.setOverridable(p.isOverridable());
            payload.setOverrideGroup(p.getOverrideGroup());
            return payload;
        }
        if (dslPayload instanceof DSLLengthValidationPayload) {
            DSLLengthValidationPayload p = (DSLLengthValidationPayload) dslPayload;
            LengthPayload payload = factory.createLengthPayload();
            payload.setLength(p.getLength());
            payload.setSeverity(convert(p.getSeverity()));
            payload.setErrorMessage(convert(p.getCode(), p.getMessage()));
            payload.setOverridable(p.isOverridable());
            payload.setOverrideGroup(p.getOverrideGroup());
            return payload;
        }
        if (dslPayload instanceof DSLAssertionValidationPayload) {
            DSLAssertionValidationPayload p = (DSLAssertionValidationPayload) dslPayload;
            AssertionPayload payload = factory.createAssertionPayload();
            payload.setAssertionExpression(convert(p.getAssertionExpression()));
            payload.setSeverity(convert(p.getSeverity()));
            payload.setErrorMessage(convert(p.getCode(), p.getMessage()));
            payload.setOverridable(p.isOverridable());
            payload.setOverrideGroup(p.getOverrideGroup());
            return payload;
        }
        if (dslPayload instanceof DSLSizePayload) {
            final DSLSizePayload p = (DSLSizePayload) dslPayload;
            final SizePayload payload = factory.createSizePayload();
            payload.setSize(p.getSize());
            payload.setOrientation(convert(p.getOrientation()));
            payload.setErrorMessage(convert(p.getCode() ,p.getMessage()));
            payload.setSeverity(convert(p.getSeverity()));
            payload.setOverridable(p.isOverridable());
            payload.setOverrideGroup(p.getOverrideGroup());
            return payload;
        }
        if (dslPayload instanceof DSLSizeRangePayload) {
            final DSLSizeRangePayload p = (DSLSizeRangePayload) dslPayload;
            final SizeRangePayload payload = factory.createSizeRangePayload();
            payload.setMin(p.getMin());
            payload.setMax(p.getMax());
            payload.setErrorMessage(convert(p.getCode() ,p.getMessage()));
            payload.setSeverity(convert(p.getSeverity()));
            payload.setOverridable(p.isOverridable());
            payload.setOverrideGroup(p.getOverrideGroup());
            return payload;
        }

        throw new IllegalStateException("Unrecognized Payload type encountered: " + dslPayload.getClass().getName());
    }

    private static SizeOrientation convert(DSLSizeOrientation orientation) {
        switch (orientation) {
            case EQUALS:
                return SizeOrientation.EQUALS;
            case MAX:
                return SizeOrientation.MAX;
            case MIN:
                return SizeOrientation.MIN;
            default:
                throw new IllegalStateException("Unrecognized Size Orientation encountered: " + orientation);
        }
    }

    private static Expression convert(DSLExpression dslExpression) {
        Expression expression = factory.createExpression();
        expression.setExpressionString(dslExpression.getExpression());
        return expression;
    }

    private static ErrorMessage convert(String code, String message) {
        if (Objects.isNull(code) && Objects.isNull(message)) {
            return null;
        }
        ErrorMessage errorMessage = factory.createErrorMessage();
        errorMessage.setErrorMessage(message);
        errorMessage.setErrorCode(code);
        return errorMessage;
    }

    private static UsageType convert(DSLUsageType dslUsageType) {
        switch (dslUsageType) {
            case MANDATORY:
                return UsageType.mandatory;
            case EMPTY:
                return UsageType.mustBeEmpty;
            default:
                throw new IllegalStateException("Unrecognized Usage Type encountered in DSL: " + dslUsageType);
        }
    }

    private static ValidationSeverity convert(DSLSeverity dslSeverity) {
        switch (dslSeverity) {
            case ERROR:
                return ValidationSeverity.critical;
            case WARN:
                return ValidationSeverity.warning;
            case INFO:
                return ValidationSeverity.info;
            default:
                throw new IllegalStateException("Unrecognized Validation Severity encountered in DSL: " + dslSeverity);
        }
    }

    private static DefaultingType convert(DSLDefaultingType dslDefaultingType) {
        switch (dslDefaultingType) {
            case DEFAULT:
                return DefaultingType.defaultValue;
            case RESET:
                return DefaultingType.resetValue;
            default:
                throw new IllegalStateException("Unrecognized DefaultingType encountered in DSL: " + dslDefaultingType);
        }
    }

}
