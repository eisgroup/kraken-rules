/*
 *  Copyright 2021 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.resource.Resource;

/**
 * Unit tests for {@code RuleDefinedOnCycleValidator} class.
 *
 * @author Tomas Dapkunas
 * @since 1.11.0
 */
public class RuleDefinedOnCycleValidatorTest {

    private DSLReader dslReader;

    @Before
    public void setUp() {
        dslReader = new DSLReader();
    }

    @Test
    public void shouldFailForRuleDefinedOnChildContextOfRecursiveContext() {
        Resource contexts = readSingleResource("RuleDefinedOnCycleValidatorTest/model-contexts-ap.rules");
        Resource rules = readSingleResource("RuleDefinedOnCycleValidatorTest/model-rules-ap.rules");

        KrakenProject krakenProject = krakenProject(contexts, rules, "AutoPolicy");

        List<ValidationMessage> result = validate(krakenProject);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getSeverity(), is(Severity.ERROR));
    }

    @Test
    public void shouldReturnRecursiveTargetRulesErrors() {
        Resource contexts = readSingleResource("RuleDefinedOnCycleValidatorTest/recursive.rules");
        Resource rules = readSingleResource("RuleDefinedOnCycleValidatorTest/recursive.rules");

        KrakenProject krakenProject = krakenProject(contexts, rules, "Recursive");

        List<ValidationMessage> result = validate(krakenProject);

        assertThat(result, hasSize(9));
        for (ValidationMessage validationMessage : result) {
            assertThat(
                    validationMessage.getMessage(),
                    containsString("Defining rules on recursive Context Definition is not supported")
            );
            assertThat(validationMessage.getSeverity(), is(Severity.ERROR));
        }
    }

    @Test
    public void shouldFailForRuleDefinedOnRecursiveContext() {
        Resource contexts = readSingleResource("RuleDefinedOnCycleValidatorTest/model-contexts-hp.rules");
        Resource rules = readSingleResource("RuleDefinedOnCycleValidatorTest/model-rules-hp.rules");

        KrakenProject krakenProject = krakenProject(contexts, rules, "HomePolicy");

        List<ValidationMessage> result = validate(krakenProject);

        assertThat(result, hasSize(1));
    }

    private KrakenProject krakenProject(Resource contexts, Resource rules, String namespace) {
        return new ResourceKrakenProject(
            namespace,
            "Policy",
            contexts.getContextDefinitions().stream()
                .collect(Collectors.toMap(ContextDefinition::getName, c -> c)),
            rules.getEntryPoints(),
            rules.getRules(),
            null,
            Map.of(),
            null,
            List.of()
        );
    }

    private Resource readSingleResource(String pathToResource) {
        return dslReader.read(pathToResource).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No resources found."));
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        RuleDefinedOnCycleValidator validator = new RuleDefinedOnCycleValidator(krakenProject);
        ValidationSession session = new ValidationSession();
        for(Rule rule : krakenProject.getRules()) {
            validator.validate(rule, session);
        }
        return session.getValidationMessages();
    }

}
