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

import { RegExpPayloadHandler } from '../../../src/engine/handlers/RegExpPayloadHandler'
import { mock } from '../../mock'
import { Payloads } from 'kraken-model'
import RegExpPayload = Payloads.Validation.RegExpPayload
import { RulesBuilder, PayloadBuilder as pb } from 'kraken-model-builder'
import { payloadResultTypeChecker } from '../../../src/engine/results/PayloadResultTypeChecker'
const rb = RulesBuilder.create()

const handler = new RegExpPayloadHandler(mock.evaluator)
const { Policy } = mock.modelTreeJson.contexts
const { session } = mock

describe('regExpPayloadHandler', () => {
    const { dataContextCustom: dc } = mock.data
    it('should return payload result with type RegExpPayloadResult', () => {
        const rule = rb
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(pb.regExp().match('[a-zA-Z]*'))
            .build()
        const result = handler.executePayload(rule, dc({ state: 'AZ' }), session)
        expect(payloadResultTypeChecker.isRegExp(result)).toBeTruthy()
    })
    it('should validate undefined and return true', () => {
        const rule = rb
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(pb.regExp().match('[a-zA-Z]*'))
            .build()
        const result = handler.executePayload(rule, dc({ state: 'AZ' }), session)
        expect(result.success).toBeTruthy()
    })
    it('should validate string and return false', () => {
        const rule = rb
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(pb.regExp().match('^[0-9]*$'))
            .build()
        const result = handler.executePayload(rule, dc({ state: 'AZ' }), session)
        expect(result.success).toBeFalsy()
    })
    it('should validate string and return false with default error message', () => {
        const rule = rb
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(pb.regExp().match('^[0-9]*$'))
            .build()
        ;(rule.payload as RegExpPayload).errorMessage = undefined
        const result = handler.executePayload(rule, dc({ state: 'AZ' }), session)
        expect(result.success).toBeFalsy()
        expect(result.message).not.toBeDefined()
    })
})
