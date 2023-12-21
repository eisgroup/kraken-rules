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
package kraken.model.project.validator.rule;

import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 */
public class RuleServerSideOnlyValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassWhenAllRuleVersionsAreMarkedAsServerSideOnly() {
        Rule firstVersion = createRule("serverSideRule", true);
        Rule secondVersion = createRule("serverSideRule", true);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldPassWhenAllRuleVersionsAreNotMarkedAsServerSideOnly() {
        Rule firstVersion = createRule("serverSideRule", false);
        Rule secondVersion = createRule("serverSideRule", false);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(secondVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldFailWhenSomeRuleVersionsAreNotMarkedAsServerSideOnly() {
        Rule firstVersion = createRule("anotherServerSideRule", true);
        Rule secondVersion = createRule("anotherServerSideRule", true);
        Rule thirdVersion = createRule("anotherServerSideRule", false);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(thirdVersion, krakenProject);

        assertTrue(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule is misconfigured, because it has versions with different restrictions. " +
                "All versions of the same rule must be consistently marked or not marked as @ServerSideOnly."));
    }

    @Test
    public void shouldFailWhenOtherRuleVersionsAreNotMarkedAsServerSideOnly() {
        Rule firstVersion = createRule("anotherServerSideRule", false);
        Rule secondVersion = createRule("anotherServerSideRule", true);
        Rule thirdVersion = createRule("anotherServerSideRule", true);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(thirdVersion, krakenProject);

        assertTrue(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule is misconfigured, because it has versions with different restrictions. " +
                "All versions of the same rule must be consistently marked or not marked as @ServerSideOnly."));
    }

    @Test
    public void shouldBeApplicableOnlyIfRuleHasNameSet() {
        RuleServerSideOnlyValidator ruleServerSideOnlyValidator = new RuleServerSideOnlyValidator(null);

        assertTrue(ruleServerSideOnlyValidator.canValidate(createRule("ssoRule", true)));
        assertFalse(ruleServerSideOnlyValidator.canValidate(createRule(null, false)));
    }

    private ValidationSession doValidate(Rule rule, KrakenProject krakenProject) {
        RuleServerSideOnlyValidator ruleServerSideOnlyValidator = new RuleServerSideOnlyValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        ruleServerSideOnlyValidator.validate(rule, validationSession);

        return validationSession;
    }

    private Rule createRule(String name, boolean isServerSide) {
        Rule rule = factory.createRule();
        rule.setServerSideOnly(isServerSide);
        rule.setName(name);

        return rule;
    }

    private KrakenProject createProject(List<Rule> allRuleVersions) {
        return krakenProject(List.of(), List.of(), allRuleVersions);
    }

}
