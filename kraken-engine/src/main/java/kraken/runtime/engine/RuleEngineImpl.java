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
package kraken.runtime.engine;

import static kraken.context.Context.DIMENSIONS;
import static kraken.context.Context.EXTERNAL_DATA;
import static kraken.context.Context.RULE_TIMEZONE_ID_DIMENSION;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.repository.ContextModelTreeRepository;
import kraken.el.TargetEnvironment;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.KelFunction;
import kraken.el.functionregistry.KelFunction.Parameter;
import kraken.namespace.Namespaces;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.EvaluationSession;
import kraken.runtime.RuleEngine;
import kraken.runtime.engine.context.CachingCrossContextPathsResolverFactory;
import kraken.runtime.engine.context.ContextDataProvider;
import kraken.runtime.engine.context.StaticContextDataProvider;
import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;
import kraken.runtime.engine.context.type.registry.TypeRegistry;
import kraken.runtime.engine.dto.bundle.EntryPointBundle;
import kraken.runtime.engine.dto.bundle.EntryPointBundleFactory;
import kraken.runtime.engine.evaluation.loop.EvaluationLoop;
import kraken.runtime.engine.trace.RuleEngineInvocationOperation;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.expressions.KrakenTypeProvider;
import kraken.runtime.logging.KrakenDataLogger;
import kraken.runtime.repository.RuntimeProjectRepository;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.tracer.Tracer;

/**
 * Default implementation for {@link RuleEngine}
 *
 * @author rimas
 * @since 1.0
 */
public class RuleEngineImpl implements RuleEngine {

    private RuntimeProjectRepositoryFactory runtimeProjectRepositoryFactory;
    private ContextModelTreeRepository contextModelTreeRepository;
    private EvaluationLoop evaluationLoop;
    private EntryPointBundleFactory entryPointBundleFactory;
    private ContextInstanceInfoResolver contextInstanceInfoResolver;
    private TypeRegistry typeRegistry;
    private KrakenDataLogger dataLogger;
    private KrakenExpressionEvaluator krakenExpressionEvaluator;
    private CachingCrossContextPathsResolverFactory crossContextPathsResolverFactory;

    @Override
    public EntryPointResult evaluate(Object data, String entryPointName) {
        return evaluate(data, entryPointName, new EvaluationConfig());
    }

    @Override
    public EntryPointResult evaluate(Object data, String entryPointName, EvaluationConfig evaluationConfig) {
        return Tracer.doOperation(
            new RuleEngineInvocationOperation(entryPointName, data, evaluationConfig),
            () -> {
                String namespace = Namespaces.toNamespaceName(entryPointName);
                ContextModelTree contextModelTree = modelTree(namespace);
                RuntimeProjectRepository repository = runtimeProjectRepositoryFactory.resolveRepository(namespace);
                EvaluationSession session = createEvaluationSession(evaluationConfig, repository, namespace, contextModelTree);
                logInputData(session.getSessionToken(), data, entryPointName, evaluationConfig.getContext());
                EntryPointBundle bundle = buildEntryPointBundle(entryPointName, evaluationConfig);
                logEffectiveRules(session.getSessionToken(), bundle);
                if (noRulesArePresent(bundle)) {
                    return new EntryPointResult(session.getTimestamp(), evaluationConfig.getRuleTimezoneId());
                }
                final ContextDataProvider provider = StaticContextDataProvider.create(
                    crossContextPathsResolverFactory.resolve(contextModelTree),
                    repository,
                    contextModelTree,
                    contextInstanceInfoResolver,
                    krakenExpressionEvaluator,
                    typeRegistry,
                    data,
                    session
                );
                final EntryPointResult entryPointResult = evaluationLoop.evaluate(
                    bundle.getEvaluation(),
                    provider,
                    session
                );
                logEvaluationResults(session.getSessionToken(), entryPointName, entryPointResult);
                return entryPointResult;
            });
    }

    @Override
    public EntryPointResult evaluateSubtree(Object data, Object node, String entryPointName) {
        return evaluateSubtree(data, node, entryPointName, new EvaluationConfig());
    }

    @Override
    public EntryPointResult evaluateSubtree(Object data, Object node, String entryPointName,
                                            EvaluationConfig evaluationConfig) {
        return Tracer.doOperation(
            new RuleEngineInvocationOperation(entryPointName, data, node, evaluationConfig),
            () -> {
                String namespace = Namespaces.toNamespaceName(entryPointName);
                ContextModelTree contextModelTree = modelTree(namespace);
                RuntimeProjectRepository repository = runtimeProjectRepositoryFactory.resolveRepository(namespace);
                EvaluationSession session = createEvaluationSession(
                    evaluationConfig,
                    repository,
                    namespace,
                    contextModelTree
                );
                logInputData(session.getSessionToken(), data, node, entryPointName, evaluationConfig.getContext());
                EntryPointBundle bundle = buildEntryPointBundle(entryPointName, evaluationConfig);
                logEffectiveRules(session.getSessionToken(), bundle);
                if (noRulesArePresent(bundle)) {
                    return new EntryPointResult(session.getTimestamp(), evaluationConfig.getRuleTimezoneId());
                }
                final ContextDataProvider provider = StaticContextDataProvider.create(
                    crossContextPathsResolverFactory.resolve(contextModelTree),
                    repository,
                    contextModelTree,
                    contextInstanceInfoResolver,
                    krakenExpressionEvaluator,
                    typeRegistry,
                    data,
                    node,
                    session
                );
                EntryPointResult entryPointResult = evaluationLoop.evaluate(bundle.getEvaluation(), provider, session);
                logEvaluationResults(session.getSessionToken(), entryPointName, entryPointResult);
                return entryPointResult;
            });
    }

    public void setEvaluationLoop(EvaluationLoop evaluationLoop) {
        this.evaluationLoop = evaluationLoop;
    }

    public void setEntryPointBundleFactory(EntryPointBundleFactory entryPointBundleFactory) {
        this.entryPointBundleFactory = entryPointBundleFactory;
    }

    public void setContextInstanceInfoResolver(ContextInstanceInfoResolver contextInstanceInfoResolver) {
        this.contextInstanceInfoResolver = contextInstanceInfoResolver;
    }

    public void setTypeRegistry(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    public void setDataLogger(KrakenDataLogger dataLogger) {
        this.dataLogger = dataLogger;
    }

    public void setKrakenExpressionEvaluator(KrakenExpressionEvaluator krakenExpressionEvaluator) {
        this.krakenExpressionEvaluator = krakenExpressionEvaluator;
    }

    public void setRuntimeProjectRepositoryFactory(RuntimeProjectRepositoryFactory runtimeProjectRepositoryFactory) {
        this.runtimeProjectRepositoryFactory = runtimeProjectRepositoryFactory;
    }

    public void setCrossContextPathsResolverFactory(CachingCrossContextPathsResolverFactory crossContextPathsResolverFactory) {
        this.crossContextPathsResolverFactory = crossContextPathsResolverFactory;
    }

    public void setContextModelTreeProvider(ContextModelTreeRepository contextModelTreeRepository) {
        this.contextModelTreeRepository = contextModelTreeRepository;
    }

    private EntryPointBundle buildEntryPointBundle(String entryPointName, EvaluationConfig evaluationConfig) {
        return entryPointBundleFactory.build(
            entryPointName,
            bundleContext(evaluationConfig),
            evaluationConfig.getEvaluationMode()
        );
    }

    private Map<String, Object> bundleContext(EvaluationConfig evaluationConfig) {
        var contextCopy = new HashMap<>(evaluationConfig.getContext());
        contextCopy.remove(EXTERNAL_DATA);
        var dimensions = contextCopy.get(DIMENSIONS);
        if(dimensions instanceof Map) {
            var dimensionsCopy = new HashMap<>((Map<String, Object>)dimensions);
            dimensionsCopy.put(RULE_TIMEZONE_ID_DIMENSION, evaluationConfig.getRuleTimezoneId());
            contextCopy.put(DIMENSIONS, dimensionsCopy);
        }
        return contextCopy;
    }

    private boolean noRulesArePresent(EntryPointBundle bundle) {
        return bundle.getEvaluation().getRules().isEmpty();
    }

    private ContextModelTree modelTree(String namespace) {
        return contextModelTreeRepository.get(namespace, TargetEnvironment.JAVA);
    }

    private void logInputData(String sessionToken, Object data, String entryPointName, Map<String, Object> context) {
        Optional.ofNullable(dataLogger)
                .ifPresent(logger -> logger.logEvaluationInputData(sessionToken, data, entryPointName, context));
    }

    private void logInputData(String sessionToken, Object data, Object node, String entryPointName, Map<String, Object> context){
        Optional.ofNullable(dataLogger)
                .ifPresent(logger -> logger.logEvaluationSubtreeInputData(sessionToken, data, node, entryPointName, context));
    }

    private void logEffectiveRules(String sessionToken, EntryPointBundle bundle){
        Optional.ofNullable(dataLogger)
                .ifPresent(logger -> logger.logEffectiveRules(sessionToken, bundle));
    }

    private void logEvaluationResults(String sessionToken, String entryPointName, EntryPointResult entryPointResult){
        Optional.ofNullable(dataLogger)
                .ifPresent(logger -> logger.logEvaluationResults(sessionToken, entryPointName, entryPointResult));
    }

    private EvaluationSession createEvaluationSession(EvaluationConfig evaluationConfig,
                                                      RuntimeProjectRepository repository,
                                                      String namespace,
                                                      ContextModelTree contextModelTree) {
        KrakenTypeProvider krakenTypeProvider = new KrakenTypeProvider(contextInstanceInfoResolver, repository);
        Map<FunctionHeader, KelFunction> functions = repository.getKrakenProject().getFunctions().values().stream()
            .map(f -> new KelFunction(
                f.getName(),
                f.getParameters().stream().map(p -> new Parameter(p.getName())).collect(Collectors.toList()),
                f.getBody().getAst()
            ))
            .collect(Collectors.toMap(KelFunction::header, f -> f));
        return new EvaluationSession(
            evaluationConfig,
            krakenTypeProvider,
            functions,
            namespace,
            contextModelTree
        );
    }
}
