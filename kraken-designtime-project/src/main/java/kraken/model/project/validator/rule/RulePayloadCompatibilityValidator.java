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
import static kraken.model.project.validator.rule.RulePayloadCompatibilityValidator.PayloadCompatibility.forPayload;

import java.util.List;
import java.util.function.Predicate;

import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.ValueList.DataType;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.SystemDataTypes;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
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
 * <p>
 * Validates {@link Rule}s {@link Payload} to be compatible with
 * {@link ContextField#getCardinality()} and {@link ContextField#getFieldType()}.
 * </p>
 * <br/>
 * <h1>Compatibility List</h1>
 * <br/>
 * {@link PrimitiveFieldDataType} {@link Cardinality#SINGLE}:
 * <ul>
 * <li>{@link UsagePayload}</li>
 * <li>{@link RegExpPayload}</li>
 * <li>{@link DefaultValuePayload}</li>
 * <li>{@link VisibilityPayload}</li>
 * <li>{@link AccessibilityPayload}</li>
 * <li>{@link AssertionPayload}</li>
 * <li>{@link LengthPayload}</li>
 * </ul>
 * <p>
 * {@link PrimitiveFieldDataType} {@link Cardinality#MULTIPLE}:
 * <ul>
 * <li>{@link VisibilityPayload}</li>
 * <li>{@link AccessibilityPayload}</li>
 * <li>{@link AssertionPayload}</li>
 * <li>{@link SizePayload}</li>
 * <li>{@link SizeRangePayload}</li>
 * </ul>
 * <p>
 * FieldType as a {@link ContextDefinition} {@link Cardinality#SINGLE}:
 * <ul>
 * <li>{@link UsagePayload}</li>
 * <li>{@link VisibilityPayload}</li>
 * <li>{@link AccessibilityPayload}</li>
 * <li>{@link AssertionPayload}</li>
 * </ul>
 * <p>
 * FieldType as a {@link ContextDefinition} {@link Cardinality#MULTIPLE}:
 * <ul>
 * <li>{@link SizePayload}</li>
 * <li>{@link SizeRangePayload}</li>
 * <li>{@link VisibilityPayload}</li>
 * <li>{@link AccessibilityPayload}</li>
 * <li>{@link AssertionPayload}</li>
 * </ul>
 * <p>
 * {@link SystemDataTypes} {@link Cardinality#SINGLE}:
 * <ul>
 * <li>{@link UsagePayload}</li>
 * <li>{@link AssertionPayload}</li>
 * </ul>
 * <p>
 * {@link SystemDataTypes} {@link Cardinality#MULTIPLE}:
 * <ul>
 * <li>{@link SizePayload}</li>
 * <li>{@link SizeRangePayload}</li>
 * <li>{@link AssertionPayload}</li>
 * </ul>
 * {@link PrimitiveFieldDataType} supported in {@link DataType} and {@link Cardinality#SINGLE}:
 * <ul>
 * <li>{@link ValueListPayload}</li>
 * </ul>
 *
 * @author mulevicius
 */
public class RulePayloadCompatibilityValidator implements RuleValidator {

    private static final List<PayloadCompatibility> payloadCompatibility = List.of(
        forPayload(DefaultValuePayload.class, f -> isPrimitiveType(f.getFieldType()) && f.getCardinality() == SINGLE),
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
                .allMatch(p -> p.isCompatible(contextField));
        if(!isCompatible) {
            String messageFormat = "%s cannot be applied on %s.%s because type '%s' and cardinality '%s' " +
                    "is incompatible with the payload type.";
            String message = String.format(
                    messageFormat,
                    rule.getPayload().getPayloadType().getTypeName(),
                    rule.getContext(),
                    rule.getTargetPath(),
                    contextField.getFieldType(),
                    contextField.getCardinality().name()
            );
            session.add(new ValidationMessage(rule, message, Severity.ERROR));
        }

        boolean isWarningCompatible = payloadCompatibility.stream()
            .filter(p -> p.isApplicable(rule.getPayload()))
            .allMatch(p -> p.isWarningCompatible(contextField));
        if(!isWarningCompatible) {
            String messageFormat = "%s cannot be applied on %s.%s because type '%s' and cardinality '%s' " +
                "is incompatible with the payload type. In the future, such configuration will not be supported.";
            String message = String.format(
                messageFormat,
                rule.getPayload().getPayloadType().getTypeName(),
                rule.getContext(),
                rule.getTargetPath(),
                contextField.getFieldType(),
                contextField.getCardinality().name()
            );
            session.add(new ValidationMessage(rule, message, Severity.WARNING));
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

        private Predicate<ContextField> payloadIsCompatible;

        private Predicate<ContextField> payloadIsWarningCompatible;

        PayloadCompatibility(Class<?> payloadClass, Predicate<ContextField> payloadIsCompatible,
                             Predicate<ContextField> payloadIsWarningCompatible) {
            this.payloadClass = payloadClass;
            this.payloadIsCompatible = payloadIsCompatible;
            this.payloadIsWarningCompatible = payloadIsWarningCompatible;
        }

        boolean isCompatible(ContextField contextField) {
            return payloadIsCompatible.test(contextField);
        }

        boolean isWarningCompatible(ContextField contextField) {
            return payloadIsWarningCompatible.test(contextField);
        }

        boolean isApplicable(Payload payload) {
            return payloadClass.isAssignableFrom(payload.getClass());
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
