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

import { FieldEvaluationResult } from './FieldEvaluationResult'
import { RuleEvaluationResults } from './RuleEvaluationResults'
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult

/**
 * Contains evaluation results from specific entry point
 */
export interface EntryPointResult {
    readonly evaluationTimestamp: Date
    readonly ruleTimeZoneId: string

    /**
     * Returns entry point result mapped by field name.
     */
    getFieldResults(): Record<string, FieldEvaluationResult>

    /**
     * Flattens and returns field results into an array of {@link RuleEvaluationResult}
     */
    getAllRuleResults(): RuleEvaluationResult[]

    /**
     * Flattens and returns field results into an array of {@link RuleEvaluationResult}
     * filtered by rule condition evaluation status.
     */
    getApplicableResults(): RuleEvaluationResult[]
}
