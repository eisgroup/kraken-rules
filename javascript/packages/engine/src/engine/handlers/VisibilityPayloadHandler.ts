/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { RulePayloadHandler } from './RulePayloadHandler'
import { VisibilityPayloadResult, PayloadResultType } from 'kraken-engine-api'
import { Payloads, Rule } from 'kraken-model'
import PayloadType = Payloads.PayloadType
import VisibilityPayload = Payloads.UI.VisibilityPayload

class VisibilityPayloadHandler implements RulePayloadHandler {
    handlesPayloadType(): Payloads.PayloadType {
        return PayloadType.VISIBILITY
    }
    executePayload(rule: Rule): VisibilityPayloadResult {
        const payload = rule.payload as VisibilityPayload
        return {
            type: PayloadResultType.VISIBILITY,
            visible: payload.visible,
        }
    }

    describePayloadResult(payloadResult: VisibilityPayloadResult): string {
        return payloadResult.visible ? `The field is not set to be hidden.` : `The field is set to be hidden.`
    }
}

export const visibilityPayloadHandler = new VisibilityPayloadHandler()
