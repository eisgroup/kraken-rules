/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.ccr;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import kraken.context.path.ContextPath;
import kraken.context.path.ContextPathExtractor;
import kraken.context.path.ContextPathExtractor.Cycle;
import kraken.cross.context.path.CrossContextPath;
import kraken.cross.context.path.CrossContextPathsResolver;
import kraken.cross.context.path.DefaultCrossContextPathsResolver;
import kraken.cross.context.path.DesigntimeContextCardinalityResolver;
import kraken.model.project.KrakenProject;

/**
 * Wraps {@link ContextPathExtractor} and {@link CrossContextServiceProvider} services under single interface for
 * designtime KrakenProject scope
 *
 * @author mulevicius
 */
public class CrossContextService {

    private final ContextPathExtractor contextPathExtractor;

    private final CrossContextPathsResolver crossContextPathsResolver;

    public CrossContextService(KrakenProject krakenProject) {
        ProjectContextPathNodeRepository pathNodeRepository = ProjectContextPathNodeRepository.create(krakenProject);
        this.contextPathExtractor = ContextPathExtractor.create(pathNodeRepository, krakenProject.getRootContextName());
        this.crossContextPathsResolver = DefaultCrossContextPathsResolver.create(
            DesigntimeContextCardinalityResolver.create(pathNodeRepository),
            contextPathExtractor.getAllPaths());
    }

    public List<ContextPath> getPathsFor(String contextDefName) {
        return contextPathExtractor.getPathsFor(contextDefName);
    }

    public Map<String, Collection<ContextPath>> getAllPaths() {
        return contextPathExtractor.getAllPaths();
    }

    public Collection<Cycle> getAllCycles() {
        return contextPathExtractor.getAllCycles();
    }

    public List<CrossContextPath> resolvePaths(ContextPath fromPath, String targetContextName) {
        return crossContextPathsResolver.resolvePaths(fromPath, targetContextName);
    }

    public boolean hasPathTo(String targetContextName) {
        return Optional.of(getPathsFor(targetContextName)).map(p -> !p.isEmpty()).orElse(false);
    }
}
