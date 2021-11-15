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
package kraken.runtime.engine.dto;

import java.util.List;

import kraken.annotations.API;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * Contains all rule results from rules in a single {@link FieldEvaluationResult}, and applying to the same field
 *
 * @author rimas
 * @since 1.0
 */
@API
public class FieldEvaluationResult {

    private final ContextFieldInfo contextFieldInfo;
    private final List<RuleEvaluationResult> ruleResults;

    public FieldEvaluationResult(
            ContextFieldInfo contextFieldInfo,
            List<RuleEvaluationResult> ruleResults
    ) {
        this.contextFieldInfo = contextFieldInfo;
        this.ruleResults = ruleResults;
    }

    /**
     * Result a collection of evaluation results of {@link RuntimeRule}'s
     * on a field.

     * @see RuleEvaluationResult
     * @return Rule evaluation results.
     */
    public List<RuleEvaluationResult> getRuleResults() {
        return ruleResults;
    }

    /**
     * Returns information about the context on which the rule was executed.
     *
     * @see ContextFieldInfo
     * @return Context field information.
     */
    public ContextFieldInfo getContextFieldInfo() {
        return contextFieldInfo;
    }

    @Override
    public String toString() {
        return "FieldEvaluationResult: " + contextFieldInfo;
    }

}
