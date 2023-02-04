/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime.engine.handlers;

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

import java.util.List;
import java.util.Optional;

import javax.money.MonetaryAmount;

import kraken.el.math.Numbers;
import kraken.model.ValueList;
import kraken.model.ValueList.DataType;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.ValueListPayloadEvaluatedOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.ValueListPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.ValueListPayload;
import kraken.tracer.Tracer;

/**
 * A payload handler specific to {@link ValueListPayload}. Evaluates that field value
 * is equal to at least one value provided in {@link ValueList}.
 *
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public final class ValueListPayloadHandler implements RulePayloadHandler {

    private final KrakenExpressionEvaluator evaluator;

    public ValueListPayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.VALUE_LIST;
    }

    @Override
    public PayloadResult executePayload(Payload payload,
                                        RuntimeRule rule,
                                        DataContext dataContext,
                                        EvaluationSession session) {
        ContextField contextField = resolveField(rule, dataContext);
        ValueListPayload valueListPayload = (ValueListPayload) payload;

        validateDataTypeCompatibility(rule, contextField, valueListPayload.getValueList().getValueType());

        Object value = evaluator.evaluateGetProperty(
            resolveTargetPath(rule, dataContext),
            dataContext.getDataObject()
        );

        List<String> templateVariables = evaluator
            .evaluateTemplateVariables(valueListPayload.getErrorMessage(), dataContext, session);
        boolean success = doExecute(value, valueListPayload);

        Tracer.doOperation(new ValueListPayloadEvaluatedOperation(valueListPayload, value, success));

        return new ValueListPayloadResult(
            success,
            valueListPayload,
            templateVariables);
    }

    private boolean doExecute(Object fieldValue, ValueListPayload valueListPayload) {
        if (fieldValue == null) {
            return true;
        }

        switch (valueListPayload.getValueList().getValueType()) {
            case STRING:
                return valueListPayload.getValueList().has(asString(fieldValue));
            case DECIMAL:
                return valueListPayload.getValueList().has(asNumber(fieldValue));
            default:
                throw new KrakenRuntimeException(
                    "Not support value list data type" + valueListPayload.getValueList().getValueType());
        }
    }

    private Number asNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }

        if (value instanceof MonetaryAmount) {
            return Numbers.fromMoney((MonetaryAmount) value);
        }

        throw new KrakenRuntimeException("Unable to convert value " + value + " to a decimal.");
    }

    private String asString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }

        throw new KrakenRuntimeException("Unable to convert value " + value + " to a string.");
    }

    private ContextField resolveField(RuntimeRule rule, DataContext dataContext) {
        return Optional.ofNullable(dataContext.getContextDefinition())
            .map(contextDefinition -> contextDefinition.getFields().get(rule.getTargetPath()))
            .orElseThrow(() -> {
                String template = "Cannot resolve context field for path '%s' in context '%s')";

                return new KrakenRuntimeException(
                    String.format(
                        template,
                        rule.getTargetPath(),
                        dataContext.getContextDefinition().getName()
                    )
                );
            });
    }

    private void validateDataTypeCompatibility(RuntimeRule rule, ContextField contextField, DataType valueListDataType) {
        PrimitiveFieldDataType fieldDataType = PrimitiveFieldDataType.valueOf(contextField.getFieldType());
        boolean isCompatible = valueListDataType.getFieldTypes().contains(fieldDataType);

        if (!isCompatible) {
            String template = "Unable to execute payload of Rule '%s'. "
                + "Field data type '%s' is not compatible with '%s' value list data type.";

            throw new KrakenRuntimeException(
                String.format(
                    template,
                    rule.getName(),
                    fieldDataType,
                    valueListDataType));
        }
    }

}
