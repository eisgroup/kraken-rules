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

import { SizePayloadHandler } from "../../../src/engine/handlers/SizePayloadHandler";
import { PayloadBuilder, RulesBuilder } from "kraken-model-builder";
import { DataContext } from "../../../src/engine/contexts/data/DataContext";
import { mock } from "../../mock";
import { Payloads, Rule } from "kraken-model";

const sizePayloadHandler = new SizePayloadHandler(mock.evaluator);
const { session } = mock;

describe("Size Payload Handler", () => {
    it("should succeed when array size is more than min", () => {
        const payload = PayloadBuilder.size().min(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(3), session);
        expect(result.success).toBeTruthy();
    });
    it("should fail when array is undefined and less than min", () => {
        const payload = PayloadBuilder.size().min(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(), session);
        expect(result.success).toBeFalsy();
    });
    it("should succeed when array size equals min", () => {
        const payload = PayloadBuilder.size().min(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(2), session);
        expect(result.success).toBeTruthy();
    });
    it("should fail when array size is less than min", () => {
        const payload = PayloadBuilder.size().min(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(1), session);
        expect(result.success).toBeFalsy();
    });
    it("should succeed when array size is less than max", () => {
        const payload = PayloadBuilder.size().max(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(1), session);
        expect(result.success).toBeTruthy();
    });
    it("should succeed when array size equals max", () => {
        const payload = PayloadBuilder.size().max(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(2), session);
        expect(result.success).toBeTruthy();
    });
    it("should succeed when array is undefined and less than max", () => {
        const payload = PayloadBuilder.size().max(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(), session);
        expect(result.success).toBeTruthy();
    });
    it("should fail when array size is more than max", () => {
        const payload = PayloadBuilder.size().max(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(3), session);
        expect(result.success).toBeFalsy();

    });
    it("should fail when array size is less than equals", () => {
        const payload = PayloadBuilder.size().equals(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(1), session);
        expect(result.success).toBeFalsy();
    });
    it("should succeed when array size is equal to equals", () => {
        const payload = PayloadBuilder.size().equals(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(2), session);
        expect(result.success).toBeTruthy();
    });
    it("should fail when array size is more equals", () => {
        const payload = PayloadBuilder.size().equals(2);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(3), session);
        expect(result.success).toBeFalsy();
    });
    it("should fail when array is undefined and not equal to equals", () => {
        const payload = PayloadBuilder.size().equals(1);
        const result = sizePayloadHandler.executePayload(payload, rule(payload), dataContext(), session);
        expect(result.success).toBeFalsy();
    });
});

function rule(payload: Payloads.Validation.SizePayload): Rule {
    return new RulesBuilder()
        .setName("rule")
        .setContext("Context")
        .setPayload(payload)
        .setTargetPath("path")
        .build();
}

function dataContext(size?: number): DataContext {
    const data = {
        path: size ? Array(size) : undefined
    };
    const dc = new DataContext(
        "id",
        "Context",
        data,
        mock.contextInstanceInfo,
        {},
        undefined
    );
    return dc;
}
