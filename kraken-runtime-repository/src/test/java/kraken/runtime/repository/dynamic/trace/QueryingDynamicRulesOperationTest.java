/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.repository.dynamic.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import kraken.runtime.model.rule.RuntimeRule;

/**
 * Unit tests for {@code QueryingDynamicRulesOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public class QueryingDynamicRulesOperationTest {

    @Test
    public void shouldCreateCorrectDescriptionForQueryingDynamicRepository() {
        var dynamicRuleOp = new QueryingDynamicRulesOperation("dynamicRepository");

        assertThat(dynamicRuleOp.describe(),
            is("Querying 'dynamicRepository' repository for dynamic rules."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForQueriedDynamicRules() {
        var dynamicRules = new HashMap<String, List<RuntimeRule>>();
        dynamicRules.put("dynamicRuleOne", List.of());
        dynamicRules.put("dynamicRuleTwo", List.of());

        var dynamicRuleOp = new QueryingDynamicRulesOperation("dynamicRepository");

        String expected = "Repository 'dynamicRepository' returned 2 dynamic rules: "
            + System.lineSeparator()
            + "'dynamicRuleOne',"
            + System.lineSeparator()
            + "'dynamicRuleTwo'";

        assertThat(dynamicRuleOp.describeAfter(dynamicRules), is(expected));
    }

}
