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

import { ContextDataProviderImpl } from "../../../src/engine/contexts/data/extraction/ContextDataProvider";
import { mock } from "../../mock";

describe("ContextDataProviderImpl", () => {
    const policy = mock.modelTreeJson.contexts.Policy;
    it("it should create instance", () => {
        const dataProviderImpl = new ContextDataProviderImpl(
            mock.data.dataContextEmpty(),
            mock.contextDataExtractor
        );
        expect(dataProviderImpl).not.toBeNull();
    });
    it("it should resolve context data", () => {
        const dataProviderImpl = new ContextDataProviderImpl(
            mock.data.dataContextEmpty(),
            mock.contextDataExtractor
        );
        const resolvedContextData = dataProviderImpl.resolveContextData(policy.name);
        expect(resolvedContextData).toHaveLength(1);
        expect(resolvedContextData.map(c => c.contextId).sort()).toMatchObject(["0"]);
    });
});
