/* eslint-disable @typescript-eslint/ban-types */
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

/**
 * Caches dynamic functions created from string expressions and variables.
 * Compiles function and stores to cache.
 */
export class FunctionCache {
    private readonly cache: Record<string, Function> = {}

    compute(key: string, functionArgs: string[]): Function {
        if (this.cache[key] === undefined) {
            // eslint-disable-next-line prefer-spread
            this.cache[key] = Function.apply(undefined, functionArgs)
        }
        return this.cache[key]
    }
}
