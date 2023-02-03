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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.annotations.API;
import kraken.model.project.KrakenProject;

/**
 * @author mulevicius
 */
@API
public class StaticKrakenProjectRepository implements KrakenProjectRepository {

    private final Map<String, KrakenProject> krakenProjects;

    public StaticKrakenProjectRepository(Collection<KrakenProject> krakenProjects) {
        this.krakenProjects = krakenProjects.stream().collect(Collectors.toMap(KrakenProject::getNamespace, p -> p));
    }

    @Override
    public KrakenProject getKrakenProject(String namespace) {
        return krakenProjects.get(namespace);
    }

}
