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

import { ContextInstanceInfoResolver } from "../contexts/info/ContextInstanceInfoResolver";
import { DataObjectInfoResolver } from "../contexts/info/DataObjectInfoResolver";
import { OrderedEvaluationLoop } from "../processors/OrderedEvaluationLoop";
import { RepoClientCache } from "../../repository/RepoClientCache";
import { EntryPointResult } from "../../dto/EntryPointResult";
import { ExecutionSession } from "../ExecutionSession";
import { requireDefinedValue } from "../../utils/Utils";
import { ContextModelTree } from "../../models/ContextModelTree";
import { RulePayloadProcessor } from "../RulePayloadProcessor";
import { ExpressionEvaluator } from "../runtime/expressions/ExpressionEvaluator";
import { DataContextBuilder } from "../contexts/data/DataContextBuilder";
import { ExtractedChildDataContextBuilder } from "../contexts/data/ExtractedChildDataContextBuilder";
import { ContextDataExtractorImpl } from "../contexts/data/extraction/ContextDataExtractorImpl";
import { FunctionRegistry } from "../runtime/expressions/functionLibrary/Registry";
import { RuleOverrideContextExtractorImpl } from "../results/RuleOverrideContextExtractor";
import { logger } from "../../utils/DevelopmentLogger";
import { RuleConditionProcessor } from "../RuleConditionProcessor";
import { DataContextUpdaterImpl } from "../contexts/data/updater/DataContextUpdaterImpl";
import { ContextDataExtractor } from "../contexts/data/extraction/ContextDataExtractor.types";
import { CachingContextDataExtractor } from "../contexts/data/extraction/CachingContextDataExtractor";
import { ReferencePathResolverImpl } from "../contexts/ccr/ReferencePathResolverImpl";
import { PathCardinalityResolver } from "../contexts/ccr/PathCardinalityResolver";
import { CommonPathResolver } from "../contexts/ccr/CommonPathResolver";
import { CachingReferencePathResolver } from "../contexts/ccr/CachingReferencePathResolver";
import { ReferencePathResolver } from "../contexts/ccr/ReferencePathResolver";
import { DataContextUpdater } from "../contexts/data/updater/DataContextUpdater";
import { ErrorCode, KrakenRuntimeError } from "../../error/KrakenRuntimeError";
import { ENGINE_VERSION } from "../generated/engineVersion";

export interface EvaluationConfig {

    context: {
        /**
         * Dimension values, that will be as a filter to get rules and entrypoints
         * that applies to that dimensions
         */
        dimensions?: Record<string, unknown>;
        externalData?: Record<string, any>
        [keyof: string]: any
    };

    /**
     * Currency to use when interpreting monetary types
     */
    currencyCd: string;

    /**
     * This id enables Kraken to treat different rules evaluations as on evaluation.
     * This enables data extraction caching between different evaluations.
     */
    evaluationId?: string;
}

/**
 * Config to pass when calling {@link Engine.withConfig}
 */
export interface SyncEngineConfig {
    /**
     * Cached artifacts grouped by dimensions
     */
    cache: RepoClientCache;
    /**
     * Resolves context definition name for specified data object instance
     */
    dataInfoResolver: DataObjectInfoResolver;
    /**
     * Provides three different info resolution methods, which are called depending on how
     * particular context instance data object was extracted
     */
    contextInstanceInfoResolver: ContextInstanceInfoResolver<{}>;

    /**
     * Static context definition model
     */
    modelTree: ContextModelTree.ContextModelTree;

    /**
     * Used for compatibility check with the backend.
     * {@link EntryPointBundle.EntryPointBundle}#engineVersion must match against
     * this version. Default version is backend artifact version at a release time.
     */
    engineCompatibilityVersion?: string;
}
type CachedDataExtractionServices = {
    contextDataExtractor: CachingContextDataExtractor
};

export class SyncEngine {

    readonly #ruleConditionProcessor: RuleConditionProcessor;
    readonly #rulePayloadProcessor: RulePayloadProcessor;
    readonly #dataContextBuilder: DataContextBuilder;
    readonly #contextDataExtractor: ContextDataExtractor;
    readonly #bundleCache: RepoClientCache;
    readonly #dataInfoResolver: DataObjectInfoResolver;
    readonly #referencePathResolver: ReferencePathResolver;
    readonly #dataContextUpdater: DataContextUpdater;

    readonly #engineCompatibilityVersion?: string;

    private evaluationId?: string;
    private serviceCache?: CachedDataExtractionServices;

    constructor(config: SyncEngineConfig) {
        function resolveInheritance(name: string): string[] {
            return config.modelTree.contexts[name].inheritedContexts;
        }
        const expressionEvaluator =
            new ExpressionEvaluator(
                FunctionRegistry.createInstanceFunctions(
                    config.dataInfoResolver,
                    resolveInheritance
                ));
        this.#ruleConditionProcessor =
            new RuleConditionProcessor(expressionEvaluator);
        this.#rulePayloadProcessor =
            new RulePayloadProcessor(
                expressionEvaluator,
                new RuleOverrideContextExtractorImpl()
            );
        this.#dataContextBuilder =
            new DataContextBuilder(
                config.modelTree,
                config.contextInstanceInfoResolver
            );
        this.#contextDataExtractor =
            new ContextDataExtractorImpl(
                config.modelTree,
                new ExtractedChildDataContextBuilder(
                    this.#dataContextBuilder,
                    expressionEvaluator
                )
            );
        this.#referencePathResolver =
            new CachingReferencePathResolver(
                new ReferencePathResolverImpl(
                    config.modelTree.pathsToNodes,
                    new PathCardinalityResolver(config.modelTree.contexts),
                    new CommonPathResolver()
                )
            );
        this.#dataContextUpdater =
            new DataContextUpdaterImpl(
                this.#referencePathResolver,
                (root, path) => this.#contextDataExtractor.extractByPath(root, path)
            );
        this.#bundleCache = config.cache;
        this.#dataInfoResolver = config.dataInfoResolver;
        this.#engineCompatibilityVersion = config.engineCompatibilityVersion;
    }

    evaluate(data: object, entryPointName: string, config: EvaluationConfig): EntryPointResult {
        return this.doEvaluate(data, entryPointName, config);
    }

    evaluateSubTree(data: object, node: object, entryPointName: string, config: EvaluationConfig): EntryPointResult {
        return this.doEvaluate(data, entryPointName, config, this.requireValidNode(node));
    }

    private doEvaluate = (data: object, entryPointName: string, evaluationConfig: EvaluationConfig, node?: object) => {
        const start = Date.now();
        logger.group(`Kraken logs: '${entryPointName}'`);
        logger.info({ evaluationConfig });
        if (node) {
            const contextName = this.#dataInfoResolver.resolveName(node);
            const id = this.#dataInfoResolver.resolveId(node);
            logger.info(`With restriction node: ${contextName}:${id}`);
        }
        const bundle = this.#bundleCache
            .getBundleForDimension(evaluationConfig.context.dimensions || {})
            (entryPointName);

        const version = this.#engineCompatibilityVersion || ENGINE_VERSION;
        if (bundle.engineVersion && bundle.engineVersion !== version) {
            logger.warning(
                `UI engine (${version}) and backend engine (${bundle.engineVersion}) versions are different.`
            );
        }

        if (!bundle.evaluation.rules.length) {
            logger.info(`Kraken rules evaluation took ${Math.round((Date.now() - start))} ms`);
            logger.groupEnd("Kraken logs");
            return EntryPointResult.empty();
        }
        requireDefinedValue(evaluationConfig.currencyCd, "Currency code cannot be absent");
        const session = new ExecutionSession(evaluationConfig, bundle.expressionContext);
        const cachingServices = this.getCachingServices(evaluationConfig.evaluationId);
        const result = OrderedEvaluationLoop
            .getInstance({
                dataContextUpdater: this.#dataContextUpdater,
                ruleConditionProcessor: this.#ruleConditionProcessor,
                contextBuilder: this.#dataContextBuilder,
                contextDataExtractor: cachingServices.contextDataExtractor,
                rulePayloadProcessor: this.#rulePayloadProcessor,
                restriction: node
            })
            .evaluate(
                bundle.evaluation,
                data,
                session
            );
        logger.info(`Kraken rules evaluation took ${Math.round((Date.now() - start))} ms`);
        logger.groupEnd("Kraken logs");
        return result;
    }

    private getCachingServices(evaluationId?: string): CachedDataExtractionServices {
        if (evaluationId) {
            if (this.evaluationId === evaluationId && this.serviceCache) {
                // present by this evaluation id
                logger.debug("Using Cached Data Extraction Services from previous execution");
                return this.serviceCache;
            } else {
                // absent by this evaluation id
                logger.debug("Initializing new Cached Data Extraction Services");
                this.evaluationId = evaluationId;
                this.serviceCache = { contextDataExtractor: this.getContextDataExtractor() };
                return this.serviceCache;
            }
        }
        // no evaluation id
        this.evaluationId = undefined;
        this.serviceCache = undefined;
        return { contextDataExtractor: this.getContextDataExtractor() };
    }

    private getContextDataExtractor(): CachingContextDataExtractor {
        return new CachingContextDataExtractor(this.#contextDataExtractor);
    }

    private requireValidNode = (node: {}): {} => {
        requireDefinedValue(node, "Restriction node cannot be absent");
        const validationErrors = this.#dataInfoResolver.validate(node);
        if (validationErrors.length) {
            throw new KrakenRuntimeError(
                ErrorCode.INCORRECT_CONTEXT_INSTANCE,
                "Restriction node is invalid Context instance:\n\t" + validationErrors.map(x => x.message).join("\n\t"
                )
            );
        }
        return node;
    }
}
