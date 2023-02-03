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
package kraken.model.project.validator;

import java.util.List;

import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultingType;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;

import org.junit.Before;
import org.junit.Test;

import static kraken.model.context.PrimitiveFieldDataType.BOOLEAN;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.context.SystemDataTypes.UNKNOWN;
import static kraken.model.project.KrakenProjectMocks.*;
import static kraken.model.project.KrakenProjectMocks.rootContextDefinition;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mulevicius
 */
public class KrakenProjectValidationServiceTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenProjectValidationService krakenProjectValidationService;

    @Before
    public void setUp() throws Exception {
        krakenProjectValidationService = new KrakenProjectValidationService();
    }

    @Test
    public void shouldReturnNoValidationMessagesIfKrakenProjectIsEmpty() {
        KrakenProject krakenProject = krakenProject(contextDefinitions(), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getValidationMessages(), empty());
    }

    @Test
    public void shouldReturnErrorsIfRuleModelIsBad() {
        Rule rule = factory.createRule();
        KrakenProject krakenProject = krakenProject(contextDefinitions(), entryPoints(), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(4));
    }

    @Test
    public void shouldReturnErrorsIfEntryPointModelIsBad() {
        EntryPoint entryPoint = factory.createEntryPoint();
        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(entryPoint), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorsIfContextDefinitionModelIsBad() {
        ContextDefinition rootContextDefinition = rootContextDefinition("Root", List.of());
        ContextDefinition badContextDefinition = factory.createContextDefinition();
        badContextDefinition.setPhysicalNamespace(rootContextDefinition.getPhysicalNamespace());

        KrakenProject krakenProject = krakenProject(List.of(rootContextDefinition, badContextDefinition), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnContextThatDoesNotExist() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Missing", "policyCd");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnFieldThatDoesNotExistInContext() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Policy", "missing");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnExternalField() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Policy", "policyCdExternal");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(externalField("policyCdExternal")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleImplementationsAreNotAppliedOnTheSameContext() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule1 = rule("R01", "Policy", "policyCd");
        Rule rule2 = rule("R01", "BasePolicy", "policyCd");
        ContextDefinition cd1 = rootContextDefinition("Policy", List.of(field("policyCd")));
        ContextDefinition cd2 = contextDefinition("BasePolicy", List.of(field("policyCd")), List.of("Policy"));

        KrakenProject krakenProject = krakenProject(List.of(cd1, cd2), List.of(entryPoint), List.of(rule1, rule2));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(3));
    }

    @Test
    public void shouldReturnErrorIfRuleImplementationsAreNotAppliedOnTheSameField() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule1 = rule("R01", "Policy", "policyCd");
        Rule rule2 = rule("R01", "Policy", "planCd");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd"), field("planCd")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule1, rule2));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(2));
    }

    @Test
    public void shouldReturnErrorIfRulePayloadIsNotCompatibleWithAppliedOnField_AndDefaultExpressionError() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Policy", "policyCd");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        Expression expression = factory.createExpression();
        expression.setExpressionString("'cd'");
        payload.setValueExpression(expression);
        rule.setPayload(payload);

        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd", UNKNOWN.toString())));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(2));
    }

    @Test
    public void shouldReturnErrorIfEntryPointIncludesItself() {
        EntryPoint entryPoint = entryPoint("Validate", List.of(), List.of("Validate"));

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(entryPoint), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfEntryPointIncludesAreTransitive() {
        EntryPoint ep1 = entryPoint("Validate", List.of(), List.of("Default"));
        EntryPoint ep2 = entryPoint("Default", List.of(), List.of("Deep"));
        EntryPoint ep3 = entryPoint("Deep", List.of(), List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep1, ep2, ep3), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfIncludedEntryPointDoesNotExist() {
        EntryPoint ep = entryPoint("Validate", List.of(), List.of("MissingEntryPoint"));

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleInEntryPointDoesNotExist() {
        EntryPoint ep = entryPoint("Validate", List.of("MissingRule"), List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfMultipleEntryPointImplementationsAreNotConsistentAboutServerSideOnly() {
        EntryPoint ep1 = serverSideOnlyEntryPoint("Validate", List.of());
        EntryPoint ep2 = entryPoint("Validate", List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep1, ep2), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRegularEntryPointIncludesServerSideOnlyEntryPoint() {
        EntryPoint ep1 = entryPoint("Validate", List.of(), List.of("ServerSideOnlyDefault"));
        EntryPoint ep2 = serverSideOnlyEntryPoint("ServerSideOnlyDefault", List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep1, ep2), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionInheritsContextThatDoesNotExist() {
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(), List.of("BasePolicy"));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionHasChildOfContextThatDoesNotExist() {
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(), List.of(), List.of(child("RiskItem")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionIsStrictButInheritedContextIsNot() {
        ContextDefinition policy = rootContextDefinition("Policy", List.of(), List.of("BasePolicy"));
        ContextDefinition basePolicy = dynamicContextDefinition("BasePolicy", List.of(), List.of());

        KrakenProject krakenProject = krakenProject(List.of(policy, basePolicy), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionFieldPathIsIncompatibleWithInheritedContextFieldPath() {
        ContextDefinition policy = rootContextDefinition(
                "Policy",
                List.of(field("policyCd", "nested.policyCd", STRING)),
                List.of("BasePolicy")
        );
        ContextDefinition basePolicy = contextDefinition(
                "BasePolicy",
                List.of(field("policyCd", BOOLEAN))
        );

        KrakenProject krakenProject = krakenProject(List.of(policy, basePolicy), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnContextWhichIsNotRelatedToRoot() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "RiskItem", "itemName");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        Expression expression = factory.createExpression();
        expression.setExpressionString("Coverage.coverageCd");
        payload.setValueExpression(expression);
        rule.setPayload(payload);

        ContextDefinition policy = rootContextDefinition("Policy", List.of(field("policyCd")));
        ContextDefinition riskItem = contextDefinition("RiskItem",
                List.of(field("itemName")),
                List.of(),
                List.of(child("Coverage"))
        );
        ContextDefinition coverage = contextDefinition("Coverage", List.of(field("coverageCd")));

        KrakenProject krakenProject = krakenProject(List.of(policy, riskItem, coverage), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorsIfContextIsNotRelatedToRootAndThatExpressionIsInvalidAndReturnTypeIncompatible() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "RiskItem", "itemName");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(DefaultingType.defaultValue);
        Expression expression = factory.createExpression();
        expression.setExpressionString("Coverage.smth");
        payload.setValueExpression(expression);
        rule.setPayload(payload);

        ContextDefinition policy = rootContextDefinition("Policy", List.of(field("policyCd")));
        ContextDefinition riskItem = contextDefinition("RiskItem",
                List.of(field("itemName")),
                List.of(),
                List.of(child("Coverage"))
        );
        ContextDefinition coverage = contextDefinition("Coverage", List.of(field("coverageCd")));

        KrakenProject krakenProject = krakenProject(List.of(policy, riskItem, coverage), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(3));
    }

    @Test
    public void shouldReturnErrorIfContextHasDuplicateParentDefinition() {
        ContextDefinition policy = rootContextDefinition("Policy", List.of(), List.of("BasePolicy", "BasePolicy"));
        ContextDefinition basePolicy = contextDefinition("BasePolicy", List.of());
        KrakenProject krakenProject = krakenProject(List.of(policy, basePolicy), List.of(), List.of());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));
        assertThat(
            result.getErrors().get(0).getMessage(),
            is("parent 'BasePolicy' is specified twice - please remove duplicated context")
        );
    }
    
    @Test
    public void shouldReturnErrorIfSystemContextHasParentContextDefined() {
        ContextDefinition systemContext = toSystemContextDefinition("SystemContext");
        systemContext.setParentDefinitions(List.of("ParentContext"));

        KrakenProject krakenProject = krakenProject(List.of(systemContext), List.of(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrors(), hasSize(1));

        ValidationMessage message = result.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is("SystemContext"));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), containsString(
            "Parent contexts are not allowed for system context definitions."));
    }

}
