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
