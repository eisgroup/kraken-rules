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

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueCollectionSizeValidationOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.SizeRangePayload;
import kraken.tracer.Tracer;

/**
 * @author psurinin
 */
public class SizeRangePayloadHandler implements RulePayloadHandler {

    private KrakenExpressionEvaluator evaluator;

    public SizeRangePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.SIZE_RANGE;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        var payload = (SizeRangePayload) rule.getPayload();

        Object value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        if (value == null) {
            value = Collections.emptyList();
        }

        boolean success = true;
        if (value instanceof Collection) {
            Tracer.doOperation(new FieldValueCollectionSizeValidationOperation((Collection<?>) value));
            int size = ((Collection<?>) value).size();
            success = size >= payload.getMin() && size <= payload.getMax();
        }

        var templateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), dataContext, session);
        return new SizeRangePayloadResult(success, payload, templateVariables);
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (SizeRangePayloadResult) payloadResult;
        return result.getSuccess()
            ? String.format("Field is valid. Collection size is in interval [%s, %s].", result.getMin(), result.getMax())
            : String.format("Field is not valid. Collection size is not in interval [%s, %s].", result.getMin(), result.getMax());
    }
}
