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

import { ContextFieldInfo, FieldEvaluationResult, RuleEvaluationResults } from 'kraken-engine-api'
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult

export class FieldEvaluationResultImpl implements FieldEvaluationResult {
    /**
     * @param {ContextFieldInfo} contextFieldInfo    Context data instance, on which rules were evaluated
     * @param {RuleEvaluationResultImpl} ruleResults List of {@link RuleEvaluationResult}s, containing entry for each
     *                                               evaluated rule
     */
    constructor(
        public readonly contextFieldInfo: ContextFieldInfo,
        public readonly ruleResults: RuleEvaluationResult[],
    ) {}
}

export class FieldEvaluationResultUtils {
    static targetId(r: FieldEvaluationResultImpl): string {
        return `${r.contextFieldInfo.contextName}:${r.contextFieldInfo.contextId}:${r.contextFieldInfo.fieldPath}`
    }
}
