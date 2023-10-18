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

import { EntryPointResult, FieldEvaluationResult, RuleEvaluationResults } from 'kraken-engine-api'
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult

import { Reducer } from 'declarative-js'
import flat = Reducer.flat

export class MockEntryPointResult implements EntryPointResult {
    readonly results: Record<string, FieldEvaluationResult>
    readonly ruleResults: RuleEvaluationResult[]
    readonly evaluationTimestamp: Date
    readonly ruleTimezoneId: string

    constructor(results: Record<string, FieldEvaluationResult>) {
        this.results = results
        this.ruleResults = Object.values(this.results)
            .map(x => x.ruleResults)
            .reduce(flat, [])
        this.evaluationTimestamp = new Date()
        this.ruleTimezoneId = 'Europe/Vilnius'
    }

    getAllRuleResults(): RuleEvaluationResult[] {
        return this.ruleResults
    }

    getFieldResults(): Record<string, FieldEvaluationResult> {
        return this.results
    }

    getApplicableResults(): RuleEvaluationResult[] {
        return this.ruleResults
    }
}
