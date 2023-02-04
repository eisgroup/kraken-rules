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
import {
    AssertionPayloadResult,
    ConditionEvaluation,
    LengthPayloadResult,
    NumberSetPayloadResult,
    RegExpPayloadResult,
    RuleEvaluationResults,
    SizePayloadResult,
    UsagePayloadResult,
} from 'kraken-engine-api'
import { Payloads } from 'kraken-model'
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'

import { DefaultConditionEvaluationResult } from '../../../src/dto/DefaultConditionEvaluationResult'
import { DefaultContextFieldInfo } from '../../../src/dto/DefaultContextFieldInfo'
import { DefaultRuleInfo } from '../../../src/engine/results/DefaultRuleInfo'
import { Localization } from '../../../src/engine/results/Localization'
import { payloadResultCreator } from '../../../src/engine/results/PayloadResultCreator'
import { mock } from '../../mock'

import ValidationRuleEvaluationResult = RuleEvaluationResults.ValidationRuleEvaluationResult

import defaultMessages = Localization.defaultMessages
import errorMessage = Localization.errorMessage
import defaultValidationMessages = Localization.defaultValidationMessages
import resolveMessage = Localization.resolveMessage

const rule = (p: Payloads.Payload) =>
    new RulesBuilder().setName('r01').setContext('mock').setTargetPath('mock').setPayload(p).build()
const info = () => new DefaultContextFieldInfo(mock.dataContextEmpty(), 'state')

describe('Localization', () => {
    it('[deprecated] should leave error code defined and resolve message', () => {
        const payload = PayloadBuilder.lengthLimit().limit(0)
        payload.errorMessage = {
            errorCode: 'BR',
            templateParts: [],
            templateExpressions: [],
        }
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new DefaultRuleInfo(rule(payload)),
            conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.length(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false,
            },
        }
        const message = errorMessage(defaultMessages)(res, info())
        expect(message.errorCode).toBe('BR')
        expect(message.errorMessage).toBe('Text must not be longer than 0')
    })
    it('[deprecated] should resolve error message for length rule', () => {
        const payload = PayloadBuilder.lengthLimit().limit(0)
        payload.errorMessage = undefined
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new DefaultRuleInfo(rule(payload)),
            conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.length(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false,
            },
        }
        const message = errorMessage(defaultMessages)(res, info())
        expect(message.errorCode).toBe('rule-length-error')
        expect(message.errorMessage).toBe('Text must not be longer than 0')
    })
    it('[deprecated] should resolve error message for usage.mandatory rule', () => {
        const payload = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mandatory)
        payload.errorMessage = undefined
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new DefaultRuleInfo(rule(payload)),
            conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.usage(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false,
            },
        }
        const message = errorMessage(defaultMessages)(res, info())
        expect(message.errorCode).toBe('rule-mandatory-error')
        expect(message.errorMessage).toBe('Field is mandatory')
    })
    it('[deprecated] should resolve error message for usage.empty rule', () => {
        const payload = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mustBeEmpty)
        payload.errorMessage = undefined
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new DefaultRuleInfo(rule(payload)),
            conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.usage(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false,
            },
        }
        const message = errorMessage(defaultMessages)(res, info())
        expect(message.errorCode).toBe('rule-mandatory-empty-error')
        expect(message.errorMessage).toBe('Field must be empty')
    })
    it('[deprecated] should resolve error message for regexp rule', () => {
        const payload = PayloadBuilder.regExp().match('*')
        payload.errorMessage = undefined
        const res: ValidationRuleEvaluationResult = {
            kind: RuleEvaluationResults.Kind.VALIDATION,
            ruleInfo: new DefaultRuleInfo(rule(payload)),
            conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
            payloadResult: payloadResultCreator.regexp(payload, false, []),
            overrideInfo: {
                overridable: false,
                overrideApplicable: false,
            },
        }
        const message = errorMessage(defaultMessages)(res, info())
        expect(message.errorCode).toBe('rule-regexp-error')
        expect(message.errorMessage).toBe('Field must match regular expression pattern: *')
    })
    it('should leave error code defined and resolve message', () => {
        const payload = PayloadBuilder.lengthLimit().limit(0)
        payload.errorMessage = {
            errorCode: 'BR',
            templateParts: [],
            templateExpressions: [],
        }
        const payloadResult: LengthPayloadResult = payloadResultCreator.length(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('BR')
        expect(message.errorMessage).toBe('Text must not be longer than {{0}}')
        expect(message.templateVariables).toStrictEqual(['0'])
    })
    it('should resolve error message for assertion rule', () => {
        const payload = PayloadBuilder.asserts().that('true', 'expression is true')
        payload.errorMessage = undefined
        const payloadResult: AssertionPayloadResult = payloadResultCreator.assertion(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('rule-assertion-error')
        expect(message.errorMessage).toBe('Assertion failed')
    })
    it('should resolve error message for length rule', () => {
        const payload = PayloadBuilder.lengthLimit().limit(0)
        payload.errorMessage = undefined
        const payloadResult: LengthPayloadResult = payloadResultCreator.length(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('rule-length-error')
        expect(message.errorMessage).toBe('Text must not be longer than {{0}}')
        expect(message.templateVariables).toStrictEqual(['0'])
    })
    it('should resolve error message for size rule', () => {
        const payload = PayloadBuilder.size().min(10)
        payload.errorMessage = undefined
        const payloadResult: SizePayloadResult = payloadResultCreator.size(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('rule-size-error')
        expect(message.errorMessage).toBe('Array length is invalid')
    })
    it('should resolve error message for usage.mandatory rule', () => {
        const payload = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mandatory)
        payload.errorMessage = undefined
        const payloadResult: UsagePayloadResult = payloadResultCreator.usage(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('rule-mandatory-error')
        expect(message.errorMessage).toBe('Field is mandatory')
        expect(message.templateVariables).toHaveLength(0)
    })
    it('should resolve error message for usage.empty rule', () => {
        const payload = PayloadBuilder.usage().is(Payloads.Validation.UsageType.mustBeEmpty)
        payload.errorMessage = undefined
        const payloadResult: UsagePayloadResult = payloadResultCreator.usage(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('rule-mandatory-empty-error')
        expect(message.errorMessage).toBe('Field must be empty')
        expect(message.templateVariables).toHaveLength(0)
    })
    it('should resolve error message for regexp rule', () => {
        const payload = PayloadBuilder.regExp().match('*')
        payload.errorMessage = undefined
        const payloadResult: RegExpPayloadResult = payloadResultCreator.regexp(payload, false, [])
        const message = resolveMessage(payloadResult, defaultValidationMessages)
        expect(message.errorCode).toBe('rule-regexp-error')
        expect(message.errorMessage).toBe('Field must match regular expression pattern: {{0}}')
        expect(message.templateVariables).toStrictEqual(['*'])
    })
    describe('NumberSetPayload', () => {
        it('should resolve error message for min max step', () => {
            const payload = PayloadBuilder.numberSet().within(0, 10, 2)
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            const message = resolveMessage(payloadResult, defaultValidationMessages)
            expect(message.errorCode).toBe('number-set-min-max-step-error')
            expect(message.errorMessage).toBe(
                'Value must be in interval between {{0}} and {{1}} inclusively with increment {{2}}',
            )
            expect(message.templateVariables).toStrictEqual(['0', '10', '2'])
        })
        it('should resolve error message for min max', () => {
            const payload = PayloadBuilder.numberSet().within(0, 10)
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            const message = resolveMessage(payloadResult, defaultValidationMessages)
            expect(message.errorCode).toBe('number-set-min-max-error')
            expect(message.errorMessage).toBe('Value must be in interval between {{0}} and {{1}} inclusively')
            expect(message.templateVariables).toStrictEqual(['0', '10'])
        })
        it('should resolve error message for min', () => {
            const payload = PayloadBuilder.numberSet().greaterThanOrEqualTo(1)
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            const message = resolveMessage(payloadResult, defaultValidationMessages)
            expect(message.errorCode).toBe('number-set-min-error')
            expect(message.errorMessage).toBe('Value must be {{0}} or larger')
            expect(message.templateVariables).toStrictEqual(['1'])
        })
        it('should resolve error message for min step', () => {
            const payload = PayloadBuilder.numberSet().greaterThanOrEqualTo(1, 2)
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            const message = resolveMessage(payloadResult, defaultValidationMessages)
            expect(message.errorCode).toBe('number-set-min-step-error')
            expect(message.errorMessage).toBe('Value must be {{0}} or larger with increment {{1}}')
            expect(message.templateVariables).toStrictEqual(['1', '2'])
        })
        it('should resolve error message for max', () => {
            const payload = PayloadBuilder.numberSet().lessThanOrEqualTo(10)
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            const message = resolveMessage(payloadResult, defaultValidationMessages)
            expect(message.errorCode).toBe('number-set-max-error')
            expect(message.errorMessage).toBe('Value must be {{0}} or smaller')
            expect(message.templateVariables).toStrictEqual(['10'])
        })
        it('should resolve error message for max step', () => {
            const payload = PayloadBuilder.numberSet().lessThanOrEqualTo(10, 2)
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            const message = resolveMessage(payloadResult, defaultValidationMessages)
            expect(message.errorCode).toBe('number-set-max-step-error')
            expect(message.errorMessage).toBe('Value must be {{0}} or smaller with decrement {{1}}')
            expect(message.templateVariables).toStrictEqual(['10', '2'])
        })
        it('should handle null', () => {
            const payload = {
                type: Payloads.PayloadType.NUMBER_SET,
                severity: Payloads.Validation.ValidationSeverity.critical,
                min: null,
            }
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            expect(() => resolveMessage(payloadResult, defaultValidationMessages)).toThrow()
        })
        it('should handle undefined', () => {
            const payload = {
                type: Payloads.PayloadType.NUMBER_SET,
                severity: Payloads.Validation.ValidationSeverity.critical,
                min: undefined,
            }
            const payloadResult: NumberSetPayloadResult = payloadResultCreator.numberSet(payload, false, [])
            expect(() => resolveMessage(payloadResult, defaultValidationMessages)).toThrow()
        })
    })
})
