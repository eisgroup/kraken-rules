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

import { LengthPayloadHandler } from '../../../src/engine/handlers/LengthPayloadHandler'
import { mock } from '../../mock'
import { RulesBuilder, PayloadBuilder as payload } from 'kraken-model-builder'
import { Payloads } from 'kraken-model'
import LengthPayload = Payloads.Validation.LengthPayload
import { payloadResultTypeChecker } from '../../../src/engine/results/PayloadResultTypeChecker'

const handler = new LengthPayloadHandler(mock.evaluator)
const { Policy } = mock.modelTreeJson.contexts
const { session } = mock

describe('lengthPayloadHandler', () => {
    const { dataContextCustom: dc } = mock.data
    const rule = new RulesBuilder()
        .setName('Length')
        .setContext(Policy.name)
        .setTargetPath(Policy.fields.state.name)
        .setPayload(payload.lengthLimit().limit(3))
        .build()
    const ruleWithMessage = new RulesBuilder()
        .setName('LengthWithMessage')
        .setContext(Policy.name)
        .setTargetPath(Policy.fields.state.name)
        .setPayload(payload.lengthLimit().limit(3, 'Invalid Length'))
        .build()
    it('should return payload payloadResult with type LengthPayloadResult', () => {
        const payloadResult = handler.executePayload(rule, dc({ state: 'TooMuchChars' }), session)
        expect(payloadResultTypeChecker.isLength(payloadResult)).toBeTruthy()
    })
    it('should fail on longer that 3 letters validation', () => {
        const payloadResult = handler.executePayload(rule, dc({ state: 'TooMuchChars' }), session)
        expect(payloadResult.success).toBeFalsy()
        expect(payloadResult.message).toBeDefined()
        expect(payloadResult.message?.errorMessage).toContain('String contains more characters than 3')
    })
    it('should validate to true', () => {
        const payloadResult = handler.executePayload(rule, dc({ state: 'Aa' }), session)
        expect(payloadResult.success).toBeTruthy()
    })
    it('should validate to true if number of chars is equal to payload length number', () => {
        const payloadResult = handler.executePayload(rule, dc({ state: 'AAA' }), session)
        expect(payloadResult.success).toBeTruthy()
    })
    it('should not use default error message', () => {
        const payloadResult = handler.executePayload(ruleWithMessage, dc({ state: 'AAABBB' }), session)
        expect(payloadResult.success).toBeFalsy()
        const defaultErrMessage = `String 'AAABBB' contains more characters than ${
            (rule.payload as LengthPayload).length
        }`
        expect(payloadResult.message).not.toBe(defaultErrMessage)
    })
})
