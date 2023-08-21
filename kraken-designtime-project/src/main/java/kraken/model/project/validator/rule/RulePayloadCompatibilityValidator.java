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
package kraken.model.project.validator.rule;

import static kraken.model.context.Cardinality.MULTIPLE;
import static kraken.model.context.Cardinality.SINGLE;
import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.context.PrimitiveFieldDataType.INTEGER;
import static kraken.model.context.PrimitiveFieldDataType.MONEY;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.context.PrimitiveFieldDataType.isPrimitiveType;
import static kraken.model.context.SystemDataTypes.isSystemDataType;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_PAYLOAD_NOT_COMPATIBLE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_PAYLOAD_NOT_COMPATIBLE_WARNING;
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidator.PayloadCompatibility.forPayload;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.ValueList.DataType;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.NumberSetPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.ValueListPayload;

/**
 *
 * @author mulevicius
 */
public class RulePayloadCompatibilityValidator implements RuleValidator {

    private static final List<PayloadCompatibility> payloadCompatibility = List.of(
        forPayload(
            DefaultValuePayload.class,
            (f, kp) -> isPrimitiveType(f.getFieldType())
                || !isSystemDataType(f.getFieldType())
                && Optional.ofNullable(kp.getContextProjection(f.getFieldType()))
                    .map(ContextDefinition::isSystem)
                    .orElse(false)
        ),
        forPayload(AccessibilityPayload.class, f -> !isSystemDataType(f.getFieldType())),
        forPayload(VisibilityPayload.class, f -> !isSystemDataType(f.getFieldType())),
        forPayload(SizePayload.class, f -> f.getCardinality() == MULTIPLE),
        forPayload(SizeRangePayload.class, f -> f.getCardinality() == MULTIPLE),
        forPayload(
            RegExpPayload.class,
            f -> isPrimitiveType(f.getFieldType()) && f.getCardinality() == SINGLE,
            f -> isPrimitiveType(f.getFieldType()) && f.getCardinality() == SINGLE
                && PrimitiveFieldDataType.valueOf(f.getFieldType()) == STRING
        ),
        forPayload(
            LengthPayload.class,
            f -> isPrimitiveType(f.getFieldType()) && f.getCardinality() == SINGLE,
            f -> isPrimitiveType(f.getFieldType()) && f.getCardinality() == SINGLE
                && PrimitiveFieldDataType.valueOf(f.getFieldType()) == STRING
        ),
        forPayload(UsagePayload.class, f -> f.getCardinality() == SINGLE),
        forPayload(
            NumberSetPayload.class,
            f -> isPrimitiveType(f.getFieldType())
                && (PrimitiveFieldDataType.valueOf(f.getFieldType()) == DECIMAL
                || PrimitiveFieldDataType.valueOf(f.getFieldType()) == MONEY
                || PrimitiveFieldDataType.valueOf(f.getFieldType()) == INTEGER)
                && f.getCardinality() == SINGLE
        ),
        forPayload(AssertionPayload.class, f -> true),
        forPayload(
            ValueListPayload.class,
            f -> f.getCardinality() == SINGLE && DataType.isSupportedFieldDataType(f.getFieldType()))
    );

    private final KrakenProject krakenProject;

    public RulePayloadCompatibilityValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        ContextDefinition contextDefinition = krakenProject.getContextProjection(rule.getContext());
        if(!contextDefinition.isStrict()) {
            return;
        }

        ContextField contextField = contextDefinition.getContextFields().get(rule.getTargetPath());

        boolean isCompatible = payloadCompatibility.stream()
                .filter(p -> p.isApplicable(rule.getPayload()))
                .allMatch(p -> p.isCompatible(contextField, krakenProject));
        if(!isCompatible) {
            var m = ValidationMessageBuilder.create(RULE_PAYLOAD_NOT_COMPATIBLE, rule)
                .parameters(
                    rule.getPayload().getPayloadType().getTypeName(),
                    rule.getContext(),
                    rule.getTargetPath(),
                    contextField.getFieldType(),
                    contextField.getCardinality().name())
                .build();
            session.add(m);
        }

        boolean isWarningCompatible = payloadCompatibility.stream()
            .filter(p -> p.isApplicable(rule.getPayload()))
            .allMatch(p -> p.isWarningCompatible(contextField));
        if(!isWarningCompatible) {
            var m = ValidationMessageBuilder.create(RULE_PAYLOAD_NOT_COMPATIBLE_WARNING, rule)
                .parameters(
                    rule.getPayload().getPayloadType().getTypeName(),
                    rule.getContext(),
                    rule.getTargetPath(),
                    contextField.getFieldType(),
                    contextField.getCardinality().name())
                .build();
            session.add(m);
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && krakenProject.getContextDefinitions().containsKey(rule.getContext())
            && rule.getTargetPath() != null
            && krakenProject.getContextProjection(rule.getContext()).getContextFields().containsKey(rule.getTargetPath())
            && rule.getPayload() != null;
    }

    static class PayloadCompatibility {

        private Class<?> payloadClass;

        private BiPredicate<ContextField, KrakenProject> payloadIsCompatible;

        private Predicate<ContextField> payloadIsWarningCompatible;

        PayloadCompatibility(Class<?> payloadClass,
                             Predicate<ContextField> payloadIsCompatible,
                             Predicate<ContextField> payloadIsWarningCompatible) {
            this.payloadClass = payloadClass;
            this.payloadIsCompatible = (f, p) -> payloadIsCompatible.test(f);
            this.payloadIsWarningCompatible = payloadIsWarningCompatible;
        }

        PayloadCompatibility(Class<?> payloadClass,
                             BiPredicate<ContextField, KrakenProject> payloadIsCompatible) {
            this.payloadClass = payloadClass;
            this.payloadIsCompatible = payloadIsCompatible;
            this.payloadIsWarningCompatible = f -> true;
        }

        boolean isCompatible(ContextField contextField, KrakenProject krakenProject) {
            return payloadIsCompatible.test(contextField, krakenProject);
        }

        boolean isWarningCompatible(ContextField contextField) {
            return payloadIsWarningCompatible.test(contextField);
        }

        boolean isApplicable(Payload payload) {
            return payloadClass.isAssignableFrom(payload.getClass());
        }

        static PayloadCompatibility forPayload(Class<?> payloadClass,
                                               BiPredicate<ContextField, KrakenProject> payloadIsCompatible) {
            return new PayloadCompatibility(payloadClass, payloadIsCompatible);
        }

        static PayloadCompatibility forPayload(Class<?> payloadClass,
                                               Predicate<ContextField> payloadIsCompatible) {
            return new PayloadCompatibility(payloadClass, payloadIsCompatible, f -> true);
        }

        static PayloadCompatibility forPayload(Class<?> payloadClass,
                                               Predicate<ContextField> payloadIsCompatible,
                                               Predicate<ContextField> payloadIsWarningCompatible) {
            return new PayloadCompatibility(payloadClass, payloadIsCompatible, payloadIsWarningCompatible);
        }
    }

}
