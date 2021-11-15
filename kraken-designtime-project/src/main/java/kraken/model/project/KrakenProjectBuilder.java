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

import kraken.model.project.exception.IllegalKrakenProjectStateException;

/**
 * @author mulevicius
 */
public interface KrakenProjectBuilder {

    /**
     * Builds a new instance of {@link KrakenProject}.
     * Implementations of this interface only guarantees that {@link KrakenProject} can be built,
     * but does not guarantee {@link KrakenProject} validity.
     *
     * @param namespace which indicates what project to build
     * @return new and unvalidated kraken project instance;
     *         each invocation of this method must build a completely new instance of {@link KrakenProject}
     * @throws IllegalKrakenProjectStateException if {@link KrakenProject} cannot be built
     * @see KrakenProject
     * @see KrakenProjectFactory
     * @see kraken.model.project.repository.KrakenProjectRepository
     */
    KrakenProject buildKrakenProject(String namespace);
}
