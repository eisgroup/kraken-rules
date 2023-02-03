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

export enum NavigationStepType {
    /**
     * this step type represents extraction from root context to child context
     */
    EXTRACTION,

    /**
     * this step type represents access by index in list or array
     */
    LIST_INDEX,

    /**
     * this step type represents accessing inherited parent context from child
     */
    PARENT_ACCESS,
}

/**
 * Describes metadata about one navigation step, collected in {@link NavigationStepsContextInstanceInfo}.
 * Each navigation step represents one context extraction operation, performed by kraken engine,
 * or indexing within results of extraction.
 */
export class NavigationStep {
    constructor(
        readonly type: NavigationStepType,
        readonly contextName: string,
        readonly contextId: string,
        readonly expression?: string | number,
    ) {}
}
