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
package kraken.cross.context.path;

import kraken.context.path.ContextPath;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching implementation of {@code CrossContextPathsResolver} which decorates path resolver and
 * stores previously resolved values in a cache.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class CachingCrossContextPathsResolver implements CrossContextPathsResolver {

    private static final Map<String, List<CrossContextPath>> PATHS_CACHE = new ConcurrentHashMap<>();
    private final CrossContextPathsResolver crossContextPathsResolver;

    private CachingCrossContextPathsResolver(CrossContextPathsResolver crossContextPathsResolver) {
        this.crossContextPathsResolver = crossContextPathsResolver;
    }

    public static CrossContextPathsResolver create(CrossContextPathsResolver crossContextPathsResolver) {
        return new CachingCrossContextPathsResolver(crossContextPathsResolver);
    }

    @Override
    public List<CrossContextPath> resolvePaths(ContextPath fromPath, String targetContextName) {
        String key = createKey(fromPath.getPathAsString(), targetContextName);

        return PATHS_CACHE
                .computeIfAbsent(key, k -> crossContextPathsResolver.resolvePaths(fromPath, targetContextName));
    }

    private String createKey(String fromPath, String targetContextName) {
        return fromPath + "_" + targetContextName;
    }

}
