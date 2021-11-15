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

import { UsagePayloadHandler } from "../../../src/engine/handlers/UsagePayloadHandler";
import { Payloads } from "kraken-model";
import { mock } from "../../mock";
import { UsagePayloadResult, payloadResultTypeChecker } from "../../../src/engine/results/PayloadResult";
import UsageType = Payloads.Validation.UsageType;
import { RulesBuilder, PayloadBuilder } from "kraken-model-builder";

const handler = new UsagePayloadHandler(mock.evaluator);
const { session } = mock;

const { Policy } = mock.modelTreeJson.contexts;
describe("usagePayloadHandler", () => {
    const rule = (payload: Payloads.Payload) => RulesBuilder.create()
        .setName("R001")
        .setContext(Policy.name)
        .setTargetPath(Policy.fields.state.name)
        .setPayload(payload)
        .build();
    const { dataContextCustom: dataContext } = mock.data;
    it("should return type", () => {
        expect(handler.handlesPayloadType()).toBe(Payloads.PayloadType.USAGE);
    });
    it("should return payload payloadResult with type UsagePayloadResult", () => {
        const payload = PayloadBuilder.usage().is(UsageType.mandatory);
        const result = handler.executePayload(
            payload,
            rule(payload),
            dataContext({ state: "AZ" }),
            session
        );
        expect(payloadResultTypeChecker.isMandatory(result)).toBeTruthy();
    });
    it("should return true on mandatory field", () => {
        const payload = PayloadBuilder.usage().is(UsageType.mandatory);
        const result = handler.executePayload(
            payload,
            rule(payload),
            dataContext({ state: "AZ" }),
            session
        ) as UsagePayloadResult;
        expect(result.success).toBeTruthy();
    });
    it("should return false on mandatory field", () => {
        const payload = PayloadBuilder.usage().is(UsageType.mandatory);
        const result = handler.executePayload(
            payload,
            rule(payload),
            dataContext({ state: undefined }),
            session
        ) as UsagePayloadResult;
        expect(result.success).toBeFalsy();
        expect(result.message!.errorMessage).toBe("The field is mandatory");
    });
    it("should return false on mustBeEmpty field", () => {
        const payload = PayloadBuilder.usage().is(UsageType.mustBeEmpty);
        const result = handler.executePayload(
            payload,
            rule(payload),
            dataContext({ state: "AZ" }),
            session
        ) as UsagePayloadResult;
        expect(result.success).toBeFalsy();
        expect(result.message!.errorMessage).toBe("The field is " + UsageType.mustBeEmpty);
    });
    it("should return true on mustBeEmpty field", () => {
        const payload = PayloadBuilder.usage().is(UsageType.mustBeEmpty);
        const result = handler.executePayload(
            payload,
            rule(payload),
            dataContext({ state: undefined }),
            session
        ) as UsagePayloadResult;
        expect(result.success).toBeTruthy();
    });

});
