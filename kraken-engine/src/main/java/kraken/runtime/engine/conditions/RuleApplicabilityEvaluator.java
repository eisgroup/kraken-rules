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
package kraken.runtime.engine.conditions;

import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.evaluation.loop.RuleEvaluationInstance;

import java.util.Map;

/**
 * Used to determine rule is applicable on particular data context instance
 *
 * @author rimas
 * @since 1.0
 */
public interface RuleApplicabilityEvaluator {

    /**
     * Determine if rule provided as part expressionOf supplied {@link RuleEvaluationInstance}, is applicable
     * on provided data context object, also supplied as part expressionOf rule evaluation
     *
     * @return  Predicate from {@link RuleEvaluationInstance} that contains rule and data context instance
     * returns true is rule is applicable, false otherwise
     */
    ConditionEvaluationResult evaluateCondition(RuleEvaluationInstance ruleEvaluationInstance, EvaluationSession session);

}
