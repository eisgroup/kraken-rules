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

import { SizePayloadHandler } from '../../../src/engine/handlers/SizePayloadHandler'
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { mock } from '../../mock'
import { Payloads, Rule } from 'kraken-model'
import { DataContext } from '../../../src/engine/contexts/data/DataContext'

const sizePayloadHandler = new SizePayloadHandler(mock.evaluator)
const { session } = mock
const { Policy } = mock.modelTree.contexts

describe('Size Payload Handler', () => {
    it('should succeed when array size is more than min', () => {
        const payload = PayloadBuilder.size().min(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(3), session)
        expect(result.success).toBeTruthy()
    })
    it('should fail when array is undefined and less than min', () => {
        const payload = PayloadBuilder.size().min(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(), session)
        expect(result.success).toBeFalsy()
    })
    it('should succeed when array size equals min', () => {
        const payload = PayloadBuilder.size().min(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(2), session)
        expect(result.success).toBeTruthy()
    })
    it('should fail when array size is less than min', () => {
        const payload = PayloadBuilder.size().min(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(1), session)
        expect(result.success).toBeFalsy()
    })
    it('should succeed when array size is less than max', () => {
        const payload = PayloadBuilder.size().max(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(1), session)
        expect(result.success).toBeTruthy()
    })
    it('should succeed when array size equals max', () => {
        const payload = PayloadBuilder.size().max(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(2), session)
        expect(result.success).toBeTruthy()
    })
    it('should succeed when array is undefined and less than max', () => {
        const payload = PayloadBuilder.size().max(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(), session)
        expect(result.success).toBeTruthy()
    })
    it('should fail when array size is more than max', () => {
        const payload = PayloadBuilder.size().max(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(3), session)
        expect(result.success).toBeFalsy()
    })
    it('should fail when array size is less than equals', () => {
        const payload = PayloadBuilder.size().equals(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(1), session)
        expect(result.success).toBeFalsy()
    })
    it('should succeed when array size is equal to equals', () => {
        const payload = PayloadBuilder.size().equals(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(2), session)
        expect(result.success).toBeTruthy()
    })
    it('should fail when array size is more equals', () => {
        const payload = PayloadBuilder.size().equals(2)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(3), session)
        expect(result.success).toBeFalsy()
    })
    it('should fail when array is undefined and not equal to equals', () => {
        const payload = PayloadBuilder.size().equals(1)
        const result = sizePayloadHandler.executePayload(rule(payload), dataContext(), session)
        expect(result.success).toBeFalsy()
    })
})

function rule(payload: Payloads.Validation.SizePayload): Rule {
    return new RulesBuilder()
        .setName('rule')
        .setContext(Policy.name)
        .setTargetPath(Policy.fields.policies.name)
        .setPayload(payload)
        .build()
}

function dataContext(size?: number): DataContext {
    const policy = mock.data.empty()
    policy.policies = size ? Array(size) : undefined
    return new DataContext('1', Policy.name, policy as Record<string, unknown>, mock.contextInstanceInfo, Policy)
}
