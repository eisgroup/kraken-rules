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

import { ContextReference } from "./ContextReference";
import { PathToNode } from "./PathToNode";
/**
 * Resolves path to reference from data context to the data context
 * @since 1.1.1
 */
export interface ReferencePathResolver {
    /**
     * Resolves paths from origin data context ot the target
     *
     * @param origin path from
     * @param targetContextName context definition to reference
     *
     * @returns path with target data context cardinality
     * @throws in case the depth of common root to the reference is more than 2
     * @throws if resolved more than 1 path
     * @throws if no paths are to select from are provided
     */
    resolveReferencePath(origin: PathToNode, targetContextName: string): ContextReference;
}
