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
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { payloadResultTypeChecker } from '../../../src'
import { ValueListPayloadHandler } from '../../../src/engine/handlers/ValueListPayloadHandler'

const handler = new ValueListPayloadHandler(mock.evaluator)

const { session } = mock
const { Policy } = mock.modelTreeJson.contexts

describe('valueListPayloadHandler', () => {
    const rule = (payload: Payloads.Payload, fieldName: string) => {
        return RulesBuilder.create()
            .setName('R001')
            .setContext(Policy.name)
            .setTargetPath(Policy.fields[fieldName].name)
            .setPayload(payload)
            .build()
    }
    const { dataContextCustom: dataContext } = mock.data
    it('should return correct payload type', () => {
        expect(handler.handlesPayloadType()).toBe(Payloads.PayloadType.VALUE_LIST)
    })
    it('should return payload result having type ValueListPayloadResult', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: ['AZ'],
            valueType: 'STRING',
        })
        const result = handler.executePayload(rule(payload, 'state'), dataContext({ state: 'AZ' }), session)
        expect(payloadResultTypeChecker.isValueList(result)).toBeTruthy()
    })
    it('should return true on string field having no value', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: ['AZ', 'NY'],
            valueType: 'STRING',
        })
        const result = handler.executePayload(rule(payload, 'state'), dataContext({ state: undefined }), session)
        expect(result.success).toBeTruthy()
    })
    it('should return true on string field having value in value list', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: ['AZ', 'NY'],
            valueType: 'STRING',
        })
        const result = handler.executePayload(rule(payload, 'state'), dataContext({ state: 'AZ' }), session)
        expect(result.success).toBeTruthy()
    })
    it('should return false on string field not having value in value list', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: ['AZ'],
            valueType: 'STRING',
        })
        const result = handler.executePayload(rule(payload, 'state'), dataContext({ state: 'NY' }), session)
        expect(result.success).toBeFalsy()
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        expect(result.message!.errorMessage).toBe('Value list does not contain provided value')
    })
    it('should return true on number field having value in value list', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: [20, 50, 45.5],
            valueType: 'DECIMAL',
        })
        const result = handler.executePayload(
            rule(payload, 'policyValue'),
            dataContext({ policyValue: { currency: 'USD', amount: 50 } }),
            session,
        )
        expect(result.success).toBeTruthy()
    })
    it('should return false on number field not having value in value list', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: [20, 50, 45.5],
            valueType: 'DECIMAL',
        })
        const result = handler.executePayload(
            rule(payload, 'policyValue'),
            dataContext({ policyValue: { currency: 'USD', amount: 500 } }),
            session,
        )
        expect(result.success).toBeFalsy()
    })
    it('should return error when data types are incompatible', () => {
        const payload = PayloadBuilder.valueList().valueList({
            values: [20, 50, 45.5],
            valueType: 'DECIMAL',
        })
        expect(() => handler.executePayload(rule(payload, 'state'), dataContext({ state: 'AZ' }), session)).toThrow()
    })
})
