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
package kraken.runtime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import kraken.context.model.tree.impl.ContextRepository;
import kraken.context.model.tree.repository.CachingContextModelTreeRepository;
import kraken.context.model.tree.repository.ContextModelTreeRepository;
import kraken.context.model.tree.repository.RecoveringStaticContextModelTreeRepository;
import kraken.el.TargetEnvironment;
import kraken.model.context.ContextNavigation;
import kraken.model.project.repository.KrakenProjectRepository;
import kraken.runtime.engine.RuleEngineImpl;
import kraken.runtime.engine.conditions.RuleApplicabilityEvaluatorImpl;
import kraken.runtime.engine.context.CachingCrossContextPathsResolverFactory;
import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.runtime.engine.context.type.registry.TypeRegistry;
import kraken.runtime.engine.context.type.registry.TypeRegistryBuilder;
import kraken.runtime.engine.dto.bundle.EntryPointBundleFactory;
import kraken.runtime.engine.evaluation.loop.OrderedEvaluationLoop;
import kraken.runtime.engine.handlers.RulePayloadProcessorImpl;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.expressions.KrakenTypeProviderFactory;
import kraken.runtime.logging.KrakenDataLogger;
import kraken.runtime.logging.Slf4jKrakenDataLogger;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.runtime.repository.RuntimeProjectRepositoryConfig;
import kraken.runtime.repository.dynamic.DynamicRuleRepository;
import kraken.runtime.repository.dynamic.DynamicRuleRepositoryCacheConfig;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.runtime.repository.filter.DimensionFilter;

/**
 * @author rimas
 * @since 1.0
 */
public class RuleEngineBuilder {

    private KrakenProjectRepository krakenProjectRepository;
    private ContextInstanceInfoResolver contextInstanceInfoResolver;
    private DynamicRuleRepositoryCacheConfig dynamicRuleRepositoryCacheConfig;
    private KrakenDataLogger krakenDataLogger;


    private final KrakenExpressionEvaluator krakenExpressionEvaluator = new KrakenExpressionEvaluator();
    private final Collection<DimensionFilter> dimensionFilters = new ArrayList<>();
    private final Collection<DynamicRuleRepository> dynamicRuleRepositories = new ArrayList<>();
    private final List<ContextTypeAdapter> customTypeAdapters = new ArrayList<>();
    private final List<IterableContextTypeAdapter> iterableTypeAdapters = new ArrayList<>();

    public static RuleEngineBuilder newInstance() {
        return new RuleEngineBuilder();
    }

    /**
     * Builds rule engine instance using current configuration
     * @return  new {@link RuleEngine} instance
     */
    public RuleEngine buildEngine() {
        DynamicRuleRepositoryCacheConfig cacheConfig = dynamicRuleRepositoryCacheConfig != null
                ? dynamicRuleRepositoryCacheConfig
                : DynamicRuleRepositoryCacheConfig.defaultConfig();

        RuleEngineImpl ruleEngine = new RuleEngineImpl();
        RuntimeProjectRepositoryFactory factory = new RuntimeProjectRepositoryFactory(
                krakenProjectRepository,
                new RuntimeProjectRepositoryConfig(cacheConfig, dimensionFilters, dynamicRuleRepositories),
                TargetEnvironment.JAVA
        );
        EntryPointBundleFactory entryPointBundleBuilder = new EntryPointBundleFactory(factory);
        ruleEngine.setRuntimeProjectRepositoryFactory(factory);
        ruleEngine.setContextInstanceInfoResolver(resolveContextInstanceInfoResolver());
        ruleEngine.setEntryPointBundleFactory(entryPointBundleBuilder);
        ruleEngine.setEvaluationLoop(
                new OrderedEvaluationLoop(
                        RulePayloadProcessorImpl.create(
                                krakenExpressionEvaluator,
                                new RuleApplicabilityEvaluatorImpl(krakenExpressionEvaluator)
                        )
                )
        );
        ruleEngine.setTypeRegistry(resolveTypeRegistry());

        ContextModelTreeRepository modelTreeRepository = new RecoveringStaticContextModelTreeRepository(
                new ProjectContextRepositoryRegistry(factory)
        );

        ruleEngine.setCrossContextPathsResolverFactory(CachingCrossContextPathsResolverFactory.getInstance());
        ruleEngine.setContextModelTreeProvider(modelTreeRepository);
        ruleEngine.setDataLogger(krakenDataLogger == null ? new Slf4jKrakenDataLogger() : krakenDataLogger);
        ruleEngine.setKrakenExpressionEvaluator(krakenExpressionEvaluator);
        ruleEngine.setKrakenTypeProviderFactory(
                new KrakenTypeProviderFactory(
                        contextInstanceInfoResolver,
                        factory
                )
        );

        return ruleEngine;
    }

    /**
     * Provide custom {@link ContextInstanceInfoResolver} implementation
     *
     * @param contextInstanceInfoResolver custom implementation of {@link ContextInstanceInfoResolver}
     * @return instance of itself for chaining calls
     */
    public RuleEngineBuilder setContextInstanceResolver(ContextInstanceInfoResolver contextInstanceInfoResolver) {
        this.contextInstanceInfoResolver = contextInstanceInfoResolver;
        return this;
    }

    /**
     * Configures Kraken for custom serialization of any type that can be extracted
     * by {@link ContextNavigation#getNavigationExpression()}.
     * e.g. {@link java.util.Optional} has predefined {@link ContextTypeAdapter}, to handle it.
     * @see kraken.runtime.engine.context.type.adapter.OptionalCustomTypeAdapter
     * @param adapter   type adapter to register.
     * @return          reference to "this" {@link RuleEngineBuilder}
     */
    public RuleEngineBuilder addCustomTypeAdapter(ContextTypeAdapter adapter) {
        customTypeAdapters.add(adapter);
        return this;
    }

    /**
     * Configures Kraken for custom serialization of type that must be iterated. Instance of this
     * type will be extracted by {@link ContextNavigation#getNavigationExpression()}.
     * Notice that order of adding {@link IterableContextTypeAdapter} is important. Adapters will be checked for
     * eligibility in order, they were added.
     * e.g. {@link List} has predefined {@link IterableContextTypeAdapter}
     * @see kraken.runtime.engine.context.type.adapter.DefaultListTypeAdapter
     * e.g. {@link java.util.Map} has predifined {@link IterableContextTypeAdapter}
     * @see kraken.runtime.engine.context.type.adapter.DefaultMapTypeAdapter
     * @param adapter   type adapter to register.
     * @return          reference to "this" {@link RuleEngineBuilder}
     */
    public RuleEngineBuilder addIterableTypeAdapter(IterableContextTypeAdapter adapter) {
        this.iterableTypeAdapters.add(adapter);

        return this;
    }

    public RuleEngineBuilder addDimensionFilter(DimensionFilter dimensionFilter) {
        this.dimensionFilters.add(dimensionFilter);
        return this;
    }

    public RuleEngineBuilder addDynamicRuleRepository(DynamicRuleRepository dynamicRuleRepository) {
        this.dynamicRuleRepositories.add(dynamicRuleRepository);
        return this;
    }

    public RuleEngineBuilder setKrakenProjectRepository(KrakenProjectRepository krakenProjectRepository) {
        this.krakenProjectRepository = krakenProjectRepository;

        return this;
    }

    private TypeRegistry resolveTypeRegistry() {
        final TypeRegistryBuilder registryBuilder = TypeRegistry.builder();
        iterableTypeAdapters.forEach(registryBuilder::addIterableTypeAdapter);
        customTypeAdapters.forEach(registryBuilder::addCustomTypeAdapter);
        return registryBuilder.build();
    }

    private ContextInstanceInfoResolver resolveContextInstanceInfoResolver() {
        if (contextInstanceInfoResolver == null) {
            contextInstanceInfoResolver = new DataNavigationContextInstanceInfoResolver();
        }
        return contextInstanceInfoResolver;
    }

    public RuleEngineBuilder setDynamicRuleRepositoryCacheConfig(DynamicRuleRepositoryCacheConfig dynamicRuleRepositoryCacheConfig) {
        this.dynamicRuleRepositoryCacheConfig = dynamicRuleRepositoryCacheConfig;

        return this;
    }

    public RuleEngineBuilder setKrakenDataLogger(KrakenDataLogger krakenDataLogger) {
        this.krakenDataLogger = krakenDataLogger;
        return this;
    }

    private static class ProjectContextRepositoryRegistry implements CachingContextModelTreeRepository.ContextRepositoryRegistry {

        private final RuntimeProjectRepositoryFactory projectRepositoryFactory;

        ProjectContextRepositoryRegistry(RuntimeProjectRepositoryFactory projectRepositoryFactory) {
            this.projectRepositoryFactory = projectRepositoryFactory;
        }

        @Override
        public ContextRepository get(String namespace, TargetEnvironment targetEnvironment) {
            final RuntimeContextRepository runtimeContextRepository =
                    projectRepositoryFactory.resolveContextRepository(namespace);
            return new EngineContextRepository(runtimeContextRepository);
        }
    }

    private static class EngineContextRepository implements ContextRepository {

        private RuntimeContextRepository runtimeContextRepository;

        EngineContextRepository(RuntimeContextRepository runtimeContextRepository) {
            this.runtimeContextRepository = runtimeContextRepository;
        }

        @Override
        public RuntimeContextDefinition get(String name) {
            return runtimeContextRepository.getContextDefinition(name);
        }

        @Override
        public Set<String> getKeys() {
            return runtimeContextRepository.getAllContextDefinitionNames();
        }


        @Override
        public String getRootName() {
            return runtimeContextRepository.getRootContextDefinition().getName();
        }
    }

}