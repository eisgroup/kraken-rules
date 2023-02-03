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

import { EntryPointResult, FieldMetadata } from 'kraken-engine-api'
import { toMatchSnapshot } from 'jest-snapshot'

declare global {
    namespace jest {
        // eslint-disable-next-line @typescript-eslint/ban-types
        interface Matchers<R, T = {}> {
            /**
             * A matcher to use to check match entry point snapshots.
             */
            k_toMatchResultsSnapshots: T extends MatchableResults
                ? () => R
                : 'Type error: Expected should be of type EntryPointResult'
        }
    }
}

export interface MatchableResults {
    entryPointResults: EntryPointResult
    fieldResults: Record<string, FieldMetadata>
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function k_toMatchResultsSnapshots<T extends MatchableResults>(this: any, received: T): any {
    return toMatchSnapshot.call(this, received, {
        entryPointResults: {
            evaluationTimestamp: expect.any(Date),
        },
    })
}
