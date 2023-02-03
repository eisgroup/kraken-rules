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

import kraken.annotations.API;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.result.ExceptionAwarePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * Result of single {@link RuntimeRule} evaluation for particular context instance
 *
 * @author rimas
 * @since 1.0
 */
@API
public class RuleEvaluationResult<T extends PayloadResult> {

    private RuleInfo ruleInfo;

    private T payloadResult;

    private ConditionEvaluationResult conditionEvaluationResult;

    private OverrideInfo overrideInfo;

    private RuleEvaluationStatus ruleEvaluationStatus;

    public RuleEvaluationResult(RuleInfo ruleInfo,
                                T payloadResult,
                                ConditionEvaluationResult conditionEvaluationResult,
                                OverrideInfo overrideInfo) {
        this.ruleInfo = ruleInfo;
        this.payloadResult = payloadResult;
        this.conditionEvaluationResult = conditionEvaluationResult;
        this.overrideInfo = overrideInfo;

        if (conditionEvaluationResult.getError() != null || payloadResult instanceof ExceptionAwarePayloadResult
            && ((ExceptionAwarePayloadResult) payloadResult).getException().isPresent()) {
            this.ruleEvaluationStatus = RuleEvaluationStatus.IGNORED;
        } else if (!conditionEvaluationResult.isApplicable()) {
            this.ruleEvaluationStatus = RuleEvaluationStatus.SKIPPED;
        } else {
            this.ruleEvaluationStatus = RuleEvaluationStatus.APPLIED;
        }
    }

    public ConditionEvaluationResult getConditionEvaluationResult() {
        return conditionEvaluationResult;
    }

    /**
     *
     * @return result of rule logic execution
     */
    public T getPayloadResult() {
        return payloadResult;
    }

    /**
     *
     * @return information about the rule which was executed
     */
    public RuleInfo getRuleInfo() {
        return ruleInfo;
    }

    /**
     *
     * @return  additional override specific information.
     */
    public OverrideInfo getOverrideInfo() {
        return overrideInfo;
    }

    public RuleEvaluationStatus getRuleEvaluationStatus() {
        return ruleEvaluationStatus;
    }

}
