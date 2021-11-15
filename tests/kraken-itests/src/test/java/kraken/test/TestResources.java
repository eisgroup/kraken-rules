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

package kraken.test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.impl.ContextRepository;
import kraken.context.model.tree.repository.ContextModelTreeRepository;
import kraken.context.model.tree.repository.RecoveringStaticContextModelTreeRepository;
import kraken.converter.KrakenProjectConverter;
import kraken.el.TargetEnvironment;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProject;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.resource.Resource;
import kraken.runtime.model.project.RuntimeKrakenProject;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.runtime.repository.RuntimeProjectRepository;
import kraken.runtime.repository.dynamic.DynamicRuleRepositoryProcessor;
import kraken.runtime.repository.filter.DimensionFilteringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static kraken.runtime.repository.dynamic.DynamicRuleRepositoryCacheConfig.noCaching;

/**
 * Resources must be created with {@link TestResources#create(Info)}.
 * Then they will be cached by sources in parameter.
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class TestResources {

    private static final Map<Info, TestResources> cache = new ConcurrentHashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResources.class);

    public static class Info {
        public static final Info TEST_PRODUCT = new Info("Policy", "database/gap/");
        public static final Info TEST_PRODUCT_EXTENDED = new Info("PolicyExtended", "database/gap/");
        public static final Info TYPE_ADAPTER = new Info("TypeAdapter", "adapter/");
        public static final Info NAMESPACE = new Info("Bar", "namespace/");
        public static final Info POLICY_GENESIS = new Info("PersonalAuto", "policy_test/");
        public static final Info CRM_GENESIS = new Info("Base", "crm_test/");
        public static final Info NOT_ROOT_INH = new Info("", "not_root_inheritance/");
        public static final Info NAMESPACE_FOO = new Info("Foo", "namespace/");

        public final String namespace;
        public final String dir;

        public Info(String namespace, String dir) {
            this.namespace = namespace;
            this.dir = dir;
        }
    }

    private static final KrakenProjectValidationService krakenProjectValidationService =
        new KrakenProjectValidationService();

    public static TestResources create(Info info) {
        return cache.computeIfAbsent(info, TestResources::compute);
    }

    private static TestResources compute(Info info) {
        DSLReader dslReader = new DSLReader();
        Collection<Resource> resources = dslReader.read(info.dir);
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);
        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject(info.namespace);
        krakenProjectValidationService.validate(krakenProject).logMessages(LOGGER);
        return new TestResources(krakenProject);
    }

    private final KrakenProject krakenProject;
    private final ContextModelTreeRepository contextModelTreeRepository;

    private TestResources(KrakenProject krakenProject) {
        this.contextModelTreeRepository = new RecoveringStaticContextModelTreeRepository(
                (namespace, targetEnvironment) -> ContextRepository.from(getRuntimeKrakenProject())
        );
        this.krakenProject = krakenProject;
    }

    public KrakenProject getKrakenProject() {
        return krakenProject;
    }

    public RuntimeKrakenProject getRuntimeKrakenProject() {
        return new KrakenProjectConverter(krakenProject, TargetEnvironment.JAVA).convert();
    }

    public ContextModelTree getModelTree() {
        return contextModelTreeRepository.get(krakenProject.getNamespace(), TargetEnvironment.JAVA);
    }

    public RuntimeContextRepository getRuntimeContextRepository() {
        return new RuntimeProjectRepository(
                getRuntimeKrakenProject(),
                new DimensionFilteringService(List.of()),
                new DynamicRuleRepositoryProcessor(
                    krakenProject,
                    null,
                    List.of(),
                    noCaching(),
                    new DimensionFilteringService(List.of()),
                    krakenProjectValidationService
                )
        );
    }
}
