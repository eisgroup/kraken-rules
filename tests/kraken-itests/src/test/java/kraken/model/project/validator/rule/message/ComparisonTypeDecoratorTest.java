/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule.message;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.AstValidatingVisitor;
import kraken.el.scope.Scope;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.model.project.scope.ScopeBuilder;
import kraken.test.TestResources;

public class ComparisonTypeDecoratorTest {

    private static ScopeBuilder scopeBuilder;
    private static KrakenProject krakenProject;
    private static CrossContextService crossContextService;

    private ComparisonTypeDecorator comparisonTypeDecorator;

    @BeforeClass
    public static void beforeClass() {
        TestResources testResources = TestResources.create(TestResources.Info.TEST_PRODUCT);

        krakenProject = testResources.getKrakenProject();
        scopeBuilder = new ScopeBuilder(testResources.getKrakenProject());
        crossContextService = CrossContextServiceProvider.forProject(krakenProject);
    }

    @Before
    public void beforeTest() {
        comparisonTypeDecorator = new ComparisonTypeDecorator(krakenProject, crossContextService);
    }

    @Test
    public void shouldDecorateErrorMessageProvidingPathToCCRMultipleNonRoot() {
        AstMessage message = validateExpression(
            "Vehicle.model != 'WV'",
            krakenProject.getContextProjection("CreditCardInfo")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("CreditCardInfo");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(
            "error in 'Vehicle.model != 'WV'' with message: "
                + "Both sides of operator 'NotEquals' must have same type, "
                + "but left side was of type 'String[]' and right side was of type 'String'. "
                + "'Vehicle.model' is a collection of values, because 'Vehicle' is a cross context reference accessed "
                + "from 'Policy.CreditCardInfo' which resolves to multiple cardinality through path(s): Policy->Vehicle.",
            decoratedError);
    }

    @Test
    public void shouldDecorateErrorMessageProvidingPathToCCRMultiple() {
        AstMessage message = validateExpression(
            "Vehicle.model != 'WV'",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals("error in 'Vehicle.model != 'WV'' with message: "
                + "Both sides of operator 'NotEquals' must have same type, "
                + "but left side was of type 'String[]' and right side was of type 'String'. "
                + "'Vehicle.model' is a collection of values, because 'Vehicle' is a cross context reference accessed "
                + "from 'Policy' which resolves to multiple cardinality through path(s): Policy->Vehicle.",
            decoratedError);
    }

    @Test
    public void shouldDecorateErrorMessageProvidingPathToCCRSingle() {
        AstMessage message = validateExpression(
            "CreditCardInfo.cardType != policies",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals("error in 'CreditCardInfo.cardType != policies' with message: "
                + "Both sides of operator 'NotEquals' must have same type, "
                + "but left side was of type 'String' and right side was of type 'String[]'. "
                + "'CreditCardInfo.cardType' is not a collection of values, "
                + "because 'CreditCardInfo' is a cross context reference accessed "
                + "from 'Policy' which resolves to single cardinality through path(s): Policy->CreditCardInfo.",
            decoratedError);
    }

    @Test
    public void shouldDecorateErrorMessageProvidingPathsToCCR() {
        AstMessage message = validateExpression(
            "CreditCardInfo.cardType != Vehicle.model",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals("error in 'CreditCardInfo.cardType != Vehicle.model' with message: "
                + "Both sides of operator 'NotEquals' must have same type, "
                + "but left side was of type 'String' and right side was of type 'String[]'. "
                + "'CreditCardInfo.cardType' is not a collection of values, "
                + "because 'CreditCardInfo' is a cross context reference accessed "
                + "from 'Policy' which resolves to single cardinality through path(s): Policy->CreditCardInfo. "
                + "'Vehicle.model' is a collection of values, because 'Vehicle' is a cross context reference accessed "
                + "from 'Policy' which resolves to multiple cardinality through path(s): Policy->Vehicle.",
            decoratedError);
    }

    @Test
    public void shouldReturnOriginalMessageWhenFirstExpressionReferenceIsNotType() {
        AstMessage message = validateExpression("riskItems.model == 'WV'",
            krakenProject.getContextProjection("Policy"));

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    @Test
    public void shouldReturnOriginalWhenExpressionTypesAreDifferent() {
        AstMessage message = validateExpression("Vehicle.modelYear == 'WV'",
            krakenProject.getContextProjection("Policy"));

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    @Test
    public void shouldReturnOriginalMessageIfCCRTypeCardinalityMatches() {
        AstMessage message = validateExpression(
            "Insured.childrenAges != 5",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    @Test
    public void shouldReturnOriginalMessageIfFieldIsNotSimplePathElement() {
        AstMessage message = validateExpression(
            "Insured.childrenAges != numberOfInsureds[0]",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    @Test
    public void shouldReturnOriginalMessageIfCcrReferenceIsNotSimplePathElement() {
        AstMessage message = validateExpression(
            "Vehicle[0].model != policies",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    @Test
    public void shouldReturnOriginalMessageWhenArrayTypesMismatch() {
        AstMessage message = validateExpression(
            "Insured.childrenAges != policies",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = comparisonTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    private AstMessage validateExpression(String expression, ContextDefinition contextDefinition) {
        Scope scope = scopeBuilder.buildScope(contextDefinition);
        AstValidatingVisitor visitor = new AstValidatingVisitor();

        visitor.visit(AstBuilder.from(expression, scope).getExpression());

        return visitor.getMessages().iterator().next();
    }

}
