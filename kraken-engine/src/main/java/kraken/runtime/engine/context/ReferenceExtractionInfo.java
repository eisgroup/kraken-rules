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

import kraken.context.path.ContextPath;
import kraken.cross.context.path.CommonPathResolver;
import kraken.model.context.Cardinality;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataContexts;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Information to extract external {@link DataContext} references (Cross component references)
 *
 * @author psurinin
 */
public class ReferenceExtractionInfo {

    private final String dependencyName;
    private final DataContext extractionRoot;
    private final List<String> extractionPath;
    private final Cardinality cardinality;

    ReferenceExtractionInfo(String dependencyName, DataContext extractionRoot, List<String> extractionPath, Cardinality cardinality) {
        this.dependencyName = dependencyName;
        this.extractionRoot = extractionRoot;
        this.extractionPath = extractionPath;
        this.cardinality = cardinality;
    }

    List<String> getExtractionPath() {
        return extractionPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReferenceExtractionInfo that = (ReferenceExtractionInfo) o;
        return Objects.equals(extractionRoot, that.extractionRoot) &&
                Objects.equals(extractionPath, that.extractionPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extractionRoot, extractionPath);
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    DataContext resolveCommonRoot() {
        List<DataContext> dataContexts = DataContexts.parents(extractionRoot);
        ContextPath commonPath = CommonPathResolver.getCommonPath(
                asPath(extractionPath),
                asPath(dataContexts.stream()
                        .map(dataContext -> dataContext.getContextName())
                        .collect(Collectors.toList())));

        return dataContexts.stream()
                .filter(paths -> paths.getContextName().equals(commonPath.getLastElement()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Failed to find common parent named " + commonPath.getLastElement()));
    }

    private ContextPath asPath(List<String> pathElements) {
        return new ContextPath.ContextPathBuilder().addPathElements(pathElements).build();
    }

    String getDependencyName() {
        return dependencyName;
    }

}
