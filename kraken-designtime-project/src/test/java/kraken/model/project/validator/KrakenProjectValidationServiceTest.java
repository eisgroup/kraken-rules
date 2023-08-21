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

import static kraken.model.context.PrimitiveFieldDataType.BOOLEAN;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.context.SystemDataTypes.UNKNOWN;
import static kraken.model.project.KrakenProjectMocks.child;
import static kraken.model.project.KrakenProjectMocks.childForbiddenAsReference;
import static kraken.model.project.KrakenProjectMocks.contextDefinition;
import static kraken.model.project.KrakenProjectMocks.contextDefinitions;
import static kraken.model.project.KrakenProjectMocks.dynamicContextDefinition;
import static kraken.model.project.KrakenProjectMocks.entryPoint;
import static kraken.model.project.KrakenProjectMocks.entryPoints;
import static kraken.model.project.KrakenProjectMocks.field;
import static kraken.model.project.KrakenProjectMocks.fieldForbiddenAsReference;
import static kraken.model.project.KrakenProjectMocks.fieldForbiddenAsTarget;
import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static kraken.model.project.KrakenProjectMocks.rootContextDefinition;
import static kraken.model.project.KrakenProjectMocks.rule;
import static kraken.model.project.KrakenProjectMocks.rules;
import static kraken.model.project.KrakenProjectMocks.serverSideOnlyEntryPoint;
import static kraken.model.project.KrakenProjectMocks.toContextDefinition;
import static kraken.model.project.KrakenProjectMocks.toSystemContextDefinition;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultingType;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.ValidationSeverity;

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

        assertThat(result.getAllMessages(), empty());
    }

    @Test
    public void shouldReturnErrorsIfRuleModelIsBad() {
        Rule rule = factory.createRule();
        KrakenProject krakenProject = krakenProject(contextDefinitions(), entryPoints(), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(4));
    }

    @Test
    public void shouldReturnErrorsIfEntryPointModelIsBad() {
        EntryPoint entryPoint = factory.createEntryPoint();
        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(entryPoint), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorsIfContextDefinitionModelIsBad() {
        ContextDefinition rootContextDefinition = rootContextDefinition("Root", List.of());
        ContextDefinition badContextDefinition = factory.createContextDefinition();
        badContextDefinition.setPhysicalNamespace(rootContextDefinition.getPhysicalNamespace());

        KrakenProject krakenProject = krakenProject(List.of(rootContextDefinition, badContextDefinition), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnContextThatDoesNotExist() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Missing", "policyCd");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnFieldThatIsForbiddenToBeTarget() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Policy", "policyCdForbidden");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(fieldForbiddenAsTarget("policyCdForbidden")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
        assertThat(result.getErrorMessages().get(0).getMessage(),
            equalTo("Cannot be applied on a field 'policyCdForbidden' because it is forbidden to be a rule target."));
    }

    @Test
    public void shouldReturnErrorIfRuleRefersFieldThatIsForbiddenToBeReference() {
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(
            fieldForbiddenAsReference("policyCdForbidden"),
            field("policyCd")
        ));

        Rule rule = rule("R01", "Policy", "policyCd");
        AssertionPayload payload = factory.createAssertionPayload();
        Expression expression = factory.createExpression();
        expression.setExpressionString("Policy.policyCdForbidden = null");
        payload.setAssertionExpression(expression);
        payload.setSeverity(ValidationSeverity.critical);
        rule.setPayload(payload);

        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
        assertThat(result.getErrorMessages().get(0).getMessage(),
            equalTo("Assertion expression has error in 'Policy.policyCdForbidden'. "
                + "Attribute 'policyCdForbidden' not found in 'Policy'."));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnFieldThatDoesNotExistInContext() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "Policy", "missing");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
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

        assertThat(result.getErrorMessages(), hasSize(3));
    }

    @Test
    public void shouldReturnErrorIfRuleImplementationsAreNotAppliedOnTheSameField() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule1 = rule("R01", "Policy", "policyCd");
        Rule rule2 = rule("R01", "Policy", "planCd");
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(field("policyCd"), field("planCd")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), List.of(entryPoint), List.of(rule1, rule2));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(2));
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

        assertThat(result.getErrorMessages(), hasSize(2));
    }

    @Test
    public void shouldReturnErrorIfEntryPointIncludesItself() {
        EntryPoint entryPoint = entryPoint("Validate", List.of(), List.of("Validate"));

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(entryPoint), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfEntryPointIncludesAreTransitive() {
        EntryPoint ep1 = entryPoint("Validate", List.of(), List.of("Default"));
        EntryPoint ep2 = entryPoint("Default", List.of(), List.of("Deep"));
        EntryPoint ep3 = entryPoint("Deep", List.of(), List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep1, ep2, ep3), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfIncludedEntryPointDoesNotExist() {
        EntryPoint ep = entryPoint("Validate", List.of(), List.of("MissingEntryPoint"));

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleInEntryPointDoesNotExist() {
        EntryPoint ep = entryPoint("Validate", List.of("MissingRule"), List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfMultipleEntryPointImplementationsAreNotConsistentAboutServerSideOnly() {
        EntryPoint ep1 = serverSideOnlyEntryPoint("Validate", List.of());
        EntryPoint ep2 = entryPoint("Validate", List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep1, ep2), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRegularEntryPointIncludesServerSideOnlyEntryPoint() {
        EntryPoint ep1 = entryPoint("Validate", List.of(), List.of("ServerSideOnlyDefault"));
        EntryPoint ep2 = serverSideOnlyEntryPoint("ServerSideOnlyDefault", List.of());

        KrakenProject krakenProject = krakenProject(contextDefinitions(), List.of(ep1, ep2), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionInheritsContextThatDoesNotExist() {
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(), List.of("BasePolicy"));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionHasChildOfContextThatDoesNotExist() {
        ContextDefinition contextDefinition = rootContextDefinition("Policy", List.of(), List.of(), List.of(child("RiskItem")));

        KrakenProject krakenProject = krakenProject(List.of(contextDefinition), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfContextDefinitionIsStrictButInheritedContextIsNot() {
        ContextDefinition policy = rootContextDefinition("Policy", List.of(), List.of("BasePolicy"));
        ContextDefinition basePolicy = dynamicContextDefinition("BasePolicy", List.of(), List.of());

        KrakenProject krakenProject = krakenProject(List.of(policy, basePolicy), entryPoints(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
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

        assertThat(result.getErrorMessages(), hasSize(1));
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

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleIsAppliedOnContextWhichIsForbidden() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "RiskItem", "itemName");
        var payload = factory.createAccessibilityPayload();
        rule.setPayload(payload);

        ContextDefinition policy = rootContextDefinition("Policy",
            List.of(field("policyCd")),
            List.of(),
            List.of(childForbiddenAsReference("RiskItem"))
        );
        ContextDefinition riskItem = contextDefinition("RiskItem",
            List.of(field("itemName"))
        );

        KrakenProject krakenProject = krakenProject(List.of(policy, riskItem), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
    }

    @Test
    public void shouldReturnErrorIfRuleRefersContextWhichIsForbidden() {
        EntryPoint entryPoint = entryPoint("Validate", List.of("R01"));
        Rule rule = rule("R01", "RiskItem", "itemName");
        var payload = factory.createAssertionPayload();
        payload.setSeverity(ValidationSeverity.critical);
        Expression expression = factory.createExpression();
        expression.setExpressionString("Coverage.coverageCd != null");
        payload.setAssertionExpression(expression);
        rule.setPayload(payload);

        ContextDefinition policy = rootContextDefinition("Policy",
            List.of(field("policyCd")),
            List.of(),
            List.of(child("RiskItem"))
        );
        ContextDefinition riskItem = contextDefinition("RiskItem",
            List.of(field("itemName")),
            List.of(),
            List.of(childForbiddenAsReference("Coverage"))
        );
        ContextDefinition coverage = contextDefinition("Coverage", List.of(field("coverageCd")));

        KrakenProject krakenProject = krakenProject(List.of(policy, riskItem, coverage), List.of(entryPoint), List.of(rule));

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
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

        assertThat(result.getErrorMessages(), hasSize(3));
    }

    @Test
    public void shouldReturnErrorIfContextHasDuplicateParentDefinition() {
        ContextDefinition policy = rootContextDefinition("Policy", List.of(), List.of("BasePolicy", "BasePolicy"));
        ContextDefinition basePolicy = contextDefinition("BasePolicy", List.of());
        KrakenProject krakenProject = krakenProject(List.of(policy, basePolicy), List.of(), List.of());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));
        assertThat(
            result.getErrorMessages().get(0).getMessage(),
            is("Inherited context definition 'BasePolicy' is specified twice - please remove duplicate.")
        );
    }
    
    @Test
    public void shouldReturnErrorIfSystemContextHasParentContextDefined() {
        ContextDefinition parentContext = toContextDefinition("ParentContext");
        ContextDefinition systemContext = toSystemContextDefinition("SystemContext");
        systemContext.setParentDefinitions(List.of("ParentContext"));

        KrakenProject krakenProject = krakenProject(List.of(systemContext, parentContext), List.of(), rules());

        ValidationResult result = krakenProjectValidationService.validate(krakenProject);

        assertThat(result.getErrorMessages(), hasSize(1));

        ValidationMessage message = result.getAllMessages().get(0);

        assertThat(message.getItem().getName(), is("SystemContext"));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), containsString(
            "Inherited context definition is not allowed for system context definition."));
    }

}
