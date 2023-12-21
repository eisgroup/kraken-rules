/*
 *  Copyright © 2023 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.model.project.validator.rule;

import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;
import kraken.model.Payload;
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
public class RuleVersionsPayloadTypeValidatorTest {
    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassWhenAllRuleVersionsAreSamePayloadType() {
        Rule firstVersion = createRule("rule", factory.createDefaultValuePayload());
        Rule secondVersion = createRule("rule", factory.createDefaultValuePayload());

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldPassWhenAllRuleVersionsAreTypesOfValidationPayload() {
        Rule firstVersion = createRule("validationRule", factory.createLengthPayload());
        Rule secondVersion = createRule("validationRule", factory.createRegExpPayload());

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldFailWhenRuleVersionsHaveDifferentEvaluationTypes() {
        Rule firstVersion = createRule("rule", factory.createDefaultValuePayload());
        Rule secondVersion = createRule("rule", factory.createVisibilityPayload());

        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule has versions with incompatible payload types. "
                + "Default Value payload type is not compatible with Visibility payload type."));
    }

    @Test
    public void shouldFailWhenRuleVersionsHaveMultipleDifferentEvaluationTypes() {
        Rule firstVersion = createRule("rule", factory.createLengthPayload());
        Rule secondVersion = createRule("rule", factory.createDefaultValuePayload());
        Rule thirdVersion = createRule("rule", factory.createAccessibilityPayload());


        KrakenProject krakenProject = createProject(List.of(firstVersion, secondVersion, thirdVersion));

        ValidationSession validationSession = doValidate(firstVersion, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(),
            containsString("Rule has versions with incompatible payload types. "
                + "Length payload type is not compatible with Accessibility, Default Value payload types."));
    }

    @Test
    public void shouldBeApplicableOnlyIfRuleHasNameSet() {
        RuleVersionsPayloadTypeValidator validator = new RuleVersionsPayloadTypeValidator(null);

        assertTrue(validator.canValidate(createRule("ruleName", factory.createVisibilityPayload())));
        assertFalse(validator.canValidate(createRule(null, factory.createVisibilityPayload())));
    }

    @Test
    public void shouldNotBeApplicableIfRuleHasNoPayload() {
        RuleVersionsPayloadTypeValidator validator = new RuleVersionsPayloadTypeValidator(null);

        Rule noPayloadRule = factory.createRule();
        noPayloadRule.setPayload(null);
        noPayloadRule.setName("emptyRuleName");

        assertFalse(validator.canValidate(noPayloadRule));
    }

    private ValidationSession doValidate(Rule rule, KrakenProject krakenProject) {
        RuleVersionsPayloadTypeValidator validator = new RuleVersionsPayloadTypeValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        validator.validate(rule, validationSession);

        return validationSession;
    }

    private Rule createRule(String name, Payload payload) {
        Rule rule = factory.createRule();
        rule.setPayload(payload);
        rule.setName(name);

        return rule;
    }

    private KrakenProject createProject(List<Rule> allRuleVersions) {
        return krakenProject(List.of(), List.of(), allRuleVersions);
    }
}