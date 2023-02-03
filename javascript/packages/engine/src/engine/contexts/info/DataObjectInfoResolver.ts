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

/**
 * Resolves context name and id from supplied context object
 */

export interface DataErrorDefinition {
    message: string
}

export interface DataObjectInfoResolver {
    /**
     * Resolve context definition name for specified data object instance
     *
     * @param data  data object instance for context
     * @return      context definition name
     */
    resolveName: (data: object) => string
    /**
     * Resolve context instance id string from specified data object instance
     *
     * @param data  data object instance for context
     * @return      context instance id string
     */
    resolveId: (data: object) => string
    /**
     * Validates if supplied data object is supported by this resolver implementation
     *
     * @param data  data object instance for context
     * @return      an array of error definitions
     */
    validate: (data: object) => DataErrorDefinition[]
}
