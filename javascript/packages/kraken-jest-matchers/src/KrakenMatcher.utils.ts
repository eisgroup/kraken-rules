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

import { getLabelPrinter, matcherHint, printExpected, printReceived } from 'jest-matcher-utils'
import MatcherHintOptions = jest.MatcherHintOptions
import CustomMatcherResult = jest.CustomMatcherResult

export namespace kraken {
    export namespace matchers {
        export namespace status {
            const labelPrinter = getLabelPrinter('Received', 'Expected')

            export const success = (
                matcherName: string,
                received: unknown,
                expected?: unknown,
                options?: MatcherHintOptions,
            ): CustomMatcherResult => {
                return {
                    pass: true,
                    message: () =>
                        matcherHint(matcherName, 'received', 'expected', options) +
                        '\n\n' +
                        labelPrinter('Received') +
                        printReceived(received) +
                        '\n' +
                        labelPrinter('Expected') +
                        printExpected(expected),
                }
            }

            export const error = (
                matcherName: string,
                received: unknown,
                expected?: unknown,
                options?: MatcherHintOptions,
            ): CustomMatcherResult => {
                return {
                    pass: false,
                    message: () =>
                        matcherHint(matcherName, 'received', 'expected', options) +
                        '\n\n' +
                        labelPrinter('Expected') +
                        printExpected(expected) +
                        '\n' +
                        labelPrinter('Received') +
                        printReceived(received),
                }
            }

            export const errorMessage = (
                matcherName: string,
                received: string,
                expected?: string,
                options?: MatcherHintOptions,
            ): CustomMatcherResult => {
                return {
                    pass: false,
                    message: () =>
                        matcherHint(matcherName, 'received', 'expected', options) +
                        '\n\n' +
                        labelPrinter('Expected') +
                        printExpected(expected) +
                        '\n' +
                        labelPrinter('Received') +
                        printReceived(received) +
                        '',
                }
            }
        }
    }
}
