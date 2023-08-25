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

import static kraken.message.SystemMessageBuilder.Message.VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_NUMBER;
import static kraken.message.SystemMessageBuilder.Message.VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_STRING;

import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import kraken.el.math.Numbers;
import kraken.message.SystemMessageBuilder;
import kraken.model.ValueList;
import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueRenderer;
import kraken.runtime.engine.handlers.trace.FieldValueValidationOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.ValueListPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
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
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        var payload = (ValueListPayload) rule.getPayload();

        var value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        Tracer.doOperation(new FieldValueValidationOperation(value));
        boolean success = doExecute(value, payload);

        var rawTemplateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), dataContext, session);

        return new ValueListPayloadResult(success, payload, rawTemplateVariables);
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (ValueListPayloadResult) payloadResult;
        var valuesString = result.getValueList().getValues().stream()
            .map(FieldValueRenderer::render)
            .collect(Collectors.joining(", "));

        return result.getSuccess()
            ? String.format("Field is valid. Field value is one of [ %s ].", valuesString)
            : String.format("Field is not valid. Field value is not one of [ %s ].", valuesString);
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
                throw new IllegalArgumentException(
                    "Unknown value list data type " + valueListPayload.getValueList().getValueType());
        }
    }

    private Number asNumber(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        }

        if (value instanceof MonetaryAmount) {
            return Numbers.fromMoney((MonetaryAmount) value);
        }

        var m = SystemMessageBuilder.create(VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_NUMBER)
            .parameters(value)
            .build();

        throw new KrakenRuntimeException(m);
    }

    private String asString(Object value) {
        if (value instanceof String) {
            return (String) value;
        }

        var m = SystemMessageBuilder.create(VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_STRING)
            .parameters(value)
            .build();

        throw new KrakenRuntimeException(m);
    }

}
