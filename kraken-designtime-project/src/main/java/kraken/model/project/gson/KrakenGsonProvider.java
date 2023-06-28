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

import kraken.model.project.KrakenProject;

/**
 * A provider to be used to obtain Kraken specific Gson instance.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public final class KrakenGsonProvider {

    /**
     * Resolves Kraken Gson instance from Kraken project if applicable, otherwise returns
     * a new instance.
     *
     * @param krakenProject Kraken project to resolve or create instance for.
     * @return Kraken Gson instance.
     */
    public static Gson forProject(KrakenProject krakenProject) {
        return krakenProject instanceof KrakenGsonSupplier
            ? ((KrakenGsonSupplier) krakenProject).getKrakenGson()
            : new KrakenGsonAdapter.Builder(krakenProject).create();
    }

}
