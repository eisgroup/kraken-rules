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

import { PayloadBuilder } from 'kraken-model-builder'
import { Payloads } from 'kraken-model'
import { accessibilityPayloadHandler } from '../../../src/engine/handlers/AccessibilityPayloadHandler'
import { payloadResultTypeChecker } from '../../../src/engine/results/PayloadResultTypeChecker'

describe('accessibilityPayloadHandler', () => {
    it('should create instance', () => {
        expect(accessibilityPayloadHandler.handlesPayloadType()).toBe(Payloads.PayloadType.ACCESSIBILITY)
    })
    it('should return payload payloadResult with type PresentationPayloadResult', () => {
        const payload = PayloadBuilder.accessibility().notAccessible()
        const result = accessibilityPayloadHandler.executePayload(payload)
        expect(payloadResultTypeChecker.isAccessibility(result)).toBeTruthy()
    })
    it('should execute payload', () => {
        const payload = PayloadBuilder.accessibility().notAccessible()
        const result = accessibilityPayloadHandler.executePayload(payload)
        expect(result.accessible).toBeFalsy()
    })
})
