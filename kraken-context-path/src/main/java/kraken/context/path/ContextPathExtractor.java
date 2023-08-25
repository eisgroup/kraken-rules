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
package kraken.context.path;

import kraken.context.path.node.ContextPathNode;
import kraken.context.path.node.ContextPathNodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that traverses {@link ContextPathNode} paths in full model.
 * If model is like:
 * parent :
 *      child-a:
 *          sister
 *      child-b:
 *          grand-child-a:
 *              sister
 * Then 'sister' will have 2 {@link ContextPath}s (parent -> child-a -> sister,
 * parent -> child-b -> grand-child-a -> sister).
 *
 * @author psurinin
 * @since 1.0
 */
public final class ContextPathExtractor  {

    private final Map<String, Collection<ContextPath>> paths;
    private final ContextPathNodeRepository repository;
    private final Collection<Cycle> cycles;

    private ContextPathExtractor(ContextPathNodeRepository repository, String contextName) {
        this.paths = new HashMap<>();
        this.cycles = new ArrayList<>();
        this.repository = repository;
        collectAllContextPaths(
                contextName,
                new ContextPath.ContextPathBuilder(contextName).build()
        );
    }

    /**
     * Creates and returns a new instance of {@code ContextPathExtractor} with given
     * arguments.
     *
     * @param repository  Context path node repository.
     * @param contextName Context name to traverse paths from.
     * @return New Instance of {@code ContextPathExtractor}.
     */
    public static ContextPathExtractor create(ContextPathNodeRepository repository, String contextName) {
        return new ContextPathExtractor(repository, contextName);
    }

    /**
     * Returns context paths to all node instances of given context definition name
     * in a model tree.
     *
     * @param contextDefName Context definition name.
     * @return Paths to context definition nodes.
     */
    public List<ContextPath> getPathsFor(String contextDefName) {
        return Optional.ofNullable(paths.get(contextDefName))
                .orElse(List.of())
                .stream()
                .distinct()
                .collect(Collectors.toList());
    }

    public Map<String, Collection<ContextPath>> getAllPaths() {
        return paths;
    }

    private void collectAllContextPaths(String contextName, ContextPath path) {
        ContextPathNode contextPathNode = repository.get(contextName);
        addPath(contextPathNode.getName(), path);

        contextPathNode.getInherited().forEach(
                inheritedContextName -> addPath(inheritedContextName, path)
        );

        contextPathNode.getChildren().keySet().stream()
                .filter(repository::has)
                .filter(child -> isPathNotRecursive(contextPathNode, path, child))
                .map(ContextPath.ContextPathBuilder::new)
                .map(childContextPath -> childContextPath.addPathElements(path.getPath()).build())
                .forEach(childContextPath -> collectAllContextPaths(childContextPath.getLastElement(), childContextPath));
    }

    private boolean isPathNotRecursive(ContextPathNode currentContext, ContextPath path, String childContextName) {
        final long numberOfCtxInPath = path.getPath().stream().filter(c -> Objects.equals(c, childContextName)).count();
        final boolean isOperationValid = numberOfCtxInPath < 1;

        if (!isOperationValid) {
            var cyclePaths = Stream.concat(path.getPath().subList(
                    path.getPath().indexOf(childContextName),
                    path.getPathLength()).stream(),
                    collectAllChildNodes(
                            new HashSet<>(),
                            currentContext.getChildren()
                                    .keySet()
                                    .stream()
                                    .filter(childName -> !childName.equals(childContextName))
                                    .collect(Collectors.toSet())
                    ).stream())
                    .collect(Collectors.toList());

            cycles.add(new Cycle(cyclePaths));
        }

        return isOperationValid;
    }

    private Set<String> collectAllChildNodes(Set<String> collected, Set<String> childContextNames) {
        for (String contextName : childContextNames) {
            if (!collected.contains(contextName)) {
                ContextPathNode contextPathNode = repository.get(contextName);
                collected.add(contextName);

                if (contextPathNode != null) {
                    collectAllChildNodes(collected, contextPathNode.getChildren().keySet());
                }
            }
        }

        return collected;
    }


    private void addPath(String contextName, ContextPath ctxPath) {
        paths.merge(contextName, new ArrayList<>(List.of(ctxPath)), (contextPaths, contextPaths2) -> {
            contextPaths.addAll(contextPaths2);
            return contextPaths;
        });
    }

    public Collection<Cycle> getAllCycles() {
        return cycles;
    }

    public static class Cycle {
        private final List<String> nodeNames;

        public Cycle(List<String> nodeNames) {
            this.nodeNames = nodeNames;
        }

        public List<String> getNodeNames() {
            return nodeNames;
        }
    }

}
