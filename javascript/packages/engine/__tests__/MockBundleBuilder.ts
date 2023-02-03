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

import { EntryPointBundle } from '../src/models/EntryPointBundle'
import { Rule } from 'kraken-model'

export class MockBundleBuilder {
    static builder(): MockBundleBuilder {
        return new MockBundleBuilder()
    }

    private rules: Rule[] = []
    private rulesOrder: Record<string, number> = {}
    private entryPointName = 'test'
    private delta = false

    addRule(rule: Rule): this {
        this.rules.push(rule)
        this.rulesOrder[rule.name] = Object.keys(this.rulesOrder).length
        return this
    }

    setEntryPointName(entryPointName: string): this {
        this.entryPointName = entryPointName
        return this
    }

    setDelta(): this {
        this.delta = true
        return this
    }

    build = (): EntryPointBundle.EntryPointEvaluation => {
        return {
            delta: this.delta,
            entryPointName: this.entryPointName,
            rules: this.rules,
            fieldOrder: [],
        }
    }
}
