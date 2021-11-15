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

import { Contexts } from "kraken-model";
import ContextDefinition = Contexts.ContextDefinition;

export namespace ContextModelTree {

    /**
     * Context definition model that is used in runtime rules processing.
     * Contains context repository,
     */
    // tslint:disable-next-line: no-shadowed-variable
    export interface ContextModelTree {
        /**
         * Metadata of generated ContextModelTree
         */
        metadata: Metadata;
        /**
         * All available valid paths for context definitions
         * key - context definition name
         * value - paths from root
         */
        pathsToNodes: Record<string, ContextPath[]>;

        contexts: Record<string, ContextDefinition>;
    }

    export interface Metadata {
        namespace: string;
        targetEnvironment: TargetEnvironment;
    }

    /**
     * Enumerates all Target Environments supported by Kraken for rule execution
     */
    export type TargetEnvironment = "JAVA" | "JAVASCRIPT";

    /**
     * Path represented as context definition names
     */
    export interface ContextPath {
        /**
         * Names of the context definitions
         */
        path: string[];
    }
}
