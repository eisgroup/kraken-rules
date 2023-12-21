/*
 *  Copyright © 2023 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.model.project.validator.rule;

import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.ValidationPayload;
import org.junit.Test;

import java.util.List;

import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author kjuraityte
 */
public class RuleVersionsOverridabilityValidatorTest {
    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassWhenAllRuleVersionsAreNotOverridable() {
        Rule firstVersion = createRule("rule", false);
        Rule secondVersion = createRule("rule", false);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldPassWhenAllRuleVersionsAreOverridable() {
        Rule firstVersion = createRule("overridableRule", true);
        Rule secondVersion = createRule("overridableRule", true);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldFailWhenRuleVersionsHaveDifferentOverridableGroups() {
        Rule firstVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupOne");
        Rule secondVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupTwo");
        Rule thirdVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupOne");

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule has versions with different override group configuration."));
    }

    @Test
    public void shouldFailWhenSomeRuleVersionsDontHaveOverridableGroup() {
        Rule firstVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupOne");
        Rule secondVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupOne");
        Rule thirdVersion = createRule("inconsistentOverridableRule", true);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule has versions with different override group configuration."));
    }

    @Test
    public void shouldAddConfigurationWarningWhenRuleVersionsHaveDifferentOverridabilityConfigurationAndGroups() {
        Rule firstVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupOne");
        Rule secondVersion = createRule("inconsistentOverridableRule", false);
        Rule thirdVersion = createRule("inconsistentOverridableRule", true);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(secondVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        containsString("Rule has versions with different override group configuration.");
    }

    @Test
    public void shouldFailWhenRuleVersionsHaveDifferentOverridabilityConfiguration() {
        Rule firstVersion = createRule("inconsistentOverridableRule", true);
        Rule secondVersion = createRule("inconsistentOverridableRule", true);
        Rule thirdVersion = createRule("inconsistentOverridableRule", false);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(thirdVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule has versions with different overridability configuration."));
    }

    @Test
    public void shouldFailWhenRuleVersionsHaveDifferentOverridabilityConfigurationAndPayloadTypes() {
        Rule firstVersion = createRule("inconsistentOverridableRule", false);
        Rule secondVersion = createRule("inconsistentOverridableRule", true);
        Rule thirdVersion = factory.createRule();
        Payload payload = factory.createDefaultValuePayload();
        thirdVersion.setPayload(payload);
        thirdVersion.setName("inconsistentOverridableRule");

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(secondVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule has versions with different overridability configuration."));
    }

    @Test
    public void shouldAddBothWarningsWhenRuleVersionsHaveDifferentOverridabilityConfigurationAndGroups() {
        Rule firstVersion = createRuleWithOverrideGroup("inconsistentOverridableRule", true, "GroupOne");
        Rule secondVersion = createRule("inconsistentOverridableRule", false);
        Rule thirdVersion = createRule("inconsistentOverridableRule", true);

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(2));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(1).getSeverity(), is(Severity.WARNING));
    }

    @Test
    public void shouldBeApplicableOnlyIfRuleHasNameSet() {
        RuleVersionsOverridabilityValidator validator = new RuleVersionsOverridabilityValidator(null);

        assertTrue(validator.canValidate(createRule("ruleName", true)));
        assertFalse(validator.canValidate(createRule(null, true)));
    }

    @Test
    public void shouldNotBeApplicableIfRuleHasNoPayload() {
        RuleVersionsOverridabilityValidator validator = new RuleVersionsOverridabilityValidator(null);

        Rule noPayloadRule = factory.createRule();
        noPayloadRule.setPayload(null);
        noPayloadRule.setName("name");

        assertFalse(validator.canValidate(noPayloadRule));
    }

    @Test
    public void shouldNotBeApplicableIfRuleHasOtherThanValidationPayload() {
        RuleVersionsOverridabilityValidator validator = new RuleVersionsOverridabilityValidator(null);

        Rule defaultValuePayloadRule = factory.createRule();
        Payload payload = factory.createDefaultValuePayload();
        defaultValuePayloadRule.setPayload(payload);
        defaultValuePayloadRule.setName("name");

        assertFalse(validator.canValidate(defaultValuePayloadRule));
    }

    private ValidationSession doValidate(Rule rule, KrakenProject krakenProject) {
        RuleVersionsOverridabilityValidator validator = new RuleVersionsOverridabilityValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        validator.validate(rule, validationSession);

        return validationSession;
    }

    private Rule createRule(String name, boolean isOverridable) {
        Rule rule = factory.createRule();
        ValidationPayload payload = factory.createLengthPayload();
        payload.setOverridable(isOverridable);
        rule.setPayload(payload);
        rule.setName(name);

        return rule;
    }

    private Rule createRuleWithOverrideGroup(String name, boolean isOverridable, String groupName) {
        Rule rule = factory.createRule();
        ValidationPayload payload = factory.createLengthPayload();
        payload.setOverridable(isOverridable);
        payload.setOverrideGroup(groupName);
        rule.setPayload(payload);
        rule.setName(name);

        return rule;
    }

    private KrakenProject createProject(List<Rule> allRuleVersions) {
        return krakenProject(List.of(), List.of(), allRuleVersions);
    }
}