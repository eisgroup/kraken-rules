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

import { RuleEvent } from "./Events";
import { Payloads } from "kraken-model";
import { ExpressionEvaluationResult } from "../runtime/expressions/ExpressionEvaluationResult";
import UsageType = Payloads.Validation.UsageType;

export enum PayloadResultType {
    ASSERTION = 1,
    SIZE = 2,
    SIZE_RANGE = 4,
    DEFAULT = 8,
    LENGTH = 16,
    REGEXP = 32,
    USAGE = 64,
    ACCESSIBILITY = 128,
    VISIBILITY = 256,
    NOT_APPLICABLE = 512
}

const VALIDATION_MASK = PayloadResultType.ASSERTION
    | PayloadResultType.SIZE
    | PayloadResultType.SIZE_RANGE
    | PayloadResultType.LENGTH
    | PayloadResultType.REGEXP
    | PayloadResultType.USAGE;

export const payloadResultTypeChecker = {
    isAssertion(result: PayloadResult): result is AssertionPayloadResult {
        return Boolean(result.type & PayloadResultType.ASSERTION);
    },
    isSize(result: PayloadResult): result is SizePayloadResult {
        return Boolean(result.type & PayloadResultType.SIZE);
    },
    isSizeRange(result: PayloadResult): result is SizeRangePayloadResult {
        return Boolean(result.type & PayloadResultType.SIZE_RANGE);
    },
    isDefault(result: PayloadResult): result is DefaultValuePayloadResult {
        return Boolean(result.type & PayloadResultType.DEFAULT);
    },
    isLength(result: PayloadResult): result is LengthPayloadResult {
        return Boolean(result.type & PayloadResultType.LENGTH);
    },
    isRegExp(result: PayloadResult): result is RegExpPayloadResult {
        return Boolean(result.type & PayloadResultType.REGEXP);
    },
    isMandatory(result: PayloadResult): result is UsagePayloadResult {
        return result.type === PayloadResultType.USAGE
            && (result as UsagePayloadResult).usageType === UsageType.mandatory;
    },
    isEmpty(result: PayloadResult): result is UsagePayloadResult {
        return result.type === PayloadResultType.USAGE
            && (result as UsagePayloadResult).usageType === UsageType.mustBeEmpty;
    },
    isAccessibility(result: PayloadResult): result is AccessibilityPayloadResult {
        return Boolean(result.type & PayloadResultType.ACCESSIBILITY);
    },
    isVisibility(result: PayloadResult): result is VisibilityPayloadResult {
        return Boolean(result.type & PayloadResultType.VISIBILITY);
    },
    isValidation(result: PayloadResult): result is ValidationPayloadResult {
        return Boolean(result.type & VALIDATION_MASK);
    }
};

export const payloadResultCreator = {
    assertionFail(error: ExpressionEvaluationResult.ErrorResult): AssertionPayloadResult {
        return {
            type: PayloadResultType.ASSERTION,
            error
        };
    },
    assertion(
        payload: Payloads.Validation.AssertionPayload, success: boolean, templateVariables: string[]
    ): AssertionPayloadResult {
        return {
            type: PayloadResultType.ASSERTION,
            success,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity
        };
    },
    length(
        payload: Payloads.Validation.LengthPayload, success: boolean, templateVariables: string[]
    ): LengthPayloadResult {
        return {
            type: PayloadResultType.LENGTH,
            success,
            length: payload.length,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity
        };
    },
    regexp(
        payload: Payloads.Validation.RegExpPayload, success: boolean, templateVariables: string[]
    ): RegExpPayloadResult {
        return {
            type: PayloadResultType.REGEXP,
            success,
            regExp: payload.regExp,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity
        };
    },
    size(
        payload: Payloads.Validation.SizePayload, success: boolean, templateVariables: string[]
    ): SizePayloadResult {
        return {
            type: PayloadResultType.SIZE,
            success,
            min: payload.size,
            sizeOrientation: payload.orientation,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity
        };
    },
    sizeRange(
        payload: Payloads.Validation.SizeRangePayload, success: boolean, templateVariables: string[]
    ): SizeRangePayloadResult {
        return {
            type: PayloadResultType.SIZE_RANGE,
            success,
            min: payload.min,
            max: payload.max,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity
        };
    },
    usage(
        payload: Payloads.Validation.UsagePayload, success: boolean, templateVariables: string[]
    ): UsagePayloadResult {
        return {
            type: PayloadResultType.USAGE,
            usageType: payload.usageType,
            success,
            message: format(payload.errorMessage, templateVariables),
            validationSeverity: payload.severity
        };
    },
    default(events: RuleEvent[]): DefaultValuePayloadResult {
        return {
            type: PayloadResultType.DEFAULT,
            events
        };
    },
    defaultFail(error: ExpressionEvaluationResult.ErrorResult): DefaultValuePayloadResult {
        return {
            type: PayloadResultType.DEFAULT,
            error
        };
    },
    defaultNoEvents(): DefaultValuePayloadResult {
        return { type: PayloadResultType.DEFAULT, events: [] };
    }
};

function format(
    message : Payloads.Validation.ErrorMessage | undefined, templateVariables : string[]
): ErrorMessage | undefined {
    if (message === undefined) {
        return undefined;
    }
    return {
        errorCode: message.errorCode,
        errorMessage: message.templateParts.length > 0
            // inserts variables between array items in templateParts
            // the assumption is that 'templateVariables.length - 1 === templateParts.length'
            ? message.templateParts.reduce(((v1, v2, i) => v1 + templateVariables[i - 1] + v2))
            : undefined,
        templateVariables: templateVariables
    };
}

export interface PayloadResult {
    readonly type: PayloadResultType;
}

export interface ErrorAwarePayloadResult {
    readonly error?: ExpressionEvaluationResult.ErrorResult;
}

export interface ValidationPayloadResult extends PayloadResult {
    type: PayloadResultType;
    success?: boolean;
    message?: ErrorMessage;
    validationSeverity?: Payloads.Validation.ValidationSeverity;
}

export interface AccessibilityPayloadResult extends PayloadResult {
    type: PayloadResultType.ACCESSIBILITY;
    accessible: boolean;
}

export interface ErrorMessage {
    errorMessage?: string;
    errorCode: string;

    /**
     * A list of variables to be used in localized message templates.
     * Variables are extracted from rule validation message template and formatted for being displayed
     * in localized message template.
     *
     * @type {string[]}
     * @memberof ErrorMessage
     */
    templateVariables: string[];
}

export interface VisibilityPayloadResult extends PayloadResult {
    type: PayloadResultType.VISIBILITY;
    visible: boolean;
}

export interface DefaultValuePayloadResult extends PayloadResult, ErrorAwarePayloadResult {
    type: PayloadResultType.DEFAULT;
    events?: RuleEvent[];
}

export interface AssertionPayloadResult extends ErrorAwarePayloadResult, ValidationPayloadResult {
    type: PayloadResultType.ASSERTION;
}

export interface SizePayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.SIZE;
    min: number;
    sizeOrientation: Payloads.Validation.SizeOrientation;
}

export interface SizeRangePayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.SIZE_RANGE;
    min: number;
    max: number;
}

export interface LengthPayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.LENGTH;
    length: number;
}

export interface RegExpPayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.REGEXP;
    regExp: string;
}

export interface UsagePayloadResult extends ValidationPayloadResult {
    type: PayloadResultType.USAGE;
    usageType: Payloads.Validation.UsageType;
}
