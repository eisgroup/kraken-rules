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
import { Payloads, Rule } from 'kraken-model'

import { EntryPointResult, FieldEvaluationResult, RuleEvaluationResults } from 'kraken-engine-api'
import { ContextDataProviderFactory } from '../contexts/data/extraction/ContextDataProviderFactory'
import { DataContextBuilder } from '../contexts/data/DataContextBuilder'
import { ExecutionSession } from '../ExecutionSession'

import { EntryPointBundle } from '../../models/EntryPointBundle'

import { RuleEvaluation, RulePayloadProcessor } from '../RulePayloadProcessor'
import { ContextDataExtractor } from '../contexts/data/extraction/ContextDataExtractor.types'
import { logger } from '../../utils/DevelopmentLogger'
import { DataContext } from '../contexts/data/DataContext'
import { ErrorCode, KrakenRuntimeError } from '../../error/KrakenRuntimeError'
import { payloadResultTypeChecker } from '../results/PayloadResultTypeChecker'
import { conditionEvaluationTypeChecker } from '../../dto/DefaultConditionEvaluationResult'
import { ContextDataProvider } from '../contexts/data/extraction/ContextDataProvider'
import { DefaultEntryPointResult } from '../../dto/DefaultEntryPointResult'
import { DefaultContextFieldInfo } from '../../dto/DefaultContextFieldInfo'
import { ErrorAwarePayloadResult } from 'kraken-engine-api/src'
import EntryPointEvaluation = EntryPointBundle.EntryPointEvaluation

import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult
import isNotApplicable = RuleEvaluationResults.isNotApplicable
import PayloadType = Payloads.PayloadType

type RuleOnInstanceEvaluationResult = {
    result: RuleEvaluationResult
    dataContext: DataContext
}

type FieldEvaluation = {
    field: string
    // holds evaluations per data context where key is contextId
    evaluations: Record<string, RuleEvaluation[]>
}

export class OrderedEvaluationLoop {
    static getInstance(options: {
        rulePayloadProcessor: RulePayloadProcessor
        contextDataExtractor: ContextDataExtractor
        contextBuilder: DataContextBuilder
        restriction?: object
    }): OrderedEvaluationLoop {
        return new OrderedEvaluationLoop(
            options.rulePayloadProcessor,
            new ContextDataProviderFactory(options.contextDataExtractor, options.contextBuilder, options.restriction),
        )
    }

    constructor(
        private readonly rulePayloadProcessor: RulePayloadProcessor,
        private readonly contextDataProviderFactory: ContextDataProviderFactory,
    ) {}

    evaluate(evaluation: EntryPointEvaluation, data: object, session: ExecutionSession): EntryPointResult {
        const dataProvider = this.contextDataProviderFactory.createContextProvider(data)

        const defaultResults = this.evaluateDefaultRules(evaluation, dataProvider, session)
        const otherResults = this.evaluateRules(evaluation, dataProvider, session)

        const results: Record<string, FieldEvaluationResult> = {}
        defaultResults.forEach(r => this.addResult(r, results))
        otherResults.forEach(r => this.addResult(r, results))

        this.validateDefaultsOnOneField(results)

        logger.info({ results })
        return new DefaultEntryPointResult(results)
    }

    private evaluateDefaultRules(
        entryPointEvaluation: EntryPointEvaluation,
        provider: ContextDataProvider,
        session: ExecutionSession,
    ): RuleOnInstanceEvaluationResult[] {
        logger.group('Default rules evaluation')
        const defaultRules = entryPointEvaluation.rules.filter(r => r.payload.type === PayloadType.DEFAULT)
        const results = this.doEvaluateDefaultRules(defaultRules, entryPointEvaluation.fieldOrder, provider, session)
        logger.groupEnd('Default rules evaluation')
        return results
    }

    private doEvaluateDefaultRules(
        defaultRules: Rule[],
        fieldOrder: string[],
        provider: ContextDataProvider,
        session: ExecutionSession,
    ): RuleOnInstanceEvaluationResult[] {
        const defaultRuleEvaluations = this.buildDefaultRuleEvaluations(defaultRules, provider)

        const allResults: RuleOnInstanceEvaluationResult[] = []
        for (const field of fieldOrder) {
            if (defaultRuleEvaluations[field]) {
                for (const evaluations of Object.values(defaultRuleEvaluations[field].evaluations)) {
                    const priorityOrderedEvaluations = evaluations.sort((a, b) => b.priority - a.priority)
                    const results = this.evaluateDefaultRulesInPriorityOrder(priorityOrderedEvaluations, session)
                    allResults.push(...results)
                }
            }
        }

        return allResults
    }

    private buildDefaultRuleEvaluations(
        defaultRules: Rule[],
        provider: ContextDataProvider,
    ): Record<string, FieldEvaluation> {
        logger.group('Context extraction')
        const defaultRuleEvaluations: Record<string, FieldEvaluation> = {}
        for (const rule of defaultRules) {
            for (const dataContext of this.resolveContexts(rule, provider)) {
                const field = `${dataContext.contextName}.${rule.targetPath}`
                if (!defaultRuleEvaluations[field]) {
                    defaultRuleEvaluations[field] = { field, evaluations: {} }
                }
                if (!defaultRuleEvaluations[field].evaluations[dataContext.contextId]) {
                    defaultRuleEvaluations[field].evaluations[dataContext.contextId] = []
                }
                defaultRuleEvaluations[field].evaluations[dataContext.contextId].push({
                    rule,
                    dataContext,
                    priority: rule.priority ?? 0,
                })
            }
        }
        logger.groupEnd('Context extraction')
        return defaultRuleEvaluations
    }

    private evaluateDefaultRulesInPriorityOrder(
        priorityOrderedEvaluations: RuleEvaluation[],
        session: ExecutionSession,
    ): RuleOnInstanceEvaluationResult[] {
        const prioritizedEvaluation = priorityOrderedEvaluations.length > 1
        const results: RuleOnInstanceEvaluationResult[] = []
        let appliedRuleEvaluation: RuleEvaluation | undefined = undefined
        for (const evaluation of priorityOrderedEvaluations) {
            if (appliedRuleEvaluation && appliedRuleEvaluation.priority > evaluation.priority) {
                const fieldId = this.toFieldId(evaluation.dataContext, evaluation.rule.targetPath)
                logger.debug(
                    `Suppressing rule '${evaluation.rule.name}' with priority '${evaluation.priority}' on ${fieldId} because rule '${appliedRuleEvaluation.rule.name}' with higher priority '${appliedRuleEvaluation.priority}' was applied. Evaluation status - UNUSED.`,
                )
                continue
            }

            const result = this.evaluateRulePayload(evaluation, session, prioritizedEvaluation)
            results.push(result)

            if (this.resolveEvaluationStatus(result) === 'APPLIED') {
                appliedRuleEvaluation = evaluation
            }
        }
        return results
    }

    private evaluateRules(
        entryPointEvaluation: EntryPointEvaluation,
        provider: ContextDataProvider,
        session: ExecutionSession,
    ): RuleOnInstanceEvaluationResult[] {
        logger.group('Other rules evaluation')

        const results = entryPointEvaluation.rules
            .filter(r => r.payload.type !== PayloadType.DEFAULT)
            .map(rule => this.evaluateRule(rule, provider, session))
            .reduce((p, n) => p.concat(n), [])

        logger.groupEnd('Other rules evaluation')

        return results
    }

    private evaluateRule(
        rule: Rule,
        provider: ContextDataProvider,
        session: ExecutionSession,
    ): RuleOnInstanceEvaluationResult[] {
        logger.group(`Rule evaluation: ${rule.name}`)

        const results = this.resolveContexts(rule, provider)
            .map(dataContext => <RuleEvaluation>{ rule, dataContext })
            .map(evaluation => this.evaluateRulePayload(evaluation, session, false))

        logger.debug(
            `Evaluated rule ${rule.name} on a total of ${results.length} instances.` +
                (results.length === 0 ? ' Evaluation status - UNUSED.' : ''),
        )
        logger.groupEnd(`Rule evaluation: ${rule.name}`)

        return results
    }

    private evaluateRulePayload(
        evaluation: RuleEvaluation,
        session: ExecutionSession,
        prioritizedEvaluation: boolean,
    ): RuleOnInstanceEvaluationResult {
        const result: RuleOnInstanceEvaluationResult = {
            result: this.rulePayloadProcessor.processRule(evaluation, session),
            dataContext: evaluation.dataContext,
        }

        const fieldId = this.toFieldId(evaluation.dataContext, evaluation.rule.targetPath)
        const evaluationStatus = this.resolveEvaluationStatus(result)

        if (evaluation.rule.payload.type === PayloadType.DEFAULT && prioritizedEvaluation) {
            logger.debug(
                `Evaluated rule '${evaluation.rule.name}' on ${fieldId} with priority ${evaluation.priority}. Evaluation status - ${evaluationStatus}.`,
            )
        } else {
            logger.debug(
                `Evaluated rule '${evaluation.rule.name}' on ${fieldId}. Evaluation status - ${evaluationStatus}.`,
            )
        }

        return result
    }

    private resolveEvaluationStatus(result: RuleOnInstanceEvaluationResult): 'SKIPPED' | 'APPLIED' | 'IGNORED' {
        if (result.result.conditionEvaluationResult.conditionEvaluation === 'ERROR') {
            return 'IGNORED'
        }
        if (isNotApplicable(result.result)) {
            return 'SKIPPED'
        }
        if ((result.result.payloadResult as ErrorAwarePayloadResult).error !== undefined) {
            return 'IGNORED'
        }
        return 'APPLIED'
    }

    private resolveContexts(rule: Rule, provider: ContextDataProvider): DataContext[] {
        const contexts = provider.resolveContextData(rule.context)

        logger.debug(`Resolved ${contexts.length} data context(s) for rule '${rule.name}' target '${rule.context}'.`)

        return contexts
    }

    private addResult(result: RuleOnInstanceEvaluationResult, results: Record<string, FieldEvaluationResult>) {
        const dataContext = result.dataContext
        const fieldName = result.result.ruleInfo.targetPath
        const id = this.toFieldId(dataContext, fieldName)
        if (!Object.hasOwnProperty.call(results, id)) {
            results[id] = {
                ruleResults: [],
                contextFieldInfo: new DefaultContextFieldInfo(dataContext, fieldName),
            }
        }
        results[id].ruleResults.push(result.result)
    }

    private toFieldId(context: DataContext, fieldName: string): string {
        return `${context.contextName}:${context.contextId}:${fieldName}`
    }

    private validateDefaultsOnOneField(results: Record<string, FieldEvaluationResult>): void {
        Object.keys(results).forEach(key => {
            const rulesOnOneField = results[key].ruleResults.filter(
                rr =>
                    conditionEvaluationTypeChecker.isApplicable(rr.conditionEvaluationResult) &&
                    !RuleEvaluationResults.isNotApplicable(rr) &&
                    payloadResultTypeChecker.isDefault(rr.payloadResult) &&
                    rr.payloadResult.error === undefined,
            )
            if (rulesOnOneField.length > 1) {
                throw new KrakenRuntimeError(
                    ErrorCode.MULTIPLE_DEFAULT,
                    "On field '" +
                        key +
                        "' applied '" +
                        rulesOnOneField.length +
                        "' default rules: '" +
                        rulesOnOneField.map(rr => rr.ruleInfo.ruleName).join(', ') +
                        "'. Only one default rule can be applied on the same field.",
                )
            }
        })
    }
}
