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

import {
    FunctionRegistry
} from "../../../../../src/engine/runtime/expressions/functionLibrary/Registry";
import { mock } from "../../../../mock";

let r: FunctionRegistry;
beforeEach(() => {
    r = new FunctionRegistry();
});

describe("Functions registry", () => {
    it("should return function names", () => {
        expect(r.names()).toContain("Now");
    });
    it("should return functions", () => {
        expect(r.functions()).toHaveLength(67);
    });
    it("should add function", () => {
        const length = r.names().length;
        r.add({
            name: "function_name",
            function: Math.abs
        });
        expect(r.names()).toHaveLength(length + 1);
    });
    it("should fail on adding non function object as a function", () => {
        const add = () => r.add({
            name: "function_name",
            // @ts-expect-error
            function: {}
        });
        expect(add).toThrowError("Failed to add function to registry: function is type of object");
    });
    it("should fail on adding empty string as a function name", () => {
        const add = () => r.add({
            name: "",
            function: Math.abs
        });
        expect(add).toThrowError("Failed to add function to registry: name is empty");
    });
    it("should fail on adding function with same name", () => {
        const add = () => r.add({
            name: "function_name",
            function: Math.abs
        });
        add();
        expect(add).toThrowError(
            "Failed to add function to registry: function with name 'function_name' already registered"
        );
    });
    it("should fail on adding function with '.' in name", () => {
        const add = () => r.add({
            name: "library.function_name",
            function: Math.abs
        });
        expect(add).toThrowError("Failed to add function to registry: function name cannot contain symbol '.'");
    });
    it("should fail on adding undefined or null as a function name", () => {
        const addU = () => r.add({
            // @ts-expect-error
            name: undefined,
            function: Math.abs
        });
        const addN = () => r.add({

            name: null as unknown as string,
            function: Math.abs
        });
        expect(addN).toThrowError("Failed to add function to registry: name is required");
        expect(addU).toThrowError("Failed to add function to registry: name is required");
    });
    it("should fail on adding undefined or null as a function", () => {
        const addU = () => r.add({
            name: "function_name",
            // @ts-expect-error
            function: undefined
        });
        const addN = () => r.add({
            name: "function_name",
            // @ts-expect-error
            function: null
        });
        expect(addN).toThrowError("Failed to add function to registry: function is required");
        expect(addU).toThrowError("Failed to add function to registry: function is required");
    });
    describe("createInstanceFunctions", () => {
        const functions = FunctionRegistry.createInstanceFunctions(
            mock.spi.dataResolver,
            (name: string) => {
                return mock.extendedModelTree.contexts[name].inheritedContexts;
            }
        );
        it("should create 3 functions", () => {
            expect(functions).toHaveLength(3);
            expect(functions.map(x => x.name)).toContain("_t");
            expect(functions.map(x => x.name)).toContain("_i");
            expect(functions.map(x => x.name)).toContain("GetType");
        });
        it("should create type of Function", () => {
            const t = functions.find(x => x.name === "_t")!.function as Function;
            expect(t(mock.data.empty(), mock.modelTreeJson.contexts.Policy.name)).toBeTruthy();
            expect(t(mock.data.empty(), mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
            expect(t(mock.data.empty(), undefined)).toBeFalsy();
            expect(t(undefined, mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
            expect(t({}, mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
        });
        it("should create instance of Function", () => {
            const i = functions.find(x => x.name === "_i")!.function as Function;
            const data = mock.data.emptyExtended();
            expect(i(data, mock.modelTreeJson.contexts.Policy.name)).toBeTruthy();
            expect(i(data, mock.extendedModelTreeJson.contexts.PolicyExtended.name)).toBeTruthy();
            expect(i(data, mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
            expect(i(undefined, mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
            expect(i({}, mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
            expect(i({ cd: "not existing" }, mock.modelTreeJson.contexts.AddressInfo.name)).toBeFalsy();
            expect(i(data, undefined)).toBeFalsy();
        });
        it("should create GetType function", () => {
            const data = mock.data.emptyExtended();
            const getType = functions.find(x => x.name === "GetType")!.function as Function;
            expect(getType(data)).toBe(mock.extendedModelTreeJson.contexts.PolicyExtended.name);
            expect(getType({})).not.toBeDefined();
            expect(getType(undefined)).not.toBeDefined();
        });
    });
});
