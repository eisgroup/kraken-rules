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

import { RulePayloadHandler } from "./RulePayloadHandler";
import { AccessibilityPayloadResult, PayloadResultType } from "../results/PayloadResult";
import { Payloads } from "kraken-model";
import PayloadType = Payloads.PayloadType;
import AccessibilityPayload = Payloads.UI.AccessibilityPayload;

class AccessibilityPayloadHandler implements RulePayloadHandler {
    handlesPayloadType(): PayloadType {
        return PayloadType.ACCESSIBILITY;
    }
    executePayload(payload: AccessibilityPayload): AccessibilityPayloadResult {
        return {
            type: PayloadResultType.ACCESSIBILITY,
            accessible: payload.accessible
        };
    }
}

export const accessibilityPayloadHandler = new AccessibilityPayloadHandler();
