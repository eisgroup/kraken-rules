/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

package kraken.el.ast.validation;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;

import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.scope.Scope;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.test.TestResources;

public class AstValidatingVisitorMessagesTest {

    private static ScopeBuilder scopeBuilder;
    private static KrakenProject krakenProject;

    @BeforeClass
    public static void staticSetup() {
        TestResources testResources = TestResources.create(TestResources.Info.TEST_PRODUCT);
        krakenProject = testResources.getKrakenProject();
        scopeBuilder = new ScopeBuilder(testResources.getKrakenProject());
    }

    @Test
    public void shouldTestNoAttributeFindMessage() {
        var expression = "Policy.riskItens";
        var scope = "Policy";
        var errorMessage = "error in 'Policy.riskItens' with message: Attribute 'riskItens' not found in 'Policy'.";

        Collection<AstMessage> errors = validate(scope, expression);
        assertThat(errors, hasSize(1));
        AstMessage actualMessage = errors.iterator().next();

        assertThat(actualMessage.getMessage(), is(errorMessage));
    }

    @Test
    public void shouldTestNoReferenceFindMessage() {
        var expression = "Policu";
        var scope = "Policy";
        var errorMessage = "error in 'Policu' with message: Reference 'Policu' not found.";

        Collection<AstMessage> errors = validate(scope, expression);
        assertThat(errors, hasSize(1));
        AstMessage actualMessage = errors.iterator().next();

        assertThat(actualMessage.getMessage(), is(errorMessage));
    }

    @Test
    public void shouldTestSyntaxErrors() {
        var expression = "in in";
        var scope = "Policy";

        try {
            validate(scope, expression);
        } catch (AstBuildingException e) {
            String message = e.getCause().getMessage();
            assertThat(message, containsString("'not'"));
            assertThat(message, containsString("'!'"));
            assertThat(message, containsString("'-'"));
            assertThat(message, containsString("for'"));
            assertThat(message, containsString("every"));
            assertThat(message, containsString("if"));
            assertThat(message, containsString("this"));
            assertThat(message, not(containsString("EOF")));
            assertThat(message, not(containsString("NOT")));
            assertThat(message, not(containsString("OP_IN")));
            return;
        }
        fail();
    }

    private Collection<AstMessage> validate(String contextDefinitionName, String expression) {
        Scope scope = scopeBuilder.buildScope(krakenProject.getContextDefinitions().get(contextDefinitionName));
        AstValidatingVisitor visitor = new AstValidatingVisitor();
        visitor.visit(AstBuilder.from(expression, scope).getExpression());
        return visitor.getMessages();
    }

}
