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
package kraken.context.model.tree.repository;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.impl.ContextModelTrees;
import kraken.context.model.tree.impl.ContextRepository;
import kraken.el.TargetEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

/**
 * Repository that will create {@link ContextModelTree} in runtime and cache it by namespace,
 * target environment and context repository.
 *
 * @author psurinin
 */
public class CachingContextModelTreeRepository implements ContextModelTreeRepository, Cached {

    private final static Logger LOGGER = LoggerFactory.getLogger(CachingContextModelTreeRepository.class);
    private final static ConcurrentHashMap<String, ContextModelTree> modelTreeCache = new ConcurrentHashMap<>();
    private final ContextRepositoryRegistry registry;

    private static ContextModelTree createOrGet(ContextRepository contextRepository, String namespace, TargetEnvironment targetEnvironment) {
        LOGGER.debug(
                "Accessing Context Model Tree: \n\tnamespace '{}', \n\ttarget environment: '{}'",
                namespace,
                targetEnvironment
        );
        return modelTreeCache.computeIfAbsent(
                requireNonNull(namespace),
                key -> ContextModelTrees.create(contextRepository, namespace, targetEnvironment)
        );
    }

    public CachingContextModelTreeRepository(ContextRepositoryRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ContextModelTree get(String namespace, TargetEnvironment targetEnvironment) {
        return createOrGet(registry.get(namespace, targetEnvironment), namespace, targetEnvironment);
    }

    @Override
    public void clearCache() {
        modelTreeCache.clear();
    }

    public interface ContextRepositoryRegistry {
        ContextRepository get(String namespace, TargetEnvironment targetEnvironment);
    }
}
