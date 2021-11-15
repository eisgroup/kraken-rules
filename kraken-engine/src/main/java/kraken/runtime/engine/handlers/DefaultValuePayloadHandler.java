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

import kraken.el.functions.MoneyFunctions;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.derive.DefaultingType;
import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.events.RuleEvent;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
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

    private KrakenExpressionEvaluator evaluator;

    private Logger logger = LoggerFactory.getLogger(DefaultValuePayloadHandler.class);

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
            events.add(new ValueChangedEvent(dataContext, rule.getTargetPath(), value, updatedValue));
        }
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
                return coerce(value, contextField.getFieldType(), session);
            }
        }
        return value;
    }

    private Object coerce(Object value, String type, EvaluationSession session) {
        if (PrimitiveFieldDataType.MONEY.toString().equals(type) && value instanceof Number) {
            return Monetary.getDefaultAmountFactory()
                    .setCurrency(Monetary.getCurrency(session.getEvaluationConfig().getCurrencyCd()))
                    .setNumber((Number) value)
                    .create();
        }
        if((PrimitiveFieldDataType.INTEGER.toString().equals(type) || PrimitiveFieldDataType.DECIMAL.toString().equals(type))
            && value instanceof MonetaryAmount) {
            return MoneyFunctions.fromMoney((MonetaryAmount) value);
        }
        if(PrimitiveFieldDataType.DATE.toString().equals(type) && value instanceof LocalDateTime) {
            return ((LocalDateTime) value).toLocalDate();
        }
        if(PrimitiveFieldDataType.DATETIME.toString().equals(type) && value instanceof LocalDate) {
            return ((LocalDate) value).atStartOfDay();
        }
        return value;
    }

}