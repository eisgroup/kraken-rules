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
package kraken.runtime.repository.dynamic;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.converter.RuleConverter;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.exception.KrakenProjectValidationException;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.project.validator.ValidationResult;
import kraken.namespace.Namespaced;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.dynamic.trace.QueryingDynamicRulesOperation;
import kraken.runtime.repository.filter.DimensionFilteringService;
import kraken.tracer.Tracer;

/**
 * Iterates over each {@link DynamicRuleRepository} and resolves rules for entryPoint.
 * Each resolved instance of {@link kraken.model.Rule} is then translated to runtime {@link RuntimeRule}.
 *
 * @author mulevicius
 */
public class DynamicRuleRepositoryProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DynamicRuleRepositoryProcessor.class);

    private final KrakenProject krakenProject;

    private final Collection<DynamicRuleRepository> dynamicRuleRepositories;

    private final RuleConverter ruleConverter;

    private final Cache<String, RuntimeRule> cache;

    private final DimensionFilteringService dimensionFilteringService;

    private final KrakenProjectValidationService krakenProjectValidationService;

    public DynamicRuleRepositoryProcessor(KrakenProject krakenProject,
                                          RuleConverter ruleConverter,
                                          Collection<DynamicRuleRepository> dynamicRuleRepositories,
                                          DynamicRuleRepositoryCacheConfig cacheConfig,
                                          DimensionFilteringService dimensionFilteringService,
                                          KrakenProjectValidationService krakenProjectValidationService) {
        this.krakenProject = krakenProject;
        this.ruleConverter = ruleConverter;
        this.dynamicRuleRepositories = dynamicRuleRepositories;
        this.dimensionFilteringService = dimensionFilteringService;

        this.cache = new Cache2kBuilder<String, RuntimeRule>() {}
                .entryCapacity(cacheConfig.getCacheMaxSize())
                .expireAfterWrite(cacheConfig.getExpireAfterWriteInSeconds(), TimeUnit.SECONDS)
                .build();

        this.krakenProjectValidationService = krakenProjectValidationService;
    }

    public Stream<RuntimeRule> resolveRules(String entryPoint, Map<String, Object> context) {
        return dynamicRuleRepositories.stream()
                .flatMap(repository -> resolveRules(repository, entryPoint, context));
    }

    private Stream<RuntimeRule> resolveRules(DynamicRuleRepository dynamicRuleRepository,
                                             String entryPoint,
                                             Map<String, Object> context) {
        return Tracer.doOperation(
                new QueryingDynamicRulesOperation(dynamicRuleRepository.getClass().getSimpleName()),
                () -> dynamicRuleRepository.resolveDynamicRules(krakenProject.getNamespace(), entryPoint, context)
                    .map(this::convertOrReadFromCache)
                    .collect(Collectors.groupingBy(RuntimeRule::getName))
            )
            .values()
            .stream()
            .map(rules -> getRule(rules, context))
            .filter(Objects::nonNull);
    }

    private RuntimeRule getRule(List<RuntimeRule> rules, Map<String, Object> context) {
        if (rules.size() == 1 && !rules.iterator().next().getDimensionSet().isDimensional()) {
            return rules.iterator().next();
        }

        return rules.isEmpty()
            ? null
            : dimensionFilteringService.filterRules(krakenProject.getNamespace(), rules, context).orElse(null);
    }

    private RuntimeRule convertOrReadFromCache(DynamicRuleHolder dynamicRuleHolder) {
        var rule = dynamicRuleHolder.getRule();
        if(rule.getRuleVariationId() == null) {
            logger.warn("Dynamic Rule '{}' does not have ruleVariationId defined. " +
                    "Validation and caching will be skipped for this rule.", rule.getName());
            return convert(dynamicRuleHolder);
        }

        String key = rule.getName() + "_" + rule.getRuleVariationId();
        return cache.computeIfAbsent(key, () -> validateAndConvert(dynamicRuleHolder));
    }

    private RuntimeRule validateAndConvert(DynamicRuleHolder dynamicRuleHolder) {
        validate(dynamicRuleHolder.getRule());
        return convert(dynamicRuleHolder);
    }

    private RuntimeRule convert(DynamicRuleHolder dynamicRuleHolder) {
        return ruleConverter.convertDynamicRule(dynamicRuleHolder);
    }

    private void validate(Rule rule) {
        ValidationResult validationResult = krakenProjectValidationService.validateDynamicRule(rule, krakenProject);
        validationResult.logMessages(logger);
        if(!validationResult.getErrors().isEmpty()) {
            String namespaceName = krakenProject.getNamespace().equals(Namespaced.GLOBAL) ? "GLOBAL" : krakenProject.getNamespace();
            String pattern = "Dynamic Rule ''{0}'' is not valid for KrakenProject in namespace ''{1}''. Validation errors:\n{2}";
            String validationErrors = validationResult.getErrors().stream()
                    .map(error -> error.toString())
                    .collect(Collectors.joining(System.lineSeparator()));
            String message = MessageFormat.format(pattern, rule.getName(), namespaceName, validationErrors);
            throw new KrakenProjectValidationException(message);
        }
    }
}
