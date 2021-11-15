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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Default implementation of {@code CrossContextPathsResolver}.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public final class DefaultCrossContextPathsResolver implements CrossContextPathsResolver {

    private final Map<String, Collection<ContextPath>> contextPaths;
    private final ContextPathCardinalityResolver contextPathCardinalityResolver;

    private DefaultCrossContextPathsResolver(ContextCardinalityResolver contextCardinalityResolver,
                                             Map<String, Collection<ContextPath>> contextPaths) {
        this.contextPaths = contextPaths;
        this.contextPathCardinalityResolver = ContextPathCardinalityResolver.create(contextCardinalityResolver);
    }

    public static DefaultCrossContextPathsResolver create(ContextCardinalityResolver contextCardinalityResolver,
                                                          Map<String, Collection<ContextPath>> contextPaths) {
        return new DefaultCrossContextPathsResolver(contextCardinalityResolver, contextPaths);
    }

    public List<CrossContextPath> resolvePaths(ContextPath fromPath, String targetContextName) {
        Collection<ContextPath> toPaths = getPaths(targetContextName);

        if (toPaths.size() == 1) {
            ContextPath contextPath = toPaths.stream()
                    .findAny().get();

            return List.of(resolveCrossContextPath(contextPath,
                    CommonPathResolver.getCommonPath(fromPath, contextPath)));
        }

        return doResolve(fromPath, toPaths);
    }

    private List<CrossContextPath> doResolve(ContextPath fromPath, Collection<ContextPath> toPaths) {
        int longestCommonPath = 0;
        int lowestScore = Integer.MAX_VALUE;
        List<CrossContextPath> result = new ArrayList<>();

        for (ContextPath toContextPath : toPaths) {
            ContextPath commonPath = CommonPathResolver.getCommonPath(fromPath, toContextPath);

            if (commonPath.getPathLength() < longestCommonPath) {
                // Skip shorter paths.
                continue;
            }

            longestCommonPath = commonPath.getPathLength();

            if (toContextPath.getPathLength() == fromPath.getPathLength() &&
                    longestCommonPath == fromPath.getPathLength()) {
                // Path to self.
                result = new ArrayList<>(List.of(resolveCrossContextPath(toContextPath, commonPath)));
                break;
            }

            int pathScore = calculatePathMatchScore(commonPath, fromPath, toContextPath);

            if (lowestScore > pathScore) {
                lowestScore = pathScore;
                result = new ArrayList<>(List.of(resolveCrossContextPath(toContextPath, commonPath)));
            } else if (lowestScore == pathScore) {
                result.add(resolveCrossContextPath(toContextPath, commonPath));
            }

        }

        return result;
    }


    /**
     * Calculates a match score between given paths. Lower score means higher match.
     *
     * @param commonPath Part of path which is common between from and to paths.
     * @param fromPath From path.
     * @param toPath To path.
     * @return Calculated paths match score.
     */
    private int calculatePathMatchScore(ContextPath commonPath, ContextPath fromPath, ContextPath toPath) {
        return (-3) * (commonPath.getPathLength() - fromPath.getPathLength()) +
                Math.min(toPath.getPathLength() - commonPath.getPathLength(), 2);
    }

    private CrossContextPath resolveCrossContextPath(ContextPath path, ContextPath commonPath) {
        ContextPath pathFrom = new ContextPath.ContextPathBuilder()
                .addPathElements(path.getPathFromInclusive(commonPath.getLastElement()))
                .build();

        return new CrossContextPath(path.getPath(), contextPathCardinalityResolver.resolve(pathFrom));
    }

    private Collection<ContextPath> getPaths(String targetContextName) {
        return Optional.ofNullable(contextPaths.get(targetContextName))
                .filter(paths -> !paths.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("No paths from root node to " +
                        "context definition " + targetContextName + " node exists."));
    }

}
