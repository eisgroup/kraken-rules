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
import CustomMatcherResult = jest.CustomMatcherResult

import success = kraken.matchers.status.success
import error = kraken.matchers.status.error

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            /**
             * A matcher to use to check whether to date time objects are equal
             */
            k_toBeDateTimeEqualTo: T extends Date
                ? (expected: Date) => R
                : 'Type error: Expected should be of type Date'
        }
    }
}

export function k_toBeDateTimeEqualTo(this: jest.MatcherContext, received: Date, expected: Date): CustomMatcherResult {
    const isMatching = received.getTime() === expected.getTime()

    if (!isMatching) {
        return error('.k_toBeDateTimeEqualTo', received, expected, {
            isNot: this.isNot,
            promise: this.promise,
        })
    }

    return success('.k_toBeDateTimeEqualTo', received, expected, {
        isNot: this.isNot,
        promise: this.promise,
    })
}
