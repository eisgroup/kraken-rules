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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kraken.model.Rule;
import kraken.model.dsl.read.DSLReader;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectFactory;
import kraken.model.project.ResourceKrakenProjectFactoryHolder;
import kraken.model.project.repository.KrakenProjectRepository;
import kraken.model.project.repository.StaticKrakenProjectRepository;
import kraken.model.resource.Resource;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.model.MetadataContainer;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.runtime.repository.dynamic.DynamicRuleRepository;
import kraken.runtime.repository.factory.RuntimeProjectRepositoryFactory;
import kraken.runtime.repository.filter.DimensionFilter;
import org.junit.Before;
import org.junit.Test;

import static kraken.el.TargetEnvironment.JAVA;
import static kraken.runtime.repository.dynamic.DynamicRuleRepositoryCacheConfig.noCaching;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.collection.IsMapContaining.hasKey;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author mulevicius
 */
public class RuntimeProjectRepositoryTest {

    private RuntimeContextRepository policyContextRepository;
    private RuntimeRuleRepository policyRuleRepository;

    private RuntimeContextRepository crmContextRepository;
    private RuntimeRuleRepository crmRuleRepository;

    @Before
    public void setUp() throws Exception {
        DSLReader reader = new DSLReader();
        Collection<Resource> resources = reader.read("RuntimeProjectRepositoryTest/");
        KrakenProjectFactory krakenProjectFactory = ResourceKrakenProjectFactoryHolder.getInstance()
                .createKrakenProjectFactory(resources);
        KrakenProject policy = krakenProjectFactory.createKrakenProject("Policy");
        KrakenProject crm = krakenProjectFactory.createKrakenProject("Crm");
        KrakenProjectRepository krakenProjectRepository = new StaticKrakenProjectRepository(List.of(policy, crm));

        RuntimeProjectRepositoryConfig config = new RuntimeProjectRepositoryConfig(
                noCaching(),
                List.of(new PackageDimensionFilter()),
                List.of(new PackageDynamicRuleRepository())
        );
        RuntimeProjectRepositoryFactory factory = new RuntimeProjectRepositoryFactory(krakenProjectRepository, config, JAVA);

        this.policyContextRepository = factory.resolveContextRepository("Policy");
        this.policyRuleRepository = factory.resolveRuleRepository("Policy");

        this.crmContextRepository = factory.resolveContextRepository("Crm");
        this.crmRuleRepository = factory.resolveRuleRepository("Crm");
    }

    @Test
    public void shouldLoadContextDefinitions() {
        assertThat(policyContextRepository.getContextDefinition("Policy"), notNullValue());
        assertThat(policyContextRepository.getContextDefinition("RiskItem"), notNullValue());
        assertThat(policyContextRepository.getContextDefinition("Coverage"), notNullValue());

        assertThat(crmContextRepository.getContextDefinition("Customer"), notNullValue());
    }

    @Test
    public void shouldLoadRulesByNamespace() {
        Map<String, RuntimeRule> policyRules = policyRuleRepository.resolveRules("Validation", Map.of());
        assertThat(policyRules, hasKey("R01-Policy"));

        Map<String, RuntimeRule> crmRules = crmRuleRepository.resolveRules("Validation", Map.of());
        assertThat(crmRules, hasKey("R01-CRM"));
    }

    @Test
    public void shouldFilterEntryPointsByDimensions() {
        Map<String, Object> context = Map.of("Package", "Empty");
        Map<String, RuntimeRule> policyRules = policyRuleRepository.resolveRules("Validation", context);

        assertThat(policyRules.values(), empty());
    }

    @Test
    public void shouldFilterRulesByDimension() {
        Map<String, Object> context = Map.of("Package", "Simple");
        Map<String, RuntimeRule> policyRules = policyRuleRepository.resolveRules("Validation", context);

        assertThat(policyRules, hasKey("R02-Policy-PackageSpecific"));
        assertThat(policyRules, hasKey("R03-Policy-Simple"));

        RuntimeRule r03PolicySimple = policyRules.get("R02-Policy-PackageSpecific");
        assertThat(r03PolicySimple.getMetadata().getProperties(), hasEntry("Package", "Simple"));
    }

    @Test
    public void shouldLoadDynamicRule() {
        Map<String, Object> context = Map.of(
                "Package", "Simple",
                "Dynamic", true
        );

        Map<String, RuntimeRule> policyRules = policyRuleRepository.resolveRules("Validation", context);

        assertThat(policyRules, hasKey("DynamicRule"));

        RuntimeRule dynamicRule = policyRules.get("DynamicRule");
        assertThat(
                ((AssertionPayload)dynamicRule.getPayload()).getAssertionExpression().getExpressionString(),
                equalTo("(Policy.packageCd == 'dynamic')")
        );
    }

    static class PackageDimensionFilter implements DimensionFilter {
        @Override
        public <T extends MetadataContainer> Collection<T> filter(Collection<T> items, Map<String, Object> context) {
            List<T> baseItems = items.stream()
                    .filter(i -> !i.getMetadata().getProperties().containsKey("Package"))
                    .collect(Collectors.toList());

            List<T> versionedItems = items.stream()
                    .filter(i -> i.getMetadata().getProperties().containsKey("Package"))
                    .collect(Collectors.toList());

            List<T> filteredVersionedItems = versionedItems.stream()
                    .filter(i -> Objects.equals(i.getMetadata().getProperties().get("Package"), context.get("Package")))
                    .collect(Collectors.toList());

            return !filteredVersionedItems.isEmpty()
                    ? filteredVersionedItems
                    : baseItems;
        }
    }

    static class PackageDynamicRuleRepository implements DynamicRuleRepository {

        private final RulesModelFactory factory = RulesModelFactory.getInstance();

        @Override
        public Stream<Rule> resolveRules(String namespace, String entryPoint, Map<String, Object> context) {
            if(entryPoint.equals("PackageValidation") && context.containsKey("Dynamic")) {
                kraken.model.Rule rule = factory.createRule();
                rule.setRuleVariationId(UUID.randomUUID().toString());
                rule.setName("DynamicRule");
                rule.setContext("Policy");
                rule.setTargetPath("policyCd");

                kraken.model.validation.AssertionPayload payload = factory.createAssertionPayload();
                kraken.model.Expression expression = factory.createExpression();
                expression.setExpressionString("Policy.packageCd == 'dynamic'");
                payload.setAssertionExpression(expression);
                kraken.model.ErrorMessage errorMessage = factory.createErrorMessage();
                errorMessage.setErrorCode("DynamicRule-error");
                errorMessage.setErrorMessage("DynamicRule-message");
                payload.setErrorMessage(errorMessage);
                payload.setSeverity(ValidationSeverity.critical);

                rule.setPayload(payload);

                return Stream.of(rule);
            }
            return Stream.empty();
        }
    }
}
