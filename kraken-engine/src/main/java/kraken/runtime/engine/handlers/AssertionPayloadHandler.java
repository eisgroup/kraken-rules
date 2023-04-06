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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.handlers.trace.AssertionExpressionEvaluationOperation;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluationException;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.tracer.Tracer;

/**
 * Payload handler implementation to process {@link AssertionPayload}s
 *
 * @author rimas
 * @since 1.0
 */
public class AssertionPayloadHandler implements RulePayloadHandler {

    private static final Logger logger = LoggerFactory.getLogger(AssertionPayloadHandler.class);

    private final KrakenExpressionEvaluator evaluator;

    public AssertionPayloadHandler(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public PayloadResult executePayload(RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        AssertionPayload assertionPayload = (AssertionPayload) rule.getPayload();
        try {
            Tracer.doOperation(
                new AssertionExpressionEvaluationOperation(assertionPayload.getAssertionExpression(), dataContext)
            );
            Object result = evaluator.evaluate(assertionPayload.getAssertionExpression(), dataContext, session);
            List<String> templateVariables
                = evaluator.evaluateTemplateVariables(assertionPayload.getErrorMessage(), dataContext, session);

            return new AssertionPayloadResult(Boolean.TRUE.equals(result), assertionPayload, templateVariables);
        } catch (KrakenExpressionEvaluationException e) {
            logger.debug(
                "Assertion expression '{}' in rule '{}' failed with exception: \n {}",
                assertionPayload.getAssertionExpression().getExpressionString(),
                rule.getName(),
                e
            );

            return new AssertionPayloadResult(e, assertionPayload);
        }
    }

    @Override
    public PayloadType handlesPayloadType() {
        return PayloadType.ASSERTION;
    }

    @Override
    public String describePayloadResult(PayloadResult payloadResult) {
        var result = (AssertionPayloadResult) payloadResult;

        if(result.getException().isPresent()) {
            return "Field is valid. Assertion is not evaluated due to expression error.";
        }
        return result.getSuccess()
            ? "Field is valid. Assertion evaluated to true."
            : "Field is not valid. Assertion evaluated to false.";
    }
}
