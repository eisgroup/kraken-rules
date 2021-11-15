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

import { ContextDataExtractorImpl } from "../../../src/engine/contexts/data/extraction/ContextDataExtractorImpl";
import { mock } from "../../mock";
import { ExtractedChildDataContextBuilder } from "../../../src/engine/contexts/data/ExtractedChildDataContextBuilder";
import { TestProduct } from "kraken-test-product";

const extractor: ContextDataExtractorImpl = mock.contextDataExtractor;
const { Policy, DriverInfo, Party, PartyRole } = mock.modelTreeJson.contexts;

describe("ContextDataExtractor", () => {
    it("should extract root context by name from object", () => {
        const contexts = extractor.extractByName(Policy.name, mock.data.dataContextEmpty());
        expect(contexts[0].contextName).toMatch(Policy.name);
        expect(contexts).toHaveLength(1);
    });
    it("should extract in array extended context", () => {
        const dc = mock.data.dataContextEmpty();
        const contexts = extractor.extractByName(PartyRole.name, dc);
        expect(contexts).toHaveLength(1);
        expect(contexts[0].contextName).toMatch(PartyRole.name);
    });
    it("should extract address context with restriction", () => {
        const ex = new ContextDataExtractorImpl(
            mock.modelTree,
            new ExtractedChildDataContextBuilder(mock.contextBuilder, mock.evaluator)
        );
        const dataContext = mock.data.dataContextEmpty();
        const policy = dataContext.dataObject as TestProduct.kraken.testproduct.domain.Policy;
        policy.parties!.push({
            cd: Party.name,
            id: "newPartyId",
            driverInfo: {
                id: "newPartyId-driverInfo",
                cd: DriverInfo.name
            }
        });
        const contexts = ex.extractByName(Party.name, dataContext);
        expect(contexts).toHaveLength(2);
        const restriction = contexts.find(dc => dc.contextId === "newPartyId");
        expect(restriction).toBeDefined();

        const infos = ex.extractByName(DriverInfo.name, dataContext, restriction);
        expect(infos).toHaveLength(1);
        expect(infos[0].contextId).toBe("newPartyId-driverInfo");
    });

    it("should throw when childContextName is undefined", () => {
        // @ts-expect-error
        expect(() => extractor.extractByName(undefined, mock.data.dataContextEmptyExtended()))
            .toThrow("childContextName");
        expect(() => extractor.extractByName("", mock.data.dataContextEmptyExtended())).toThrow("childContextName");
    });

    it("should throw when root is undefined", () => {
        // @ts-expect-error
        expect(() => extractor.extractByName("Person", undefined)).toThrow("root");
    });

    it("should throw when invalid child name provided", () => {
        expect(() => extractor.extractByName("NotValid", mock.data.dataContextEmptyExtended())).toThrow("NotValid");
    });
});
