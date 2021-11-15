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

import { Expressions } from "./Expressions";

export namespace Payloads {

    export interface Payload {
        type: PayloadType;
    }

    /**
     * To resolve {@link EvaluationType} by the PayloadType use
     * {@link Payloads.resolveEvaluationType} function
     */
    export enum PayloadType {
        ASSERTION = "ASSERTION",
        DEFAULT = "DEFAULT",
        USAGE = "USAGE",
        REGEX = "REGEX",
        ACCESSIBILITY = "ACCESSIBILITY",
        VISIBILITY = "VISIBILITY",
        LENGTH = "LENGTH",
        SIZE = "SIZE",
        SIZE_RANGE = "SIZE_RANGE"
    }
    /**
     * To resolve {@link EvaluationType} by the PayloadType use
     * {@link Payloads.resolveEvaluationType} function
     */
    export type EvaluationType = "VALIDATION"
        | "DEFAULT"
        | "VISIBILITY"
        | "ACCESSIBILITY";

    export function resolveEvaluationType(pt: PayloadType): EvaluationType {
        switch (pt) {
            case PayloadType.ASSERTION:
            case PayloadType.LENGTH:
            case PayloadType.REGEX:
            case PayloadType.SIZE:
            case PayloadType.SIZE_RANGE:
            case PayloadType.USAGE:
                return "VALIDATION";
            case PayloadType.DEFAULT:
                return "DEFAULT";
            case PayloadType.ACCESSIBILITY:
                return "ACCESSIBILITY";
            case PayloadType.VISIBILITY:
                return "VISIBILITY";
        }
    }

    export namespace UI {
        export interface AccessibilityPayload extends Payload {
            accessible: boolean;
        }

        export interface VisibilityPayload extends Payload {
            visible: boolean;
        }
    }

    export namespace Validation {
        export interface ValidationPayload extends Payload {
            isOverridable?: boolean;
            overrideGroup?: string;
            severity: ValidationSeverity;
            errorMessage?: ErrorMessage;
        }

        /**
         * Usage payload defines validation logic governing field mandatory status.
         * If field value does not comply with {@Link UsageType} validation will fail.
         */
        export interface UsagePayload extends ValidationPayload {
            usageType: UsageType;
        }

        export enum UsageType {
            mandatory = "mandatory",
            mustBeEmpty = "mustBeEmpty"
        }

        /**
         * Allows specifying arbitrary validation logic in form of boolean expression.
         * Expression is ran against context instance - if it yields false, field is considered
         * invalid. Otherwise, field is considered to be valid.
         */
        export interface AssertionPayload extends ValidationPayload {
            assertionExpression: Expressions.Expression;
        }

        /**
         * Validates filed value against specified regular expression
         */
        export interface RegExpPayload extends ValidationPayload {
            regExp: string;
        }

        /**
         * Validates string length
         */
        export interface LengthPayload extends ValidationPayload {
            length: number;
        }

        /**
         * Validates array length
         */
        export interface SizePayload extends ValidationPayload {
            orientation: SizeOrientation;
            size: number;
        }

        /**
         * Validates array length ranges
         */
        export interface SizeRangePayload extends ValidationPayload {
            min: number;
            max: number;
        }

        export interface ErrorMessage {
            templateParts: string[];
            templateExpressions: Expressions.Expression[];
            errorCode: string;
        }

        export enum SizeOrientation {
            MIN = "MIN",
            MAX = "MAX",
            EQUALS = "EQUALS"
        }

        export enum ValidationSeverity {
            critical = "critical",
            warning = "warning",
            info = "info"
        }
    }

    export namespace Derive {
        /**
         * Used to default field to value generated using provided value expression
         */
        export interface DefaultValuePayload extends Payload {
            valueExpression: Expressions.Expression;
            defaultingType: DefaultingType;
        }

        export enum DefaultingType {
            /**
             * This type of payload indicates that rule wil set a value,
             * only if the value is not null, or empty string
             */
            defaultValue = "defaultValue",
            /**
             * This type of payload indicates that rule wil reset a value
             */
            resetValue = "resetValue"
        }
    }

    export function isAccessibilityPayload(p: Payload): p is UI.AccessibilityPayload {
        return p.type === PayloadType.ACCESSIBILITY;
    }
    export function isVisibilityPayload(p: Payload): p is UI.VisibilityPayload {
        return p.type === PayloadType.VISIBILITY;
    }
    export function isDefaultValuePayload(p: Payload): p is Derive.DefaultValuePayload {
        return p.type === PayloadType.DEFAULT;
    }
    export function isAssertionPayload(p: Payload): p is Validation.AssertionPayload {
        return p.type === PayloadType.ASSERTION;
    }
    export function isLengthPayload(p: Payload): p is Validation.LengthPayload {
        return p.type === PayloadType.LENGTH;
    }
    export function isRegExpPayload(p: Payload): p is Validation.RegExpPayload {
        return p.type === PayloadType.REGEX;
    }
    export function isSizePayload(p: Payload): p is Validation.SizePayload {
        return p.type === PayloadType.SIZE;
    }
    export function isSizeRangePayload(p: Payload): p is Validation.SizeRangePayload {
        return p.type === PayloadType.SIZE_RANGE;
    }
    export function isUsagePayload(p: Payload): p is Validation.UsagePayload {
        return p.type === PayloadType.USAGE;
    }
    export function isValidationPayload(p: Payload): p is Validation.ValidationPayload {
        return isAssertionPayload(p)
            || isUsagePayload(p)
            || isLengthPayload(p)
            || isRegExpPayload(p)
            || isSizePayload(p)
            || isSizeRangePayload(p);
    }
}
