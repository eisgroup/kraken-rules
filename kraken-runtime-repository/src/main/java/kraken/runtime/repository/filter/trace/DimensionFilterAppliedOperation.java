/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.repository.filter.trace;

import java.util.Collection;

import kraken.runtime.repository.filter.DimensionFilter;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after {@code DimensionFilter} is applied. States
 * applied dimension filter name and a number of previous and remaining versions
 * of versioned item.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class DimensionFilterAppliedOperation implements VoidOperation {

    private final DimensionFilter dimensionFilter;
    private final Collection<?> beforeFilter;
    private final Collection<?> afterFilter;

    public DimensionFilterAppliedOperation(DimensionFilter dimensionFilter,
                                           Collection<?> beforeFilter,
                                           Collection<?> afterFilter) {
        this.dimensionFilter = dimensionFilter;
        this.beforeFilter = beforeFilter;
        this.afterFilter = afterFilter;
    }

    @Override
    public String describe() {
        var template = "Dimension filter '%s' applied. Versions before filter - %s, versions after filter - %s.";

        return String.format(template,
            dimensionFilter.getClass().getSimpleName(),
            beforeFilter.size(),
            afterFilter.size());
    }

}
