/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.repository.filter.trace;

import java.util.Collection;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.tracer.Operation;
import kraken.utils.GsonUtils;

/**
 * Operation to be added to trace to wrap entrypoint dimension filtering logic.
 * Describes input before dimension filters are applied and result.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class EntryPointDimensionFilteringOperation implements Operation<RuntimeEntryPoint> {

    private final Gson gson = GsonUtils.gson();

    private final Collection<RuntimeEntryPoint> entryPointVersion;

    public EntryPointDimensionFilteringOperation(Collection<RuntimeEntryPoint> runtimeEntryPoints) {
        this.entryPointVersion = runtimeEntryPoints;
    }

    @Override
    public String describe() {
        var template = "Applying dimension filters on entry point '%s' which has %s version(s): %s";

        return String.format(template,
            entryPointVersion.iterator().next().getName(),
            entryPointVersion.size(),
            System.lineSeparator() + entryPointVersion.stream()
                .map(this::describeEntryPoint)
                .collect(Collectors.joining(System.lineSeparator())));
    }

    @Override
    public String describeAfter(RuntimeEntryPoint result) {
        var template = "Dimension filters applied. %s";
        var noVersionsTemplate = "All version were filtered out.";
        var filteredVersionTemplate = "Remaining version: %s";

        return String.format(template,
            result == null
                ? noVersionsTemplate
                : String.format(filteredVersionTemplate, describeEntryPoint(result)));
    }

    private String describeEntryPoint(RuntimeEntryPoint runtimeEntryPoint) {
        return runtimeEntryPoint.getMetadata() == null
            ? "{}"
            : gson.toJson(runtimeEntryPoint.getMetadata().getProperties());
    }

}
