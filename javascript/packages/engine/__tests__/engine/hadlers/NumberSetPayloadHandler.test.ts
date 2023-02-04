/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

import { mock } from '../../mock'
import { Payloads } from 'kraken-model'
import { RulesBuilder, PayloadBuilder } from 'kraken-model-builder'
import { payloadResultTypeChecker } from '../../../src/engine/results/PayloadResultTypeChecker'
import { NumberSetPayloadHandler } from '../../../src/engine/handlers/NumberSetPayloadHandler'
import NumberSetPayload = Payloads.Validation.NumberSetPayload

const handler = new NumberSetPayloadHandler(mock.evaluator)
const { Policy } = mock.modelTreeJson.contexts

describe('NumberSetPayloadHandler', () => {
    it('should return payload result with type NumberSetPayloadResult', () => {
        const rule = RulesBuilder.create()
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.termNo.name)
            .setPayload(PayloadBuilder.numberSet().within(1, 100, 1))
            .build()
        const policy = mock.data.dataContextCustom({
            termDetails: {
                id: 'TermDetails-1',
                cd: 'TermDetails',
                termNo: 0,
            },
        })
        const result = handler.executePayload(rule.payload as NumberSetPayload, rule, policy, mock.session)

        expect(payloadResultTypeChecker.isNumberSet(result)).toBeTruthy()
        expect(result.success).toBeFalsy()
        expect(result.message).not.toBeDefined()
        expect(result.min).toBe(1)
        expect(result.max).toBe(100)
        expect(result.step).toBe(1)
    })
    it('should validate undefined and return true', () => {
        const rule = RulesBuilder.create()
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.termNo.name)
            .setPayload(PayloadBuilder.numberSet().within(1, 100, 1))
            .build()
        const policy = mock.data.dataContextCustom({
            termDetails: {
                id: 'TermDetails-1',
                cd: 'TermDetails',
                termNo: undefined,
            },
        })
        const result = handler.executePayload(rule.payload as NumberSetPayload, rule, policy, mock.session)

        expect(result.success).toBeTruthy()
    })
    it('should return true for valid value', () => {
        const rule = RulesBuilder.create()
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.termNo.name)
            .setPayload(PayloadBuilder.numberSet().within(1, 100, 1))
            .build()
        const policy = mock.data.dataContextCustom({
            termDetails: {
                id: 'TermDetails-1',
                cd: 'TermDetails',
                termNo: 100,
            },
        })
        const result = handler.executePayload(rule.payload as NumberSetPayload, rule, policy, mock.session)

        expect(result.success).toBeTruthy()
    })
})
