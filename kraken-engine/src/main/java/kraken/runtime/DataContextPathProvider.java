/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import kraken.annotations.SPI;

/**
 * SPI that provides methods to resolve a path to data context based
 * on data context traits.
 * <p>
 * A custom implementation can be injected into {@link RuleEngine}
 * by providing instance of it to {@link EvaluationConfig}.
 *
 * @author Tomas Dapkunas
 * @since 1.52.0
 * @see EvaluationConfig
 * @see kraken.runtime.engine.context.data.DataContext
 */
@SPI
@FunctionalInterface
public interface DataContextPathProvider {

    /**
     * A default implementation to be used when no custom implementation
     * is provided or data context paths are not needed.
     */
    DataContextPathProvider DEFAULT = dataContextId -> null;

    /**
     * Returns a path to data context for a given data context identifier.
     *
     * @param dataContextId A unique identifier of data context.
     * @return A path to data context or {@code null} if no path can be found.
     */
    @Nullable
    String getPath(@Nonnull String dataContextId);

}
