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
package kraken.model.project.repository;

import kraken.annotations.API;
import kraken.model.project.KrakenProject;

/**
 * Holds and provides instances of {@link KrakenProject}
 *
 * @author mulevicius
 */
@API
public interface KrakenProjectRepository {

    /**
     *
     * @param namespace
     * @return instance of {@link KrakenProject} for the namespace.
     *         {@link KrakenProject} is allowed to mutate between calls to this method and a different instance
     *         may be returned for the same namespace if it has been changed in the meantime.
     *         If {@link KrakenProject} for this namespace does not exist then a null is returned.
     */
    KrakenProject getKrakenProject(String namespace);

}
