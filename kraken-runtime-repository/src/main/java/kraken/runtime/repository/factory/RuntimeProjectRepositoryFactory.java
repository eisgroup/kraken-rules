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
package kraken.runtime.repository.factory;

import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kraken.converter.KrakenProjectConverter;
import kraken.converter.RuleConverter;
import kraken.converter.dimensional.DimensionalRuleChecker;
import kraken.converter.translation.KrakenExpressionTranslator;
import kraken.el.TargetEnvironment;
import kraken.model.project.KrakenProject;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.repository.KrakenProjectRepository;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.runtime.repository.KrakenRepositoryException;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.runtime.repository.RuntimeProjectRepository;
import kraken.runtime.repository.RuntimeProjectRepositoryConfig;
import kraken.runtime.repository.RuntimeRuleRepository;
import kraken.runtime.repository.dynamic.DynamicRuleRepositoryProcessor;
import kraken.runtime.repository.filter.DimensionFilteringService;

/**
 * Instantiates {@link RuntimeRuleRepository} for a {@link TargetEnvironment}
 *
 * @author mulevicius
 */
public class RuntimeProjectRepositoryFactory implements RuntimeRepositoryFactory {

    private DimensionFilteringService dimensionFilteringService;

    private KrakenProjectRepository krakenProjectRepository;

    private Map<String, RuntimeProjectRepository> repositories = new ConcurrentHashMap<>();

    private TargetEnvironment targetEnvironment;

    private RuntimeProjectRepositoryConfig config;

    private KrakenProjectValidationService krakenProjectValidationService;

    public RuntimeProjectRepositoryFactory(KrakenProjectRepository krakenProjectRepository,
                                           RuntimeProjectRepositoryConfig config,
                                           TargetEnvironment targetEnvironment) {
        this.krakenProjectRepository = krakenProjectRepository;
        this.config = config;
        this.dimensionFilteringService = new DimensionFilteringService(config.getDimensionFilters());
        this.targetEnvironment = targetEnvironment;
        this.krakenProjectValidationService = new KrakenProjectValidationService();
    }

    @Override
    public RuntimeRuleRepository resolveRuleRepository(String namespace) {
        return repositories.compute(namespace,
                (ns, cachedRepository) -> returnOrCreateRuntimeProjectRepository(ns, cachedRepository)
        );
    }

    @Override
    public RuntimeContextRepository resolveContextRepository(String namespace) {
        return repositories.compute(namespace,
                (ns, cachedRepository) -> returnOrCreateRuntimeProjectRepository(ns, cachedRepository)
        );
    }

    private RuntimeProjectRepository returnOrCreateRuntimeProjectRepository(String namespace,
                                                                            RuntimeProjectRepository cachedRepository) {
        KrakenProject newKrakenProject = krakenProjectRepository.getKrakenProject(namespace);
        throwIfKrakenProjectIsMissing(namespace, newKrakenProject);
        if(cachedRepository != null
                && newKrakenProject.getIdentifier().equals(cachedRepository.getKrakenProject().getChecksum())) {
            return cachedRepository;
        }
        return createRuntimeProjectRepository(newKrakenProject);
    }

    private RuntimeProjectRepository createRuntimeProjectRepository(KrakenProject krakenProject) {
        KrakenProjectConverter krakenProjectConverter = new KrakenProjectConverter(krakenProject, targetEnvironment);

        return new RuntimeProjectRepository(
                krakenProjectConverter.convert(),
                dimensionFilteringService,
                createDynamicRuleRepositoryProcessor(krakenProject.getNamespace())
        );
    }

    private DynamicRuleRepositoryProcessor createDynamicRuleRepositoryProcessor(String namespace) {
        KrakenProject krakenProject = krakenProjectRepository.getKrakenProject(namespace);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        KrakenExpressionTranslator krakenExpressionTranslator = new KrakenExpressionTranslator(
                krakenProject,
                targetEnvironment,
                ruleDependencyExtractor
        );
        DimensionalRuleChecker dimensionalRuleChecker = new DimensionalRuleChecker(krakenProject);
        RuleConverter ruleConverter = new RuleConverter(ruleDependencyExtractor, krakenExpressionTranslator, dimensionalRuleChecker);
        return new DynamicRuleRepositoryProcessor(
                krakenProject,
                ruleConverter,
                config.getDynamicRuleRepositories(),
                config.getDynamicRuleRepositoryCacheConfig(),
                dimensionFilteringService,
                krakenProjectValidationService
        );
    }

    private void throwIfKrakenProjectIsMissing(String namespace, KrakenProject krakenProject) {
        if(krakenProject == null) {
            String format = "Unknown namespace: ''{0}''. This indicates a Kraken Rule deployment gone wrong " +
                    "or Kraken Engine is executed with namespace that does not exist";
            String message = MessageFormat.format(format, namespace);
            throw new KrakenRepositoryException(message);
        }
    }
}
