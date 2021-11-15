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

import kraken.annotations.API;

/**
 * DTO that will be stored in {@link kraken.runtime.engine.EntryPointResult} to each rule evaluation.
 * It will hold info was rule applicable or or not or not applicable, due to failure with exception.
 *
 * @author psurinin
 */
@API
public class ConditionEvaluationResult {

    private ConditionEvaluation conditionEvaluation;

    private Exception exception;

    public static final ConditionEvaluationResult APPLICABLE = new ConditionEvaluationResult(ConditionEvaluation.APPLICABLE);

    public static final ConditionEvaluationResult NOT_APPLICABLE = new ConditionEvaluationResult(ConditionEvaluation.NOT_APPLICABLE);

    public ConditionEvaluationResult(ConditionEvaluation conditionEvaluation) {
        this.conditionEvaluation = conditionEvaluation;
    }

    public ConditionEvaluationResult(Exception exception) {
        this.conditionEvaluation = ConditionEvaluation.ERROR;
        this.exception = exception;
    }

    public boolean isApplicable() {
        return ConditionEvaluation.APPLICABLE.equals(conditionEvaluation);
    }

    public Exception getError() {
        return exception;
    }
}
