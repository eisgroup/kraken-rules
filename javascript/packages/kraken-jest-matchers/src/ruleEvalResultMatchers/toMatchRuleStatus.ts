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

import { kraken } from '../KrakenMatcher.utils'
import { ConditionEvaluation, ErrorAwarePayloadResult, RuleEvaluationResults } from 'kraken-engine-api'
import CustomMatcherResult = jest.CustomMatcherResult

import success = kraken.matchers.status.success
import error = kraken.matchers.status.error
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult
import isNotApplicable = RuleEvaluationResults.isNotApplicable

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            k_toBeSkipped: T extends RuleEvaluationResult
                ? () => R
                : 'Type error: Expected should be of type RuleEvaluationResult'

            k_toBeIgnored: T extends RuleEvaluationResult
                ? () => R
                : 'Type error: Expected should be of type RuleEvaluationResult'

            k_toBeApplied: T extends RuleEvaluationResult
                ? () => R
                : 'Type error: Expected should be of type RuleEvaluationResult'
        }
    }
}

export function k_toBeSkipped<T extends RuleEvaluationResult>(
    this: jest.MatcherContext,
    received: T,
): CustomMatcherResult {
    return matchesStatus('SKIPPED', received)
}

export function k_toBeIgnored<T extends RuleEvaluationResult>(
    this: jest.MatcherContext,
    received: T,
): CustomMatcherResult {
    return matchesStatus('IGNORED', received)
}

export function k_toBeApplied<T extends RuleEvaluationResult>(
    this: jest.MatcherContext,
    received: T,
): CustomMatcherResult {
    return matchesStatus('APPLIED', received)
}

function matchesStatus(expectedStatus: EvaluationStatus, result: RuleEvaluationResult): CustomMatcherResult {
    const actualStatus = resolveEvaluationStatus(result)
    if (actualStatus !== expectedStatus) {
        return error('.k_toBeApplied', `${actualStatus}`, `${expectedStatus}`)
    }
    return success('.k_toBeApplied', `${actualStatus}`)
}

type EvaluationStatus = 'SKIPPED' | 'IGNORED' | 'APPLIED'

function resolveEvaluationStatus(result: RuleEvaluationResult): EvaluationStatus {
    if (result.conditionEvaluationResult.conditionEvaluation === ConditionEvaluation.ERROR) {
        return 'IGNORED'
    }
    if (
        result.conditionEvaluationResult.conditionEvaluation === ConditionEvaluation.NOT_APPLICABLE ||
        isNotApplicable(result)
    ) {
        return 'SKIPPED'
    }
    if (result.payloadResult && (result.payloadResult as ErrorAwarePayloadResult).error) {
        return 'IGNORED'
    }
    return 'APPLIED'
}
