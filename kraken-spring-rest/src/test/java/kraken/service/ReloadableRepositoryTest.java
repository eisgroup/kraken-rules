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

package kraken.service;

import kraken.model.EntryPointName;
import kraken.model.Rule;
import kraken.model.dsl.read.DSLReader;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.resource.Resource;
import kraken.model.state.AccessibilityPayload;
import kraken.runtime.repository.dynamic.DynamicRuleHolder;
import kraken.service.ReloadableRepository.Return;
import kraken.testproduct.TestProduct;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kraken.model.EntryPointName.*;
import static kraken.utils.TestUtils.createMockRules;
import static kraken.utils.TestUtils.toDSL;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ReloadableRepositoryTest {

    private static ResourceKrakenProject project;
    private ReloadableRepository repository;

    @BeforeClass
    public static void setUpClass() {
        DSLReader dslReader = new DSLReader();
        Collection<Resource> resources = dslReader.read(TestProduct.RULES_DIR);
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);
        project = (ResourceKrakenProject) krakenProjectBuilder.buildKrakenProject(TestProduct.NAMESPACE);
    }

    @Before
    public void setUp() {
        this.repository = new ReloadableRepository(new ReloadableStorage(TestProduct.NAMESPACE), project);
    }

    @Test
    public void shouldAddRules() {
        final Return aReturn = repository.addRules(QA1, toDSL(createMockRules("TestRule")));
        assertThat(aReturn.getFailure().isEmpty(), is(true));
        assertThat(aReturn.getSuccess().isEmpty(), is(false));
        assertThat(aReturn.getSuccess().get(), is("Rules are imported"));
        assertThat(repository.getRules(), hasSize(1));
        assertThat(repository.getEntryPoint(QA1).getRuleNames(), hasSize(1));
        assertThat(repository.getEntryPoint(QA1).getRuleNames().get(0), is("TestRule"));
    }

    @Test
    public void shouldContainCorrectEntryPoints() {
        for (EntryPointName entryPointName : List.of(QA1, QA2, QA3, QA4, QA5, UI)) {
            assertThat(repository.getEntryPoint(entryPointName).getName(), is(entryPointName.name()));
            assertThat(repository.getEntryPoint(entryPointName).getPhysicalNamespace(), is(TestProduct.NAMESPACE));
            assertThat(repository.getEntryPoint(entryPointName).getRuleNames(), hasSize(0));
        }
    }

    @Test
    public void shouldClearAllEntryPoints() {
        repository.addRules(QA1, toDSL(createMockRules("TestRule1")));
        repository.addRules(QA2, toDSL(createMockRules("TestRule2")));
        repository.addRules(QA3, toDSL(createMockRules("TestRule3")));
        final Return aReturn = repository.clear();

        assertThat(aReturn.getFailure().isEmpty(), is(true));
        assertThat(aReturn.getSuccess().isEmpty(), is(false));
        assertThat(aReturn.getSuccess().get(), is("All Rules are removed and EntryPoints contains no rules"));

        for (EntryPointName entryPointName : List.of(QA1, QA2, QA3, QA4, QA5, UI)) {
            assertThat(repository.getEntryPoint(entryPointName).getName(), is(entryPointName.name()));
            assertThat(repository.getEntryPoint(entryPointName).getPhysicalNamespace(), is(TestProduct.NAMESPACE));
            assertThat(repository.getEntryPoint(entryPointName).getRuleNames(), hasSize(0));
        }
        assertThat(repository.getRules(), hasSize(0));
    }

    @Test
    public void shouldClearOneEntryPoints() {
        repository.addRules(QA1, toDSL(createMockRules("TestRule1")));
        repository.addRules(QA2, toDSL(createMockRules("TestRule2")));
        repository.addRules(QA3, toDSL(createMockRules("TestRule3")));
        final Return aReturn = repository.clear(QA1);

        assertThat(aReturn.getFailure().isEmpty(), is(true));
        assertThat(aReturn.getSuccess().isEmpty(), is(false));
        assertThat(aReturn.getSuccess().get(), is("EntryPoint QA1 contains no rules"));

        assertThat(repository.getEntryPoint(QA1).getRuleNames(), hasSize(0));
        assertThat(repository.getRules(), hasSize(3));
    }

    @Test
    public void shouldRemoveSpecificRules() {
        repository.addRules(QA1, toDSL(createMockRules("TestRule1")));
        repository.addRules(QA2, toDSL(createMockRules("TestRule2")));
        final Return aReturn = repository.removeRules(QA1, List.of("TestRule1"));

        assertThat(aReturn.getFailure().isEmpty(), is(true));
        assertThat(aReturn.getSuccess().isEmpty(), is(false));
        assertThat(aReturn.getSuccess().get(),
            is("Rules: [TestRule1] are removed, from an EntryPoint and dynamic repository"));

        assertThat(repository.getEntryPoint(QA1).getRuleNames(), hasSize(0));
        assertThat(repository.getRules(), hasSize(1));
        assertThat(repository.getRules().iterator().next().getName(), is("TestRule2"));
    }

    @Test
    public void shouldResolveRules() {
        repository.addRules(QA1, toDSL(createMockRules("TestRule1")));
        List<DynamicRuleHolder> rules = repository.resolveDynamicRules(TestProduct.NAMESPACE, QA1.name(), Map.of())
                .collect(Collectors.toList());
        assertThat(rules, hasSize(1));
        assertThat(rules.get(0).getRule().getName(), is("TestRule1"));
    }

    @Test
    public void shouldValidateRules() {
        final RulesModelFactory factory = RulesModelFactory.getInstance();
        final Rule rule = factory
            .createRule();
        rule.setName("TestRule");
        rule.setTargetPath("NA");
        rule.setContext("NA");
        final AccessibilityPayload payload = factory.createAccessibilityPayload();
        payload.setAccessible(true);
        rule.setPayload(payload);

        final Return aReturn = repository.addRules(QA1, toDSL(List.of(rule)));
        assertThat(aReturn.getFailure().isEmpty(), is(false));
        assertThat(aReturn.getSuccess().isEmpty(), is(true));
        assertThat(aReturn.getFailure().get(),
            containsString("[kvr027] Missing context definition with name 'NA'."));
    }

}
