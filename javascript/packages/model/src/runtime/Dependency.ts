/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

export interface Dependency {
    contextName: string

    /**
     * Can be undefined if dependency is to a context without field name
     */
    fieldName?: string

    /**
     *
     * Indicates if dependency is resolved from a cross context reference.
     * Reference to rule target context is NOT a cross context reference but rather a {@link #selfDependency}.
     */
    ccrDependency: boolean

    /**
     * Indicates if dependency is resolved from a reference to target context or a field of target context.
     */
    selfDependency: boolean
}
