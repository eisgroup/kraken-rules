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

package kraken.benchmarks.engine;

import java.util.Collection;
import java.util.List;

import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProject;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.project.repository.KrakenProjectRepository;
import kraken.model.project.repository.StaticKrakenProjectRepository;
import kraken.model.resource.Resource;
import kraken.runtime.RuleEngine;
import kraken.runtime.RuleEngineBuilder;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfoResolver;
import kraken.testproduct.domain.meta.Identifiable;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class Engines {

    public static RuleEngine fromResourceDirectory(String namespace, String resourceDir) {
        DataNavigationContextInstanceInfoResolver resolver = new DataNavigationContextInstanceInfoResolver();
        resolver.setInfoResolver(new MockInfoResolver());

        DSLReader dslReader = new DSLReader();
        Collection<Resource> resources = dslReader.read(resourceDir);
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);
        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject(namespace);
        KrakenProjectRepository krakenProjectRepository = new StaticKrakenProjectRepository(List.of(krakenProject));

        return RuleEngineBuilder.newInstance()
                .setContextInstanceResolver(resolver)
                .setKrakenProjectRepository(krakenProjectRepository)
                .buildEngine();
    }

    private static final class MockInfoResolver extends SimpleDataObjectInfoResolver {

        @Override
        public String resolveContextNameForObject(Object data) {
            return data.getClass().getSimpleName();
        }

        @Override
        public String resolveContextIdForObject(Object data) {
            return ((Identifiable) data).getId();
        }
    }

}
