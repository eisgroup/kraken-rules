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
import { Reducer } from 'declarative-js'
import { ExpressionEvaluationResult, RuleInfo, RuleOverride } from 'kraken-engine-api'
import { Contexts } from 'kraken-model'

import { ErrorCode, KrakenRuntimeError } from '../../error/KrakenRuntimeError'
import { DataContext } from '../contexts/data/DataContext'
import { ExpressionEvaluator } from '../runtime/expressions/ExpressionEvaluator'
import { Expressions } from '../runtime/expressions/Expressions'

export interface RuleOverrideContextExtractor {
    extract(
        dataContext: DataContext,
        ruleInfo: RuleInfo,
        dependencies: RuleOverride.OverrideDependencyInfo[],
        timestamp: Date,
    ): RuleOverride.OverridableRuleContextInfo
}

export class RuleOverrideContextExtractorImpl implements RuleOverrideContextExtractor {
    extract(
        dataContext: DataContext,
        ruleInfo: RuleInfo,
        dependencies: RuleOverride.OverrideDependencyInfo[],
        timestamp: Date,
    ): RuleOverride.OverridableRuleContextInfo {
        return {
            contextAttributeValue: extractAttributeValue(dataContext, ruleInfo.targetPath),
            contextId: dataContext.contextId,
            contextName: ruleInfo.context,
            rootContextId: extractRootId(dataContext),
            overrideDependencies: extractOverrideDependencies(dependencies, dataContext),
            rule: ruleInfo,
            ruleEvaluationTimeStamp: timestamp,
        }
    }
}

/**
 * Extracts {@link OverrideDependency}ies from {@link DataContext}
 * @param dependencies  info about rule override dependencies
 * @param dataContext   context definition instance, on which rule is defined
 */
export function extractOverrideDependencies(
    dependencies: RuleOverride.OverrideDependencyInfo[],
    dataContext: DataContext,
): Record<string, RuleOverride.OverrideDependency> {
    const overrideDependencies: RuleOverride.OverrideDependency[] = []
    const dependenciesKeys = new Set()
    for (const dependency of dependencies) {
        if (dependency.contextFieldName === undefined) {
            continue
        }
        const dataReference = dataContext.dataContextReferences[dependency.contextName]
        if (!dataReference || dataReference.cardinality !== 'SINGLE' || !dataReference.dataContexts.length) {
            continue
        }
        const ref = dataReference.dataContexts[0]
        const type = ref.definitionProjection[dependency.contextFieldName].fieldType as Contexts.PrimitiveDataType
        if (!Contexts.fieldTypeChecker.isPrimitive(type)) {
            continue
        }
        const name = RuleOverride.resolveOverrideDependencyName(ref.contextName, dependency.contextFieldName)
        if (dependenciesKeys.has(name)) {
            continue
        }
        const value = extractAttributeValue(ref, dependency.contextFieldName)
        dependenciesKeys.add(name)
        overrideDependencies.push({ name, type, value })
    }
    return overrideDependencies.reduce(
        Reducer.toObject(d => d.name),
        {},
    )
}

/**
 * Extracts value from data context by provided path
 *
 * @export
 * @param {DataContext} dataContext data context to extract value from
 * @param {string} targetPath       path to value in data object (context instance)
 * @returns {*} value from data context data object by target path
 */
export function extractAttributeValue(
    dataContext: DataContext,
    targetPath: string,
): Contexts.KrakenPrimitive | undefined {
    const path = Expressions.createPathResolver(dataContext)(targetPath)
    const result = ExpressionEvaluator.DEFAULT.evaluateGet(path, dataContext.dataObject)
    if (ExpressionEvaluationResult.isError(result)) {
        throw new KrakenRuntimeError(ErrorCode.EXTRACT_EXPRESSION_FAILED, `Failed to extract attribute ${path}`)
    }
    return result.success as Contexts.KrakenPrimitive | undefined
}

/**
 * Resolves root id from {@link DataContext}
 * @param dataContext current data context
 */
export function extractRootId(dataContext: DataContext): string {
    let dc = dataContext
    while (dc.parent) {
        dc = dc.parent
    }
    return dc.contextId
}
