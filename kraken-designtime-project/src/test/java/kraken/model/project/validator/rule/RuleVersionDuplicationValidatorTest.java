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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import kraken.model.Metadata;
import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 * @since 1.54.0
 */
public class RuleVersionDuplicationValidatorTest {

    private static final RulesModelFactory FACTORY = RulesModelFactory.getInstance();

    @Test
    public void shouldNotAddWarningForRuleWithSameNameButDifferentDimensionValue() {
        Rule firstRuleVersion = createRule("versioned rule", "firstId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );
        Rule secondRuleVersion = createRule("versioned rule", "secondId", createMetadata(
            DimensionEntry.of("first", "secondValue"))
        );

        KrakenProject krakenProject = createProject(firstRuleVersion, secondRuleVersion);

        ValidationSession validationSession = doValidate(firstRuleVersion, krakenProject);

        assertTrue(validationSession.getValidationMessages().isEmpty());
    }

    @Test
    public void shouldNotAddWarningForRuleWithDifferentNameAndSameDimensionValue() {
        Rule firstRule = createRule("a rule", "firstId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );
        Rule secondRule = createRule("another rule", "secondId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );

        KrakenProject krakenProject = createProject(firstRule, secondRule);

        ValidationSession validationSession = doValidate(firstRule, krakenProject);

        assertTrue(validationSession.getValidationMessages().isEmpty());
    }

    @Test
    public void shouldNotAddWarningForRuleWithSameNameButDifferentDimensions() {
        Rule firstRuleVersion = createRule("versioned rule", "firstId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );
        Rule secondRuleVersion = createRule("versioned rule", "secondId", null);

        KrakenProject krakenProject = createProject(firstRuleVersion, secondRuleVersion);

        ValidationSession validationSession = doValidate(firstRuleVersion, krakenProject);

        assertTrue(validationSession.getValidationMessages().isEmpty());
    }

    @Test
    public void shouldAddWarningForRuleWithSameNameAndDimensions() {
        Rule firstRuleVersion = createRule("versioned rule", "firstId", createMetadata(
                DimensionEntry.of("string", "package"),
                DimensionEntry.of("boolean", true)
            )
        );
        Rule secondRuleVersion = createRule("versioned rule", "secondId", createMetadata(
                DimensionEntry.of("string", "package"),
                DimensionEntry.of("boolean", true)
            )
        );

        KrakenProject krakenProject = createProject(firstRuleVersion, secondRuleVersion);

        ValidationSession validationSession = doValidate(firstRuleVersion, krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getCode(), is(Message.DUPLICATE_RULE_VERSION.getCode()));
    }

    private ValidationSession doValidate(Rule rule, KrakenProject krakenProject) {
        RuleVersionDuplicationValidator duplicateRuleValidator = new RuleVersionDuplicationValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        duplicateRuleValidator.validate(rule, validationSession);

        return validationSession;
    }

    private Rule createRule(String name, String variationId, Metadata metadata) {
        Rule rule = FACTORY.createRule();
        rule.setName(name);
        rule.setMetadata(metadata);
        rule.setRuleVariationId(variationId);

        return rule;
    }

    private Metadata createMetadata(DimensionEntry... dimensions) {
        Metadata metadata = FACTORY.createMetadata();

        for (DimensionEntry dimension : dimensions) {
            metadata.setProperty(dimension.getName(), dimension.getValue());
        }

        return metadata;
    }

    private KrakenProject createProject(Rule... allRuleVersions) {
        return krakenProject(List.of(), List.of(), Arrays.asList(allRuleVersions));
    }

    private static class DimensionEntry {

        private final String name;
        private final Object value;

        private DimensionEntry(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public static DimensionEntry of(String name, Object value) {
            return new DimensionEntry(name, value);
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }

}
