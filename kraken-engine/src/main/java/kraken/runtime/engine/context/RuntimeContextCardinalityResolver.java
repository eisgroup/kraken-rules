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
package kraken.runtime.engine.context;

import kraken.context.model.tree.ContextModelTree;
import kraken.cross.context.path.ContextCardinalityResolver;
import kraken.cross.context.path.CrossContextNavigationException;
import kraken.model.context.Cardinality;
import kraken.runtime.model.context.RuntimeContextDefinition;

import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@code ContextCardinalityResolver} which is backed by a map of context definitions.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class RuntimeContextCardinalityResolver implements ContextCardinalityResolver {

    private final Map<String, RuntimeContextDefinition> contextDefinitions;

    private RuntimeContextCardinalityResolver(Map<String, RuntimeContextDefinition> contextDefinitions) {
        this.contextDefinitions = contextDefinitions;
    }

    public static RuntimeContextCardinalityResolver create(ContextModelTree modelTree) {
        return new RuntimeContextCardinalityResolver(modelTree.getContexts());
    }


    @Override
    public Cardinality getCardinality(String parent, String child) {
        return Optional.ofNullable(contextDefinitions.get(parent))
                .map(RuntimeContextDefinition::getChildren)
                .map(stringContextNavigationMap -> stringContextNavigationMap.get(child))
                .orElseThrow(() -> new CrossContextNavigationException(String.format(
                        "Couldn't find navigation from '%s' to '%s'", parent, child))).getCardinality();
    }

}
