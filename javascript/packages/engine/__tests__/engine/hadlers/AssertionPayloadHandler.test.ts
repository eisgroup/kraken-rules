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

import { AssertionPayloadHandler } from "../../../src/engine/handlers/AssertionPayloadHandler";
import { Payloads } from "kraken-model";
import { DataContext } from "../../../src/engine/contexts/data/DataContext";
import { mock } from "../../mock";
import { AssertionPayloadResult, payloadResultTypeChecker } from "../../../src/engine/results/PayloadResult";
import { RulesBuilder, PayloadBuilder } from "kraken-model-builder";

let dataContext: DataContext;
const handler = new AssertionPayloadHandler(mock.evaluator);
const { session } = mock;
const { Policy } = mock.modelTreeJson.contexts;

beforeEach(() => {
    dataContext = mock.data.dataContextEmpty();
});
describe("assertionPayloadHandler", () => {
    it("should create instance", () => {
        expect(handler.handlesPayloadType()).toBe(Payloads.PayloadType.ASSERTION);
    });
    it("should return payload payloadResult with type AssertionPayloadResult", () => {
        const rule = RulesBuilder.create().setName("R001")
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(PayloadBuilder.asserts().that("false"))
            .build();
        const result = handler.executePayload(
            rule.payload as Payloads.Validation.AssertionPayload,
            rule,
            dataContext,
            session);
        expect(payloadResultTypeChecker.isAssertion(result)).toBeTruthy();
    });
    it("should execute payload", () => {
        const rule = RulesBuilder.create().setName("R001")
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(PayloadBuilder.asserts().that("__dataObject__.state != null", "assertion failed"))
            .build();
        const result = handler.executePayload(
            rule.payload as Payloads.Validation.AssertionPayload,
            rule,
            dataContext,
            session) as AssertionPayloadResult;
        expect(result.message!.errorMessage).toBe("assertion failed");
        expect(result.success).toBeFalsy();
    });
    it("should execute payload with no err message and return false", () => {
        const ruleNoError = RulesBuilder.create().setName("R001")
            .setContext(Policy.name)
            .setTargetPath(Policy.fields.state.name)
            .setPayload(PayloadBuilder.asserts().that("false", "message"))
            .build();
        const result = handler.executePayload(
            ruleNoError.payload as Payloads.Validation.AssertionPayload,
            ruleNoError,
            dataContext,
            session) as AssertionPayloadResult;
        expect(result.message!.errorMessage).toBe("message");
        expect(result.success).toBeFalsy();
    });
});
