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

import static java.util.List.of;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;
import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.test.TestResources;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
@RunWith(Parameterized.class)
public class AstValidatingVisitorTest {

    private static ScopeBuilder scopeBuilder;
    private static KrakenProject krakenProject;

    @BeforeClass
    public static void staticSetup() {
        TestResources testResources = TestResources.create(TestResources.Info.TEST_PRODUCT);
        krakenProject = testResources.getKrakenProject();
        scopeBuilder = new ScopeBuilder(testResources.getKrakenProject());
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<AstValidationTestCase> testCases() {
        return of(
            tcNotValid("a.b.c.d", "AnubisCoverage", 1),

            tcValid("this in Policy.riskItems[0].collCoverages", "COLLCoverage"),
            tcValid("Policy.riskItems[0].collCoverages[0] in Vehicle.collCoverages", "COLLCoverage"),
            tcValid("Vehicle.collCoverages[0] in Vehicle.collCoverages", "COLLCoverage"),
            tcValid("this in Vehicle.collCoverages", "COLLCoverage"),
            tcValid("BillingAddress = addressInfo", "Insured"),
            tcValid("addressInfo = BillingAddress", "Insured"),
            tcValid("addressInfo = AddressInfo", "Insured"),
            tcValid("AddressInfo = addressInfo", "Insured"),
            tcValid("if(true) then AddressInfo else addressInfo", "Insured"),
            tcValid("if(true) then addressInfo else AddressInfo", "Insured"),
            tcValid("if(true) then BillingAddress else addressInfo", "Insured"),
            tcValid("if(true) then addressInfo else BillingAddress", "Insured"),
            tcValid("{AddressInfo} = {addressInfo}", "Insured"),
            tcValid("CreditCardInfo.cardCreditLimitAmount = 1", "Insured"),
            tcValid("1 = CreditCardInfo.cardCreditLimitAmount", "Insured"),
            tcValid("-CreditCardInfo.cardCreditLimitAmount = 1", "Insured"),
            tcValid("-CreditCardInfo.cardCreditLimitAmount", "Insured"),
            tcNotValid("this.", "Insured"),
            tcNotValid("Policy.riskItems[this. = this.limitAmount]", "COLLCoverage"),
            tcNotValid("Policy.", "COLLCoverage"),

            // iteration
            tcValid(""
                + "every c "
                + "in Vehicle.collCoverages "
                + "satisfies c.code == 'a'", "COLLCoverage"),

            tcValid(""
                + "some c "
                + "in Vehicle.collCoverages "
                + "satisfies c.code == 'a'", "COLLCoverage"),

            tcNotValid(""
                + "every c "
                + "in Vehicle.collCoverages "
                + "satisfies c.code", "COLLCoverage"),

            tcNotValid(""
                + "some c "
                + "in Vehicle.collCoverages "
                + "satisfies c.code", "COLLCoverage"),

            tcValid(""
                + "for c "
                + "in Vehicle.collCoverages "
                + "return c.code", "COLLCoverage"),

            tcNotValid(""
                + "for code "
                + "in Vehicle.collCoverages[*].code "
                + "return code", "COLLCoverage"),

            tcNotValid(""
                + "some code "
                + "in Vehicle.collCoverages[*].code "
                + "satisfies code", "COLLCoverage"),

            tcNotValid(""
                + "every code "
                + "in Vehicle.collCoverages[*].code "
                + "satisfies code", "COLLCoverage"),

            tcNotValid(""
                + "for r "
                + "in Policy.riskItems "
                + "return (for r in r.collCoverages return r.limitAmount)", "COLLCoverage"),

            tcValid(""
                + "for r "
                + "in Policy.riskItems "
                + "return (for c in r.collCoverages return c.limitAmount)", "COLLCoverage"),

            tcNotValid("Policy.riskItems[1 > Sum(for i in {1} return " +
                "  i + Sum(for j in {10} return " +
                "    i + j + Sum(for k in {100} return i + j + k) " +
                "  )" +
                ")]", "COLLCoverage"),
            // in
            tcNotValid("Vehicle.collCoverages[0] in Vehicle.collCoverages[0]", "COLLCoverage"),
            tcNotValid("Vehicle.collCoverages in Vehicle.collCoverages[0]", "COLLCoverage"),
            tcNotValid("Vehicle.collCoverages[0] in COLLCoverage", "COLLCoverage"),

            tcNotValid("UnknownFunction('a')", "COLLCoverage"),

            // AnubisCoverage is not child of any context and it is only used as a field type,
            // therefore it cannot be used as a CCR
            tcNotValid("AnubisCoverage.limitAmount", "Vehicle"),

            // equality
            tcNotValid("Policy = addressInfo", "Insured"),
            tcNotValid("Policy == addressInfo", "Insured"),

            // unary
            tcNotValid("-Policy", "Insured"),
            tcNotValid("-addressInfo", "Insured"),

            // math
            tcNotValid("Policy - addressInfo", "Insured"),
            tcNotValid("Policy + addressInfo", "Insured"),
            tcNotValid("Policy / addressInfo", "Insured"),
            tcNotValid("Policy * addressInfo", "Insured"),
            tcNotValid("Policy % addressInfo", "Insured"),
            tcNotValid("Policy ** addressInfo", "Insured"),

            // logical
            tcNotValid("Policy > addressInfo", "Insured"),
            tcNotValid("Policy >= addressInfo", "Insured"),
            tcNotValid("Policy < addressInfo", "Insured"),
            tcNotValid("Policy <= addressInfo", "Insured"),
            tcNotValid("Policy in addressInfo", "Insured"),

            // propositional
            tcNotValid("Policy and addressInfo", "Insured"),
            tcNotValid("Policy && addressInfo", "Insured"),
            tcNotValid("Policy or addressInfo", "Insured"),
            tcNotValid("Policy || addressInfo", "Insured"),

            // dynamic context
            tcValid("context.externalData.limitAmount > cardCreditLimitAmount", "CreditCardInfo"),
            tcValid("context.externalData.limitAmount > limitAmount", "COLLCoverage"),

            tcValid(""
                + "some limit "
                + "in context.externalData.limitAmounts "
                + "satisfies limit > limitAmount && limit == CreditCardInfo.cardCreditLimitAmount", "COLLCoverage"),

            tcValid(""
                + "some limit "
                + "in context.externalData[*].coverages[*].limitAmount "
                + "satisfies limit > limitAmount && limit == CreditCardInfo.cardCreditLimitAmount", "COLLCoverage"),

            tcValid("context.externalData.value - limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value + limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value / limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value * limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value % limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value ** limitAmount", "COLLCoverage"),
            tcValid("limitAmount in context.externalData.values", "COLLCoverage"),
            tcValid("context.externalData.value < limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value <= limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value >= limitAmount", "COLLCoverage"),
            tcValid("context.externalData.value > limitAmount", "COLLCoverage"),
            tcValid("-context.externalData.value", "COLLCoverage"),
            tcValid("context.externalData.value and haveChildren", "Insured"),
            tcValid("context.externalData.value or haveChildren", "Insured"),
            tcValid("!context.externalData.value", "Insured"),

            tcNotValid("context.external.data[limitAmount > 'string']", "COLLCoverage"),
            tcValid("context.external.data[unknownLimitAmount > 'string']", "COLLCoverage"),
            tcValid("context.external.data[this.limitAmount > 'string']", "COLLCoverage"),
            tcValid("context.external.data[this.unknownLimitAmount > 'string']", "COLLCoverage"),
            tcValid("context.external.data[this > 'string']", "COLLCoverage"),

            tcNotValid("Vehicle.refsToDriver[this.name = null]", "Policy"),
            tcValid("Vehicle.refsToDriver[this = null]", "Policy"),

            tcNotValid(""
                + "for limitAmount "
                + "in Policy.riskItems "
                + "return limitAmount", "COLLCoverage"),

            tcValid(""
                + "for ri "
                + "in Policy.riskItems "
                + "return this.limitAmount == limitAmount", "COLLCoverage"),

            tcValid(""
                + "for limitAmount "
                + "in Policy.riskItems "
                + "return limitAmount", "Insured"),

            tcValid("context.external.data[Count("
                + "for limitAmount "
                + "in Policy.riskItems "
                + "return limitAmount"
                + ") > 1]", "Insured"),

            tcValid("context.external.data[Count("
                + "for limitAmount "
                + "in Policy.riskItems "
                + "return unknownLimitAmount = 100"
                + ") > 1]", "Insured"),

            // cast
            tcHasInfoMessages("(COLLCoverage) this", "COLLCoverage"),
            tcHasInfoMessages("(CarCoverage) this", "COLLCoverage"),
            tcHasWarningMessages("(Policy) this", "COLLCoverage"),
            tcNotValid("(Whatever) this", "COLLCoverage"),
            tcNotValid("(<T>) this", "COLLCoverage"),
            tcNotValid("(FullCoverage | CarCoverage) this", "COLLCoverage"),
            tcValid("(FullCoverage[]) this", "COLLCoverage"),
            tcValid("(FullCoverage) this", "COLLCoverage"),

            // variables
            tcNotValid("return true", "AnubisCoverage"),
            tcValid("set a to Vehicle return a.costNew", "COLLCoverage"),
            tcValid("set a to Vehicle set b to Vehicle.costNew return a.costNew < b", "COLLCoverage"),
            tcValid("set a to Vehicle set b to a.costNew return a.costNew < b", "COLLCoverage"),
            tcValid("set a to Vehicle set b to a.collCoverages return Count(b) < 10", "COLLCoverage"),
            tcNotValid("set a to Vehicle set b to a.collCoverages return b.unknownProperty < 10", "COLLCoverage"),
            tcNotValid("set a to Vehicle return", "COLLCoverage"),
            tcNotValid("set a to return a.costNew", "COLLCoverage"),
            // this cannot refer to variable, it refers to COLLCoverage which does not have attribute 'a'
            tcNotValid("set a to COLLCoverage.limitAmount return this.a = a", "COLLCoverage"),
            tcValid("set a to COLLCoverage.limitAmount return this.limitAmount = a", "COLLCoverage"),

            // generic function invocation
            tcValid("Min(this.limitAmount, 10) + 20", "COLLCoverage"),
            tcValid("Min(2022-01-01, 2022-01-01) > 2022-01-01", "COLLCoverage"),
            tcNotValid("Min('10', '10')", "COLLCoverage"),
            tcNotValid("Min(2022-01-01, 2022-01-01) > 10", "COLLCoverage"),
            tcValid("Min(Min(10, 20), Min(10, 20)) + 20", "COLLCoverage"),
            tcNotValid("Min(Min(10, 20), Min(2022-01-01, 2022-01-01)) + 20", "COLLCoverage"),

            // number precision
            tcHasNoMessages("1234567890.123456", "Insured"),
            tcHasNoMessages("1234567890.", "Insured"),
            tcHasNoMessages("1234567890.000000000000000000000", "Insured"),
            tcHasWarningMessages("1234567890.1234567", "Insured")
        );
    }

    private final AstValidationTestCase astValidationTestCase;

    public AstValidatingVisitorTest(AstValidationTestCase astValidationTestCase) {
        this.astValidationTestCase = astValidationTestCase;
    }

    @Test
    public void shouldValidateAst() {
        ContextDefinition contextDefinition = getContextDefinition(astValidationTestCase.getOnContext());
        Collection<AstMessage> errors = validate(contextDefinition, astValidationTestCase.getExpression()).stream()
            .filter(astValidationTestCase.requiredErrors())
            .collect(Collectors.toList());

        assertThat(errors, is(astValidationTestCase.asExpected()));
    }

    private Collection<AstMessage> validate(ContextDefinition contextDefinition, String expression) {
        Scope scope = scopeBuilder.buildScope(contextDefinition);
        return AstBuilder.from(expression, scope).getValidationMessages();
    }

    private ContextDefinition getContextDefinition(String contextName) {
        return krakenProject.getContextDefinitions().get(contextName);
    }

    private static final Predicate<AstMessage> errors = err -> err.getSeverity() == AstMessageSeverity.ERROR;
    private static final Predicate<AstMessage> warnings = err -> err.getSeverity() == AstMessageSeverity.WARNING;
    private static final Predicate<AstMessage> infos = err -> err.getSeverity() == AstMessageSeverity.INFO;

    private static AstValidationTestCase tcValid(String expression, String onContext) {
        return new AstValidationTestCase(expression, onContext, empty(), errors);
    }

    private static AstValidationTestCase tcNotValid(String expression, String onContext) {
        return new AstValidationTestCase(expression, onContext, not(empty()), errors);
    }

    private static AstValidationTestCase tcHasWarningMessages(String expression, String onContext) {
        return new AstValidationTestCase(expression, onContext, not(empty()), warnings);
    }

    private static AstValidationTestCase tcHasInfoMessages(String expression, String onContext) {
        return new AstValidationTestCase(expression, onContext, not(empty()), infos);
    }

    private static AstValidationTestCase tcNotValid(String expression, String onContext, int syntaxErrorCount) {
        return new AstValidationTestCase(expression, onContext, hasSize(syntaxErrorCount), errors);
    }

    private static AstValidationTestCase tcHasNoMessages(String expression, String onContext) {
        return new AstValidationTestCase(expression, onContext, empty(), err -> true);
    }

    public static final class AstValidationTestCase {

        private final String expression;

        private final String onContext;

        private final Matcher<Collection<?>> asExpectedMatcher;

        private final Predicate<AstMessage> requiredErrors;

        public AstValidationTestCase(String expression,
                                     String onContext,
                                     Matcher<Collection<?>> asExpectedMatcher,
                                     Predicate<AstMessage> requiredErrors) {
            this.expression = expression;
            this.onContext = onContext;
            this.asExpectedMatcher = asExpectedMatcher;
            this.requiredErrors = requiredErrors;
        }

        public String getExpression() {
            return expression;
        }

        public String getOnContext() {
            return onContext;
        }

        public Matcher<Collection<?>> asExpected() {
            return asExpectedMatcher;
        }

        public Predicate<AstMessage> requiredErrors() {
            return requiredErrors;
        }

        @Override
        public String toString() {
            return "'" + expression + "' on " + onContext + " should validate with " + asExpectedMatcher + " of errors";
        }
    }
}
