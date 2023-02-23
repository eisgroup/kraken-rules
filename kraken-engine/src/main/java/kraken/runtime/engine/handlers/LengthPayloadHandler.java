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

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.LengthPayloadEvaluatedOperation;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import kraken.tracer.Tracer;

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

import java.util.List;

/**
 * Payload handler implementation to process {@link LengthPayload}s
 *
 * @author psurinin
 * @since 1.0
 */
public class LengthPayloadHandler implements RulePayloadHandler {

    private KrakenExpressionEvaluator evaluator;

    public LengthPayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.LENGTH;
    }

    @Override
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        final LengthPayload lengthPayload = (LengthPayload) payload;

        String path = resolveTargetPath(rule, dataContext);
        Object target = evaluator.evaluateGetProperty(path, dataContext.getDataObject());
        int targetLength = target instanceof String ? ((String) target).length() : 0;
        boolean success = targetLength <= lengthPayload.getLength();
        List<String> templateVariables = evaluator.evaluateTemplateVariables(lengthPayload.getErrorMessage(), dataContext, session);

        Tracer.doOperation(new LengthPayloadEvaluatedOperation(lengthPayload, targetLength, success));
        return new LengthPayloadResult(success, lengthPayload, templateVariables);
    }

}
