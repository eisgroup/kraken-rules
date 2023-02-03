/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.conditions;

import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.evaluation.loop.RuleEvaluationInstance;
import kraken.runtime.expressions.KrakenExpressionEvaluationException;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.rule.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Function;

/**
 * Default implementation for {@link RuleApplicabilityEvaluator}. Evaluates rule condition expression to
 * boolean result, and uses latter to determine rule applicability status
 *
 * @author rimas
 * @since 1.0
 */
public class RuleApplicabilityEvaluatorImpl implements RuleApplicabilityEvaluator {

    private Logger logger = LoggerFactory.getLogger(RuleApplicabilityEvaluatorImpl.class);

    private KrakenExpressionEvaluator evaluator;

    public RuleApplicabilityEvaluatorImpl(KrakenExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    @Override
    public ConditionEvaluationResult evaluateCondition(RuleEvaluationInstance evaluation, EvaluationSession session) {
        return Optional.ofNullable(evaluation.getRule().getCondition())
                .map(Condition::getExpression)
                .map(toConditionEvaluationResult(evaluation, session))
                .orElse(new ConditionEvaluationResult(ConditionEvaluation.APPLICABLE));
    }

    private Function<CompiledExpression, ConditionEvaluationResult> toConditionEvaluationResult(
        RuleEvaluationInstance evaluation,
        EvaluationSession session
    ) {
        return expression -> {
            ConditionEvaluationResult conditionEvaluationResult;

            try {
                Object result = evaluator.evaluate(expression, evaluation.getDataContext(), session);
                conditionEvaluationResult = new ConditionEvaluationResult(Boolean.TRUE.equals(result)
                    ? ConditionEvaluation.APPLICABLE
                    : ConditionEvaluation.NOT_APPLICABLE
                );
            } catch (KrakenExpressionEvaluationException e) {
                logger.debug(
                    "Condition expression '{}' failed with exception, \n{}",
                    expression.getExpressionString(),
                    e);

                conditionEvaluationResult = new ConditionEvaluationResult(e);
            }

            return conditionEvaluationResult;
        };
    }

}
