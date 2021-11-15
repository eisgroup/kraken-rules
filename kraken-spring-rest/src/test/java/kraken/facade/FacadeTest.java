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

import kraken.model.BundleRequest;
import kraken.model.Request;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.entrypoint.EntryPoint;
import kraken.testproduct.TestProduct;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static kraken.utils.TestUtils.createMockRules;
import static kraken.utils.TestUtils.toDSL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class FacadeTest extends AbstractFacadeTest {

    @Before
    public void setUp() {
        rest.delete("/dynamic/rules/clear");
    }

    @Test
    public void loadEntryPoint() {
        final ResponseEntity<EntryPoint> response =
                rest.getForEntity("/entrypoint/InitAutoPolicy", EntryPoint.class);
        final EntryPoint entryPoint = response.getBody();
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(entryPoint, notNullValue());
        assertThat(entryPoint.getName(), is("InitAutoPolicy"));
        assertThat(entryPoint.getRuleNames(), hasSize(15));
    }

    @Test
    public void shouldLoadRule() {
        final ResponseEntity<Rule> response = rest.getForEntity("/rule/R0009", Rule.class);
        final Rule rule = response.getBody();
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(rule, notNullValue());
        assertThat(rule.getName(), is("R0009"));
        assertThat(rule.getContext(), is("Policy"));
    }

    @Test
    public void shouldLoadContext() {
        final ResponseEntity<ContextDefinition> response =
                rest.getForEntity("/context/Policy", ContextDefinition.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        final ContextDefinition context = response.getBody();
        assertThat(context, notNullValue());
        assertThat(context.getName(), is("Policy"));
    }

    @Test
    public void shouldEntryPointResponseReturnErrorOnInvalidName() {
        final ResponseEntity<Object> response = rest.getForEntity("/entrypoint/noname", Object.class);
        assertThat(response.getStatusCode().value(), is(500));
    }

    @Test
    public void shouldRuleResponseReturnErrorOnInvalidName() {
        final ResponseEntity<Object> response = rest.getForEntity("/rule/noname", Object.class);
        assertThat(response.getStatusCode().value(), is(500));
    }

    @Test
    public void shouldReturnQA5Bundle() {
        ResponseEntity<Object> response =
                rest.postForEntity("/bundle/QA5", new BundleRequest(Map.of()), Object.class);
        assertThat(response, is(notNullValue()));
        assertThat(response.getBody(), is(notNullValue()));
        assertThat(response.getStatusCode().value(), is(200));
    }

    @Test
    public void shouldReturnQA1Bundle() {
        rest.postForEntity(
                "/rule/dsl/QA1",
                toDSL(createMockRules("R001", "R002")),
                String.class
        );
        ResponseEntity<Map> response = rest.postForEntity(
                "/bundle/QA1",
                new BundleRequest(Map.of()),
                Map.class
        );
        assertThat(response.getStatusCode().value(), is(200));
        assertThat(response.getBody().get("evaluation"), is(notNullValue()));
    }
}