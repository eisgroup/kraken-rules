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

import { kraken } from '../KrakenMatcher.utils'
import { ErrorAwarePayloadResult, RuleEvaluationResults, ValidationPayloadResult } from 'kraken-engine-api'
import CustomMatcherResult = jest.CustomMatcherResult
import ApplicableRuleEvaluationResult = RuleEvaluationResults.ApplicableRuleEvaluationResult

import success = kraken.matchers.status.success
import error = kraken.matchers.status.error
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult
import Kind = RuleEvaluationResults.Kind

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            /**
             * A matcher to use to check whether rule was evaluated successfully.
             */
            k_toBeValidRuleResult: T extends RuleEvaluationResult
                ? () => R
                : 'Type error: Expected should be of type RuleEvaluationResult'
        }
    }
}

export function k_toBeValidRuleResult<T extends RuleEvaluationResult>(
    this: jest.MatcherContext,
    received: T,
): CustomMatcherResult {
    switch (received.kind) {
        case Kind.REGULAR:
        case Kind.VALIDATION: {
            const appRuleResult = received as ApplicableRuleEvaluationResult

            const evalError = (appRuleResult.payloadResult as ErrorAwarePayloadResult).error

            if (evalError !== undefined) {
                return error(
                    '.k_toBeValidRuleResult',
                    `Payload having error: ${JSON.stringify(evalError)}.`,
                    'Payload to have no errors.',
                    {
                        isNot: this.isNot,
                        promise: this.promise,
                    },
                )
            }

            const evalStatus = (appRuleResult.payloadResult as ValidationPayloadResult).success

            if (evalStatus !== undefined && !evalStatus) {
                return error(
                    '.k_toBeValidRuleResult',
                    `Payload ${JSON.stringify(received)} not evaluating to success.`,
                    'Payload to be evaluating to success.',
                    {
                        isNot: this.isNot,
                        promise: this.promise,
                    },
                )
            }

            return success('.k_toBeValidRuleResult', received, 'to evaluate to success', {
                isNot: this.isNot,
                promise: this.promise,
            })
        }
        case RuleEvaluationResults.Kind.NOT_APPLICABLE:
            return error(
                '.k_toBeValidRuleResult',
                `Payload ${JSON.stringify(received)} which is not applicable due to condition evaluation result.`,
                'Payload to be evaluating to success.',
                {
                    isNot: this.isNot,
                    promise: this.promise,
                },
            )

        default:
            return error(
                '.k_toBeValidRuleResult',
                `Payload of type ${received.kind}.`,
                `Payload of type ${Kind.VALIDATION} or ${Kind.REGULAR} or ${Kind.NOT_APPLICABLE}.`,
                {
                    isNot: this.isNot,
                    promise: this.promise,
                },
            )
    }
}
