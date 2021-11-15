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
import kraken.cross.context.path.CachingCrossContextPathsResolver;
import kraken.cross.context.path.CrossContextPathsResolver;
import kraken.cross.context.path.DefaultCrossContextPathsResolver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A factory which creates or resolves existing {@code CachingCrossContextPathsResolver} instances.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public final class CachingCrossContextPathsResolverFactory {

    private static final CachingCrossContextPathsResolverFactory INSTANCE = new CachingCrossContextPathsResolverFactory();
    private final Map<String, CrossContextPathsResolver> PATH_RESOLVERS = new ConcurrentHashMap<>();

    private CachingCrossContextPathsResolverFactory() {

    }

    public static CachingCrossContextPathsResolverFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Resolves {@code CachingCrossContextPathsResolver} for given context model tree.
     *
     * @param contextModelTree Context model tree.
     * @return Caching cross context paths resolver.
     */
    public CrossContextPathsResolver resolve(ContextModelTree contextModelTree) {
        return PATH_RESOLVERS.computeIfAbsent(contextModelTree.getMetadata().getNamespace(), namespace ->
                CachingCrossContextPathsResolver.create(
                        DefaultCrossContextPathsResolver.create(
                                RuntimeContextCardinalityResolver.create(contextModelTree),
                contextModelTree.getPathsToNodes())));
    }

}
