/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
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
