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

import { CachingDataContextUpdater } from "../../../../../src/engine/contexts/data/updater/CachingDataContextUpdater";
import { DataContextUpdater } from "../../../../../src/engine/contexts/data/updater/DataContextUpdater";
import { mock } from "../../../../mock";

describe("CachingDataContextUpdater", () => {
    let updater: DataContextUpdater;
    let spy: jest.Mock;
    beforeEach(() => {
        spy = jest.fn();
        updater = new CachingDataContextUpdater({
            update: (root, deps) => {
                spy(root, deps);
                return undefined;
            }
        });
    });
    it("should call once per dependency", () => {
        updater.update(mock.dataContextEmpty(), { contextName: "test" });
        expect(spy).toHaveBeenCalledTimes(1);
    });
    it("should call once per same dependency", () => {
        const dc = mock.dataContextEmpty();
        updater.update(dc, { contextName: "test" });
        updater.update(dc, { contextName: "test" });
        expect(spy).toHaveBeenCalledTimes(1);
    });
    it("should call once per data context with same dependency", () => {
        const a = mock.dataContextEmpty();
        updater.update(mock.dataContextEmpty(), { contextName: "a" });
        updater.update(a, { contextName: "a" });
        updater.update(a, { contextName: "b" });
        updater.update(a, { contextName: "a" });
        updater.update(a, { contextName: "b" });
        expect(spy).toHaveBeenCalledTimes(3);
    });
});
