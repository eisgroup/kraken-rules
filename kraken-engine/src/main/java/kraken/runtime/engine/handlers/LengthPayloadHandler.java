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

import java.util.List;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueValidationOperation;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import kraken.tracer.Tracer;

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
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        var payload = (LengthPayload) rule.getPayload();

        var value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        Tracer.doOperation(new FieldValueValidationOperation(value));
        int targetLength = value instanceof String ? ((String) value).length() : 0;
        boolean success = targetLength <= payload.getLength();

        var templateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), dataContext, session);
        return new LengthPayloadResult(success, payload, templateVariables);
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (LengthPayloadResult) payloadResult;

        return result.getSuccess()
            ? String.format("Field is valid. String length is not more than %s.", result.getLength())
            : String.format("Field is not valid. String length is more than %s.", result.getLength());
    }
}
