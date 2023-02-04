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
package kraken.converter;

import java.util.List;
import java.util.stream.Collectors;

import kraken.converter.translation.KrakenExpressionTranslator;
import kraken.dimensions.DimensionSet;
import kraken.model.dimensions.DimensionSetService;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.rule.Condition;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.runtime.model.rule.payload.ui.AccessibilityPayload;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import kraken.runtime.model.rule.payload.validation.NumberSetPayload;
import kraken.runtime.model.rule.payload.validation.RegExpPayload;
import kraken.runtime.model.rule.payload.validation.SizePayload;
import kraken.runtime.model.rule.payload.validation.SizeRangePayload;
import kraken.runtime.model.rule.payload.validation.UsagePayload;
import kraken.runtime.model.rule.payload.validation.ValueListPayload;
import kraken.runtime.repository.dynamic.DynamicRuleHolder;

/**
 * @author mulevicius
 */
public class RuleConverter {

    private RuleDependencyExtractor ruleDependencyExtractor;

    private KrakenExpressionTranslator krakenExpressionTranslator;

    private MetadataConverter metadataConverter = new MetadataConverter();

    private DimensionSetService dimensionSetService;

    private String namespace;

    public RuleConverter(RuleDependencyExtractor ruleDependencyExtractor,
                         KrakenExpressionTranslator krakenExpressionTranslator,
                         DimensionSetService dimensionSetService,
                         String namespace) {
        this.ruleDependencyExtractor = ruleDependencyExtractor;
        this.krakenExpressionTranslator = krakenExpressionTranslator;
        this.dimensionSetService = dimensionSetService;
        this.namespace = namespace;
    }

    public List<RuntimeRule> convert(List<kraken.model.Rule> rules) {
        return rules.stream()
            .map(r -> convertRule(r, dimensionSetService.resolveRuleDimensionSet(namespace, r)))
            .collect(Collectors.toList());
    }

    public RuntimeRule convertDynamicRule(DynamicRuleHolder dynamicRuleHolder) {
        return convertRule(dynamicRuleHolder.getRule(), dynamicRuleHolder.getDimensionSet());
    }

    private RuntimeRule convertRule(kraken.model.Rule rule, DimensionSet dimensionSet) {
        List<Dependency> dependencies = ruleDependencyExtractor.extractDependencies(rule).stream()
            .map(d -> new Dependency(d.getContextName(), d.getFieldName(), d.isCcrDependency(), d.isSelfDependency()))
            .collect(Collectors.toList());

        Condition condition = rule.getCondition() != null
            ? new Condition(convertRuleExpression(rule, rule.getCondition().getExpression()))
            : null;

        return new RuntimeRule(
            rule.getName(),
            rule.getContext(),
            rule.getTargetPath(),
            condition,
            convert(rule, rule.getPayload()),
            dependencies,
            dimensionSet,
            metadataConverter.convert(rule.getMetadata()),
            rule.getPriority()
        );
    }

    private Payload convert(kraken.model.Rule rule, kraken.model.Payload payload) {
        if(payload instanceof kraken.model.state.AccessibilityPayload) {
            kraken.model.state.AccessibilityPayload p = (kraken.model.state.AccessibilityPayload) payload;
            return new AccessibilityPayload(p.isAccessible());
        }
        if(payload instanceof kraken.model.state.VisibilityPayload) {
            kraken.model.state.VisibilityPayload p = (kraken.model.state.VisibilityPayload) payload;
            return new kraken.runtime.model.rule.payload.ui.VisibilityPayload(p.isVisible());
        }
        if(payload instanceof kraken.model.derive.DefaultValuePayload) {
            kraken.model.derive.DefaultValuePayload p = (kraken.model.derive.DefaultValuePayload) payload;
            return new DefaultValuePayload(
                    convertRuleExpression(rule, p.getValueExpression()),
                    p.getDefaultingType()
            );
        }
        if(payload instanceof kraken.model.validation.SizeRangePayload) {
            kraken.model.validation.SizeRangePayload p = (kraken.model.validation.SizeRangePayload) payload;
            return new SizeRangePayload(
                    convert(rule, p.getErrorMessage()),
                    p.getSeverity(),
                    p.isOverridable(),
                    p.getOverrideGroup(),
                    p.getMin(),
                    p.getMax()
            );
        }
        if(payload instanceof kraken.model.validation.SizePayload) {
            kraken.model.validation.SizePayload p = (kraken.model.validation.SizePayload) payload;
            return new SizePayload(
                    convert(rule, p.getErrorMessage()),
                    p.getSeverity(),
                    p.isOverridable(),
                    p.getOverrideGroup(),
                    p.getOrientation(),
                    p.getSize()
            );
        }
        if(payload instanceof kraken.model.validation.RegExpPayload) {
            kraken.model.validation.RegExpPayload p = (kraken.model.validation.RegExpPayload) payload;
            return new RegExpPayload(
                    convert(rule, p.getErrorMessage()),
                    p.getSeverity(),
                    p.isOverridable(),
                    p.getOverrideGroup(),
                    p.getRegExp()
            );
        }
        if(payload instanceof kraken.model.validation.UsagePayload) {
            kraken.model.validation.UsagePayload p = (kraken.model.validation.UsagePayload) payload;
            return new UsagePayload(
                    convert(rule, p.getErrorMessage()),
                    p.getSeverity(),
                    p.isOverridable(),
                    p.getOverrideGroup(),
                    p.getUsageType()
            );
        }
        if(payload instanceof kraken.model.validation.LengthPayload) {
            kraken.model.validation.LengthPayload p = (kraken.model.validation.LengthPayload) payload;
            return new LengthPayload(
                    convert(rule, p.getErrorMessage()),
                    p.getSeverity(),
                    p.isOverridable(),
                    p.getOverrideGroup(),
                    p.getLength()
            );
        }
        if(payload instanceof kraken.model.validation.AssertionPayload) {
            kraken.model.validation.AssertionPayload p = (kraken.model.validation.AssertionPayload) payload;
            return new AssertionPayload(
                    convert(rule, p.getErrorMessage()),
                    p.getSeverity(),
                    p.isOverridable(),
                    p.getOverrideGroup(),
                    convertRuleExpression(rule, p.getAssertionExpression())
            );
        }
        if(payload instanceof kraken.model.validation.NumberSetPayload) {
            var p = (kraken.model.validation.NumberSetPayload) payload;
            return new NumberSetPayload(
                p.getMin(),
                p.getMax(),
                p.getStep(),
                convert(rule, p.getErrorMessage()),
                p.getSeverity(),
                p.isOverridable(),
                p.getOverrideGroup()
            );
        }
        if (payload instanceof  kraken.model.validation.ValueListPayload) {
            kraken.model.validation.ValueListPayload valueListPayload
                = (kraken.model.validation.ValueListPayload) payload;

            return new ValueListPayload(
                convert(rule, valueListPayload.getErrorMessage()),
                valueListPayload.getSeverity(),
                valueListPayload.isOverridable(),
                valueListPayload.getOverrideGroup(),
                valueListPayload.getValueList()
            );
        }
        throw new IllegalStateException("Unknown Payload: " + payload.getClass());
    }

    private ErrorMessage convert(kraken.model.Rule rule, kraken.model.ErrorMessage errorMessage) {
        return krakenExpressionTranslator.translateErrorMessage(rule, errorMessage);
    }

    private CompiledExpression convertRuleExpression(kraken.model.Rule rule, kraken.model.Expression expression) {
        return krakenExpressionTranslator.translateExpression(rule, expression);
    }
}
