/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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

/**
 * SPI that provides methods to resolve a path to data context based
 * on data context traits.
 * <p>
 * A custom implementation can be injected into {@link SyncEngine}
 * by providing instance of it to {@link EvaluationConfig}.
 *
 * @author Tomas Dapkunas
 * @since 1.52.0
 */
export interface DataContextPathProvider {
    /**
     * Returns a path to data context for a given data context identifier.
     *
     * @param dataContextId A unique identifier of data context.
     * @return A path to data context or {@code undefined} if no path can be found.
     */
    getPath(dataContextId: string): string | undefined
}

export const DEFAULT_PATH_PROVIDER: DataContextPathProvider = {
    getPath(_dataContextId: string): string | undefined {
        return undefined
    },
}
