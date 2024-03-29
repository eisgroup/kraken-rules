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
package kraken.runtime.engine.context;

import static kraken.message.SystemMessageBuilder.Message.CONTEXT_PATH_EXTRACTION_MISSING;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import kraken.context.path.ContextPath;
import kraken.cross.context.path.CrossContextPath;
import kraken.cross.context.path.CrossContextPathsResolver;
import kraken.message.SystemMessageBuilder;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataContexts;
import kraken.runtime.engine.context.data.DataReference;
import kraken.runtime.model.rule.Dependency;

/**
 * Updates {@link DataContext}. Adds external references.
 *
 * @author psurinin
 */
class DataContextReferenceUpdater {

    private final Function<ReferenceExtractionInfo, Collection<DataContext>> extractReferences;
    private final Collection<Dependency> dependencies;
    private final CrossContextPathsResolver crossContextPathsResolver;

    public DataContextReferenceUpdater(
            CrossContextPathsResolver crossContextPathsResolver,
            Function<ReferenceExtractionInfo, Collection<DataContext>> extractReferences,
            Collection<Dependency> dependencies) {
        this.extractReferences = extractReferences;
        this.dependencies = dependencies;
        this.crossContextPathsResolver = crossContextPathsResolver;
    }

    public void update(DataContext dataContext) {
        referenceExtractions(dataContext)
            .forEach(ref -> dataContext.updateReference(
                new DataReference(ref.getDependencyName(), extractReferences.apply(ref), ref.getCardinality())));
    }

    private Stream<ReferenceExtractionInfo> referenceExtractions(DataContext dataContext) {
        final Function<String, ReferenceExtractionInfo> toExtractionInfo = toExtractionInfo(dataContext);
        return dependencies.stream()
                .filter(Dependency::isCcrDependency)
                .map(Dependency::getContextName)
                .distinct()
                .map(toExtractionInfo);
    }

    private Function<String, ReferenceExtractionInfo> toExtractionInfo(DataContext dataContext) {
        final ContextPath contextPath = DataContexts.getAsContextPath(dataContext);

        return dependencyName -> {
            List<CrossContextPath> ccPaths = crossContextPathsResolver.resolvePaths(contextPath, dependencyName);

            if (ccPaths.size() != 1) {
                var m = SystemMessageBuilder.create(CONTEXT_PATH_EXTRACTION_MISSING)
                    .parameters(contextPath.getPathAsString(), dependencyName)
                    .build();
                throw new KrakenRuntimeException(m);
            }

            return new ReferenceExtractionInfo(
                    dependencyName,
                    dataContext,
                    ccPaths.get(0).getPath(),
                    ccPaths.get(0).getCardinality()
            );
        };
    }

}
