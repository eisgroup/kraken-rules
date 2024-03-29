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

import kraken.dimensions.DimensionSet;
import kraken.runtime.engine.core.EntryPointEvaluation;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO for expressionContext definitions, that need to be accessed in process of
 * {@link EntryPointEvaluation} evaluation.
 *
 * Use {@link kraken.utils.GsonUtils} when serializing bundle for UI Kraken Engine
 *
 * @author psurinin
 * @since 1.0
 */
public class EntryPointBundle {

    private final EntryPointEvaluation evaluation;

    private final Map<String, Object> expressionContext;

    /**
     * Engine version is used to validate Ui and backend versions in the runtime.
     * By default 'kraken-engine' maven module version is used.
     * In UI engine it is compared with SyncEngineConfig#engineCompatibilityVersion.
     *
     * @since 1.14.0
     */
    private final String engineVersion;

    public EntryPointBundle(
        EntryPointEvaluation evaluation,
        Map<String, Object> expressionContext,
        String engineVersion
    ) {
        this.evaluation = evaluation;
        this.expressionContext = expressionContext;
        this.engineVersion = engineVersion;
    }

    public EntryPointEvaluation getEvaluation() {
        return evaluation;
    }

    public Map<String, Object> getExpressionContext() {
        return expressionContext;
    }

    public String getEngineVersion() {
        return engineVersion;
    }

    /**
     * Rebuilds and returns a new instance of {@link EntryPointBundle} without rules that are excluded by dimension sets.
     * This allows to reduce the size of the bundle and return only those rules that are missing in the target
     * environment.
     *
     * @param dimensionSets dimension sets that indicate rules that are already present in the target environment
     *                      and therefore should not be in the bundle.
     * @return a new instance of {@link EntryPointBundle}
     */
    public EntryPointBundle withoutRulesExcludedBy(Set<DimensionSet> dimensionSets) {
        return new EntryPointBundle(
            new EntryPointEvaluation(
                evaluation.getEntryPointName(),
                evaluation.getRules().stream()
                    .filter(r -> !dimensionSets.contains(r.getDimensionSet()))
                    .collect(Collectors.toList()),
                evaluation.getFieldOrder()
            ),
            expressionContext,
            engineVersion
        );
    }
}
