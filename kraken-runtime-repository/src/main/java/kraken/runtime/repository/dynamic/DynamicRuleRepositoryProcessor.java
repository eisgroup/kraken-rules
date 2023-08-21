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

import static kraken.message.SystemMessageBuilder.Message.DYNAMIC_RULE_MISSING_VARIATION_ID;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.DYNAMIC_RULE_SERVER_SIDE_ONLY_IN_REGULAR_ENTRYPOINT;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;

import kraken.converter.RuleConverter;
import kraken.message.SystemMessage;
import kraken.message.SystemMessageBuilder;
import kraken.message.SystemMessageBuilder.Message;
import kraken.message.SystemMessageLogger;
import kraken.model.Rule;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.exception.KrakenProjectValidationException;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.project.validator.ValidationMessageBuilder;
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

    private static final SystemMessageLogger logger = SystemMessageLogger.getLogger(DynamicRuleRepositoryProcessor.class);

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
                    .map(dynamicRuleHolder -> convertOrReadFromCache(dynamicRuleHolder, entryPoint))
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

    private RuntimeRule convertOrReadFromCache(DynamicRuleHolder dynamicRuleHolder, String entryPointName) {
        var rule = dynamicRuleHolder.getRule();
        validateServerSideOnly(rule, entryPointName);

        if(rule.getRuleVariationId() == null) {
            logger.warn(DYNAMIC_RULE_MISSING_VARIATION_ID, rule.getName());
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
        validationResult.logAllFormattedMessages(logger.getSl4jLogger());
        if(validationResult.hasErrorMessages()) {
            throw new KrakenProjectValidationException(createMessage(rule, validationResult.formatErrorMessages()));
        }
    }

    private void validateServerSideOnly(Rule rule, String entryPointName) {
        if (rule.isServerSideOnly() && !isEveryEntryPointVersionServerSideOnly(entryPointName)) {
            var message = ValidationMessageBuilder.create(DYNAMIC_RULE_SERVER_SIDE_ONLY_IN_REGULAR_ENTRYPOINT, rule)
                .parameters(entryPointName)
                .build();
            var validationResult = new ValidationResult(List.of(message));
            throw new KrakenProjectValidationException(createMessage(rule, validationResult.formatErrorMessages()));
        }
    }

    private boolean isEveryEntryPointVersionServerSideOnly(String entryPointName) {
        List<EntryPoint> definedEntryPointVersions = krakenProject.getEntryPointVersions().get(entryPointName);

        return definedEntryPointVersions != null
            && definedEntryPointVersions.stream().allMatch(EntryPoint::isServerSideOnly);
    }

    private SystemMessage createMessage(Rule rule, String formattedValidationMessage) {
        String namespaceName = krakenProject.getNamespace().equals(Namespaced.GLOBAL)
            ? "GLOBAL"
            : krakenProject.getNamespace();
        return SystemMessageBuilder.create(Message.KRAKEN_PROJECT_DYNAMIC_RULE_NOT_VALID)
            .parameters(rule.getName(), namespaceName, System.lineSeparator() + formattedValidationMessage)
            .build();
    }

}
