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
import kraken.context.model.tree.impl.ContextRepository;
import kraken.context.model.tree.repository.CachingContextModelTreeRepository.ContextRepositoryRegistry;
import kraken.el.TargetEnvironment;

import java.util.Optional;

/**
 * If no {@link ContextModelTree} is found by provide  parameters, then it will be resolved in
 * runtime and cached. For Caching repository is used {@link CachingContextModelTreeRepository}.
 * After calling method {@link RecoveringStaticContextModelTreeRepository#clearCache()},
 * static repository is considered as invalid and only runtime caching repository will be used to
 * resolve context model trees. This is a mechanism to support dynamic {@link ContextRepository},
 * which context might be changed in runtime.
 *
 * @author psurinin
 */
public class RecoveringStaticContextModelTreeRepository implements ContextModelTreeRepository, Cached {

    private final static StaticContextModelTreeRepository staticContextModelTreeRepository =
            StaticContextModelTreeRepository.initialize();

    private boolean isStaticRepositoryApplicable;
    private final CachingContextModelTreeRepository cachingContextModelTreeRepository;

    public RecoveringStaticContextModelTreeRepository(ContextRepositoryRegistry registry) {
        this.cachingContextModelTreeRepository = new CachingContextModelTreeRepository(registry);
        this.isStaticRepositoryApplicable = true;
    }

    @Override
    public ContextModelTree get(String namespace, TargetEnvironment targetEnvironment) {
        if (isStaticRepositoryApplicable) {
            return Optional.ofNullable(staticContextModelTreeRepository.get(namespace, targetEnvironment))
                    .orElseGet(() -> cachingContextModelTreeRepository.get(namespace, targetEnvironment));
        }
        return cachingContextModelTreeRepository.get(namespace, targetEnvironment);
    }

    @Override
    public void clearCache() {
        this.isStaticRepositoryApplicable = false;
        cachingContextModelTreeRepository.clearCache();
    }
}
