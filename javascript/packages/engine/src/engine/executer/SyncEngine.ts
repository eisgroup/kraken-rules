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

import { ContextInstanceInfoResolver } from '../contexts/info/ContextInstanceInfoResolver'
import { DataObjectInfoResolver } from '../contexts/info/DataObjectInfoResolver'
import { EntryPointResult } from 'kraken-engine-api'
import { ExecutionSession } from '../ExecutionSession'
import { requireDefinedValue } from '../../utils/Utils'
import { ContextModelTree } from '../../models/ContextModelTree'
import { RulePayloadProcessor } from '../RulePayloadProcessor'
import { ExpressionEvaluator, KelFunction } from '../runtime/expressions/ExpressionEvaluator'
import { DataContextBuilder } from '../contexts/data/DataContextBuilder'
import { ExtractedChildDataContextBuilder } from '../contexts/data/ExtractedChildDataContextBuilder'
import { ContextDataExtractorImpl } from '../contexts/data/extraction/ContextDataExtractorImpl'
import { FunctionRegistry } from '../runtime/expressions/functionLibrary/Registry'
import { RuleOverrideContextExtractorImpl } from '../results/RuleOverrideContextExtractor'
import { logger, restartLogger } from '../../utils/DevelopmentLogger'
import { DataContextUpdaterImpl } from '../contexts/data/updater/DataContextUpdaterImpl'
import { ContextDataExtractor } from '../contexts/data/extraction/ContextDataExtractor.types'
import { CachingContextDataExtractor } from '../contexts/data/extraction/CachingContextDataExtractor'
import { ReferencePathResolverImpl } from '../contexts/ccr/ReferencePathResolverImpl'
import { PathCardinalityResolver } from '../contexts/ccr/PathCardinalityResolver'
import { CommonPathResolver } from '../contexts/ccr/CommonPathResolver'
import { CachingReferencePathResolver } from '../contexts/ccr/CachingReferencePathResolver'
import { ENGINE_VERSION } from '../generated/engineVersion'
import { DefaultEntryPointResult } from '../../dto/DefaultEntryPointResult'
import {
    KrakenRuntimeError,
    RESTRICTION_NODE_NOT_VALID_CONTEXT,
    SystemMessageBuilder,
} from '../../error/KrakenRuntimeError'
import { EntryPointBundle } from '../../models/EntryPointBundle'
import EntryPointEvaluation = EntryPointBundle.EntryPointEvaluation
import { EvaluationMode, isSupportedPayloadType } from '../runtime/EvaluationMode'
import { EntryPointBundleCache, ruleTimezoneIdDimension } from '../../bundle-cache/EntryPointBundleCache'
import { OrderedEvaluationLoop } from '../processors/OrderedEvaluationLoop'
import { DateCalculator } from '../runtime/expressions/date/DateCalculator'
import { DataContextPathProvider, DEFAULT_PATH_PROVIDER } from '../runtime/DataContextPathProvider'

export interface EvaluationConfig {
    context: {
        /**
         * Dimension values, that will be as a filter to get rules and entrypoints
         * that applies to that dimensions
         */
        dimensions?: Record<string, unknown>
        externalData?: Record<string, unknown>
        [keyof: string]: unknown
    }

    /**
     * Currency to use when interpreting monetary types
     */
    currencyCd: string

    /**
     * Evaluation mode to use when evaluating rules. Evaluation mode dictates what type
     * of rules will be evaluated during a Kraken engine call.
     *
     * If evaluation mode is not specified, then {@link EvaluationMode.ALL} is used by default.
     */
    evaluationMode?: EvaluationMode

    /**
     * This id enables Kraken to treat different rules evaluations as on evaluation.
     * This enables data extraction caching between different evaluations.
     */
    evaluationId?: string

    /**
     *
     */
    dataContextPathProvider?: DataContextPathProvider

    /**
     * Timezone used for rule date calculations. If not specified then system locale will be used for calculations.
     * If specified, then {@link SyncEngineConfig#cache} will be queried and cached by using rule timezone id as
     * an additional dimension by key with value {@link ruleTimezoneIdDimension}
     */
    ruleTimezoneId?: string

    /**
     * Environment specific implementation of {@link DateCalculator}.
     * If not specified then default calculator is used which does not support timezone specific date calculations.
     */
    dateCalculator?: DateCalculator
}

/**
 * Config to pass when calling {@link Engine.withConfig}
 */
export interface SyncEngineConfig {
    /**
     * Cached artifacts grouped by dimensions
     */
    cache: EntryPointBundleCache
    /**
     * Resolves context definition name for specified data object instance
     */
    dataInfoResolver: DataObjectInfoResolver
    /**
     * Provides three different info resolution methods, which are called depending on how
     * particular context instance data object was extracted
     */
    contextInstanceInfoResolver: ContextInstanceInfoResolver<unknown>

    /**
     * Static context definition model
     */
    modelTree: ContextModelTree.ContextModelTree

    /**
     * Custom functions that can be invoked in rules within this engine instance
     */
    functions?: KelFunction[]

    /**
     * Used for compatibility check with the backend.
     * {@link EntryPointBundle.EntryPointBundle}#engineVersion must match against
     * this version. Default version is backend artifact version at a release time.
     */
    engineCompatibilityVersion?: string
}
type CachedDataExtractionServices = {
    contextDataExtractor: CachingContextDataExtractor
}

export class SyncEngine {
    readonly #rulePayloadProcessor: RulePayloadProcessor
    readonly #dataContextBuilder: DataContextBuilder
    readonly #contextDataExtractor: ContextDataExtractor
    readonly #bundleCache: EntryPointBundleCache
    readonly #dataInfoResolver: DataObjectInfoResolver

    readonly #expressionEvaluator: ExpressionEvaluator
    readonly #engineCompatibilityVersion?: string
    #dataContextPathProvider?: DataContextPathProvider

    private evaluationId?: string
    private serviceCache?: CachedDataExtractionServices

    constructor(config: SyncEngineConfig) {
        function getContextDataBuilder(this: SyncEngine) {
            if (this.#dataContextPathProvider) {
                return this.#dataContextPathProvider
            }

            return DEFAULT_PATH_PROVIDER
        }
        function resolveInheritance(name: string): string[] {
            return config.modelTree.contexts[name].inheritedContexts
        }
        this.#expressionEvaluator = new ExpressionEvaluator(
            FunctionRegistry.createInstanceFunctions(config.dataInfoResolver, resolveInheritance),
            config.functions ?? [],
        )
        // TODO refactor to pass function data context builder... ?? ->
        this.#dataContextBuilder = new DataContextBuilder(
            config.modelTree,
            config.contextInstanceInfoResolver,
            getContextDataBuilder.bind(this),
        )
        this.#contextDataExtractor = new ContextDataExtractorImpl(
            config.modelTree,
            new ExtractedChildDataContextBuilder(this.#dataContextBuilder, this.#expressionEvaluator),
        )
        const referencePathResolver = new CachingReferencePathResolver(
            new ReferencePathResolverImpl(
                config.modelTree.pathsToNodes,
                new PathCardinalityResolver(config.modelTree.contexts),
                new CommonPathResolver(),
            ),
        )
        const dataContextUpdater = new DataContextUpdaterImpl(referencePathResolver, (root, path) =>
            this.#contextDataExtractor.extractByPath(root, path),
        )
        this.#rulePayloadProcessor = new RulePayloadProcessor(
            this.#expressionEvaluator,
            new RuleOverrideContextExtractorImpl(),
            dataContextUpdater,
            config.modelTree,
        )
        this.#bundleCache = config.cache
        this.#dataInfoResolver = config.dataInfoResolver
        this.#engineCompatibilityVersion = config.engineCompatibilityVersion
    }

    evaluate(data: object, entryPointName: string, config: EvaluationConfig): EntryPointResult {
        restartLogger()
        this.updateDataContextPathProvider(config)
        return logger.group(
            () => `Kraken logs: '${entryPointName}'`,
            () => this.doEvaluate(data, entryPointName, config),
            result => `Kraken logs: '${entryPointName}'. Timestamp: ${result.evaluationTimestamp.toISOString()}`,
        )
    }

    evaluateSubTree(data: object, node: object, entryPointName: string, config: EvaluationConfig): EntryPointResult {
        restartLogger()
        this.updateDataContextPathProvider(config)
        return logger.group(
            () => `Kraken logs: '${entryPointName}'`,
            () => this.doEvaluate(data, entryPointName, config, this.requireValidNode(node)),
            result => `Kraken logs: '${entryPointName}'. Timestamp: ${result.evaluationTimestamp.toISOString()}`,
        )
    }

    private doEvaluate = (data: object, entryPointName: string, evaluationConfig: EvaluationConfig, node?: object) => {
        const start = Date.now()

        this.logEngineInputData(data, evaluationConfig, node)
        const bundleDimensionContext = {
            ...evaluationConfig.context.dimensions,
        }
        if (evaluationConfig.ruleTimezoneId) {
            bundleDimensionContext[ruleTimezoneIdDimension] = evaluationConfig.ruleTimezoneId
        }

        const bundle = this.#bundleCache.get(entryPointName, bundleDimensionContext)

        const version = this.#engineCompatibilityVersion || ENGINE_VERSION
        if (bundle.engineVersion && this.normalizedVersion(bundle.engineVersion) !== this.normalizedVersion(version)) {
            logger.warning(
                () => `UI engine (${version}) and backend engine (${bundle.engineVersion}) versions are different.`,
            )
        }

        requireDefinedValue(evaluationConfig.currencyCd, 'Currency code cannot be absent')
        const session = new ExecutionSession(
            evaluationConfig,
            bundle.expressionContext,
            bundle.evaluation.entryPointName,
        )

        const filteredRulesEvaluation = this.filterRules(bundle.evaluation, evaluationConfig.evaluationMode)
        if (!filteredRulesEvaluation.rules.length) {
            logger.debug(() => `Kraken rules evaluation took ${Math.round(Date.now() - start)} ms`, true)
            return new DefaultEntryPointResult({}, session.timestamp, session.ruleTimezoneId)
        }
        this.#expressionEvaluator.rebuildFunctions(
            FunctionRegistry.INSTANCE.bindRegisteredFunctions({
                zoneId: session.ruleTimezoneId,
                dateCalculator: session.dateCalculator,
            }),
        )
        const cachingServices = this.getCachingServices(evaluationConfig.evaluationId)
        const result = OrderedEvaluationLoop.getInstance({
            rulePayloadProcessor: this.#rulePayloadProcessor,
            contextBuilder: this.#dataContextBuilder,
            contextDataExtractor: cachingServices.contextDataExtractor,
            restriction: node,
        }).evaluate(filteredRulesEvaluation, data, session)
        logger.debug(() => `Kraken rules evaluation took ${Math.round(Date.now() - start)} ms`, true)
        return result
    }

    private logEngineInputData(data: object, evaluationConfig: EvaluationConfig, node?: object) {
        logger.debug(() => [`Entity:`, data])
        if (node) {
            const contextName = this.#dataInfoResolver.resolveName(node)
            const id = this.#dataInfoResolver.resolveId(node)
            logger.info(() => `Restriction entity node: ${contextName}:${id}`)
        }
        logger.info(() => [`Configuration:`, evaluationConfig])
    }

    private filterRules(
        entryPointEvaluation: EntryPointEvaluation,
        evaluationMode?: EvaluationMode,
    ): EntryPointEvaluation {
        if (evaluationMode === undefined || evaluationMode === EvaluationMode.ALL) {
            return entryPointEvaluation
        }

        return {
            ...entryPointEvaluation,
            rules: entryPointEvaluation.rules.filter(value =>
                isSupportedPayloadType(evaluationMode, value.payload.type),
            ),
        }
    }

    /**
     * Normalizes stage version postfix to be compatible between mvn snapshot version and semver.
     * Theoretically, this could create false positive in some extremely rare case (1.0.0-12.3 === 1.0.0-1.23),
     * but this is unlikely to happen in practice
     *
     * @param version
     * @private
     */
    private normalizedVersion(version: string): string {
        const versionParts = version.split('-')
        if (versionParts.length !== 2) {
            return version
        }

        const normalizedSnapshotPart = versionParts[1].replace('.', '')
        return `${versionParts[0]}-${normalizedSnapshotPart}`
    }

    private getCachingServices(evaluationId?: string): CachedDataExtractionServices {
        if (evaluationId) {
            if (this.evaluationId === evaluationId && this.serviceCache) {
                // present by this evaluation id
                logger.debug(() => 'Using Cached Data Extraction Services from previous execution')
                return this.serviceCache
            } else {
                // absent by this evaluation id
                logger.debug(() => 'Initializing new Cached Data Extraction Services')
                this.evaluationId = evaluationId
                this.serviceCache = { contextDataExtractor: this.getContextDataExtractor() }
                return this.serviceCache
            }
        }
        // no evaluation id
        this.evaluationId = undefined
        this.serviceCache = undefined
        return { contextDataExtractor: this.getContextDataExtractor() }
    }

    private getContextDataExtractor(): CachingContextDataExtractor {
        return new CachingContextDataExtractor(this.#contextDataExtractor)
    }

    private requireValidNode = (node: object): object => {
        requireDefinedValue(node, 'Restriction node cannot be absent')
        const validationErrors = this.#dataInfoResolver.validate(node)

        if (validationErrors.length) {
            const errors = validationErrors.map(x => `  ${x.message}`).join('\n')
            const m = new SystemMessageBuilder(RESTRICTION_NODE_NOT_VALID_CONTEXT).parameters('\n' + errors).build()
            throw new KrakenRuntimeError(m)
        }
        return node
    }

    private readonly updateDataContextPathProvider = (evaluationConfig: EvaluationConfig): void => {
        if (evaluationConfig.dataContextPathProvider) {
            this.#dataContextPathProvider = evaluationConfig.dataContextPathProvider
        } else {
            this.#dataContextPathProvider = DEFAULT_PATH_PROVIDER
        }
    }
}
