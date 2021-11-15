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
package kraken.context.model.tree.impl;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.ContextModelTreeMetadata;
import kraken.context.path.ContextPath;
import kraken.runtime.model.context.RuntimeContextDefinition;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A default implementation of {@code ContextModelTree}. Holds runtime context definitions, metadata
 * and precalculated paths from root node to context nodes in a model tree.
 *
 * @author psurinin
 */
public class ContextModelTreeImpl implements Serializable, ContextModelTree {

    private static final long serialVersionUID = -4325360987758438510L;

    private final Map<String, RuntimeContextDefinition> contexts;
    private final Map<String, Collection<ContextPath>> pathsToNodes = new HashMap<>();

    private final ContextModelTreeMetadata metadata;

    /**
     * Creates a new instance of {@code ContextModelTree} with given arguments.
     *
     * @param contexts          Runtime context definitions by name.
     * @param pathsToNodes      Paths from root to context definition nodes.
     * @param metadata          Tree model metadata.
     */
    public ContextModelTreeImpl(
            Map<String, RuntimeContextDefinition> contexts,
            Map<String, Collection<ContextPath>> pathsToNodes,
            ContextModelTreeMetadata metadata) {
        this.contexts = contexts;
        this.metadata = metadata;
        this.pathsToNodes.putAll(pathsToNodes);
    }

    @Override
    public Map<String, Collection<ContextPath>> getPathsToNodes() {
        return pathsToNodes;
    }

    @Override
    public Map<String, RuntimeContextDefinition> getContexts() {
        return contexts;
    }

    @Override
    public Collection<ContextPath> getPathsToNode(String contextName) {
        return pathsToNodes.get(contextName);
    }

    @Override
    public RuntimeContextDefinition getContext(String contextName) {
        return contexts.get(contextName);
    }

    @Override
    public ContextModelTreeMetadata getMetadata() {
        return metadata;
    }

}
