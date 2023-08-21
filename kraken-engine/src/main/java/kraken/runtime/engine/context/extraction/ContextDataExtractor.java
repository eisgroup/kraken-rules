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
package kraken.runtime.engine.context.extraction;

import static kraken.message.SystemMessageBuilder.Message.CONTEXT_EXTRACTION_MISSING_EXTRACTION_PATH;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.path.ContextPath;
import kraken.message.SystemMessageBuilder;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.ExtractedChildDataContextBuilder;
import kraken.runtime.engine.context.data.NodeInstanceInfo;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.utils.cache.Memoizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Extracts {@link RuntimeContextDefinition}s instances ({@link DataContext}s)
 * from root {@link DataContext}.
 * <p>
 * Created by rimas on 19/01/17.
 */
@SuppressWarnings("WeakerAccess")
public class ContextDataExtractor {

    private final static Logger logger = LoggerFactory.getLogger(ContextDataExtractor.class);
    private final ContextModelTree modelTree;
    private final RuntimeContextRepository contextRepository;
    private final Function<ContextChildExtractionInfo, List<DataContext>> getChildDataContexts;

    public ContextDataExtractor(RuntimeContextRepository contextRepository,
                                ContextModelTree modelTree,
                                ExtractedChildDataContextBuilder contextBuilder) {
        this.contextRepository = contextRepository;
        this.modelTree = modelTree;
        this.getChildDataContexts = Memoizer.memoize(contextBuilder::resolveImmediateChildren);
    }

    /**
     * Founds {@link RuntimeContextDefinition} instances by provided path in parameters.
     * If tree is A -> B* -> C* (* - multiple cardinality)
     * Root is instance of A, path is [A, B, C],  then all
     * {@link RuntimeContextDefinition} instances of C will be found and flattened to
     * one list.
     *
     * @param root {@link DataContext} that is root of the tree
     * @param path Path to {@link RuntimeContextDefinition} instance, represented by {@link String}s
     * @return DataContexts, that are found by path.
     */
    public Collection<DataContext> extractByPath(DataContext root, List<String> path) {
        List<String> fromRoot = new ContextPath.ContextPathBuilder().addPathElements(path).build()
                .getPathFromInclusive(root.getContextName());

        if (fromRoot.size() == 1 && root.getContextName().equals(fromRoot.get(0))) {
            return List.of(root);
        }
        final List<DataContext> dataContexts = extractContextsByPath(
                fromRoot.stream()
                        .map(contextRepository::getContextDefinition)
                        .collect(Collectors.toList()),
                root,
                dataContext -> true
        );
        logger.debug("Extracted from ContextDefinition instance (id: {}, name: {}) by path {}, {} of instances",
                root.getContextId(),
                root.getContextName(),
                fromRoot,
                dataContexts.size()
        );
        return dataContexts;
    }

    /**
     * Founds {@link RuntimeContextDefinition} instances by provided name in
     * parameters in all places in the tree.
     * If model tree is: (* - multiple cardinality)
     * <pre>
     * A
     *      B
     *          C
     *          D
     *      E*
     *          D*
     * </pre>
     * {@link RuntimeContextDefinition} name is D, then extractor will search instances in
     * <pre>
     * A -> B -> D
     * A -> E -> D
     * </pre>
     * And flatten it in on {@link List}
     *
     * @param childContextName name to find {@link RuntimeContextDefinition}
     * @param root             Root of the tree to search {@link RuntimeContextDefinition}s
     * @return ContextDefinitions found by name in parameters
     */
    public List<DataContext> extractByName(String childContextName, DataContext root) {
        List<DataContext> contexts = getExtractionPaths(childContextName, root).stream()
                .filter(path -> !path.getPath().isEmpty())
                .flatMap(path -> extractContextsByPath(path.getPath(), root, dataContext -> true).stream())
                .collect(Collectors.toList());
        logger.debug("Extracted from {} to {}: {} instances", root.getContextName(), childContextName, contexts.size());
        return contexts;
    }

    /**
     * Founds {@link RuntimeContextDefinition} instances by provided name in
     * parameters restricted by tree mode.
     * If model tree is: (* - multiple cardinality)
     * <pre>
     * A
     *      B
     *          C
     *          D
     *      E*
     *          D*
     * </pre>
     * {@link RuntimeContextDefinition} name is D, restriction node is instance of E,
     * then extractor will search instances in
     * <pre>
     * A -> E -> D
     * </pre>
     * Filter by instance of E from parameters to be in path
     * and flatten it in on {@link List}
     *
     * @param childContextName name to find {@link RuntimeContextDefinition}
     * @param root             Root of the tree to search {@link RuntimeContextDefinition}s
     * @param evaluationNode   node to be target or on of parent nodes in extracted instances
     * @return ContextDefinitions found by name in parameters
     */
    public List<DataContext> extractByName(String childContextName, DataContext root, NodeInstanceInfo evaluationNode) {
        List<ContextPathExtractionResult> extractionPaths = getExtractionPaths(childContextName, root).stream()
                .filter(extractionPath -> isRestrictionWithinPath(extractionPath, evaluationNode))
                .collect(Collectors.toList());
        if (extractionPaths.isEmpty()) {
            logger.debug("Context {} is not part of subtree of {}", childContextName, evaluationNode.getContextName());
            return Collections.emptyList();
        }
        List<DataContext> contexts = extractionPaths.stream()
                .filter(path -> !path.getPath().isEmpty())
                .flatMap(path -> extractContextsByPath(
                        path.getPath(),
                        root,
                        dataContext -> filterByRestriction(dataContext, evaluationNode)
                ).stream())
                .collect(Collectors.toList());
        logger.debug("Extracted from {} to {}: {} instances", root.getContextName(), childContextName, contexts.size());
        return contexts;
    }

    private List<ContextPathExtractionResult> getExtractionPaths(String childContextName, DataContext root) {
        List<ContextPathExtractionResult> extractionPaths = modelTree.getPathsToNodes().get(childContextName)
                .stream()
                .map(path -> path.getPathFromInclusive(root.getContextName()).stream().map(contextRepository::getContextDefinition))
                .map(ContextPathExtractionResult::new)
                .collect(Collectors.toList());

        if (extractionPaths.isEmpty()) {
            var m = SystemMessageBuilder.create(CONTEXT_EXTRACTION_MISSING_EXTRACTION_PATH)
                .parameters(root.getContextName(), childContextName)
                .build();
            throw new ContextExtractionException(m);
        }
        return extractionPaths;
    }

    private List<DataContext> extractContextsByPath(
            List<RuntimeContextDefinition> extractionPath,
            DataContext root,
            Predicate<DataContext> filterExtractedChildren
    ) {
        List<DataContext> contexts = new ArrayList<>();
        contexts.add(root);

        Iterator<RuntimeContextDefinition> iterator = extractionPath.iterator();
        RuntimeContextDefinition next = iterator.next();
        while (iterator.hasNext()) {
            RuntimeContextDefinition from = next;
            next = iterator.next();
            String nextContextName = next.getName();
            List<DataContext> children = contexts.stream()
                    .map(parent -> new ContextChildExtractionInfo(from, parent, nextContextName))
                    .map(getChildDataContexts)
                    .flatMap(Collection::stream)
                    .filter(filterExtractedChildren)
                    .collect(Collectors.toList());
            contexts = children;
        }
        return contexts;
    }

    private boolean filterByRestriction(DataContext child, NodeInstanceInfo evaluationNodeContext) {
        if (evaluationNodeContext == null) {
            return true;
        } else {
            if (child.getContextName().equals(evaluationNodeContext.getContextName())) {
                return child.getContextId().equals(evaluationNodeContext.getContextId());
            } else {
                return true;
            }
        }
    }

    private boolean isRestrictionWithinPath(ContextPathExtractionResult extractionPath, NodeInstanceInfo evaluationNodeContext) {
        return extractionPath.getPath()
                .stream()
                .anyMatch(cd -> cd.getName().equals(evaluationNodeContext.getContextName()));
    }

}
