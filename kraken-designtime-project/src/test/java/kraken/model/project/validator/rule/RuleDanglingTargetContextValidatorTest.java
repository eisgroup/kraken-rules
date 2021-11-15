/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
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
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.resource.Resource;

/**
 * Unit tests for {@code RuleDanglingTargetContextValidator} class.
 *
 * @author Tomas Dapkunas
 * @since 1.0.44
 */
public class RuleDanglingTargetContextValidatorTest {

    private DSLReader dslReader;

    @Before
    public void setUp() {
        dslReader = new DSLReader();
    }

    @Test
    public void shouldNotFailForRuleDefinedOnRootContext() {
        Resource contexts = readSingleResource("RuleDanglingTargetContextValidatorTest/base-contexts.rules");
        Resource rules = readSingleResource("RuleDanglingTargetContextValidatorTest/base-rules-valid.rules");

        KrakenProject krakenProject = krakenProject(contexts, rules);

        List<ValidationMessage> result = validate(krakenProject);

        assertThat(result, hasSize(0));
    }

    @Test
    public void shouldFailForRuleDefinedOnContextNotRelatedToRoot() {
        Resource contexts = readSingleResource("RuleDanglingTargetContextValidatorTest/base-contexts.rules");
        Resource rules = readSingleResource("RuleDanglingTargetContextValidatorTest/base-rules-invalid.rules");

        KrakenProject krakenProject = krakenProject(contexts, rules);

        List<ValidationMessage> result = validate(krakenProject);

        assertThat(result, hasSize(1));
        assertThat(toMessages(result), contains("[ERROR] Rule - 'RULE01': applied on ContextDefinition 'OtherContext' which is not related to Root Context 'PolicySummary'"));
    }

    private KrakenProject krakenProject(Resource contexts, Resource rules) {
        return new ResourceKrakenProject(
            "Base",
            "PolicySummary",
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

    private List<String> toMessages(List<ValidationMessage> messages) {
        return messages.stream().map(ValidationMessage::toString)
                .collect(Collectors.toList());
    }

    private Resource readSingleResource(String pathToResource) {
        return dslReader.read(pathToResource).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No resources found."));
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        RuleDanglingTargetContextValidator validator = new RuleDanglingTargetContextValidator(krakenProject);
        ValidationSession session = new ValidationSession();
        for(Rule rule : krakenProject.getRules()) {
            validator.validate(rule, session);
        }
        return session.getValidationMessages();
    }

}
