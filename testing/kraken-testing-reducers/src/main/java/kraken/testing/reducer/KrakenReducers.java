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

package kraken.testing.reducer;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import kraken.model.payload.EvaluationType;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import kraken.runtime.engine.result.reducers.EntryPointResultReducer;

/**
 * Reducers implemented for testing purposes.
 * This class contains {@link EntryPointResultReducer}s for different
 * {@link kraken.model.Rule} payload types.
 * <p> For {@link kraken.model.validation.ValidationPayload} - {@link KrakenReducers#VALIDATION} </p>
 * <p> For {@link kraken.model.derive.DefaultValuePayload} - {@link KrakenReducers#DEFAULT} </p>
 * <p> For {@link kraken.model.state.AccessibilityPayload} -  {@link KrakenReducers#ACCESSIBILITY} </p>
 * <p> For {@link kraken.model.state.VisibilityPayload} -  {@link KrakenReducers#VISIBILITY} </p>
 *
 * @author psurinin
 * @see EntryPointResultReducer
 * @since 1.0.38
 */
public class KrakenReducers {

    /**
     * {@link kraken.model.Rule#getCondition()} evaluation result
     */
    public static class ConditionResult {
        /**
         * Condition is not evaluated, because invalid of data or expression
         */
        public static final String EXPRESSION_EVALUATION_FAILED = "ignored";
        /**
         * Condition evaluated to {@code false}
         */
        public static final String NOT_APPLICABLE = "not applicable";
        /**
         * Condition evaluated to {@code true} or is absent
         */
        public static final String APPLICABLE = "applicable";
    }

    /**
     * {@link EntryPointResultReducer} for {@link kraken.model.state.VisibilityPayload}
     */
    public static EntryPointResultReducer<Collection<VisibilityMetadata>> VISIBILITY = results -> createMetadata(
            results,
            EvaluationType.VISIBILITY,
            VisibilityPayloadResult.class,
            VisibilityMetadata::new
    );

    /**
     * {@link EntryPointResultReducer} for {@link kraken.model.state.AccessibilityPayload}
     */
    public static EntryPointResultReducer<Collection<AccessibilityMetadata>> ACCESSIBILITY = results -> createMetadata(
            results,
            EvaluationType.ACCESSIBILITY,
            AccessibilityPayloadResult.class,
            AccessibilityMetadata::new
    );

    /**
     * {@link EntryPointResultReducer} for {@link kraken.model.validation.ValidationPayload}
     */
    public static EntryPointResultReducer<Collection<ValidationMetadata>> VALIDATION = results -> createMetadata(
            results,
            EvaluationType.VALIDATION,
            ValidationPayloadResult.class,
            ValidationMetadata::new
    );

    /**
     * {@link EntryPointResultReducer} for {@link kraken.model.derive.DefaultValuePayload}
     */
    public static EntryPointResultReducer<Collection<DefaultValueMetadata>> DEFAULT = results -> createMetadata(
            results,
            EvaluationType.DEFAULT,
            DefaultValuePayloadResult.class,
            DefaultValueMetadata::new
    );

    /**
     * Filters collection of reduced results and returns results applicable for rule name from parameters.
     * If none of rules are evaluated by this rule name, an empty collection will be returned.
     *
     * @param results  reduced rule results
     * @param ruleName rule name
     * @param <T>      reduced result type
     * @return filtered results.
     */
    public static <T extends ResultMetadataContainer> Collection<T> forOneRule(Collection<T> results, String ruleName) {
        return results.stream()
                .filter(x -> x.getResultMetadata().getRuleName().equals(ruleName))
                .collect(Collectors.toList());
    }

    private static <M, RESULT extends PayloadResult> Collection<M> createMetadata(
            EntryPointResult results,
            EvaluationType evaluationType,
            Class<RESULT> resultClass,
            BiFunction<RESULT, ResultMetadata, M> toResult
    ) {
        return results.getFieldResults()
                .values().stream()
                .flatMap(field -> field.getRuleResults().stream()
                        .filter(x -> evaluationType == x.getRuleInfo().getPayloadType().getEvaluationType())
                        .map(result -> toResult.apply(
                                resultClass.cast(result.getPayloadResult()),
                                new ResultMetadata(
                                        result.getRuleInfo().getRuleName(),
                                        field.getContextFieldInfo().getContextId(),
                                        field.getContextFieldInfo().getContextName(),
                                        field.getContextFieldInfo().getFieldName(),
                                        result.getConditionEvaluationResult().getError() == null
                                                ? (result.getConditionEvaluationResult().isApplicable() ? ConditionResult.APPLICABLE : ConditionResult.NOT_APPLICABLE)
                                                : ConditionResult.EXPRESSION_EVALUATION_FAILED
                                ))
                        )
                )
                .collect(Collectors.toList());
    }

}
