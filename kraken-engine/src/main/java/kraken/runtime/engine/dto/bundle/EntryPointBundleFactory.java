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
package kraken.runtime.engine.dto.bundle;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.Kraken;
import kraken.dimensions.DimensionSet;
import kraken.runtime.EvaluationMode;
import kraken.runtime.engine.core.EntryPointData;
import kraken.runtime.engine.core.EntryPointEvaluation;
import kraken.runtime.engine.core.EntryPointOrderedEvaluationFactory;
import kraken.runtime.engine.dto.bundle.trace.EntryPointBundleBuildOperation;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.RuntimeProjectRepository;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.tracer.Tracer;
import kraken.utils.Namespaces;

/**
 * @author psurinin
 * @since 1.0.20
 */
public final class EntryPointBundleFactory {

    private final RuntimeProjectRepositoryFactory runtimeProjectRepositoryFactory;

    public EntryPointBundleFactory(RuntimeProjectRepositoryFactory runtimeProjectRepositoryFactory) {
        this.runtimeProjectRepositoryFactory = requireNonNull(runtimeProjectRepositoryFactory);
    }

    /**
     * Builds {@link EntryPointBundle}, containing all Kraken model data for specified entry point name.
     * Constructed bundle is used by core rule engine to evaluate rules.
     * Executed each time entry point evaluation is requested from backend.
     *
     * @param entryPointName full entry point name with namespace prefixed
     * @param context        to resolve {@link RuntimeRule}s.
     */
    public EntryPointBundle build(
            String entryPointName,
            Map<String, Object> context,
            Set<DimensionSet> excludes,
            EvaluationMode evaluationMode
    ) {
        return doBuild(entryPointName, context, evaluationMode, excludes);
    }

    private EntryPointBundle doBuild(String entryPointName,
                                     Map<String, Object> context,
                                     EvaluationMode evaluationMode,
                                     Set<DimensionSet> excludes) {
        Objects.requireNonNull(entryPointName);
        Objects.requireNonNull(context);
        Objects.requireNonNull(evaluationMode);

        return Tracer.doOperation(
            new EntryPointBundleBuildOperation(entryPointName, excludes),
            () -> {
                String namespace = Namespaces.toNamespaceName(entryPointName);
                RuntimeProjectRepository repository = runtimeProjectRepositoryFactory.resolveRepository(namespace);

                String simpleEntryPointName = Namespaces.toSimpleName(entryPointName);
                Map<String, RuntimeRule> rules = filterRules(
                    repository.resolveRules(simpleEntryPointName, context),
                    evaluationMode
                );

                EntryPointData entryPointData
                    = new EntryPointData(entryPointName, rules);
                EntryPointOrderedEvaluationFactory evaluationFactory
                    = createEvaluationFactory(repository);
                EntryPointEvaluation entryPointEvaluation
                    = evaluationFactory.create(entryPointData, excludes);

                return new EntryPointBundle(entryPointEvaluation, context, Kraken.VERSION);
            }
        );
    }

    private Map<String, RuntimeRule> filterRules(Map<String, RuntimeRule> allRules, EvaluationMode evaluationMode) {
        if (evaluationMode == EvaluationMode.ALL) {
            return allRules;
        }

        return allRules.entrySet()
            .stream()
            .filter(entry -> evaluationMode.isSupported(entry.getValue().getPayload().getType()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private EntryPointOrderedEvaluationFactory createEvaluationFactory(RuntimeProjectRepository repository) {
        return new EntryPointOrderedEvaluationFactory(repository);
    }

}
