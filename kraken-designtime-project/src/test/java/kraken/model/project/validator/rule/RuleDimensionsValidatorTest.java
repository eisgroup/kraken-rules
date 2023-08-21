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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.Metadata;
import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public class RuleDimensionsValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassValidationsForValidRule() {
        Metadata metadata = createMetadata(Map.of("dateDimension", LocalDate.of(2022, 1, 1), "booleanDimension", true));
        Dimension stringDimension = createDimension("dateDimension", DimensionDataType.DATE);
        Dimension numberDimension = createDimension("booleanDimension", DimensionDataType.BOOLEAN);

        Rule rule = createRule("dimensionRule", metadata);

        KrakenProject krakenProject = createProject(
            List.of(rule),
            List.of(stringDimension, numberDimension)
        );

        ValidationSession validationSession = doValidate(rule, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldPassValidationsForRuleNoDimensions() {
        Metadata metadata = createMetadata(Map.of("someParam", LocalDate.of(2022, 1, 1)));

        Rule rule = createRule("dimensionRule", metadata);

        KrakenProject krakenProject = createProject(
            List.of(rule),
            List.of()
        );

        ValidationSession validationSession = doValidate(rule, krakenProject);

        assertFalse(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldValidateIncompatibleTypesNumber() {
        Metadata metadata = createMetadata(Map.of("numberDimension", false));
        Dimension dimension = createDimension("numberDimension", DimensionDataType.INTEGER);

        Rule rule = createRule("dimensionRule", metadata);

        KrakenProject krakenProject = createProject(
            List.of(rule),
            List.of(dimension)
        );

        ValidationSession validationSession = doValidate(rule, krakenProject);

        assertTrue(validationSession.hasRuleError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(), containsString(
            "Dimension 'numberDimension' value is 'false', but such value cannot be set to this dimension. "
                + "Expected dimension value type is 'Integer'."
        ));
    }

    private ValidationSession doValidate(Rule rule, KrakenProject krakenProject) {
        RuleDimensionsValidator ruleDimensionsValidator
            = new RuleDimensionsValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        ruleDimensionsValidator.validate(rule, validationSession);

        return validationSession;
    }


    private Metadata createMetadata(Map<String, Object> properties) {
        Metadata metadata = factory.createMetadata();
        properties.forEach(metadata::setProperty);

        return metadata;
    }

    private Rule createRule(String name, Metadata metadata) {
        Rule rule = factory.createRule();
        rule.setName(name);
        rule.setMetadata(metadata);

        return rule;
    }

    private Dimension createDimension(String name, DimensionDataType dataType) {
        Dimension dimension = factory.createDimension();
        dimension.setName(name);
        dimension.setDataType(dataType);

        return dimension;
    }

    private KrakenProject createProject(List<Rule> rules, List<Dimension> dimensions) {
        return krakenProject(
            List.of(),
            null,
            List.of(),
            List.of(),
            rules,
            List.of(),
            List.of(),
            dimensions
        );
    }

}
