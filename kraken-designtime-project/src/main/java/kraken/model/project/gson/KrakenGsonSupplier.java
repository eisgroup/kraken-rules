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
package kraken.model.project.gson;

import com.google.gson.Gson;

/**
 * To be implemented by {@link kraken.model.project.KrakenProject} if it provides Kraken specific Gson
 * instance.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public interface KrakenGsonSupplier {

    /**
     * Returns Kraken specific Gson instance to be used to correctly serialize/deserialize
     * Kraken types.
     *
     * @implNote Any subsequent calls to this method will return the same Gson instance.
     */
    Gson getKrakenGson();

}
