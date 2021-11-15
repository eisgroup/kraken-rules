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

import { CachingReferencePathResolver } from "../../../../src/engine/contexts/ccr/CachingReferencePathResolver";

describe("CachingReferencePathResolver", () => {
    it("should cache calls", () => {
        const resolveReferencePath = jest.fn();
        const resolver = new CachingReferencePathResolver({ resolveReferencePath });
        resolver.resolveReferencePath(["a"], "b");
        resolver.resolveReferencePath(["a"], "b");
        expect(resolveReferencePath).toHaveBeenCalledTimes(1);
    });
});
