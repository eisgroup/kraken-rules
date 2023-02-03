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

import { Payloads, Rule } from 'kraken-model'
import { RuleInfo } from 'kraken-engine-api'

export class DefaultRuleInfo implements RuleInfo {
    readonly ruleName: string
    readonly context: string
    readonly targetPath: string
    readonly payloadtype: Payloads.PayloadType

    constructor(rule: Rule) {
        this.context = rule.context
        this.ruleName = rule.name
        this.targetPath = rule.targetPath
        this.payloadtype = rule.payload.type
    }
}
