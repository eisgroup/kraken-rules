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

export type JestResult = { message: () => string, pass: boolean };

declare global {
    namespace jest {
        interface Matchers<R> {
            /**
             * Applicable for {@link EntryPointResult}             * 
             * @param num   number of failures 
             */
            k_toHaveExpressionsFailures(num?: number): void;
            /**
             * Makes snapshots of raw results, reduced results with
             * FieldMetadataReducer and ValidationStatusReducer 
             */
            k_toMatchResultsSnapshots(): void;
            /**
             * Matches rule results according to provided parameters.
             * @param stats     statistics results should match
             */
            k_toMatchResultsStats(stats: {
                total: number,
                critical?: number,
                warning?: number,
                info?: number,
                disabled?: number,
                hidden?: number
            }): void;
            k_toBeDate(argument: Date): void;
            k_toBeDateTime(argument: Date): void;
            k_toBeTodayDate(): void;
            /**
             * Applicable for {@link RuleEvaluationResult}
             */
            k_toBeValidRuleResult(): void;
        }
    }
}