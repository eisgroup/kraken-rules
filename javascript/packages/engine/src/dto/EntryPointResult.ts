/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { FieldEvaluationResult } from "./FieldEvaluationResult";
import { RuleEvaluationResults } from "./RuleEvaluationResults";
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult;

import { Reducer } from "declarative-js";
import flat = Reducer.flat;
import { ConditionEvaluationResult } from "./ConditionEvaluationResult";

/**
 * Contains evaluation results from specific entry point
 */
export class EntryPointResult {
    private readonly results: Record<string, FieldEvaluationResult>;
    readonly evaluationTimestamp: Date;
    constructor(results: Record<string, FieldEvaluationResult>) {
        this.results = results;
        this.getApplicableResults.bind(this);
        this.evaluationTimestamp = new Date();
    }
    /**
     * Flatten contents into single list of {@link RuleEvaluationResult}s
     */
    getAllRuleResults(): RuleEvaluationResult[] {
        return Object.values(this.results)
            .map(x => x.ruleResults)
            .reduce(flat, []);
    }
    getFieldResults(): Record<string, FieldEvaluationResult> {
        return this.results;
    }

    getApplicableResults(): RuleEvaluationResult[] {
        return this.getAllRuleResults()
            .filter(r => ConditionEvaluationResult.isApplicable(r.conditionEvaluationResult));
    }

    static empty(): EntryPointResult {
        return new EntryPointResult({});
    }
}
