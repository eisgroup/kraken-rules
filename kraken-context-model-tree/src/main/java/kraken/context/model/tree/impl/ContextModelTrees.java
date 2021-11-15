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
import kraken.context.path.ContextPathExtractor;
import kraken.context.path.node.ContextPathNodeRepository;
import kraken.el.TargetEnvironment;
import kraken.runtime.model.context.RuntimeContextDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class with reusable utility methods to construct {@link ContextModelTree}
 *
 * @author psurinin
 */
public final class ContextModelTrees {

    private final static Logger LOGGER = LoggerFactory.getLogger(ContextModelTrees.class);

    private ContextModelTrees() {
    }

    /**
     * Creates {@link ContextModelTree}. During creation it collects all {@link RuntimeContextDefinition}s
     * from {@link ContextRepository} by provided namespace. Translates them according
     * {@link TargetEnvironment} and resolves inheritance model and context paths.
     *
     * @param contextRepository Context Repository.
     * @param namespace         Namespace to create context model tree for.
     * @param targetEnvironment Target environment.
     * @return Serializable context model tree.
     */
    public static ContextModelTree create(
            ContextRepository contextRepository,
            String namespace,
            TargetEnvironment targetEnvironment
    ) {
        LOGGER.debug("Initializing Context Model Tree: namespace '{}', target environment: '{}'", namespace, targetEnvironment);
        final long startTime = System.nanoTime();
        var allContextNames = contextRepository.getKeys();
        final Map<String, RuntimeContextDefinition> contextDefinitionMap = allContextNames.stream()
                .map(contextRepository::get)
                .collect(Collectors.toMap(RuntimeContextDefinition::getName, x -> x));
        final ContextPathNodeRepository repository = ContextRepository.asContextNodeRepository(contextRepository);
        final ContextPathExtractor contextPathExtractor = ContextPathExtractor.create(repository, contextRepository.getRootName());
        final ContextModelTreeMetadata metadata = new ContextModelTreeMetadata(namespace, targetEnvironment);
        final long endTime = System.nanoTime();

        LOGGER.debug("Model tree initialization time: {}ms", (endTime - startTime) / 1000000);

        return new ContextModelTreeImpl(contextDefinitionMap, contextPathExtractor.getAllPaths(), metadata);
    }

}
