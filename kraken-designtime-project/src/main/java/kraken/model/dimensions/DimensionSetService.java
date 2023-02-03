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
package kraken.model.dimensions;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import kraken.dimensions.DimensionSet;
import kraken.dimensions.DimensionSet.Variability;
import kraken.model.Rule;
import kraken.model.dimensions.DimensionSetResolver;
import kraken.model.dimensions.DimensionSetResolverHolder;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;

/**
 * Resolves dimension set by which the rule or entry point varies.
 * Uses {@link kraken.model.dimensions.DimensionSetResolver} implementation if available.
 *
 * @author kjuraityte
 * @since 1.40.0
 */
public class DimensionSetService {

    private final KrakenProject krakenProject;

    private final DimensionSetResolver dimensionSetResolver;

    public DimensionSetService(KrakenProject krakenProject) {
        this(krakenProject, DimensionSetResolverHolder.getInstance());
    }

    public DimensionSetService(KrakenProject krakenProject, DimensionSetResolver dimensionSetResolver) {
        this.krakenProject = Objects.requireNonNull(krakenProject);
        this.dimensionSetResolver = Objects.requireNonNull(dimensionSetResolver);
    }

    public DimensionSet resolveRuleDimensionSet(String namespace, Rule rule) {
        List<DimensionSet> dimensionSets = krakenProject.getRuleVersions().get(rule.getName()).stream()
            .map(ruleVersion -> dimensionSetResolver.resolve(namespace, ruleVersion))
            .collect(Collectors.toList());

        return merge(dimensionSets);
    }

    public DimensionSet resolveEntryPointDimensionSet(String namespace, EntryPoint entryPoint) {
        List<DimensionSet> dimensionSets = krakenProject.getEntryPointVersions().get(entryPoint.getName()).stream()
            .map(entryPointVersion -> dimensionSetResolver.resolve(namespace, entryPointVersion))
            .collect(Collectors.toList());

        return merge(dimensionSets);
    }

    private DimensionSet merge(List<DimensionSet> dimensionSets) {
        if(dimensionSets.stream().allMatch(DimensionSet::isStatic)) {
            return DimensionSet.createStatic();
        }
        if(dimensionSets.stream().anyMatch(d -> d.getVariability() == Variability.UNKNOWN)) {
            return DimensionSet.createForUnknownDimensions();
        }
        Set<String> allDimensionNames = dimensionSets.stream()
            .filter(d -> d.getDimensions() != null)
            .flatMap(d -> d.getDimensions().stream())
            .collect(Collectors.toSet());
        return DimensionSet.createForDimensions(allDimensionNames);
    }

}
