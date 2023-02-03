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

import {
    AccessibilityPayloadResult,
    EntryPointResult,
    RuleEvaluationResults,
    VisibilityPayloadResult,
} from 'kraken-engine-api'
import { kraken } from '../KrakenMatcher.utils'
import error = kraken.matchers.status.error
import success = kraken.matchers.status.success
import CustomMatcherResult = jest.CustomMatcherResult
import ApplicableRuleEvaluationResult = RuleEvaluationResults.ApplicableRuleEvaluationResult
import { EntryPointResultUtils, ValidationStatus } from './EntryPointResultUtils'

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            /**
             * A matcher to use to check whether total applicable rule results matches
             * given expected value.
             */
            k_toMatchResultsStats: T extends EntryPointResult
                ? (expected: ExpectedResult) => R
                : 'Type error: received should be of type EntryPointResult'
        }
    }
}

export interface ExpectedResult {
    total?: number
    disabled?: number
    hidden?: number
    critical?: number
    warning?: number
    info?: number
}

export function k_toMatchResultsStats<T extends EntryPointResult>(
    this: jest.MatcherContext,
    received: T,
    expected: ExpectedResult,
): CustomMatcherResult {
    if (this.isNot) {
        error(
            '.k_toMatchResultsStats',
            'Matcher k_toMatchResultsStats is negated.',
            "Matcher k_toMatchResultsStats is not negated. This matcher cannot be used with '.not' condition.",
            {
                isNot: this.isNot,
                promise: this.promise,
            },
        )
    }

    const asserResult = assertExpectations(received, expected)

    if (asserResult.hasFailures) {
        return error('.k_toMatchResultsStats', asserResult.received, asserResult.expected, {
            isNot: this.isNot,
            promise: this.promise,
        })
    }

    return success('.k_toMatchResultsStats', asserResult.received, asserResult.expected, {
        isNot: this.isNot,
        promise: this.promise,
    })
}

interface AssertResult {
    failed: boolean
    failureMessage: string
    successMessage: string
}

interface AssertionResult {
    hasFailures: boolean
    expected: string
    received: string
}

function assertExpectations<T extends EntryPointResult>(result: T, expected: ExpectedResult): AssertionResult {
    const validationStatus = EntryPointResultUtils.reduce(result)
    const results = [
        assertExpected('Total', result, expected.total, getActualCount),
        assertExpected('Hidden', result, expected.hidden, getHiddenCount),
        assertExpected('Disabled', result, expected.disabled, getDisabledCount),
        assertExpected('Info', validationStatus, expected.info, getInfoCount),
        assertExpected('Warning', validationStatus, expected.warning, getWarningCount),
        assertExpected('Critical', validationStatus, expected.critical, getCriticalCount),
    ]

    let hasFailures = false
    let expectedMessage = ''
    let receivedMessage = ''

    results.forEach(value => {
        if (value !== undefined) {
            if (!hasFailures) {
                hasFailures = value.failed
            }

            expectedMessage = expectedMessage.concat(value.successMessage)
            receivedMessage = receivedMessage.concat(value.failed ? value.failureMessage : value.successMessage)
        }
    })

    return {
        hasFailures,
        expected: expectedMessage,
        received: receivedMessage,
    }
}

function assertExpected<T extends EntryPointResult | ValidationStatus>(
    hint: string,
    result: T,
    expected: number | undefined,
    actualExtractor: (result: T) => number,
): AssertResult | undefined {
    if (expected === undefined) {
        return undefined
    }

    const actualCount = actualExtractor(result)

    return {
        failed: expected !== actualCount,
        failureMessage: `${hint} actual ${actualCount}, expected ${expected}. `,
        successMessage: `${hint} actual is equal to expected. `,
    }
}

function getActualCount(result: EntryPointResult): number {
    return result.getApplicableResults().length
}

function getHiddenCount(result: EntryPointResult): number {
    return result
        .getApplicableResults()
        .map(value => ((value as ApplicableRuleEvaluationResult).payloadResult as VisibilityPayloadResult).visible)
        .filter(value => value !== undefined && !value).length
}

function getDisabledCount(result: EntryPointResult): number {
    return result
        .getApplicableResults()
        .map(
            value => ((value as ApplicableRuleEvaluationResult).payloadResult as AccessibilityPayloadResult).accessible,
        )
        .filter(value => value !== undefined && !value).length
}

function getCriticalCount(result: ValidationStatus): number {
    return result.critical.length
}

function getWarningCount(result: ValidationStatus): number {
    return result.warning.length
}

function getInfoCount(result: ValidationStatus): number {
    return result.info.length
}
