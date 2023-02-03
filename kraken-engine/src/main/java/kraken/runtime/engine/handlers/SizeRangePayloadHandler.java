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
import kraken.runtime.engine.handlers.trace.SizeRangePayloadEvaluatedOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.SizeRangePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.SizeRangePayload;
import kraken.tracer.Tracer;

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

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
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext,
                                        EvaluationSession session) {
        final SizeRangePayload rangePayload = (SizeRangePayload) payload;
        Object value = evaluator.evaluateGetProperty(resolveTargetPath(rule, dataContext), dataContext.getDataObject());
        List<String> templateVariables = evaluator.evaluateTemplateVariables(rangePayload.getErrorMessage(),
            dataContext, session);

        boolean success = true;

        if (value == null) {
            value = Collections.emptyList();
        }

        if (value instanceof Collection) {
            final int size = ((Collection<?>) value).size();
            success = size >= rangePayload.getMin() && size <= rangePayload.getMax();
        }

        Tracer.doOperation(new SizeRangePayloadEvaluatedOperation(rangePayload, value, success));
        return new SizeRangePayloadResult(success, rangePayload, templateVariables);
    }

}
