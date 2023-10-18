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
package kraken.runtime.engine.handlers;

import static kraken.message.SystemMessageBuilder.Message.DEFAULT_VALUE_PAYLOAD_INCOMPATIBLE_VALUE;
import static kraken.message.SystemMessageBuilder.Message.RULE_DEFAULT_VALUE_EXPRESSION_EVALUATION_FAILURE;
import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import kraken.el.date.DateCalculator;
import kraken.el.math.Numbers;
import kraken.message.SystemMessageBuilder;
import kraken.message.SystemMessageLogger;
import kraken.model.context.Cardinality;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.derive.DefaultingType;
import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.events.RuleEvent;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.handlers.trace.DefaultValueExpressionEvaluationOperation;
import kraken.runtime.engine.handlers.trace.FieldValueRenderer;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.tracer.Tracer;

/**
 * Payload handler implementation to process {@link DefaultValuePayload}s
 *
 * @author rimas
 * @since 1.0
 */
public class DefaultValuePayloadHandler implements RulePayloadHandler {

    private static final SystemMessageLogger logger = SystemMessageLogger.getLogger(DefaultValuePayloadHandler.class);

    private final KrakenExpressionEvaluator evaluator;

    public DefaultValuePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.DEFAULT;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        DefaultValuePayload defaultValuePayload = (DefaultValuePayload) rule.getPayload();


        Object value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        Object updatedValue = value;

        String path = resolveTargetPath(rule, dataContext);
        try {
            if (DefaultingType.defaultValue == defaultValuePayload.getDefaultingType()) {
                if (PayloadHandlerUtils.isEmptyValue(value)) {
                    Object valueToSet = evaluateValueToSet(rule, dataContext, session, defaultValuePayload);
                    updatedValue = evaluator.evaluateSetProperty(valueToSet, path, dataContext.getDataObject());
                }
            } else if (DefaultingType.resetValue == defaultValuePayload.getDefaultingType()) {
                Object valueToSet = evaluateValueToSet(rule, dataContext, session, defaultValuePayload);
                updatedValue = evaluator.evaluateSetProperty(valueToSet, path, dataContext.getDataObject());
            } else {
                throw new UnsupportedOperationException(String.format(
                        "Default rule type %s is not supported",
                        defaultValuePayload.getDefaultingType())
                );
            }
        } catch (KrakenRuntimeException e) {
            logger.debug(
                RULE_DEFAULT_VALUE_EXPRESSION_EVALUATION_FAILURE,
                defaultValuePayload.getValueExpression().getOriginalExpressionString(),
                rule.getName(),
                e
            );

            return new DefaultValuePayloadResult(e);
        }

        List<RuleEvent> events = new ArrayList<>();
        if (!Objects.equals(value, updatedValue)) {
            events.add(new ValueChangedEvent(dataContext, path, value, updatedValue));
        }

        return new DefaultValuePayloadResult(events);
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (DefaultValuePayloadResult) payloadResult;

        if(result.getEvents().isEmpty()) {
            if(result.getException().isPresent()) {
                return "Field value was not changed. Default value is not applied due to expression error.";
            }
            return "Field value was not changed.";
        }

        var defaultValueEvent = (ValueChangedEvent) result.getEvents().get(0);
        if(PayloadHandlerUtils.isEmptyValue(defaultValueEvent.getPreviousValue())) {
            return String.format(
                "Field value set to '%s'.",
                FieldValueRenderer.render(defaultValueEvent.getNewValue())
            );
        }
        return String.format(
            "Field value reset from '%s' to '%s'.",
            FieldValueRenderer.render(defaultValueEvent.getPreviousValue()),
            FieldValueRenderer.render(defaultValueEvent.getNewValue())
        );
    }

    private Object evaluateValueToSet(
            RuntimeRule rule,
            DataContext dataContext,
            EvaluationSession session,
            DefaultValuePayload defaultValuePayload
    ) {
        Tracer.doOperation(
            new DefaultValueExpressionEvaluationOperation(defaultValuePayload.getValueExpression(), dataContext)
        );
        Object rawValueToSet = evaluator.evaluate(defaultValuePayload.getValueExpression(), dataContext, session);
        return coerce(rawValueToSet, rule, dataContext, session);
    }

    private Object coerce(Object value, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        if(dataContext.getContextDefinition() != null) {
            ContextField contextField = dataContext.getContextDefinition().getFields().get(rule.getTargetPath());
            if (contextField != null) {
                boolean isPrimitive = PrimitiveFieldDataType.isPrimitiveType(contextField.getFieldType());
                boolean isComplexSystemType = Optional
                    .ofNullable(session.getContextModelTree().getContext(contextField.getFieldType()))
                    .map(RuntimeContextDefinition::isSystem)
                    .orElse(false);
                if(!isPrimitive && !isComplexSystemType) {
                    String template = "Unsupported operation. "
                        + "Default value rule '%s' is being applied on attribute '%s.%s' whose type type is '%s'. "
                        + "Default value rule can only be applied on attribute which is primitive or a collection of primitives or a complex system type.";
                    String type = toTypeSymbol(contextField);
                    String message = String.format(
                        template,
                        rule.getName(),
                        dataContext.getContextDefinition().getName(),
                        contextField.getName(),
                        type
                    );
                    throw new UnsupportedOperationException(message);
                }
                if (isComplexSystemType){
                    return value;
                }

                return coerce(value, dataContext, contextField, session);
            }
        }
        return value;
    }

    private Object coerce(Object value, DataContext dataContext, ContextField field, EvaluationSession session) {
        var type = PrimitiveFieldDataType.valueOf(field.getFieldType());
        if(Cardinality.MULTIPLE == field.getCardinality()) {
            if(value == null) {
                return null;
            }
            if(value instanceof Collection) {
                var coercedCollectionValue = new ArrayList<>();
                for (var v : (Collection<?>) value) {
                    var coercedValue = coerce(v, dataContext, type, field, session);
                    coercedCollectionValue.add(coercedValue);
                }
                return coercedCollectionValue;
            }
            throwAndLogIncompatibleValueType(value, dataContext, field);
        }

        return coerce(value, dataContext, type, field, session);
    }

    private Object coerce(Object value, DataContext dataContext, PrimitiveFieldDataType type, ContextField field,
                          EvaluationSession session) {
        if(value == null) {
            return null;
        }
        switch (type) {
            case MONEY:
                if(value instanceof MonetaryAmount) {
                    return value;
                }
                if(value instanceof Number) {
                    return Monetary.getDefaultAmountFactory()
                        .setCurrency(Monetary.getCurrency(session.getEvaluationConfig().getCurrencyCd()))
                        .setNumber((Number) value)
                        .create();
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case INTEGER:
            case DECIMAL:
                if(value instanceof Number) {
                    return value;
                }
                if(value instanceof MonetaryAmount) {
                    return Numbers.fromMoney((MonetaryAmount) value);
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case DATE:
                if(value instanceof LocalDate) {
                    return value;
                }
                if(value instanceof LocalDateTime) {
                    var ruleTimezoneId = session.getEvaluationConfig().getRuleTimezoneId();
                    return DateCalculator.getInstance().toDate((LocalDateTime) value, ruleTimezoneId);
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case DATETIME:
                if(value instanceof LocalDateTime) {
                    return value;
                }
                if(value instanceof LocalDate) {
                    var ruleTimezoneId = session.getEvaluationConfig().getRuleTimezoneId();
                    return DateCalculator.getInstance().toDateTime((LocalDate) value, ruleTimezoneId);
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case STRING:
                if(value instanceof String) {
                    return value;
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case BOOLEAN:
                if(value instanceof Boolean) {
                    return value;
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
        }
        throw new UnsupportedOperationException("Unknown primitive field type: " + field.getFieldType());
    }

    private void throwAndLogIncompatibleValueType(Object value, DataContext dataContext, ContextField field) {
        var m = SystemMessageBuilder.create(DEFAULT_VALUE_PAYLOAD_INCOMPATIBLE_VALUE)
            .parameters(
                FieldValueRenderer.render(value),
                value.getClass().getName(),
                dataContext.getContextDefinition().getName(),
                field.getName(),
                toTypeSymbol(field)
            )
            .build();

        logger.getSl4jLogger().warn(m.formatMessageWithCode());

        throw new KrakenRuntimeException(m);
    }

    private String toTypeSymbol(ContextField field) {
        return field.getCardinality() == Cardinality.SINGLE
            ? field.getFieldType()
            : field.getFieldType() + "[]";
    }
}
