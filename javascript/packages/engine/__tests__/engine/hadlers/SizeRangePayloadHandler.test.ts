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

import { SizeRangePayloadHandler } from '../../../src/engine/handlers/SizeRangePayloadHandler'
import { PayloadBuilder, RulesBuilder } from 'kraken-model-builder'
import { mock } from '../../mock'
import { Payloads, Rule } from 'kraken-model'
import { DataContext } from '../../../src/engine/contexts/data/DataContext'

const sizeRangePayloadHandler = new SizeRangePayloadHandler(mock.evaluator)
const { session } = mock
const { Policy } = mock.modelTree.contexts

describe('Size Payload Handler', () => {
    it('should succeed when array size is in range', () => {
        const payload = PayloadBuilder.size().range(2, 3)
        const result = sizeRangePayloadHandler.executePayload(rule(payload), dataContext(3), session)
        expect(result.success).toBeTruthy()
    })
    it('should fail when array is undefined and less then range', () => {
        const payload = PayloadBuilder.size().range(2, 3)
        const result = sizeRangePayloadHandler.executePayload(rule(payload), dataContext(), session)
        expect(result.success).toBeFalsy()
    })
    it('should fail when array size is less than range', () => {
        const payload = PayloadBuilder.size().range(2, 3)
        const result = sizeRangePayloadHandler.executePayload(rule(payload), dataContext(1), session)
        expect(result.success).toBeFalsy()
    })
    it('should fail when array size is more than range', () => {
        const payload = PayloadBuilder.size().range(2, 3)
        const result = sizeRangePayloadHandler.executePayload(rule(payload), dataContext(4), session)
        expect(result.success).toBeFalsy()
    })
    it('should succeed when array is undefined and in range', () => {
        const payload = PayloadBuilder.size().range(0, 3)
        const result = sizeRangePayloadHandler.executePayload(rule(payload), dataContext(), session)
        expect(result.success).toBeTruthy()
    })
})

function rule(payload: Payloads.Validation.SizeRangePayload): Rule {
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
    return new DataContext('1', Policy.name, '', policy as Record<string, unknown>, mock.contextInstanceInfo, Policy)
}
