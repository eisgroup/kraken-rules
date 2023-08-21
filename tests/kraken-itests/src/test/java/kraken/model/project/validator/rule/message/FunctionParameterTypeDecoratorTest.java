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

public class FunctionParameterTypeDecoratorTest {

    private static ScopeBuilder scopeBuilder;
    private static KrakenProject krakenProject;
    private static CrossContextService crossContextService;

    private FunctionParameterTypeDecorator functionParameterTypeDecorator;

    @BeforeClass
    public static void beforeClass() {
        TestResources testResources = TestResources.create(TestResources.Info.TEST_PRODUCT);

        krakenProject = testResources.getKrakenProject();
        scopeBuilder = new ScopeBuilder(testResources.getKrakenProject());
        crossContextService = CrossContextServiceProvider.forProject(krakenProject);
    }

    @Before
    public void beforeTest() {
        functionParameterTypeDecorator
            = new FunctionParameterTypeDecorator(krakenProject, crossContextService);
    }

    @Test
    public void shouldDecorateErrorMessageProvidingPathToCCRMultiple() {
        AstMessage message = validateExpression(
            "GetVehicleModel(Vehicle) != 'WV'",
            krakenProject.getContextProjection("Policy")
        );

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = functionParameterTypeDecorator.decorate(message, rule);

        assertEquals("Incompatible type 'Vehicle[]' of function parameter at index 0 when invoking "
                + "function GetVehicleModel(Vehicle). Expected type is 'Vehicle'. 'Vehicle' is a collection of values, "
                + "because 'Vehicle' is a cross context reference accessed from "
                + "'Policy' which resolves to multiple cardinality through path(s): Policy->Vehicle.",
            decoratedError);
    }

    @Test
    public void shouldDecorateErrorMessageProvidingPathToCCRSingle() {
        AstMessage message = validateExpression("GetFirstElement(CreditCardInfo.cardType) != 'Visa'",
            krakenProject.getContextProjection("Policy"));

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = functionParameterTypeDecorator.decorate(message, rule);

        assertEquals("Incompatible type 'String' of function parameter at index 0 when invoking function "
                + "GetFirstElement(CreditCardInfo.cardType). Expected type is 'String[]'. "
                + "'CreditCardInfo.cardType' is not a collection of values, "
                + "because 'CreditCardInfo' is a cross context reference accessed "
                + "from 'Policy' which resolves to single cardinality through path(s): Policy->CreditCardInfo.",
            decoratedError);
    }

    @Test
    public void shouldReturnOriginalMessageWhenFirstExpressionReferenceIsNotType() {
        AstMessage message = validateExpression("GetVehicleModel(riskItems) != 'WV'",
            krakenProject.getContextProjection("Policy"));

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = functionParameterTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    @Test
    public void shouldReturnOriginalWhenExpressionTypesAreDifferent() {
        AstMessage message = validateExpression("GetFirstElement(Vehicle.modelYear) != 'VAL'",
            krakenProject.getContextProjection("Policy"));

        Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setContext("Policy");

        String decoratedError = functionParameterTypeDecorator.decorate(message, rule);

        assertEquals(message.getMessage(), decoratedError);
    }

    private AstMessage validateExpression(String expression, ContextDefinition contextDefinition) {
        Scope scope = scopeBuilder.buildScope(contextDefinition);
        AstValidatingVisitor visitor = new AstValidatingVisitor();

        visitor.visit(AstBuilder.from(expression, scope).getExpression());

        return visitor.getMessages().iterator().next();
    }

}
