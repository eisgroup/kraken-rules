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
package kraken.runtime;

import kraken.annotations.API;

import java.util.Collections;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * @author psurinin
 */
@API
public final class EvaluationConfig {

    /**
     * Context that will be as a filter to create {@link kraken.model.Rule} and
     * {@link kraken.model.entrypoint.EntryPoint}s that applies to that dimensions
     */
    private final Map<String, Object> context;

    /**
     * Currency to use when interpreting monetary types
     */
    private final String currencyCd;

    private final EvaluationMode evaluationMode;

    private final DataContextPathProvider dataContextPathProvider;

    public EvaluationConfig() {
        this(Collections.emptyMap(), Currency.getInstance(Locale.getDefault()).getCurrencyCode());
    }

    public EvaluationConfig(@Nonnull String currencyCd) {
        this(Collections.emptyMap(), Objects.requireNonNull(currencyCd));
    }

    public EvaluationConfig(@Nonnull Map<String, Object> context, @Nonnull String currencyCd) {
        this(context, currencyCd, EvaluationMode.ALL);
    }

    public EvaluationConfig(@Nonnull Map<String, Object> context,
                            @Nonnull String currencyCd,
                            @Nonnull EvaluationMode evaluationMode) {
        this(context, currencyCd, evaluationMode, DataContextPathProvider.DEFAULT);
    }

    public EvaluationConfig(@Nonnull Map<String, Object> context,
                            @Nonnull String currencyCd,
                            @Nonnull EvaluationMode evaluationMode,
                            @Nonnull DataContextPathProvider dataContextPathProvider) {
        this.context = Objects.requireNonNull(context);
        this.currencyCd = Objects.requireNonNull(currencyCd);
        this.evaluationMode = Objects.requireNonNull(evaluationMode);
        this.dataContextPathProvider = Objects.requireNonNull(dataContextPathProvider);
    }

    @Nonnull
    public Map<String, Object> getContext() {
        return Collections.unmodifiableMap(context);
    }

    @Nonnull
    public String getCurrencyCd() {
        return currencyCd;
    }

    @Nonnull
    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    @Nonnull
    public DataContextPathProvider getDataContextPathProvider() {
        return dataContextPathProvider;
    }

}
