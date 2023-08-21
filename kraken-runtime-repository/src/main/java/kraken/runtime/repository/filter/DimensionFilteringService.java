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

import static kraken.message.SystemMessageBuilder.Message.RULE_REPOSITORY_FILTERING_MULTIPLE_ENTRYPOINTS;
import static kraken.message.SystemMessageBuilder.Message.RULE_REPOSITORY_FILTERING_MULTIPLE_RULES;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import kraken.message.SystemMessageLogger;
import kraken.runtime.model.MetadataContainer;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.filter.trace.DimensionFilterAppliedOperation;
import kraken.runtime.repository.filter.trace.EntryPointDimensionFilteringOperation;
import kraken.runtime.repository.filter.trace.MultipleDimensionResultOperation;
import kraken.runtime.repository.filter.trace.RuleDimensionFilteringOperation;
import kraken.tracer.Tracer;

/**
 * @author mulevicius
 */
public class DimensionFilteringService {

    private final Collection<DimensionFilter> filters;

    private final static SystemMessageLogger logger = SystemMessageLogger.getLogger(DimensionFilteringService.class);

    public DimensionFilteringService(Collection<DimensionFilter> filters) {
        this.filters = Objects.requireNonNull(filters);
    }

    /**
     * Filters out rules using a list of {@link DimensionFilter}. Filters are used in defined order.
     *
     * @param rules to be filtered
     * @return first {@link RuntimeRule} that is not filtered out or empty if no rules remain
     */
    public Optional<RuntimeRule> filterRules(String namespace,
                                             Collection<RuntimeRule> rules,
                                             Map<String, Object> context) {
        return Optional.ofNullable(
            Tracer.doOperation(new RuleDimensionFilteringOperation(rules), () -> {
                Collection<RuntimeRule> filteredRules = filter(namespace, rules, context);

                if (filteredRules.size() > 1) {
                    Tracer.doOperation(new MultipleDimensionResultOperation());
                    var ruleName = rules.iterator().next().getName();
                    logger.warn(RULE_REPOSITORY_FILTERING_MULTIPLE_RULES, ruleName, context);
                }

                return getItem(filteredRules);
            }));
    }

    /**
     * Filters out entryPoints using a list of {@link DimensionFilter}. Filters are used in defined order.
     *
     * @param entryPoints to be filtered
     * @return first {@link RuntimeEntryPoint} that is not filtered out or empty if no entryPoints remain
     */
    public Optional<RuntimeEntryPoint> filterEntryPoints(String namespace,
                                                         Collection<RuntimeEntryPoint> entryPoints,
                                                         Map<String, Object> context) {
        return Optional.ofNullable(
            Tracer.doOperation(new EntryPointDimensionFilteringOperation(entryPoints), () -> {
                Collection<RuntimeEntryPoint> filteredEntryPoints = filter(namespace, entryPoints, context);

                if (filteredEntryPoints.size() > 1) {
                    Tracer.doOperation(new MultipleDimensionResultOperation());
                    var entryPointName = entryPoints.iterator().next().getName();
                    logger.warn(RULE_REPOSITORY_FILTERING_MULTIPLE_ENTRYPOINTS, entryPointName, context);
                }

                return getItem(filteredEntryPoints);
            }));
    }

    private <T extends MetadataContainer> T getItem(Collection<T> items) {
        if (items.isEmpty()) {
            return null;
        }

        return items.iterator().next();
    }

    private <T extends MetadataContainer> Collection<T> filter(String namespace,
                                                               Collection<T> items,
                                                               Map<String, Object> context) {
        Collection<T> filteredItems = items;
        
        for (DimensionFilter dimensionFilter : filters) {
            filteredItems = dimensionFilter.filter(namespace, Collections.unmodifiableCollection(filteredItems), context);
            Tracer.doOperation(new DimensionFilterAppliedOperation(dimensionFilter, items, filteredItems));
        }
        
        return filteredItems;
    }
}
