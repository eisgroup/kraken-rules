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
package kraken.model.project;

import kraken.annotations.API;
import kraken.model.project.exception.IllegalKrakenProjectStateException;
import kraken.model.project.exception.KrakenProjectValidationException;

/**
 * Provides a way to create <b>new</b> independent and validated {@link KrakenProject} instances.
 * <p/>
 * To access <b>existing</b> instances of {@link KrakenProject} that are already prebuilt and available in the system
 * then {@link kraken.model.project.repository.KrakenProjectRepository} should be used instead.
 *
 * @author mulevicius
 */
@API
public interface KrakenProjectFactory {

    /**
     * Constructs a new valid instance of {@link KrakenProject} from {@link kraken.model.resource.Resource}s available in the system.
     * <p/>
     * Repeated invocations of this method for the same namespace will result
     * in multiple different instances of {@link KrakenProject} that has exactly the same contents semantically,
     * but may or may not have matching {@link KrakenProject#getIdentifier()}
     *
     * @param namespace indicates a {@link KrakenProject} to construct.
     * @return new immutable instance of {@link KrakenProject} for the namespace
     * @throws IllegalKrakenProjectStateException when {@link KrakenProject} cannot be constructed
     * @throws KrakenProjectValidationException when {@link KrakenProject} fails validation
     */
    KrakenProject createKrakenProject(String namespace);
}
