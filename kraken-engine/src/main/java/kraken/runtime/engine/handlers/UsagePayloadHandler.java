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
import kraken.model.validation.UsageType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueValidationOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.UsagePayload;
import kraken.tracer.Tracer;

/**
 * Payload handler implementation for {@link UsagePayload}s
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class UsagePayloadHandler implements RulePayloadHandler {

    private KrakenExpressionEvaluator evaluator;

    public UsagePayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        var payload = (UsagePayload) rule.getPayload();

        var value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        Tracer.doOperation(new FieldValueValidationOperation(value));
        boolean valid = isMandatoryAndNotNull(value, payload) || isEmptyAndNull(value, payload);

        var templateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), dataContext, session);
        return new UsagePayloadResult(valid, payload, templateVariables);
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.USAGE;
    }

    private boolean isMandatoryAndNotNull(Object value, UsagePayload payload) {
        return UsageType.mandatory.equals(payload.getUsageType())
                && !PayloadHandlerUtils.isEmptyValue(value);
    }

    private boolean isEmptyAndNull(Object value, UsagePayload payload) {
        return UsageType.mustBeEmpty.equals(payload.getUsageType())
                && PayloadHandlerUtils.isEmptyValue(value);
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (UsagePayloadResult) payloadResult;
        switch (result.getUsageType()) {
            case mandatory:
                return result.getSuccess()
                    ? "Field is valid. Field is mandatory and it has a value."
                    : "Field is not valid. Field is mandatory but it has no value.";
            case mustBeEmpty:
                return result.getSuccess()
                    ? "Field is valid. Field must be empty and it has no value."
                    : "Field is not valid. Field must be empty but it has a value.";
            default:
                throw new IllegalArgumentException("Unsupported usage type: " + result.getUsageType());
        }
    }
}
