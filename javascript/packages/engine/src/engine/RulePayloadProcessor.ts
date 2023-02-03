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

import { Reducer, optional, MethodMap } from 'declarative-js'
import toMap = Reducer.toMap
import Map = Reducer.Map

import {
    RuleEvaluationResults,
    ConditionEvaluation,
    ValidationPayloadResult,
    RuleInfo,
    PayloadResult,
} from 'kraken-engine-api'
import RuleEvaluationResult = RuleEvaluationResults.RuleEvaluationResult
import { ExecutionSession } from './ExecutionSession'
import { RulePayloadHandler } from './handlers/RulePayloadHandler'
import { AssertionPayloadHandler } from './handlers/AssertionPayloadHandler'
import { DefaultValuePayloadHandler } from './handlers/DefaultValuePayloadHandler'
import { accessibilityPayloadHandler } from './handlers/AccessibilityPayloadHandler'
import { visibilityPayloadHandler } from './handlers/VisibilityPayloadHandler'
import { RegExpPayloadHandler } from './handlers/RegExpPayloadHandler'
import { UsagePayloadHandler } from './handlers/UsagePayloadHandler'
import { LengthPayloadHandler } from './handlers/LengthPayloadHandler'
import { SizePayloadHandler } from './handlers/SizePayloadHandler'
import { SizeRangePayloadHandler } from './handlers/SizeRangePayloadHandler'
import { ExpressionEvaluator } from './runtime/expressions/ExpressionEvaluator'
import { Payloads, Dependency, Expressions, Condition, Rule } from 'kraken-model'
import { RuleOverrideContextExtractor } from './results/RuleOverrideContextExtractor'
import { DataContext } from './contexts/data/DataContext'
import { payloadResultTypeChecker } from './results/PayloadResultTypeChecker'
import {
    conditionEvaluationTypeChecker,
    DefaultConditionEvaluationResult,
} from '../dto/DefaultConditionEvaluationResult'
import { DefaultRuleInfo } from './results/DefaultRuleInfo'
import { DataContextDependency, DataContextUpdater } from './contexts/data/updater/DataContextUpdater'
import { RuleConditionProcessor } from './RuleConditionProcessor'
import ExpressionVariable = Expressions.ExpressionVariable

export type RuleEvaluation = {
    rule: Rule
    dataContext: DataContext
    priority: number
}

export class RulePayloadProcessor {
    private readonly payloadHandlers: MethodMap<RulePayloadHandler>
    private readonly overrideExtractor: RuleOverrideContextExtractor
    private readonly ruleConditionProcessor: RuleConditionProcessor
    private readonly dataContextUpdater: DataContextUpdater

    constructor(
        expressionEvaluator: ExpressionEvaluator,
        overrideExtractor: RuleOverrideContextExtractor,
        dataContextUpdater: DataContextUpdater,
    ) {
        this.overrideExtractor = overrideExtractor
        this.payloadHandlers = [
            new AssertionPayloadHandler(expressionEvaluator),
            new DefaultValuePayloadHandler(expressionEvaluator),
            accessibilityPayloadHandler,
            visibilityPayloadHandler,
            new RegExpPayloadHandler(expressionEvaluator),
            new UsagePayloadHandler(expressionEvaluator),
            new LengthPayloadHandler(expressionEvaluator),
            new SizePayloadHandler(expressionEvaluator),
            new SizeRangePayloadHandler(expressionEvaluator),
        ].reduce(
            toMap(h => h.handlesPayloadType().toString()),
            Map(),
        )
        this.ruleConditionProcessor = new RuleConditionProcessor(expressionEvaluator)
        this.dataContextUpdater = dataContextUpdater
    }

    processRule(ruleEvaluation: RuleEvaluation, session: ExecutionSession): RuleEvaluationResult {
        const { rule, dataContext } = ruleEvaluation

        const ruleInfo = new DefaultRuleInfo(rule)

        extractConditionDependencies(rule.condition).forEach(dependency =>
            this.dataContextUpdater.update(dataContext, dependency),
        )
        const conditionResult = this.ruleConditionProcessor.evaluateCondition(dataContext, session, rule.condition)

        if (conditionEvaluationTypeChecker.isApplicable(conditionResult)) {
            extractPayloadDependencies(rule.payload).forEach(dependency =>
                this.dataContextUpdater.update(dataContext, dependency),
            )
            const payloadResult = this.evaluatePayload(ruleEvaluation, session)

            if (payloadResultTypeChecker.isValidation(payloadResult) && Payloads.isValidationPayload(rule.payload)) {
                // results for failed validation with rule
                return this.resolveValidationResult(
                    payloadResult,
                    rule.payload,
                    dataContext,
                    ruleInfo,
                    rule.dependencies || [],
                    session.timestamp,
                )
            } else {
                return {
                    kind: RuleEvaluationResults.Kind.REGULAR,
                    ruleInfo,
                    conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                    payloadResult,
                }
            }
        } else {
            return {
                kind: RuleEvaluationResults.Kind.NOT_APPLICABLE,
                ruleInfo,
                conditionEvaluationResult: conditionResult,
            }
        }
    }

    private evaluatePayload(ruleEvaluation: RuleEvaluation, session: ExecutionSession): PayloadResult {
        const { rule, dataContext } = ruleEvaluation
        return optional(this.payloadHandlers.get(rule.payload.type))
            .orElseThrow(`Not supported payload type: ${rule.payload.type}`)
            .executePayload(rule.payload, rule, dataContext, session)
    }

    private resolveValidationResult(
        payloadResult: ValidationPayloadResult,
        payload: Payloads.Validation.ValidationPayload,
        dataContext: DataContext,
        ruleInfo: RuleInfo,
        dependencies: Dependency[],
        timestamp: Date,
    ): RuleEvaluationResults.ValidationRuleEvaluationResult {
        if (payloadResult.success === false && payload.isOverridable) {
            return {
                kind: RuleEvaluationResults.Kind.VALIDATION,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                ruleInfo,
                payloadResult,
                overrideInfo: {
                    overrideApplicable: true,
                    overridable: payload.isOverridable,
                    overrideGroup: payload.overrideGroup,
                    overrideContext: this.overrideExtractor.extract(
                        dataContext,
                        ruleInfo,
                        dependencies
                            .filter(d => d.ccrDependency || d.selfDependency)
                            .map(d => ({
                                contextName: d.contextName,
                                contextFieldName: d.fieldName,
                            })),
                        timestamp,
                    ),
                },
            }
        } else {
            return {
                kind: RuleEvaluationResults.Kind.VALIDATION,
                conditionEvaluationResult: DefaultConditionEvaluationResult.of(ConditionEvaluation.APPLICABLE),
                ruleInfo,
                payloadResult,
                overrideInfo: {
                    overrideApplicable: false,
                    overridable: Boolean(payload.isOverridable),
                    overrideGroup: payload.overrideGroup,
                },
            }
        }
    }
}

function extractPayloadDependencies(payload: Payloads.Payload): DataContextDependency[] {
    const allExpressionVariables: ExpressionVariable[] = []

    // validation error message template variables extraction
    if (
        Payloads.isValidationPayload(payload) &&
        payload.errorMessage &&
        payload.errorMessage.templateExpressions &&
        payload.errorMessage.templateExpressions.length
    ) {
        for (const te of payload.errorMessage.templateExpressions) {
            if (Expressions.isComplex(te) && te.expressionVariables) {
                allExpressionVariables.push(...te.expressionVariables)
            }
        }
    }

    if (
        Payloads.isAssertionPayload(payload) &&
        Expressions.isComplex(payload.assertionExpression) &&
        payload.assertionExpression.expressionVariables
    ) {
        allExpressionVariables.push(...payload.assertionExpression.expressionVariables)
    }

    if (
        Payloads.isDefaultValuePayload(payload) &&
        Expressions.isComplex(payload.valueExpression) &&
        payload.valueExpression.expressionVariables
    ) {
        allExpressionVariables.push(...payload.valueExpression.expressionVariables)
    }

    return toDataContextDependencies(allExpressionVariables)
}

function extractConditionDependencies(condition?: Condition): DataContextDependency[] {
    if (condition && Expressions.isComplex(condition.expression) && condition.expression.expressionVariables) {
        return toDataContextDependencies(condition.expression.expressionVariables)
    }
    return []
}

function toDataContextDependencies(expressionVariables: ExpressionVariable[]): DataContextDependency[] {
    return expressionVariables
        .filter(variable => variable.type === 'CROSS_CONTEXT')
        .map(variable => <DataContextDependency>{ contextName: variable.name })
}
