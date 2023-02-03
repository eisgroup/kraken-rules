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

import { EntryPointResult, ErrorAwarePayloadResult, RuleEvaluationResults } from 'kraken-engine-api'
import CustomMatcherResult = jest.CustomMatcherResult
import { kraken } from '../KrakenMatcher.utils'

import ApplicableRuleEvaluationResult = RuleEvaluationResults.ApplicableRuleEvaluationResult
import success = kraken.matchers.status.success
import error = kraken.matchers.status.error

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            /**
             * A matcher to use to check whether an entry point result has expression failures (errors)
             * in either condition or payload evaluation result
             */
            k_toHaveExpressionsFailures: T extends EntryPointResult
                ? (expectedFailures?: number) => R
                : 'Type error: Expected should be of type EntryPointResult'
        }
    }
}

export function k_toHaveExpressionsFailures<T extends EntryPointResult>(
    this: jest.MatcherContext,
    received: T,
    expectedFailures?: number,
): CustomMatcherResult {
    const failedConditions = received.getAllRuleResults().filter(value => !!value.conditionEvaluationResult.error)

    const failedPayloads = received
        .getAllRuleResults()
        .filter(value => !value.conditionEvaluationResult.error)
        .filter(value => !!(value as ApplicableRuleEvaluationResult).payloadResult)
        .filter(value => !!((value as ApplicableRuleEvaluationResult).payloadResult as ErrorAwarePayloadResult).error)

    const numberOfFailures = failedConditions.length + failedPayloads.length
    const condition = expectedFailures !== undefined ? numberOfFailures === expectedFailures : numberOfFailures > 0

    if (!condition) {
        return error(
            '.k_toHaveExpressionsFailures',
            `Total of ${numberOfFailures} failure(s): ${
                failedConditions.length
                    ? '\n Failed condition expressions ' + JSON.stringify(failedConditions, undefined, 2)
                    : []
            }, ${
                failedPayloads.length
                    ? '\n Failed payload expressions ' + JSON.stringify(failedPayloads, undefined, 2)
                    : []
            }`,
            `To have ${expectedFailures} failure(s).`,
            {
                isNot: this.isNot,
                promise: this.promise,
            },
        )
    }

    return success(
        '.k_toHaveExpressionsFailures',
        `Total of ${numberOfFailures} failure(s): ${
            failedConditions.length
                ? '\n Failed condition expressions ' + JSON.stringify(failedConditions, undefined, 2)
                : []
        }, ${
            failedPayloads.length ? '\n Failed payload expressions ' + JSON.stringify(failedPayloads, undefined, 2) : []
        }`,
        `Expected expressions to be evaluated without failure(s).`,
        {
            isNot: this.isNot,
            promise: this.promise,
        },
    )
}
