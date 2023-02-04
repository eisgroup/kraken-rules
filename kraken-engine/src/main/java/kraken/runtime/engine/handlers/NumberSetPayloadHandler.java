/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

import static kraken.runtime.utils.TargetPathUtils.resolveTargetPath;

import java.util.List;

import javax.money.MonetaryAmount;

import kraken.el.functionregistry.functions.MoneyFunctions;
import kraken.el.math.Numbers;
import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.NumberSetPayloadEvaluatedOperation;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.NumberSetPayload;
import kraken.tracer.Tracer;

/**
 * @author Mindaugas Ulevicius
 */
public class NumberSetPayloadHandler implements RulePayloadHandler {
    private final KrakenExpressionEvaluator evaluator;

    public NumberSetPayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.NUMBER_SET;
    }

    @Override
    public PayloadResult executePayload(Payload p, RuntimeRule rule, DataContext context, EvaluationSession session) {
        var payload = (NumberSetPayload) p;
        var value = evaluator.evaluateGetProperty(resolveTargetPath(rule, context), context.getDataObject());
        boolean success = true;
        if(value instanceof MonetaryAmount) {
            value = MoneyFunctions.fromMoney((MonetaryAmount) value);
        }
        if(value instanceof Number) {
            success = Numbers.isValueInNumberSet((Number) value, payload.getMin(), payload.getMax(), payload.getStep());
        }

        List<String> templateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), context, session);

        Tracer.doOperation(new NumberSetPayloadEvaluatedOperation(payload, value, success));
        return new NumberSetPayloadResult(success, payload, templateVariables);
    }

}
