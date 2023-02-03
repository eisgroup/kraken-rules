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

import error = kraken.matchers.status.error
import success = kraken.matchers.status.success

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            /**
             * A matcher to use to check whether expected date is today
             */
            k_toBeTodayDate: T extends Date | undefined ? () => R : 'Type error: Expected should be of type Date'
        }
    }
}

export function k_toBeTodayDate<T extends Date | undefined>(
    this: jest.MatcherContext,
    received: T,
): CustomMatcherResult {
    const expected = new Date()

    if (received === undefined) {
        return error('.k_toBeTodayDate', received, expected, {
            isNot: this.isNot,
            promise: this.promise,
        })
    }

    const pass =
        received.getFullYear() === expected.getFullYear() &&
        received.getMonth() === expected.getMonth() &&
        received.getDate() === expected.getDate()

    if (!pass) {
        return error('.k_toBeTodayDate', received, expected, {
            isNot: this.isNot,
            promise: this.promise,
        })
    }

    return success('.k_toBeTodayDate', received, expected, {
        isNot: this.isNot,
        promise: this.promise,
    })
}
