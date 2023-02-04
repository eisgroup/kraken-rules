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
    AssertionPayloadResult,
    DefaultValuePayloadResult,
    LengthPayloadResult,
    NumberSetPayloadResult,
    PayloadResult,
    PayloadResultType,
    RegExpPayloadResult,
    SizePayloadResult,
    SizeRangePayloadResult,
    UsagePayloadResult,
    ValidationPayloadResult,
    ValueListPayloadResult,
    VisibilityPayloadResult,
} from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import UsageType = Payloads.Validation.UsageType

const VALIDATION_MASK =
    PayloadResultType.ASSERTION |
    PayloadResultType.SIZE |
    PayloadResultType.SIZE_RANGE |
    PayloadResultType.LENGTH |
    PayloadResultType.REGEXP |
    PayloadResultType.USAGE |
    PayloadResultType.NUMBER_SET |
    PayloadResultType.VALUE_LIST

export const payloadResultTypeChecker = {
    isAssertion(result: PayloadResult): result is AssertionPayloadResult {
        return Boolean(result.type & PayloadResultType.ASSERTION)
    },

    isSize(result: PayloadResult): result is SizePayloadResult {
        return Boolean(result.type & PayloadResultType.SIZE)
    },

    isSizeRange(result: PayloadResult): result is SizeRangePayloadResult {
        return Boolean(result.type & PayloadResultType.SIZE_RANGE)
    },

    isDefault(result: PayloadResult): result is DefaultValuePayloadResult {
        return Boolean(result.type & PayloadResultType.DEFAULT)
    },

    isLength(result: PayloadResult): result is LengthPayloadResult {
        return Boolean(result.type & PayloadResultType.LENGTH)
    },

    isRegExp(result: PayloadResult): result is RegExpPayloadResult {
        return Boolean(result.type & PayloadResultType.REGEXP)
    },

    isNumberSet(result: PayloadResult): result is NumberSetPayloadResult {
        return Boolean(result.type & PayloadResultType.NUMBER_SET)
    },

    isUsage(result: PayloadResult): result is UsagePayloadResult {
        return result.type === PayloadResultType.USAGE
    },

    isMandatory(result: PayloadResult): result is UsagePayloadResult {
        return this.isUsage(result) && (result as UsagePayloadResult).usageType === UsageType.mandatory
    },

    isEmpty(result: PayloadResult): result is UsagePayloadResult {
        return this.isUsage(result) && (result as UsagePayloadResult).usageType === UsageType.mustBeEmpty
    },

    isAccessibility(result: PayloadResult): result is AccessibilityPayloadResult {
        return Boolean(result.type & PayloadResultType.ACCESSIBILITY)
    },

    isVisibility(result: PayloadResult): result is VisibilityPayloadResult {
        return Boolean(result.type & PayloadResultType.VISIBILITY)
    },

    isValueList(result: PayloadResult): result is ValueListPayloadResult {
        return Boolean(result.type & PayloadResultType.VALUE_LIST)
    },

    isValidation(result: PayloadResult): result is ValidationPayloadResult {
        return Boolean(result.type & VALIDATION_MASK)
    },
}
