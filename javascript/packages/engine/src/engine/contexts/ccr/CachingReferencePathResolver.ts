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

import { ReferencePathResolver } from "./ReferencePathResolver";
import { PathToNode } from "./PathToNode";
import { ContextReference } from "./ContextReference";

/**
 * Caches reference resolution results
 * @since 1.1.1
 */
export class CachingReferencePathResolver implements ReferencePathResolver {

    private readonly referencePathResolver: ReferencePathResolver;
    private readonly cache: Map<string, ContextReference>;

    constructor(referencePathResolver: ReferencePathResolver) {
        this.referencePathResolver = referencePathResolver;
        this.cache = new Map();
    }

    /**
     * @override
     */
    resolveReferencePath(origin: PathToNode, targetContextName: string): ContextReference {
        const key = `${origin.join(".")}:${targetContextName}`;
        if (this.cache.has(key)) {
            return this.cache.get(key)!;
        } else {
            const reference = this.referencePathResolver.resolveReferencePath(origin, targetContextName);
            this.cache.set(key, reference);
            return reference;
        }
    }

}
