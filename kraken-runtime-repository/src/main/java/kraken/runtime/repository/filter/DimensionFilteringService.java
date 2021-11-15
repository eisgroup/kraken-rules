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
package kraken.runtime.repository.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import kraken.runtime.model.MetadataContainer;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.runtime.model.rule.RuntimeRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mulevicius
 */
public class DimensionFilteringService {

    private final Collection<DimensionFilter> filters;

    private final static Logger logger = LoggerFactory.getLogger(DimensionFilteringService.class);

    public DimensionFilteringService(Collection<DimensionFilter> filters) {
        this.filters = Objects.requireNonNull(filters);
    }

    /**
     * Filters out rules using a list of {@link DimensionFilter}. Filters are used in defined order.
     *
     * @param rules to be filtered
     * @return first {@link RuntimeRule} that is not filtered out or empty if no rules remain
     */
    public Optional<RuntimeRule> filterRules(Collection<RuntimeRule> rules, Map<String, Object> context) {
        Collection<RuntimeRule> filteredRules =  filter(rules, context);
        if (filteredRules.isEmpty()) {
            return Optional.empty();
        } else if (filteredRules.size() > 1) {
            logger.warn(
                    "Returning first of multiple rules found for name {} and context {}",
                    rules.iterator().next().getName(),
                    context
            );
        }
        return Optional.of(filteredRules.iterator().next());
    }

    /**
     * Filters out entryPoints using a list of {@link DimensionFilter}. Filters are used in defined order.
     *
     * @param entryPoints to be filtered
     * @return first {@link RuntimeEntryPoint} that is not filtered out or empty if no entryPoints remain
     */
    public Optional<RuntimeEntryPoint> filterEntryPoints(Collection<RuntimeEntryPoint> entryPoints, Map<String, Object> context) {
        Collection<RuntimeEntryPoint> filteredEntryPoints = filter(entryPoints, context);
        if (filteredEntryPoints.isEmpty()) {
            return Optional.empty();
        } else if (filteredEntryPoints.size() > 1) {
            logger.warn(
                    "Returning first of multiple entryPoints found for name {} and context {}",
                    entryPoints.iterator().next().getName(),
                    context
            );
        }
        return Optional.of(filteredEntryPoints.iterator().next());
    }

    private <T extends MetadataContainer> Collection<T> filter(Collection<T> items, Map<String, Object> context) {
        Collection<T> filteredItems = items;
        for (DimensionFilter dimensionFilter : filters) {
            filteredItems = dimensionFilter.filter(Collections.unmodifiableCollection(filteredItems), context);
        }
        return filteredItems;
    }
}
