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
package kraken.runtime.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.runtime.model.project.RuntimeKrakenProject;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.dynamic.DynamicRuleRepositoryProcessor;
import kraken.runtime.repository.filter.DimensionFilteringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link RuntimeRuleRepository} and {@link RuntimeContextRepository}
 * based on {@link RuntimeKrakenProject} concept.
 * <p/>
 * Use {@link kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory} to easily create instances as it handles
 * instantiation of {@link DynamicRuleRepositoryProcessor}.
 *
 * @author mulevicius
 */
public class RuntimeProjectRepository implements RuntimeRuleRepository, RuntimeContextRepository {

    private final static Logger logger = LoggerFactory.getLogger(RuntimeProjectRepository.class);

    private RuntimeKrakenProject krakenProject;

    private DimensionFilteringService dimensionFilteringService;

    private DynamicRuleRepositoryProcessor dynamicRuleRepositoryProcessor;

    public RuntimeProjectRepository(RuntimeKrakenProject krakenProject,
                                    DimensionFilteringService dimensionFilteringService,
                                    DynamicRuleRepositoryProcessor dynamicRuleRepositoryProcessor) {
        this.krakenProject = Objects.requireNonNull(krakenProject);
        this.dimensionFilteringService = Objects.requireNonNull(dimensionFilteringService);
        this.dynamicRuleRepositoryProcessor = Objects.requireNonNull(dynamicRuleRepositoryProcessor);
    }

    @Override
    public RuntimeContextDefinition getContextDefinition(String name) {
        return krakenProject.getContextDefinitions().get(name);
    }

    @Override
    public RuntimeContextDefinition getRootContextDefinition() {
        return krakenProject.getContextDefinitions().get(krakenProject.getRootContextName());
    }

    @Override
    public Set<String> getAllContextDefinitionNames() {
        return krakenProject.getContextDefinitions().keySet();
    }

    @Override
    public Map<String, RuntimeRule> resolveRules(String entryPointName, Map<String, Object> context) {
        Map<String, RuntimeRule> rules = new HashMap<>();
        collectRules(entryPointName, context, rules);
        return rules;
    }

    public RuntimeKrakenProject getKrakenProject() {
        return krakenProject;
    }

    private void collectRules(String entryPointName, Map<String, Object> context, Map<String, RuntimeRule> collectedRules) {
        List<RuntimeEntryPoint> entryPoints = krakenProject.getEntryPointVersions().get(entryPointName);

        // backwards compatibility for case when EntryPoint is not in DSL but is used in DynamicRuleRepository
        if(entryPoints == null) {
            collectDynamicRules(entryPointName, context, collectedRules);
            return;
        }

        dimensionFilteringService.filterEntryPoints(entryPoints, context)
                .ifPresent(ep -> {
                    collectRules(ep, context, collectedRules);
                    collectDynamicRules(ep.getName(), context, collectedRules);
                });
    }

    private void collectDynamicRules(String entryPointName, Map<String, Object> context, Map<String, RuntimeRule> collectedRules) {
        dynamicRuleRepositoryProcessor.resolveRules(entryPointName, context)
                .forEach(rule -> collectRuleOrLogWarningIfAlreadyExists(entryPointName, rule, collectedRules));
    }

    private void collectRules(RuntimeEntryPoint entryPoint, Map<String, Object> context, Map<String, RuntimeRule> collectedRules) {
        for(String ruleName : entryPoint.getRuleNames()) {
            List<RuntimeRule> rules = krakenProject.getRuleVersions().get(ruleName);
            dimensionFilteringService.filterRules(rules, context)
                    .ifPresent(rule -> collectRuleOrLogWarningIfAlreadyExists(entryPoint.getName(), rule, collectedRules));
        }
        for(String includedEntryPoint : entryPoint.getIncludedEntryPoints()) {
            collectRules(includedEntryPoint, context, collectedRules);
        }
    }

    private void collectRuleOrLogWarningIfAlreadyExists(String entryPointName, RuntimeRule rule, Map<String, RuntimeRule> collectedRules) {
        if(collectedRules.containsKey(rule.getName())) {
            logger.warn(
                    "More than one rule found with the same name '{}' while resolving EntryPoint '{}'. "
                            + " Additional rules with the same name will be ignored.",
                    rule.getName(), entryPointName
            );
        } else {
            collectedRules.put(rule.getName(), rule);
        }
    }

}
