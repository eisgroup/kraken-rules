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

import { PathToNode } from './PathToNode'

/**
 * Service to resolve common path of two {@link PathToNode}s.
 * @since 1.1.1
 */
export class CommonPathResolver {
    /**
     * @param {PathToNode} from
     * @param {PathToNode} to
     * @returns {PathToNode} common path
     * @example
     *  commonPathResolver.resolveCommon(
     *   ["Policy", "Vehicle"],
     *   ["Policy", "Vehicle", "Driver"]
     * ) // => ["Policy", "Vehicle"]
     *  commonPathResolver.resolveCommon(
     *   ["Policy", "Vehicle"],
     *   ["Policy", "Party", "Role"]
     * ) // => ["Policy"]
     */
    resolveCommon(from: PathToNode, to: PathToNode): PathToNode {
        for (let index = 0; index < from.length; index++) {
            if (from[index] !== to[index]) {
                return from.slice(0, index)
            }
        }
        return from
    }
}
