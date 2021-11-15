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
package kraken.runtime.engine.context;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import kraken.context.model.tree.ContextModelTree;
import kraken.cross.context.path.CrossContextPathsResolver;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataContextBuilder;
import kraken.runtime.engine.context.data.ExtractedChildDataContextBuilder;
import kraken.runtime.engine.context.data.NodeInstanceInfo;
import kraken.runtime.engine.context.extraction.ContextDataExtractor;
import kraken.runtime.engine.context.extraction.instance.ContextExtractionResultBuilder;
import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;
import kraken.runtime.engine.context.type.registry.TypeRegistry;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.utils.cache.Memoizer;
import kraken.utils.dto.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticContextDataProvider implements ContextDataProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(StaticContextDataProvider.class);

    private final DataContext rootDataContext;
    private final NodeInstanceInfo nodeInstanceInfo;
    private final Function<ReferenceExtractionInfo, Collection<DataContext>> getReferencedContext;
    private final BiFunction<String, DataContext, List<DataContext>> getExtractedContexts;
    private final Function<Triplet<String, DataContext, NodeInstanceInfo>, List<DataContext>> getContextsExtractionSubTree;
    private final CrossContextPathsResolver crossContextPathsResolver;

    private StaticContextDataProvider(
            CrossContextPathsResolver crossContextPathsResolver,
            DataContext rootDataContext,
            NodeInstanceInfo nodeInstanceInfo,
            ContextDataExtractor extractor) {
        this.nodeInstanceInfo = nodeInstanceInfo;
        this.rootDataContext = rootDataContext;
        this.getExtractedContexts = Memoizer.memoize(extractor::extractByName);
        this.getContextsExtractionSubTree = Memoizer.memoize(triplet -> extractor.extractByName(
                triplet.getLeft(), triplet.getCenter(), triplet.getRight()));
        this.getReferencedContext = Memoizer.memoize(info -> extractor.extractByPath(
                info.resolveCommonRoot(),
                info.getExtractionPath()));
        this.crossContextPathsResolver = crossContextPathsResolver;
    }

    public static ContextDataProvider create(
            CrossContextPathsResolver crossContextPathsResolver,
            RuntimeContextRepository contextRepository,
            ContextModelTree contextModelTree,
            ContextInstanceInfoResolver contextInstanceInfoResolver,
            KrakenExpressionEvaluator krakenExpressionEvaluator,
            TypeRegistry typeRegistry,
            Object data
    ) {
        return create(crossContextPathsResolver, contextRepository, contextModelTree, contextInstanceInfoResolver, krakenExpressionEvaluator,
                typeRegistry, data, null);
    }

    public static ContextDataProvider create(
            CrossContextPathsResolver crossContextPathsResolver,
            RuntimeContextRepository contextRepository,
            ContextModelTree contextModelTree,
            ContextInstanceInfoResolver contextInstanceInfoResolver,
            KrakenExpressionEvaluator krakenExpressionEvaluator,
            TypeRegistry typeRegistry,
            Object data,
            Object node
    ) {
        NodeInstanceInfo nodeInstanceInfo = createNodeContext(node, contextInstanceInfoResolver);

        final DataContextBuilder dataContextBuilder = new DataContextBuilder(contextRepository, contextInstanceInfoResolver);
        final DataContext root = dataContextBuilder.buildFromRoot(data);

        final ContextDataExtractor contextDataExtractor = new ContextDataExtractor(
                contextRepository,
                contextModelTree,
                new ExtractedChildDataContextBuilder(
                        dataContextBuilder,
                        new ContextExtractionResultBuilder(typeRegistry),
                        krakenExpressionEvaluator
                )
        );
        return new StaticContextDataProvider(crossContextPathsResolver, root, nodeInstanceInfo, contextDataExtractor);
    }

    private static NodeInstanceInfo createNodeContext(Object node, ContextInstanceInfoResolver contextInstanceInfoResolver) {
        if (node == null) {
            return null;
        }

        return NodeInstanceInfo.from(contextInstanceInfoResolver.resolveRootInfo(node));
    }

    @Override
    public List<DataContext> resolveContextData(String targetContextName, Collection<Dependency> dependencies) {
        final List<DataContext> dataContexts = Optional.ofNullable(nodeInstanceInfo)
                .map(node -> new Triplet<>(targetContextName, rootDataContext, node))
                .map(getContextsExtractionSubTree)
                .orElseGet(() -> getExtractedContexts.apply(targetContextName, rootDataContext));
        final DataContextReferenceUpdater updater =
                new DataContextReferenceUpdater(crossContextPathsResolver, getReferencedContext, dependencies);
        dataContexts.forEach(updater::update);
        log(targetContextName, dataContexts);
        return dataContexts;
    }

    private void log(String targetContextName, List<DataContext> targetContextInstances) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resolved {} to instances: {}", targetContextName,
                    targetContextInstances.stream()
                            .map(DataContext::getIdString)
                            .collect(Collectors.joining(", ")));
        }
    }
}
