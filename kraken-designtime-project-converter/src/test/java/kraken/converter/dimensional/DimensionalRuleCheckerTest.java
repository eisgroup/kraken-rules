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

package kraken.converter.dimensional;

import kraken.model.Metadata;
import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProject;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.41
 */
public class DimensionalRuleCheckerTest {

    private static final RulesModelFactory MODEL_FACTORY = RulesModelFactory.getInstance();

    @Test
    public void shouldReturnFalseForNonDimensional_oneRule() {
        final Rule rule = createRule("A", false);
        final DimensionalRuleChecker predicate = new DimensionalRuleChecker(krakenProject(List.of(rule)));
        Assert.assertThat(predicate.isRuleDimensional(rule), is(false));
    }

    @Test
    public void shouldReturnFalseForNonDimensional_twoNonDimRules() {
        final Rule rule = createRule("A", false);
        final Rule rule2 = createRule("B", false);
        final DimensionalRuleChecker predicate = new DimensionalRuleChecker(krakenProject(List.of(rule, rule2)));
        Assert.assertThat(predicate.isRuleDimensional(rule), is(false));
    }

    @Test
    public void shouldReturnTrueForNonDimensional_twoNonDimRules_EqualName() {
        final Rule rule = createRule("A", false);
        final Rule rule2 = createRule("A", false);
        final DimensionalRuleChecker predicate = new DimensionalRuleChecker(krakenProject(List.of(rule, rule2)));
        Assert.assertThat(predicate.isRuleDimensional(rule), is(true));
    }

    @Test
    public void shouldReturnTrueForDimensional_DimRule() {
        final Rule rule = createRule("A", true);
        final DimensionalRuleChecker predicate = new DimensionalRuleChecker(krakenProject(List.of(rule)));
        Assert.assertThat(predicate.isRuleDimensional(rule), is(true));
    }

    @Test
    public void shouldReturnFalseForNonDimensional_DimRule() {
        final Rule rule = createRule("A", false);
        final DimensionalRuleChecker predicate = new DimensionalRuleChecker(krakenProject(List.of(rule)));
        Assert.assertThat(predicate.isRuleDimensional(rule), is(false));
    }

    private Rule createRule(String name, boolean dimensional) {
        final Rule rule = MODEL_FACTORY.createRule();
        rule.setName(name);
        rule.setDimensional(dimensional);
        return rule;
    }

    private KrakenProject krakenProject(List<Rule> rules) {
        return new ResourceKrakenProject("", "Root", Map.of(), List.of(), rules, null, Map.of(), null, List.of());
    }
}
