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
package kraken.context.model.tree;

import kraken.context.path.ContextPath;
import kraken.runtime.model.context.RuntimeContextDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Model that used to resolve all additional info about context definition
 *
 * @author psurinin
 */
public interface ContextModelTree {

    /**
     * Finds context definition. If no context definition is found <code>null</code> will be returned
     * @param contextName {@link RuntimeContextDefinition#getName()}
     * @return {@link RuntimeContextDefinition} by name from parameters
     */
    RuntimeContextDefinition getContext(String contextName);

    Map<String, RuntimeContextDefinition> getContexts();

    /**
     * Returns paths to all nodes of all context definitions starting from root
     * context node. If no paths exists an empty map will be returned.
     *
     * @return Paths to context nodes.
     */
    Map<String, Collection<ContextPath>> getPathsToNodes();

    /**
     * Returns paths to all nodes of given context definition starting from root
     * context node. If no paths are found {@code null} will be returned.
     *
     * @param contextName Context definition name.
     * @return Paths to context node.
     */
    Collection<ContextPath> getPathsToNode(String contextName);

    /**
     * Returns context model tree metadata which was used as an input for tree
     * creation.
     *
     * @return Model tree metadata.
     */
    ContextModelTreeMetadata getMetadata();

}
