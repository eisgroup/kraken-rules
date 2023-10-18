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

import { Rule } from 'kraken-model'
import { WithKraken } from '../config'
import { debug } from '../debugger/Debugger'
import { EvaluationConfig } from './executer/SyncEngine'
import { DataContext } from './contexts/data/DataContext'
import { DateCalculator, DefaultDateCalculator } from './runtime/expressions/date/DateCalculator'

export class ExecutionSession {
    public readonly entryPointName: string
    public readonly currencyCd: string
    public readonly expressionContext: Record<string, unknown>
    public readonly timestamp: Date
    public readonly ruleTimezoneId: string
    public readonly dateCalculator: DateCalculator
    private readonly breakPointMatcher: debug.impl.BreakPointMatcher
    constructor(
        evaluationConfig: EvaluationConfig,
        expressionContext: Record<string, unknown>,
        entryPointName: string,
    ) {
        this.currencyCd = evaluationConfig.currencyCd
        this.expressionContext = {
            external: { ...evaluationConfig.context.externalData },
            dimensions: { ...evaluationConfig.context.dimensions },
            ...expressionContext,
        }
        this.timestamp = new Date()
        this.entryPointName = entryPointName
        this.dateCalculator = evaluationConfig.dateCalculator ?? new DefaultDateCalculator()
        this.ruleTimezoneId = evaluationConfig.ruleTimezoneId ?? Intl.DateTimeFormat().resolvedOptions().timeZone
        const g: unknown = globalThis
        if (globalKrakenObjectInitialized(g)) {
            this.breakPointMatcher = new debug.impl.BreakPointMatcher(g.Kraken.debugger.breakPoints, {
                ...g.Kraken.debugger,
            })
        } else {
            this.breakPointMatcher = new debug.impl.BreakPointMatcher(new Map(), { break: false, log: false })
        }
    }

    shouldBreak(rule: Rule, dataContext: DataContext): boolean {
        return this.breakPointMatcher.ruleMatches({
            contextId: dataContext.contextId,
            contextName: dataContext.contextName,
            entryPointName: this.entryPointName,
            ruleName: rule.name,
        })
    }

    shouldBreakOnEntryPoint(): boolean {
        return this.breakPointMatcher.entryPointMatches({ entryPointName: this.entryPointName })
    }
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
function globalKrakenObjectInitialized(o: any): o is WithKraken {
    return 'Kraken' in o
}
