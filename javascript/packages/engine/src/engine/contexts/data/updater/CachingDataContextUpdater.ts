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

import { DataContext } from "../DataContext";
import { DataContextUpdater, DataContextDependency } from "./DataContextUpdater";

export class CachingDataContextUpdater implements DataContextUpdater {

    private readonly cache = new Set<string>();
    private readonly dcCache = new WeakSet<DataContext>();

    constructor(private readonly dataContextUpdater: DataContextUpdater) { }

    update(dataContext: DataContext, dependency: DataContextDependency): void {
        const key = createCacheKey(dataContext, dependency);
        if (this.dcCache.has(dataContext) && this.cache.has(key)) {
            return;
        }
        this.dcCache.add(dataContext);
        this.cache.add(key);
        this.dataContextUpdater.update(dataContext, dependency);
    }
}

function createCacheKey(dataContext: DataContext, dependency: DataContextDependency): string {
    return `${dataContext.contextId}:${dependency.contextName}`;
}
