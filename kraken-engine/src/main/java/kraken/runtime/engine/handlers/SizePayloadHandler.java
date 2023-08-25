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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueCollectionSizeValidationOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.SizePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.SizePayload;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.tracer.Tracer;

/**
 * @author psurinin
 */
public class SizePayloadHandler implements RulePayloadHandler {

    private final KrakenExpressionEvaluator evaluator;

    public SizePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.SIZE;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        Object value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        if (value == null) {
            value = Collections.emptyList();
        }

        var payload = (SizePayload) rule.getPayload();
        List<Object> rawTemplateVariables = evaluator.evaluateTemplateVariables(
            payload.getErrorMessage(),
            dataContext,
            session
        );

        boolean success = true;
        if (value instanceof Collection) {
            Tracer.doOperation(new FieldValueCollectionSizeValidationOperation((Collection<?>) value));
            success = evaluateCollection((Collection<?>) value, payload);
        }

        return new SizePayloadResult(success, payload, rawTemplateVariables);
    }

    private boolean evaluateCollection(Collection<?> fieldValues, SizePayload payload) {
        switch (payload.getOrientation()) {
            case MIN:
                return fieldValues.size() >= payload.getSize();
            case MAX:
                return fieldValues.size() <= payload.getSize();
            case EQUALS:
                return fieldValues.size() == payload.getSize();
            default:
                throw new IllegalArgumentException(
                    "Unknown size payload orientation encountered:" + payload.getOrientation());
        }
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (SizePayloadResult) payloadResult;
        return result.getSuccess()
            ? String.format("Field is valid. Collection size is %s.", describeExpectedSize(result))
            : String.format("Field is not valid. Collection size is not %s.", describeExpectedSize(result));
    }

    private String describeExpectedSize(SizePayloadResult payloadResult) {
        switch (payloadResult.getSizeOrientation()) {
            case MIN:
                return "equal to or more than " + payloadResult.getSize();
            case MAX:
                return "equal to or less than " + payloadResult.getSize();
            case EQUALS:
                return "equal to " + payloadResult.getSize();
            default:
                throw new IllegalArgumentException("Unknown size orientation: " + payloadResult.getSizeOrientation());
        }
    }
}
