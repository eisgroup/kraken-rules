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
package kraken.runtime.engine.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.dimensions.DimensionSet;
import kraken.model.payload.PayloadType;
import kraken.runtime.engine.evaluation.loop.OrderedEvaluationLoop;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeContextRepository;

/**
 * Creates {@link EntryPointEvaluation}.
 * Field order is resolved by default {@link RuntimeRule} according to rule dependencies.
 * {@link OrderedEvaluationLoop} will process default rules according to field order.
 *
 * @see OrderResolver
 * @see OrderedEvaluationLoop
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class EntryPointOrderedEvaluationFactory {

    private final OrderResolver orderResolver;

    public EntryPointOrderedEvaluationFactory(RuntimeContextRepository contextRepository) {
        this.orderResolver = new OrderResolver(contextRepository);
    }

    /**
     * Creates {@link EntryPointEvaluation} with rules and field evaluation order for default rules
     *
     * @param entryPointData with rules
     * @return entry point evaluation with rules and field order
     */
    public EntryPointEvaluation create(EntryPointData entryPointData) {
        List<RuntimeRule> rules = new ArrayList<>(entryPointData.getIncludedRules().values());
        List<String> fieldOrder = calculateFieldOrder(entryPointData);
        return new EntryPointEvaluation(entryPointData.getEntryPoint(), rules, fieldOrder);
    }

    private List<String> calculateFieldOrder(EntryPointData entryPointData) {
        List<RuntimeRule> defaultRules = entryPointData.getIncludedRules().values().stream()
            .filter(rule -> rule.getPayload().getType().equals(PayloadType.DEFAULT))
            .collect(Collectors.toList());
        return orderResolver.resolveOrderedFields(defaultRules).stream()
            .map(a -> a.getContextName() + "." + a.getContextField())
            .collect(Collectors.toList());
    }
}
