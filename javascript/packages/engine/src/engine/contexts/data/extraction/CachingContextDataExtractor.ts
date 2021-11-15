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

import { ContextDataExtractor } from "./ContextDataExtractor.types";
import { DataContext } from "../DataContext";

/**
 * Caches extraction of {@link DataContext}.
 *
 * @export
 * @class CachingContextDataExtractor
 * @implements {ContextDataExtractor}
 *
 * @since 11.2
 */
export class CachingContextDataExtractor implements ContextDataExtractor {

    private readonly cache: Map<string, DataContext[]>;

    constructor(
        private readonly extractor: ContextDataExtractor
    ) {
        this.cache = new Map();
    }

    extractByPath(root: DataContext, path: string[]): DataContext[] {
        const key = pathKey(root, path);
        if (this.cache.has(key)) {
            return this.cache.get(key)!;
        } else {
            const extracted = this.extractor.extractByPath(root, path);
            this.cache.set(key, extracted);
            return extracted;
        }
    }
    extractByName(childContextName: string, root: DataContext, restriction?: DataContext): DataContext[] {
        const key = nameKey(childContextName, root, restriction);
        if (this.cache.has(key)) {
            return this.cache.get(key)!;
        } else {
            const extracted = this.extractor.extractByName(childContextName, root, restriction);
            this.cache.set(key, extracted);
            return extracted;
        }
    }

}

function pathKey(root: DataContext, path: string[]): string {
    return `${root.contextId}:${path.join(".")}`;
}

function nameKey(
    childContextName: string,
    root: DataContext,
    restriction: DataContext | undefined
): string {
    return `${root.contextId}:${childContextName}:${restriction ? restriction.contextId : ""}:`;
}
