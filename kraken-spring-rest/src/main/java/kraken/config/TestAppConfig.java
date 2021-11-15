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
package kraken.config;

import java.util.Collection;
import java.util.List;

import kraken.el.TargetEnvironment;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.project.repository.KrakenProjectRepository;
import kraken.model.project.repository.StaticKrakenProjectRepository;
import kraken.model.resource.Resource;
import kraken.runtime.RuleEngine;
import kraken.runtime.RuleEngineBuilder;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfoResolver;
import kraken.runtime.engine.dto.bundle.EntryPointBundleFactory;
import kraken.runtime.repository.RuntimeProjectRepositoryConfig;
import kraken.runtime.repository.dynamic.DynamicRuleRepository;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.runtime.repository.filter.DimensionFilter;
import kraken.service.ReloadableRepository;
import kraken.service.ReloadableStorage;
import kraken.testproduct.dimension.filter.StateDimensionFilter;
import kraken.testproduct.domain.meta.Identifiable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import static kraken.runtime.repository.dynamic.DynamicRuleRepositoryCacheConfig.noCaching;

/**
 * @author psurinin
 * @since 1.1.0
 */
@PropertySource("classpath:config/kraken.properties")
@Configuration
public class TestAppConfig {

    public static final String NAMESPACE = "Policy";

    // build information
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public RuleEngine engine(
            KrakenProjectRepository krakenProjectRepository,
            Collection<DimensionFilter> dimensionFilters,
            Collection<DynamicRuleRepository> dynamicRuleRepositories
    ) {
        final DataNavigationContextInstanceInfoResolver resolver =
                new DataNavigationContextInstanceInfoResolver();
        resolver.setInfoResolver(new InfoResolver());

        final RuleEngineBuilder builder = RuleEngineBuilder.newInstance()
                .setKrakenProjectRepository(krakenProjectRepository)
                .setContextInstanceResolver(resolver);
        dimensionFilters
                .forEach(builder::addDimensionFilter);
        dynamicRuleRepositories
                .forEach(builder::addDynamicRuleRepository);

        return builder.buildEngine();
    }

    @Bean
    public ReloadableRepository reloadableRepository(KrakenProjectRepository krakenProjectRepository) {
        var krakenProject = (ResourceKrakenProject) krakenProjectRepository.getKrakenProject(NAMESPACE);
        return new ReloadableRepository(new ReloadableStorage(NAMESPACE), krakenProject);
    }

    @Bean
    public EntryPointBundleFactory bundleFactory(
            KrakenProjectRepository krakenProjectRepository,
            Collection<DimensionFilter> dimensionFilters,
            Collection<DynamicRuleRepository> dynamicRuleRepositories
    ) {
        return new EntryPointBundleFactory(
                new RuntimeProjectRepositoryFactory(
                        krakenProjectRepository,
                        new RuntimeProjectRepositoryConfig(noCaching(), dimensionFilters, dynamicRuleRepositories),
                        TargetEnvironment.JAVASCRIPT
                )
        );
    }

    @Bean
    public KrakenProjectRepository krakenProjectRepository() {
        DSLReader dslReader = new DSLReader();
        Collection<Resource> resources = dslReader.read("database/gap/");
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);
        KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject(NAMESPACE);
        return new StaticKrakenProjectRepository(List.of(krakenProject));
    }

    @Bean
    public DimensionFilter stateDimensionFilter() {
        return new StateDimensionFilter();
    }

    public static final class InfoResolver extends SimpleDataObjectInfoResolver {

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
