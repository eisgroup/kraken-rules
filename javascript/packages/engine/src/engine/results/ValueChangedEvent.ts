/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

import { RuleEvent } from 'kraken-engine-api'

export class ValueChangedEvent implements RuleEvent {
    /**
     * @param {string} attributeTarget  Target path to changed field attribute
     * @param {string} contextName      Context definition name for context on which event was emitted
     * @param {string} contextId        Context instance identification string for context on which event was emitted
     * @param {Object} newValue         New field value
     * @param {Object} previousValue    Old field value
     */
    constructor(
        public readonly attributeTarget: string,
        public readonly contextName: string,
        public readonly contextId: string,
        public readonly newValue: unknown,
        public readonly previousValue: unknown,
    ) {}
}
