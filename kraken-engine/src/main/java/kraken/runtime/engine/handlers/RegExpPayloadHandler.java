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

import static kraken.runtime.engine.handlers.PayloadHandlerUtils.convertToString;
import static kraken.runtime.engine.handlers.PayloadHandlerUtils.isEmptyValue;

import java.util.List;
import java.util.regex.Pattern;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.FieldValueValidationOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.RegExpPayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.RegExpPayload;
import kraken.tracer.Tracer;

/**
 * Payload handler implementation to process {@link RegExpPayload}s
 *
 * @author rimas
 * @since 1.0
 */
public class RegExpPayloadHandler implements RulePayloadHandler {

    private final KrakenExpressionEvaluator evaluator;

    public RegExpPayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        var payload = (RegExpPayload) rule.getPayload();

        var value = evaluator.evaluateTargetField(rule.getTargetPath(), dataContext);
        Tracer.doOperation(new FieldValueValidationOperation(value));
        boolean valid = isEmptyValue(value) || Pattern.matches(payload.getRegExp(), convertToString(value));

        var templateVariables = evaluator.evaluateTemplateVariables(payload.getErrorMessage(), dataContext, session);
        return new RegExpPayloadResult(valid, payload, templateVariables);
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.REGEX;
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (RegExpPayloadResult) payloadResult;
        return result.getSuccess()
            ? String.format("Field is valid. String value matches regular expression %s.", result.getRegExp())
            : String.format("Field is not valid. String value does not match regular expression %s.", result.getRegExp());
    }
}
