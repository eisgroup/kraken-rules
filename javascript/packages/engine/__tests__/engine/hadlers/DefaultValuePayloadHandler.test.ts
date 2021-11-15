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

import { DataContext } from "../../../src/engine/contexts/data/DataContext";
import { mock } from "../../mock";
import { DefaultValuePayloadHandler } from "../../../src/engine/handlers/DefaultValuePayloadHandler";
import { Payloads } from "kraken-model";
import DefaultingType = Payloads.Derive.DefaultingType;
import DefaultValuePayload = Payloads.Derive.DefaultValuePayload;
import { ValueChangedEvent } from "../../../src/engine/results/Events";
import { DefaultValuePayloadResult, payloadResultTypeChecker } from "../../../src/engine/results/PayloadResult";
import { RulesBuilder, PayloadBuilder } from "kraken-model-builder";

const handler = new DefaultValuePayloadHandler(mock.evaluator);
const { session } = mock;
let dataContext: DataContext;

type Data = {
    cat: {
        name: string
        lastName: string
    }
};

beforeEach(() => {
    const data = { cat: { name: "Tom" } };
    dataContext = new DataContext("1", "PersonContext", data, mock.contextInstanceInfo, {}, undefined);
});

describe("defaultValuePayloadHandler", () => {
    describe("Default type", () => {
        it("should get payload type", () => {
            expect(handler.handlesPayloadType()).toBe(Payloads.PayloadType.DEFAULT);
        });
        it("should return payload payloadResult with type DefaultValuePayloadResult", () => {
            const rule = RulesBuilder.create()
                .setContext("R001")
                .setTargetPath("cat.name")
                .setPayload(PayloadBuilder.default().to("'Murzik'"))
                .setName("mock")
                .build();
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                mock.session
            );
            expect(payloadResultTypeChecker.isDefault(result)).toBeTruthy();
        });
        it("should not change value", () => {
            const rule = RulesBuilder.create()
                .setContext("R001")
                .setTargetPath("cat.name")
                .setPayload(PayloadBuilder.default().to("'Murzik'"))
                .setName("mock")
                .build();
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                mock.session
            ) as DefaultValuePayloadResult;
            expect(result.events).toHaveLength(0);
        });
        it("should create not existing key and set value", () => {
            const rule = RulesBuilder.create()
                .setContext("R001")
                .setTargetPath("cat.lastName")
                .setPayload(PayloadBuilder.default().to("'Thomas'"))
                .setName("mock")
                .build();
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session
            ) as DefaultValuePayloadResult;
            expect(result.events).toHaveLength(1);
            expect((result.events![0] as ValueChangedEvent).newValue)
                .toBe((dataContext.dataObject as Data).cat.lastName);
        });
    });
    describe("Reset type", () => {
        it("should reset value", () => {
            const rule = RulesBuilder.create()
                .setContext("R001")
                .setTargetPath("cat.name")
                .setPayload(PayloadBuilder.default().apply(DefaultingType.resetValue).to("'Jerry'"))
                .setName("mock")
                .build();
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session
            ) as DefaultValuePayloadResult;
            expect(result.events).toHaveLength(1);
            expect((result.events![0] as ValueChangedEvent).newValue).toBe("Jerry");
            expect((result.events![0] as ValueChangedEvent).newValue)
                .toBe((dataContext.dataObject as Data)["cat"]["name"]);
            expect((result.events![0] as ValueChangedEvent).previousValue).toBe("Tom");
        });
        it("should not change value", () => {
            const rule = RulesBuilder.create()
                .setContext("R001")
                .setTargetPath("cat.name")
                .setPayload(PayloadBuilder.default().apply(DefaultingType.resetValue).to("'Tom'"))
                .setName("mock")
                .build();
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session
            ) as DefaultValuePayloadResult;
            expect(result.events).toHaveLength(0);
        });
        it("should change value from undefined to Thomas", () => {
            const rule = RulesBuilder.create()
                .setContext("R001")
                .setTargetPath("cat.lastName")
                .setPayload(PayloadBuilder.default().apply(DefaultingType.resetValue).to("'Thomas'"))
                .setName("mock")
                .build();
            const result = handler.executePayload(
                rule.payload as DefaultValuePayload,
                rule,
                dataContext,
                session
            ) as DefaultValuePayloadResult;
            expect(result.events).toHaveLength(1);
            expect((result.events![0] as ValueChangedEvent).newValue).toBe("Thomas");
            expect((result.events![0] as ValueChangedEvent).newValue)
                .toBe((dataContext.dataObject as Data)["cat"]["lastName"]);
            expect((result.events![0] as ValueChangedEvent).previousValue).not.toBeDefined();
        });
    });
});
