/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
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
        var template = "Will apply dimension filters for entry point '%s' which has %s version(s): %s";

        return String.format(template,
            entryPointVersion.iterator().next().getName(),
            entryPointVersion.size(),
            System.lineSeparator() + entryPointVersion.stream()
                .map(this::describeEntryPoint)
                .collect(Collectors.joining(System.lineSeparator())));
    }

    @Override
    public String describeAfter(RuntimeEntryPoint result) {
        var template = "Entry point dimension filtering completed. %s";
        var noVersionsTemplate = "All version were filtered out.";
        var filteredVersionTemplate = "Filtered version metadata: %s";

        return String.format(template,
            result == null
                ? noVersionsTemplate
                : String.format(filteredVersionTemplate, gson.toJson(result.getMetadata().getProperties())));
    }

    private String describeEntryPoint(RuntimeEntryPoint runtimeEntryPoint) {
        return runtimeEntryPoint.getMetadata() == null
            ? ""
            : gson.toJson(runtimeEntryPoint.getMetadata().getProperties());
    }

}
