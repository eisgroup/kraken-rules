/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime.repository.dynamic;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.converter.RuleConverter;
import kraken.dimensions.DimensionSet;
import kraken.model.Rule;
import kraken.model.dsl.read.DSLReader;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProjectFactoryHolder;
import kraken.model.project.exception.KrakenProjectValidationException;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.repository.filter.DimensionFilteringService;

/**
 * @author Tomas Dapkunas
 */
@RunWith(MockitoJUnitRunner.class)
public class DynamicRuleRepositoryProcessorServerSideOnlyTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private static final KrakenProject krakenProject = ResourceKrakenProjectFactoryHolder.getInstance()
        .createKrakenProjectFactory(new DSLReader().read("DynamicRuleRepositoryProcessorTest/"))
        .createKrakenProject("Policy");

    private DynamicRuleRepositoryProcessor dynamicRuleRepositoryProcessor;

    @Mock
    public DimensionFilteringService dimensionFilteringService;

    @Mock
    public RuleConverter ruleConverter;

    @Mock
    public DynamicRuleRepository dynamicRuleRepository;

    @Mock
    private KrakenProjectValidationService krakenProjectValidationService;

    @Before
    public void setUp() {
        dynamicRuleRepositoryProcessor = new DynamicRuleRepositoryProcessor(
            krakenProject,
            ruleConverter,
            List.of(dynamicRuleRepository),
            DynamicRuleRepositoryCacheConfig.noCaching(),
            dimensionFilteringService,
            krakenProjectValidationService
        );
    }

    @Test
    public void shouldThrowExceptionWhenServerSideDynamicRuleIsResolvedForNonServerSideEntryPoint() {
        Rule rule = factory.createRule();
        rule.setServerSideOnly(true);
        rule.setName("SSORule");

        when(dynamicRuleRepository.resolveDynamicRules(any(), any(), anyMap()))
            .thenReturn(Stream.of(DynamicRuleHolder.createNonDimensional(rule)));

        assertThrows(KrakenProjectValidationException.class,
            () -> dynamicRuleRepositoryProcessor.resolveRules("ValidationNonSSO", Map.of())
                .collect(Collectors.toList()));
    }

    @Test
    public void shouldThrowExceptionWhenServerSideDynamicRuleWithVariationIdIsResolvedForNonServerSideEntryPoint() {
        Rule rule = factory.createRule();
        rule.setServerSideOnly(true);
        rule.setName("SSORule");
        rule.setRuleVariationId("SSORule");

        when(dynamicRuleRepository.resolveDynamicRules(any(), any(), anyMap()))
            .thenReturn(Stream.of(DynamicRuleHolder.createNonDimensional(rule)));

        assertThrows(KrakenProjectValidationException.class,
            () -> dynamicRuleRepositoryProcessor.resolveRules("ValidationNonSSO", Map.of())
                .collect(Collectors.toList()));
    }

    @Test
    public void shouldThrowExceptionWhenServerSideDynamicRuleWithVariationIdIsResolvedForNotDefinedEntryPoint() {
        Rule rule = factory.createRule();
        rule.setServerSideOnly(true);
        rule.setName("SSORule");
        rule.setRuleVariationId("SSORule");

        when(dynamicRuleRepository.resolveDynamicRules(any(), any(), anyMap()))
            .thenReturn(Stream.of(DynamicRuleHolder.createNonDimensional(rule)));

        assertThrows(KrakenProjectValidationException.class,
            () -> dynamicRuleRepositoryProcessor.resolveRules("ValidationNonSSO", Map.of())
                .collect(Collectors.toList()));
    }

    @Test
    public void shouldThrowExceptionWhenServerSideDynamicRuleIsResolvedForNotDefinedEntryPoint() {
        Rule rule = factory.createRule();
        rule.setServerSideOnly(true);
        rule.setName("SSORule");
        rule.setRuleVariationId("SSORule");

        when(dynamicRuleRepository.resolveDynamicRules(any(), any(), anyMap()))
            .thenReturn(Stream.of(DynamicRuleHolder.createNonDimensional(rule)));

        assertThrows(KrakenProjectValidationException.class,
            () -> dynamicRuleRepositoryProcessor.resolveRules("NotDefined", Map.of())
                .collect(Collectors.toList()));
    }

    @Test
    public void shouldNotThrowExceptionWhenServerSideDynamicRuleIsResolvedForServerSideEntryPoint() {
        Rule rule = factory.createRule();
        rule.setServerSideOnly(true);
        rule.setName("SSORule");

        RuntimeRule runtimeRule = createRuntimeRule(rule);

            when(dynamicRuleRepository.resolveDynamicRules(any(), any(), anyMap()))
            .thenReturn(Stream.of(DynamicRuleHolder.createNonDimensional(rule)));
        when(ruleConverter.convertDynamicRule(any())).thenReturn(runtimeRule);

        List<RuntimeRule> rules = dynamicRuleRepositoryProcessor.resolveRules("ValidationSSO", Map.of())
            .collect(Collectors.toList());

        assertThat(rules, hasSize(1));
    }

    private RuntimeRule createRuntimeRule(Rule rule) {
        RuntimeRule runtimeRule = mock(RuntimeRule.class);

        DimensionSet dimensionSet = DimensionSet.createStatic();

        when(runtimeRule.getName()).thenReturn(rule.getName());
        when(runtimeRule.getDimensionSet()).thenReturn(dimensionSet);

        return runtimeRule;
    }

}
