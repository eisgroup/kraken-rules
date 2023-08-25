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

import { Payloads, ValueList } from 'kraken-model'

import UsageType = Payloads.Validation.UsageType
import ValidationSeverity = Payloads.Validation.ValidationSeverity
import UsagePayload = Payloads.Validation.UsagePayload
import RegExpPayload = Payloads.Validation.RegExpPayload
import SizePayload = Payloads.Validation.SizePayload
import SizeRangePayload = Payloads.Validation.SizeRangePayload
import AssertionPayload = Payloads.Validation.AssertionPayload
import LengthPayload = Payloads.Validation.LengthPayload
import SizeOrientation = Payloads.Validation.SizeOrientation

import DefaultingType = Payloads.Derive.DefaultingType
import PayloadType = Payloads.PayloadType
import AccessibilityPayload = Payloads.UI.AccessibilityPayload
import VisibilityPayload = Payloads.UI.VisibilityPayload
import DefaultValuePayload = Payloads.Derive.DefaultValuePayload
import ValueListPayload = Payloads.Validation.ValueListPayload
import NumberSetPayload = Payloads.Validation.NumberSetPayload

export class PayloadBuilder {
    static usage(): UsagePayloadBuilder {
        return new UsagePayloadBuilder()
    }
    static accessibility(): AccessibilityPayloadBuilder {
        return new AccessibilityPayloadBuilder()
    }
    static visibility(): VisibilityPayloadBuilder {
        return new VisibilityPayloadBuilder()
    }
    static regExp(): RegExpPayloadBuilder {
        return new RegExpPayloadBuilder()
    }
    static default(): DefaultValuePayloadBuilder {
        return new DefaultValuePayloadBuilder()
    }
    static asserts(): AssertionPayloadBuilder {
        return new AssertionPayloadBuilder()
    }
    static lengthLimit(): LengthLimitPayloadBuilder {
        return new LengthLimitPayloadBuilder()
    }
    static size(): SizePayloadBuilder {
        return new SizePayloadBuilder()
    }
    static numberSet(): NumberSetPayloadBuilder {
        return new NumberSetPayloadBuilder()
    }
    static valueList() {
        return new ValueListPayloadBuilder()
    }
}

export class SizePayloadBuilder {
    min(size: number, message?: string): SizePayload {
        return {
            errorMessage: {
                errorCode: 'size-generated',
                templateParts: [message || 'Array contains more than  ' + size],
                templateExpressions: [],
            },
            orientation: SizeOrientation.MIN,
            severity: ValidationSeverity.critical,
            size: size,
            type: PayloadType.SIZE,
        }
    }
    max(size: number, message?: string): SizePayload {
        return {
            errorMessage: {
                errorCode: 'size-generated',
                templateParts: [message || 'Array contains less than  ' + size],
                templateExpressions: [],
            },
            orientation: SizeOrientation.MAX,
            severity: ValidationSeverity.critical,
            size: size,
            type: PayloadType.SIZE,
        }
    }
    equals(size: number, message?: string): SizePayload {
        return {
            errorMessage: {
                errorCode: 'size-generated',
                templateParts: [message || 'Array must contain ' + size + ' number of elements'],
                templateExpressions: [],
            },
            orientation: SizeOrientation.EQUALS,
            severity: ValidationSeverity.critical,
            size: size,
            type: PayloadType.SIZE,
        }
    }
    range(min: number, max: number, message?: string): SizeRangePayload {
        return {
            errorMessage: {
                errorCode: 'size-range-generated',
                templateParts: [message || 'Array length must be within ' + min + ' and ' + max],
                templateExpressions: [],
            },
            severity: ValidationSeverity.critical,
            type: PayloadType.SIZE_RANGE,
            min,
            max,
        }
    }
}

export class LengthLimitPayloadBuilder {
    limit(length: number, message?: string): LengthPayload {
        return {
            type: PayloadType.LENGTH,
            errorMessage: {
                errorCode: 'limit-generated',
                templateParts: [message || 'String contains more characters than ' + length],
                templateExpressions: [],
            },
            length: length,
            severity: ValidationSeverity.critical,
        }
    }
    overridableLimit(length: number, message?: string, overrideGroup?: string): LengthPayload {
        return {
            type: PayloadType.LENGTH,
            errorMessage: {
                errorCode: 'limit-generated',
                templateParts: [message || 'String contains more characters than ' + length],
                templateExpressions: [],
            },
            length: length,
            isOverridable: true,
            overrideGroup,
            severity: ValidationSeverity.critical,
        }
    }
}

export class AssertionPayloadBuilder {
    that(expression: string, message?: string): AssertionPayload {
        return {
            type: PayloadType.ASSERTION,
            errorMessage: {
                errorCode: 'assert-generated',
                templateParts: [message || "Value didn't match assertion: " + expression],
                templateExpressions: [],
            },
            assertionExpression: {
                expressionType: 'COMPLEX',
                expressionString: expression,
                originalExpressionString: expression,
            },
            severity: ValidationSeverity.critical,
        }
    }
    overridableThat(expression: string, message?: string, overrideGroup?: string): AssertionPayload {
        return {
            type: PayloadType.ASSERTION,
            errorMessage: {
                errorCode: 'assert-generated',
                templateParts: [message || "Value didn't match assertion: " + expression],
                templateExpressions: [],
            },
            assertionExpression: {
                expressionType: 'COMPLEX',
                expressionString: expression,
                originalExpressionString: expression,
            },
            isOverridable: true,
            overrideGroup,
            severity: ValidationSeverity.critical,
        }
    }
}

export class DefaultValuePayloadBuilder {
    private defaultingType: DefaultingType = DefaultingType.defaultValue

    apply(type: DefaultingType): DefaultValuePayloadBuilder {
        this.defaultingType = type
        return this
    }

    to(expression: string): DefaultValuePayload {
        return {
            type: PayloadType.DEFAULT,
            defaultingType: this.defaultingType,
            valueExpression: {
                expressionType: 'COMPLEX',
                expressionString: expression,
                originalExpressionString: expression,
            },
        }
    }
}

export class RegExpPayloadBuilder {
    match(regexp: string): RegExpPayload {
        return {
            type: PayloadType.REGEX,
            errorMessage: {
                errorCode: 'regexp-generated',
                templateParts: ["String doesn't match Regular Expression: " + regexp],
                templateExpressions: [],
            },
            regExp: regexp,
            severity: ValidationSeverity.critical,
        }
    }
    matchOverridable(regexp: string, overrideGroup?: string): RegExpPayload {
        return {
            type: PayloadType.REGEX,
            errorMessage: {
                errorCode: 'regexp-generated',
                templateParts: ["String doesn't match Regular Expression: " + regexp],
                templateExpressions: [],
            },
            regExp: regexp,
            isOverridable: true,
            overrideGroup,
            severity: ValidationSeverity.critical,
        }
    }
}

export class UsagePayloadBuilder {
    is(usage: UsageType): UsagePayload {
        return {
            type: PayloadType.USAGE,
            usageType: usage,
            severity: ValidationSeverity.critical,
            errorMessage: {
                errorCode: 'usage-generated',
                templateParts: ['The field is ' + usage.toString()],
                templateExpressions: [],
            },
        }
    }
    isOverridable(usage: UsageType, overrideGroup?: string): UsagePayload {
        return {
            type: PayloadType.USAGE,
            usageType: usage,
            errorMessage: {
                errorCode: 'usage-generated',
                templateParts: ['The field is ' + usage.toString()],
                templateExpressions: [],
            },
            isOverridable: true,
            overrideGroup,
            severity: ValidationSeverity.critical,
        }
    }
}

export class VisibilityPayloadBuilder {
    private type: PayloadType = PayloadType.VISIBILITY

    notVisible(): VisibilityPayload {
        return {
            type: this.type,
            visible: false,
        }
    }
}

export class AccessibilityPayloadBuilder {
    private type: PayloadType = PayloadType.ACCESSIBILITY

    notAccessible(): AccessibilityPayload {
        return {
            type: this.type,
            accessible: false,
        }
    }
}

export class NumberSetPayloadBuilder {
    greaterThanOrEqualTo(min: number, step?: number): NumberSetPayload {
        return {
            type: PayloadType.NUMBER_SET,
            severity: ValidationSeverity.critical,
            min,
            step,
        }
    }

    lessThanOrEqualTo(max: number, step?: number): NumberSetPayload {
        return {
            type: PayloadType.NUMBER_SET,
            severity: ValidationSeverity.critical,
            max,
            step,
        }
    }

    within(min: number, max: number, step?: number): NumberSetPayload {
        return {
            type: PayloadType.NUMBER_SET,
            severity: ValidationSeverity.critical,
            min,
            max,
            step,
        }
    }
}

export class ValueListPayloadBuilder {
    valueList(valueList: ValueList): ValueListPayload {
        return {
            type: PayloadType.VALUE_LIST,
            valueList: valueList,
            severity: ValidationSeverity.critical,
            errorMessage: {
                errorCode: 'value-list-generated',
                templateParts: ['Value list does not contain provided value'],
                templateExpressions: [],
            },
        }
    }
}
