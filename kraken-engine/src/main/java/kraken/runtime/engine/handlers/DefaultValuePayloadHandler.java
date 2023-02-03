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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.money.Monetary;
import javax.money.MonetaryAmount;

import kraken.el.functionregistry.functions.MoneyFunctions;
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
import kraken.runtime.engine.handlers.trace.DefaultValuePayloadEvaluatedOperation;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

/**
 * Payload handler implementation to process {@link DefaultValuePayload}s
 *
 * @author rimas
 * @since 1.0
 */
public class DefaultValuePayloadHandler implements RulePayloadHandler {

    private final KrakenExpressionEvaluator evaluator;

    private final Logger logger = LoggerFactory.getLogger(DefaultValuePayloadHandler.class);

    public DefaultValuePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.DEFAULT;
    }

    @Override
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        DefaultValuePayload defaultValuePayload = (DefaultValuePayload) payload;

        String path = resolveTargetPath(rule, dataContext);

        Object value = evaluator.evaluateGetProperty(path, dataContext.getDataObject());
        Object updatedValue = value;

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
        } catch (KrakenRuntimeException ex) {
            logger.debug(
                    "Default value expression '{}' in rule '{}' failed with exception: \n {}",
                    ((DefaultValuePayload) payload).getValueExpression().getExpressionString(),
                    rule.getName(),
                    ex
            );

            return new DefaultValuePayloadResult(ex);
        }

        List<RuleEvent> events = new ArrayList<>();
        if (!Objects.equals(value, updatedValue)) {
            events.add(new ValueChangedEvent(dataContext, path, value, updatedValue));
        }

        Tracer.doOperation(new DefaultValuePayloadEvaluatedOperation(defaultValuePayload, value, updatedValue));
        return new DefaultValuePayloadResult(events);
    }

    private Object evaluateValueToSet(
            RuntimeRule rule,
            DataContext dataContext,
            EvaluationSession session,
            DefaultValuePayload defaultValuePayload
    ) {
        Object rawValueToSet = evaluator.evaluate(defaultValuePayload.getValueExpression(), dataContext, session);
        return coerce(rawValueToSet, rule, dataContext, session);
    }

    private Object coerce(Object value, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        if(dataContext.getContextDefinition() != null) {
            ContextField contextField = dataContext.getContextDefinition().getFields().get(rule.getTargetPath());
            if (contextField != null) {
                return coerce(value, rule, dataContext, contextField, session);
            }
        }
        return value;
    }

    private Object coerce(Object value,
                          RuntimeRule rule,
                          DataContext dataContext,
                          ContextField field,
                          EvaluationSession session) {
        if(!PrimitiveFieldDataType.isPrimitiveType(field.getFieldType())
            || field.getCardinality() != Cardinality.SINGLE) {
            String template = "Unsupported operation. Default value rule '%s' is being applied on attribute '%s.%s' "
                + "which is not a primitive attribute, but a '%s'.";
            String type = toTypeSymbol(field);
            String message = String.format(
                template,
                rule.getName(),
                dataContext.getContextDefinition().getName(),
                field.getName(),
                type
            );
            throw new UnsupportedOperationException(message);
        }

        // null can be assigned to any primitive type
        if(value == null) {
            return value;
        }

        switch (PrimitiveFieldDataType.valueOf(field.getFieldType())) {
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
                    return MoneyFunctions.fromMoney((MonetaryAmount) value);
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case DATE:
                if(value instanceof LocalDate) {
                    return value;
                }
                if(value instanceof LocalDateTime) {
                    return ((LocalDateTime) value).toLocalDate();
                }
                throwAndLogIncompatibleValueType(value, dataContext, field);
                break;
            case DATETIME:
                if(value instanceof LocalDateTime) {
                    return value;
                }
                if(value instanceof LocalDate) {
                    return ((LocalDate) value).atStartOfDay();
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
        String template = "Cannot apply value '%s (instanceof %s)' on '%s.%s' because value type is not assignable to "
            + "field type '%s'. Rule will be silently ignored.";
        String type = toTypeSymbol(field);
        String message = String.format(
            template,
            value,
            value.getClass().getName(),
            dataContext.getContextDefinition().getName(),
            field.getName(),
            type
        );

        logger.warn(message);

        throw new KrakenRuntimeException(message);
    }

    private String toTypeSymbol(ContextField field) {
        return field.getCardinality() == Cardinality.SINGLE
            ? field.getFieldType()
            : field.getFieldType() + "[]";
    }
}
