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
package kraken.converter;

import java.util.List;
import java.util.stream.Collectors;

import kraken.dimensions.DimensionSet;
import kraken.model.dimensions.DimensionSetService;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;

/**
 * @author mulevicius
 */
public class EntryPointConverter {

    private MetadataConverter metadataConverter = new MetadataConverter();

    private DimensionSetService dimensionSetService;

    private String namespace;

    public EntryPointConverter(DimensionSetService dimensionSetService, String namespace) {
        this.dimensionSetService = dimensionSetService;
        this.namespace = namespace;
    }

    public List<RuntimeEntryPoint> convert(List<kraken.model.entrypoint.EntryPoint> entryPoints) {
        return entryPoints.stream()
            .map(ep -> convert(ep, dimensionSetService.resolveEntryPointDimensionSet(namespace, ep)))
            .collect(Collectors.toList());
    }

    private RuntimeEntryPoint convert(kraken.model.entrypoint.EntryPoint entryPoint, DimensionSet dimensionSet) {
        return new RuntimeEntryPoint(
            entryPoint.getName(),
            entryPoint.getRuleNames().stream().distinct().collect(Collectors.toList()),
            entryPoint.getIncludedEntryPointNames().stream().distinct().collect(Collectors.toList()),
            metadataConverter.convert(entryPoint.getMetadata()),
            dimensionSet);
    }
}
