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
package kraken.facade;

import kraken.model.Request;
import kraken.model.Rule;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static kraken.utils.TestUtils.createMockRules;
import static kraken.utils.TestUtils.toDSL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DynamicRulesFacadeTest extends AbstractFacadeTest {
    @Before
    public void setUp() {
        rest.delete("/dynamic/rules/clear");
    }

    @Test
    public void shouldContainStoredRules() {
        ResponseEntity addRuleResponse = rest.postForEntity(
                "/dynamic/rule/dsl/QA1",
                toDSL(createMockRules("R00A", "R002")),
                String.class
        );

        assertThat(addRuleResponse.getBody(), is("Rules are imported"));

        ResponseEntity<List> getRulesResponse = rest.getForEntity("/dynamic/rule/QA1",List.class);
        List<Map<String, Object>> list = getRulesResponse.getBody();
        List<String> ruleNames = list.stream().map(item -> item.get("name").toString()).collect(Collectors.toList());

        assertThat(list, is(notNullValue()));
        assertThat(list, hasSize(2));
        assertThat(ruleNames, containsInAnyOrder("R00A", "R002"));

        rest.postForEntity("/dynamic/entrypoint/QA1/reset", null, String.class);
        removeRules("R00A", "R002");
    }

    @Test
    public void shouldAddDSLRulesToEntryPoint(){
        String dslRules =
                "EntryPoints{" +
                        "EntryPoint 'ep1'{ 'rule1', 'rule2'} " +
                        "EntryPoint 'ep2' {'rule3'}" +
                "}"       +
                "Rules {" +
                    "Rule 'rule1' On Vehicle.model { Reset To 'model'}" +
                    "Rule 'rule2' On Vehicle.model { Set Disabled}" +
                    "Rule 'rule3' On Vehicle.model { Set Hidden}" +
                    "Rule 'rule4' On Vehicle.model { Set Mandatory }" +
                "}";
        ResponseEntity<String> responseEntity = rest.postForEntity("/dynamic/rule/dsl/QA1", dslRules, String.class);
        assertThat(responseEntity.getStatusCodeValue(), is(200));
        assertThat(responseEntity.getBody(), is("Rules are imported"));

        List ruleList = rest.getForEntity("/dynamic/rule/QA1", List.class).getBody();
        assertThat(ruleList.size(), is(4));
        removeRules("rule1", "rule2", "rule3", "rule4");
    }

    @Test
    public void shouldReturnEmptyList() {
        List<Rule> list = rest.getForEntity("/dynamic/rule/QA2", List.class).getBody();
        assertThat(list, is(empty()));
    }

    @Test
    public void shouldAddAndRemoveGivenRule(){
        //add rules to entryPoint
        ResponseEntity addRuleResponse = rest.postForEntity(
                "/dynamic/rule/dsl/QA2", toDSL(createMockRules("Rule9999", "Rule8888")), String.class
        );
        assertThat(addRuleResponse.getBody(), is("Rules are imported"));

        //remove rule from entryPoint
        rest.delete("/dynamic/rule/Rule9999/entrypoint/QA2", nullValue());
        ResponseEntity<List> responseWithOneRuleOnQA2 = rest.getForEntity("/dynamic/rule/QA2",List.class);
        List<Map<String, Object>> mapWithRule8888 = responseWithOneRuleOnQA2.getBody();
        List<String> ruleNames = mapWithRule8888.stream()
                .map(item -> item.get("name").toString())
                .collect(Collectors.toList());
        assertThat(mapWithRule8888, hasSize(1));
        assertThat(ruleNames, contains("Rule8888"));

        ResponseEntity<List> responseWithOneRuleInTotal = rest.getForEntity("/dynamic/rules/all",List.class);
        List<Map<String, Object>> mapWithRule9999 = responseWithOneRuleInTotal.getBody();
        List<String> ruleNames2 = mapWithRule9999.stream()
                .map(item -> item.get("name").toString())
                .collect(Collectors.toList());
        assertThat(ruleNames2, hasSize(1));

        removeRules("Rule9999");
    }

    @Test
    public void shouldReturnErrorOnInvalidEntryPointName(){
        final ResponseEntity<Object> response = rest.postForEntity(
                "/dynamic/rule/dsl/NotExistingEntryPoint",
                toDSL(createMockRules("R001")),
                Object.class
        );
        assertThat(response.getStatusCode().value(), is(500));
    }

    @Test
    public void shouldResetEntryPoint(){
        rest.postForEntity(
                "/dynamic/rule/dsl/QA1",
                toDSL(createMockRules("B112", "B113", "B114", "B115")),
                String.class
        );

        List<Map<String, Object>> ruleListBeforeReset = rest.getForEntity(
                "/dynamic/rule/QA1",
                List.class
        ).getBody();
        assertThat(ruleListBeforeReset, hasSize(4));

        rest.postForEntity("/dynamic/entrypoint/QA1/reset", new Request(), String.class);

        List<Map<String, Object>> ruleListAfterReset = rest.getForEntity("/dynamic/rule/QA1", List.class).getBody();
        assertThat(ruleListAfterReset, hasSize(0));
        removeRules("B112", "B113", "B114", "B115");
    }

    @Test
    public void shouldAddRulesAndRetrieveThem(){
        String dslRulesBase =
                "Namespace NS1 " +
                        "Rule 'base_rule1' On Vehicle.model { Reset To 'model'}" +
                        "Rule 'base_rule2' On Vehicle.model { Set Disabled}";
        ResponseEntity<String> responseEntityBaseRules = rest.postForEntity("/dynamic/rule/dsl/QA1", dslRulesBase, String.class);
        assertThat(responseEntityBaseRules.getStatusCodeValue(), is(200));
        assertThat(responseEntityBaseRules.getBody(), is("Rules are imported"));

        ResponseEntity<List> getRulesResponse = rest.getForEntity("/dynamic/rule/QA1",List.class);
        List<Map<String, Object>> list = getRulesResponse.getBody();
        List<String> ruleNames = list.stream().map(item -> item.get("name").toString()).collect(Collectors.toList());

        assertThat(list, is(notNullValue()));
        assertThat(list, hasSize(2));
        assertThat(ruleNames, containsInAnyOrder("base_rule1", "base_rule2"));

        removeRules("base_rule1", "base_rule2");
        rest.postForEntity("/dynamic/entrypoint/QA1/reset", null, String.class);
    }

    private void removeRules(String... ruleNames){
        Arrays.stream(ruleNames).forEach(name -> rest.delete("/rule/" + name));
    }
}
