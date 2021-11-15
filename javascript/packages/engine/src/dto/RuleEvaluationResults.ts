/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { PayloadResult, ValidationPayloadResult } from "../engine/results/PayloadResult";
import { ConditionEvaluationResult } from "./ConditionEvaluationResult";
import { RuleInfo } from "../engine/results/RuleInfo";
import { RuleOverride } from "..";

export namespace RuleEvaluationResults {

    export enum Kind {
        REGULAR = 1,
        VALIDATION = 2,
        NOT_APPLICABLE = 4
    }

    interface BaseValidationResult {
        kind: Kind;
        ruleInfo: RuleInfo;
        conditionEvaluationResult: ConditionEvaluationResult;
    }

    export interface NotApplicableRuleEvaluationResult extends BaseValidationResult {
        kind: Kind.NOT_APPLICABLE;
    }

    export interface RegularRuleEvaluationResult extends BaseValidationResult {
        kind: Kind.REGULAR;
        payloadResult: PayloadResult;
    }

    export interface ValidationRuleEvaluationResult extends BaseValidationResult {
        kind: Kind.VALIDATION;
        payloadResult: ValidationPayloadResult;
        overrideInfo: OverrideInfo;
    }

    export type RuleEvaluationResult = ValidationRuleEvaluationResult
        | RegularRuleEvaluationResult
        | NotApplicableRuleEvaluationResult;

    export type ApplicableRuleEvaluationResult = ValidationRuleEvaluationResult
        | RegularRuleEvaluationResult;

    export function isValidation(r: RuleEvaluationResult): r is ValidationRuleEvaluationResult {
        return Boolean(r.kind & Kind.VALIDATION);
    }

    export function isNotApplicable(r: RuleEvaluationResult): r is NotApplicableRuleEvaluationResult {
        return Boolean(r.kind & Kind.NOT_APPLICABLE);
    }

    export interface OverridePresentInfo {
        /**
         * when rule is failed and rule is overridable
         */
        overrideApplicable: true;
        overridable: true;
        overrideGroup?: string;
        overrideContext: RuleOverride.OverridableRuleContextInfo;
    }

    export interface OverrideNotPresentInfo {
        overrideApplicable: false;
        overridable: boolean;
        overrideGroup?: string;
    }

    export type OverrideInfo = OverridePresentInfo | OverrideNotPresentInfo;

    export const overrideInfoTypeChecker = {
        isOverrideApplicable(p: OverrideInfo): p is OverridePresentInfo {
            return p.overrideApplicable;
        }
    };
}
