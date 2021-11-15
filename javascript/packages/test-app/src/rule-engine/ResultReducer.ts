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

import { EntryPointResult, RuleEvaluationResults } from "kraken-typescript-engine";
import { FieldEvaluationResult } from "kraken-typescript-engine";
import { ValidationStatus, ResultErrorMessage, ValidationStatusReducer } from "./ValidationStatusReducer";
import { JMap } from "declarative-js";

export interface ReducedResults {
    allRuleResults: RuleEvaluationResults.RuleEvaluationResult[];
    fieldResults: { [keyof: string]: FieldEvaluationResult }[];
    validationStatus: ValidationStatus;
}

export const reduceResult = (entryPointResult: EntryPointResult): ReducedResults => {
    const allRuleResults = entryPointResult.getAllRuleResults()
        .sort((first, second) => {
            if (first.ruleInfo.ruleName === second.ruleInfo.ruleName) {
                return 0;
            }
            return first.ruleInfo.ruleName < second.ruleInfo.ruleName ? -1 : 1;
        });

    const fieldResults = new JMap<FieldEvaluationResult>();
    Object.keys(entryPointResult.getFieldResults())
        .sort((first, second) => first < second ? -1 : 1)
        .forEach(key => fieldResults.put(key, entryPointResult.getFieldResults()[key]));

    const fieldResultsWithoutExternalReferences = fieldResults.values();

    const byKrakenInfo = (first: ResultErrorMessage, second: ResultErrorMessage) => {
        if (first.ruleName === second.ruleName
            && first.info.getContextInstanceId === second.info.getContextInstanceId
        ) {
            return 0;
        }
        return first.ruleName > second.ruleName
            ? first.info.getContextInstanceId < second.info.getContextInstanceId ? -1 : 1
            : first.info.getContextInstanceId > second.info.getContextInstanceId ? 1 : -1;
    };

    const sortValidationStatus = (validationStatus: ValidationStatus) => {
        validationStatus.critical.sort(byKrakenInfo);
        validationStatus.warning.sort(byKrakenInfo);
        validationStatus.info.sort(byKrakenInfo);
        return validationStatus;
    };

    return {
        allRuleResults: allRuleResults,
        fieldResults: fieldResultsWithoutExternalReferences as any,
        validationStatus: sortValidationStatus(new ValidationStatusReducer().reduce(entryPointResult))
    };
};
