/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import { mock } from "../../../mock";
import { PathCardinalityResolver } from "../../../../src/engine/contexts/ccr/PathCardinalityResolver";

const { Policy, Insured, Vehicle, FullCoverage } = mock.modelTreeJson.contexts;

describe("Reference Path Resolver", () => {
    let resolver: PathCardinalityResolver;
    beforeEach(() => {
        resolver = new PathCardinalityResolver(mock.modelTree.contexts);
    });
    it("should resolve to single with one element", () => {
        const cardinality = resolver.resolveCardinality({ path: [Policy.name] });
        expect(cardinality).toBe("SINGLE");
    });
    it("should resolve to single in multiple elements", () => {
        const cardinality = resolver.resolveCardinality({ path: [Policy.name, Insured.name] });
        expect(cardinality).toBe(Policy.children.Insured.cardinality);
        expect(cardinality).toBe("SINGLE");
    });
    it("should resolve to multiple in multiple elements", () => {
        const cardinality = resolver.resolveCardinality({ path: [Policy.name, Vehicle.name] });
        expect(cardinality).toBe(Policy.children.Vehicle.cardinality);
        expect(cardinality).toBe("MULTIPLE");
    });
    it("should throw on multiple in middle of path", () => {
        const cardinality = resolver.resolveCardinality({ path: [Policy.name, Vehicle.name, FullCoverage.name] });
        expect(cardinality).toBe(Policy.children.Vehicle.cardinality);
        expect(cardinality).toBe("MULTIPLE");
    });
});
