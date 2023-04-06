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

import java.util.List;

import javax.money.MonetaryAmount;

import kraken.el.functionregistry.functions.MoneyFunctions;
import kraken.el.math.Numbers;
import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueValidationOperation;
import kraken.runtime.engine.result.NumberSetPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
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
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        var payload = (NumberSetPayload) rule.getPayload();

        var value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        boolean success = true;
        if(value instanceof MonetaryAmount) {
            value = MoneyFunctions.fromMoney((MonetaryAmount) value);
        }
        if(value instanceof Number) {
            Tracer.doOperation(new FieldValueValidationOperation(value));
            success = Numbers.isValueInNumberSet((Number) value, payload.getMin(), payload.getMax(), payload.getStep());
        }

        var templateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), dataContext, session);
        return new NumberSetPayloadResult(success, payload, templateVariables);
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (NumberSetPayloadResult) payloadResult;
        return result.getSuccess()
            ? String.format("Field is valid. Field value is in %s.", describeExpectedNumberSet(result))
            : String.format("Field is not valid. Field value is not in %s.", describeExpectedNumberSet(result));
    }

    private String describeExpectedNumberSet(NumberSetPayloadResult payloadResult) {
        return String.format(
            "number set [%s, %s]%s",
            payloadResult.getMin() != null ? payloadResult.getMin() : "-∞",
            payloadResult.getMax() != null ? payloadResult.getMax() : "∞",
            payloadResult.getStep() != null ? " with step " + payloadResult.getStep() : ""
        );
    }
}
