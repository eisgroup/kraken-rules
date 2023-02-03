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
import kraken.runtime.engine.handlers.trace.AssertionPayloadEvaluatedOperation;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.expressions.KrakenExpressionEvaluationException;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.tracer.Tracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public PayloadResult executePayload(Payload payload, RuntimeRule rule, DataContext dataContext, EvaluationSession session) {
        AssertionPayload assertionPayload = (AssertionPayload) payload;
        try {
            Object result = evaluator.evaluate(assertionPayload.getAssertionExpression(), dataContext, session);
            List<String> templateVariables
                = evaluator.evaluateTemplateVariables(assertionPayload.getErrorMessage(), dataContext, session);

            Tracer.doOperation(new AssertionPayloadEvaluatedOperation(assertionPayload, result));
            return new AssertionPayloadResult(Boolean.TRUE.equals(result), assertionPayload, templateVariables);
        } catch (KrakenExpressionEvaluationException e) {
            logger.debug(
                    "Assertion expression '{}' in rule '{}' failed with exception: \n {}",
                    ((AssertionPayload) payload).getAssertionExpression().getExpressionString(),
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

}
