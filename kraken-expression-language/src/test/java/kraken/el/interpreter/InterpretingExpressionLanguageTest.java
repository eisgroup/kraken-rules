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
package kraken.el.interpreter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import kraken.el.*;
import kraken.el.InvocationContextHolder.InvocationContext;
import kraken.el.ast.Ast;
import kraken.el.ast.Identifier;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.Literals;
import kraken.el.ast.token.Token;
import kraken.el.TypeProvider;
import kraken.el.FunctionContextHolder;
import kraken.el.FunctionContextHolder.FunctionContext;
import kraken.el.functionregistry.FunctionInvoker;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;
import kraken.el.scope.Scope;
import kraken.el.scope.type.Type;

import org.hamcrest.MatcherAssert;
import org.javamoney.moneta.Money;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThrows;

/**
 * @author mulevicius
 */
public class InterpretingExpressionLanguageTest {

    private ExpressionLanguage expressionLanguage;

    private ExpressionLanguage nonStrictExpressionLanguage;

    @Before
    public void setUp() throws Exception {
        this.expressionLanguage = new InterpretingExpressionLanguage(
                ExpressionLanguageConfiguration.builder()
                        .strictTypeMode()
                        .build()
        );
        this.nonStrictExpressionLanguage = new InterpretingExpressionLanguage(
                ExpressionLanguageConfiguration.builder()
                        .allowAutomaticIterationWhenInvokingFunctions()
                        .build()
        );
    }

    @Test
    public void shouldEvaluateLiteral() {
        assertThat(eval("null"), nullValue());
        assertThat(eval("true"), is(true));
        assertThat(eval("false"), is(false));
        assertThat(eval("10"), equalTo(decimal(10)));
        assertThat(eval("10.00"), equalTo(decimal("10.00")));
        assertThat(eval("'string'"), equalTo("string"));
        assertThat(eval("2020-01-01T10:00:00Z"), equalTo(toLocalDateTime("2020-01-01T10:00:00Z")));
    }

    @Test
    public void shouldEvaluateMath() {
        assertThat(eval("-2"), equalTo(decimal(-2)));
        assertThat(eval("2+22.22"), equalTo(decimal("24.22")));
        assertThat(eval("-2-2"), equalTo(decimal(-4)));
        assertThat(eval("-(2-1)"), equalTo(decimal(-1)));
        assertThat(eval("2*2"), equalTo(decimal(4)));
        assertThat(eval("1/3"), equalTo(decimal("0.3333333333333333")));
        assertThat(eval("2**2"), equalTo(decimal(4)));
        assertThat(eval("5%2"), equalTo(decimal(1)));

        assertThat(eval("2+2*2-2/2**2"), equalTo(decimal("5.5")));
        assertThat(eval("(2+2)*(2-2/2)**2"), equalTo(decimal("4")));
    }

    @Test
    public void shouldEvaluateLogic() {
        assertThat(eval("true && true"), equalTo(true));
        assertThat(eval("true && false"), equalTo(false));
        assertThat(eval("false || true"), equalTo(true));
        assertThat(eval("false || false"), equalTo(false));
        assertThat(eval("true || smth"), equalTo(true));
        assertThat(eval("false && smth"), equalTo(false));
        assertThat(eval("!true"), equalTo(false));
    }

    @Test
    public void shouldEvaluateComparison() {
        assertThat(eval("2 > 2"), equalTo(false));
        assertThat(eval("2 >= 2"), equalTo(true));
        assertThat(eval("2 < 2"), equalTo(false));
        assertThat(eval("2 <= 2"), equalTo(true));
        assertThat(eval("2020-01-01T10:00:00Z > 2020-01-01T10:00:00Z"), equalTo(false));
        assertThat(eval("2020-01-01T10:00:00Z >= 2020-01-01T10:00:00Z"), equalTo(true));
        assertThat(eval("2020-01-01T10:00:00Z < 2020-01-01T10:00:00Z"), equalTo(false));
        assertThat(eval("2020-01-01T10:00:00Z <= 2020-01-01T10:00:00Z"), equalTo(true));
    }

    @Test
    public void shouldEvaluateEquality() {
        assertThat(eval("2 = 2"), equalTo(true));
        assertThat(eval("2.00 = 2.00"), equalTo(true));
        assertThat(eval("2 = 2.00"), equalTo(true));
        assertThat(eval("'2' = '2'"), equalTo(true));
        assertThat(eval("null = null"), equalTo(true));
        assertThat(eval("true = true"), equalTo(true));
        assertThat(eval("false = false"), equalTo(true));
        assertThat(eval("2020-01-01T10:00:00Z = 2020-01-01T10:00:00Z"), equalTo(true));

        assertThat(eval("2 != 2"), equalTo(false));
        assertThat(eval("2.00 != 2.00"), equalTo(false));
        assertThat(eval("2 != 2.00"), equalTo(false));
        assertThat(eval("'2' != '2'"), equalTo(false));
        assertThat(eval("null != null"), equalTo(false));
        assertThat(eval("true != true"), equalTo(false));
        assertThat(eval("false != false"), equalTo(false));
        assertThat(eval("2020-01-01T10:00:00Z != 2020-01-01T10:00:00Z"), equalTo(false));

        assertThat(eval("2 != 3"), equalTo(true));
        assertThat(eval("2.00 != 2.01"), equalTo(true));
        assertThat(eval("2 != 2.01"), equalTo(true));
        assertThat(eval("'2' != '21'"), equalTo(true));
        assertThat(eval("null != false"), equalTo(true));
        assertThat(eval("true != false"), equalTo(true));
        assertThat(eval("false != true"), equalTo(true));
        assertThat(eval("2020-01-01T10:00:00Z != 2020-01-01T10:00:01Z"), equalTo(true));
    }

    @Test
    public void shouldEvaluateMatchesRegExp() {
        assertThat(eval("'123' matches '^[0-9]*$'"), equalTo(true));
        assertThat(eval("'text' matches '^[0-9]*$'"), equalTo(false));
    }

    @Test
    public void shouldEvaluateInCollection() {
        assertThat(eval("2 in {1, 2, 3}"), equalTo(true));
        assertThat(eval("2.00 in {1, 2, 3}"), equalTo(true));
        assertThat(eval("'2' in {1, 2, 3}"), equalTo(false));
        assertThat(eval("null in { null }"), equalTo(true));
        assertThat(eval("null in { }"), equalTo(false));
    }

    @Test
    public void shouldEvaluateIf() {
        assertThat(eval("if(true) then true"), equalTo(true));
        assertThat(eval("if(false) then true else false"), equalTo(false));
    }

    @Test
    public void shouldEvaluateInlineArray() {
        assertThat(
                (Collection<BigDecimal>) eval("{1, 2, 3}"),
                hasItems(decimal(1), decimal(2), decimal(3))
        );
        assertThat((Collection<BigDecimal>) eval("{}"), empty());
    }

    @Test
    public void shouldEvaluateInlineMap() {
        assertThat(
                (Map<String, Object>) eval("{'key' : 'value'}"),
                hasEntry("key", "value")
        );
    }

    @Test
    public void shouldEvaluateForSome() {
        assertThat(eval("some n in {1, 2, 3} satisfies n = 2"), equalTo(true));
        assertThat(eval("some n in {1, 2, 3} satisfies n = 4"), equalTo(false));
        assertThat(eval("some n in {} satisfies n = 2"), equalTo(false));
        assertThat(eval("some n in {null} satisfies n = 2"), equalTo(false));
        assertThat(eval("some n in null satisfies n = 2"), equalTo(false));
    }

    @Test
    public void shouldEvaluateForEvery() {
        assertThat(eval("every n in {1, 2, 3} satisfies n > 0"), equalTo(true));
        assertThat(eval("every n in {1, 2, 3} satisfies n > 1"), equalTo(false));
        assertThat(eval("every n in {} satisfies n = 2"), equalTo(true));
        assertThat(eval("every n in {null} satisfies n = 2"), equalTo(false));
        assertThat(eval("every n in null satisfies n = 2"), equalTo(true));
    }

    @Test
    public void shouldEvaluateForEach() {
        assertThat(
                (Collection<BigDecimal>) eval("for n in {1, 2, 3} return n+1"),
                hasItems(decimal(2), decimal(3), decimal(4))
        );

        assertThat((Collection<?>) eval("for n in {} return n"), empty());
        assertThat((Collection<?>) eval("for n in null return n"), empty());
        assertThat(((Collection<?>) eval("for n in {null} return n")).iterator().next(), nullValue());
    }

    @Test
    public void shouldEvaluatePath() {
        assertThat(eval("property", null), nullValue());
        assertThat(eval("property", Map.of("property", "value")), equalTo("value"));
        MatcherAssert.assertThat(eval("money", Map.of("money", Money.of(1, "USD"))), equalTo(Money.of(1, "USD")));
        assertThat(eval("property.nested", Map.of("property", Map.of("nested", "value"))), equalTo("value"));

        assertThat(
                eval(
                        "array.property",
                        Map.of("array", List.of(Map.of("property", "value1"), Map.of("property", "value2")))
                ),
                equalTo(List.of("value1", "value2"))
        );
        assertThat(
                eval(
                        "array.property",
                        Map.of("array", List.of(Map.of("property", List.of("value10", "value11")), Map.of("property", List.of("value20", "value21"))))
                ),
                equalTo(List.of("value10", "value11", "value20", "value21"))
        );
    }

    @Test
    public void shouldEvaluateNullSafePath() {
        assertThat(eval("property?.nested"), nullValue());
        assertThat(eval("array[*]?.property"), nullValue());
        assertThat(eval("context?.external?.property"), nullValue());
        assertThat(eval("this?.property"), nullValue());
        assertThat(
            eval(
                "array[this?.nested?.property = null][0]",
                Map.of("array", List.of("value"))
            ),
            equalTo("value")
        );
    }

    @Test
    public void shouldEvaluatePathInContextVars() {
        assertThat(
            eval(
                "context.external.property",
                new Object(),
                Map.of("context", Map.of("external", Map.of("propertyOther", "value")))
            ),
            nullValue()
        );
    }

    @Test
    public void shouldEvaluateRewrittenIdentifier() {
        Ast ast = new Ast(new Identifier("property", "nested.property", Scope.dynamic(), Type.ANY, new Token(0, 8, "property")));
        Expression e = new Expression("nested.property", ast);
        var evaluationContext = createEvaluationContext(e, Map.of("nested", Map.of("property", "value")));
        Object result = expressionLanguage.evaluate(e, evaluationContext);

        assertThat(result, equalTo("value"));
    }

    @Test
    public void shouldEvaluateByCoercingMoney() {
        assertThat(eval("money + 10", Map.of("money", Money.of(10, "USD"))), equalTo(decimal(20)));
    }

    @Test
    public void shouldEvaluateThis() {
        assertThat(eval("this", "value"), equalTo("value"));
        assertThat(eval("this", null), nullValue());
        assertThat(eval("this.property", Map.of("property", "value")), equalTo("value"));
    }

    @Test
    public void shouldEvaluateAccessByIndex() {
        assertThat(eval("array[0]", Map.of("array", List.of("value"))), equalTo("value"));
        assertThat(eval("array[0].property", Map.of("array", List.of(Map.of("property", "value")))), equalTo("value"));
        assertThat(eval("array[1 - index].property", Map.of("array", List.of(Map.of("property", "value")), "index", 1)), equalTo("value"));
    }

    @Test
    public void shouldEvaluateFilter() {
        assertThat(eval("array?[true]", Map.of("array", List.of("value"))), equalTo(List.of("value")));
        assertThat(eval("array[this > 2]", Map.of("array", List.of(1, 2, 3))), equalTo(List.of(3)));
        assertThat(eval("array[n > 2].n", Map.of("array", List.of(Map.of("n", 3)))), equalTo(List.of(3)));
        assertThat(eval("(array[n > 2].n)[0]", Map.of("array", List.of(Map.of("n", 3)))), equalTo(3));
    }

    @Test
    public void shouldEvaluateThisFromParentContext() {
        assertThat(
                eval(
                        "array[every n in this.list satisfies n <= 3  && Count(this.list)=3][0].list[0]",
                        Map.of("array", List.of(Map.of("list", List.of(1, 2, 3))))
                ),
                equalTo(1)
        );
    }

    @Test
    public void shouldThrowIfNullInComparison() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("null > 10"));
    }

    @Test
    public void shouldThrowIfNullInsteadOfNumber() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("null + 10"));
    }

    @Test
    public void shouldThrowIfMathWithNotNumber() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("10 + '10'"));
    }

    @Test
    public void shouldThrowIfLogicWithNotBoolean() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("true && 'false'"));
    }

    @Test
    public void shouldThrowIfConditionWithNotBoolean() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("if('true') then true"));
    }

    @Test
    public void shouldThrowIfMatchesRegExpWithNotString() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("123 matches '^[0-9]*$'"));
    }

    @Test
    public void shouldThrowIfAccessByIndexNotNumber() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("array['0']", Map.of("array", List.of("value"))));
    }

    @Test
    public void shouldThrowIfObjectIsNull() {
        assertThrows(ExpressionEvaluationException.class, () -> eval("this.property", null));
    }

    @Test
    public void shouldEvaluateNullIfObjectIsMapWithoutKey() {
        assertThat(evalNonStrict("map.key", Map.of("map", new HashMap<>())), nullValue());
    }

    @Test
    public void shouldEvaluateGlobalProperty() {
        assertThat(eval("prop", null, Map.of("prop", "value")), equalTo("value"));
    }

    @Test
    public void shouldPreferLocalObjectToGlobalProperty() {
        assertThat(eval("prop", Map.of("prop", "value1"), Map.of("prop", "value2")), equalTo("value1"));
    }

    @Test
    public void shouldEvaluateFunction() {
        assertThat(eval("Sign(1)"), equalTo(decimal(1)));
        assertThat(eval("array[Sign(1)]", Map.of("array", List.of(1, 2, 3))), equalTo(2));
    }

    @Test
    public void shouldEvaluateInstanceOf() {
        assertThat(eval("null instanceof Object", new Object()), equalTo(false));
        assertThat(eval("this instanceof Object", new Object()), equalTo(true));
        assertThat(eval("null typeof Object", new Object()), equalTo(false));
        assertThat(eval("this typeof Object", new Object()), equalTo(true));
    }

    @Test
    public void shouldEvaluateNonStrict() {
        assertThat(evalNonStrict("null > 10"), equalTo(false));
        assertThat(evalNonStrict("null >= 10"), equalTo(false));
        assertThat(evalNonStrict("null < 10"), equalTo(false));
        assertThat(evalNonStrict("null <= 10"), equalTo(false));
        assertThat(evalNonStrict("null && true"), equalTo(false));
        assertThat(evalNonStrict("null || true"), equalTo(true));
        assertThat(evalNonStrict("!null"), equalTo(true));
        assertThat(evalNonStrict("!!null"), equalTo(false));
        assertThat(evalNonStrict("if(null) then true else false"), equalTo(false));
        assertThat(evalNonStrict("null matches '^[0-9]*$'"), equalTo(false));
        assertThat(evalNonStrict("this.property", null), nullValue());
    }

    @Test
    public void shouldEvaluateAutomaticFunctionIteration() {
        assertThat(
                evalNonStrict("Sign(this)", List.of(1, 2, 3)),
                equalTo(List.of(decimal(1), decimal(1), decimal(1)))
        );
        assertThat(
                evalNonStrict("Sign(list)", Map.of("list", List.of(1, 2, 3))),
                equalTo(List.of(decimal(1), decimal(1), decimal(1)))
        );
    }

    @Test
    public void shouldEvaluateComplexExpressionOnDomainObject() {
        String expression =
                "for riskItem in Policy.riskItems[Count(coverages[limitAmount >= 2]) > 1] return " +
                        "if" +
                        "  every coverage in riskItem.coverages satisfies coverage.limitAmount < 4 " +
                        "then" +
                        "  Sum(riskItem.coverages[*].limitAmount) " +
                        "else" +
                        "  Max(for coverage in riskItem.coverages return coverage.limitAmount * 10)";

        Policy policy = new Policy(List.of(
                new RiskItem(
                        List.of(new Coverage(decimal("1.0")))
                ),
                new RiskItem(
                        List.of(
                                new Coverage(decimal("1.0")),
                                new Coverage(decimal("2.0")),
                                new Coverage(decimal("3.0"))
                        )
                ),
                new RiskItem(
                        List.of(
                                new Coverage(decimal("1.5")),
                                new Coverage(decimal("2.5")),
                                new Coverage(decimal("3.5")),
                                new Coverage(decimal("4.5"))
                        )
                )
        ));

        assertThat(
                eval(expression, null, Map.of("Policy", policy)),
                equalTo(List.of(decimal("6.0"), decimal("45.0")))
        );
    }

    @Test
    public void shouldSetProperty() {
        Coverage coverage = new Coverage(null);
        expressionLanguage.evaluateSetExpression(decimal("1.00"), "limitAmount", coverage);
        assertThat(coverage.getLimitAmount(), equalTo(decimal("1.00")));
    }

    @Test
    public void shouldSetPropertyAndCoerceNumber() {
        Coverage coverage = new Coverage(null);
        expressionLanguage.evaluateSetExpression(1, "limitAmount", coverage);
        assertThat(coverage.getLimitAmount(), equalTo(decimal("1")));
    }

    @Test
    public void shouldSetPropertyAndCoerceMoneyToNumber() {
        Coverage coverage = new Coverage(null);
        expressionLanguage.evaluateSetExpression(Money.of(1, "USD"), "limitAmount", coverage);
        assertThat(coverage.getLimitAmount(), equalTo(decimal("1")));
    }

    @Test
    public void shouldSetToMap() {
        Map<String, Object> map = new HashMap<>();
        expressionLanguage.evaluateSetExpression(decimal("1.00"), "limitAmount", map);
        assertThat(map.get("limitAmount"), equalTo(decimal("1.00")));
    }

    @Test
    public void shouldSetPath() {
        Coverage coverage =  new Coverage(null);
        Map<String, Object> map = new HashMap<>();
        map.put("coverage", coverage);
        expressionLanguage.evaluateSetExpression(decimal("1.00"), "coverage.limitAmount", map);
        assertThat(coverage.getLimitAmount(), equalTo(decimal("1.00")));
    }

    @Test
    public void shouldSetNullToProperty() {
        Coverage coverage = new Coverage(null);
        expressionLanguage.evaluateSetExpression(null, "limitAmount", coverage);
        assertThat(coverage.getLimitAmount(), nullValue());
    }

    @Test
    public void shouldEvaluateVariables() {
        String expression =
            ""
                + "set coverages to Policy.riskItems.coverages "
                + "set limitAmount to coverages[0].limitAmount "
                // coverages is dynamic, so a clash is possible; this should refer to filter element,
                // but regular name should refer to variable
                + "return Count(coverages[this.limitAmount = limitAmount])";

        Policy policy = new Policy(List.of(
            new RiskItem(
                List.of(
                    new Coverage(decimal("1"))
                )
            ),
            new RiskItem(
                List.of(
                    new Coverage(decimal("2"))
                )
            )
        ));

        assertThat(eval(expression, null, Map.of("Policy", policy)), equalTo(1));
    }

    @Test
    public void shouldEvaluateVariablesInIteration() {
        String expression =
            "some c in Policy.riskItems.coverages satisfies "
                + "set coverages to Policy.riskItems.coverages "
                + "set limitAmount to c.limitAmount "
                + "return every innerC in coverages satisfies"
                + "  set innerLimit to innerC.limitAmount"
                + "  return limitAmount = innerLimit";

        Policy policy = new Policy(List.of(
            new RiskItem(
                List.of(
                    new Coverage(decimal("1"))
                )
            ),
            new RiskItem(
                List.of(
                    new Coverage(decimal("1"))
                )
            )
        ));

        assertThat(eval(expression, null, Map.of("Policy", policy)), equalTo(true));
    }

    @Test
    public void shouldEvaluateVariablesInIf() {
        String expression =
            "if true then "
                + "set coverages to Policy.riskItems.coverages "
                + "set limitAmount to coverages[0].limitAmount "
                + "return limitAmount = 1";

        Policy policy = new Policy(List.of(
            new RiskItem(
                List.of(
                    new Coverage(decimal("1"))
                )
            )
        ));

        assertThat(eval(expression, null, Map.of("Policy", policy)), equalTo(true));
    }

    public static class Policy {

        private Collection<RiskItem> riskItems;

        public Policy(Collection<RiskItem> riskItems) {
            this.riskItems = riskItems;
        }

        public Collection<RiskItem> getRiskItems() {
            return riskItems;
        }

        public void setRiskItems(Collection<RiskItem> riskItems) {
            this.riskItems = riskItems;
        }
    }

    public static class RiskItem {

        private Collection<Coverage> coverages;

        public RiskItem(Collection<Coverage> coverages) {
            this.coverages = coverages;
        }

        public Collection<Coverage> getCoverages() {
            return coverages;
        }

        public void setCoverages(Collection<Coverage> coverages) {
            this.coverages = coverages;
        }

    }

    public static class Coverage {

        private BigDecimal limitAmount;

        public Coverage(BigDecimal limitAmount) {
            this.limitAmount = limitAmount;
        }

        public BigDecimal getLimitAmount() {
            return limitAmount;
        }

        public void setLimitAmount(BigDecimal limitAmount) {
            this.limitAmount = limitAmount;
        }

    }

    private BigDecimal decimal(Number number) {
        return new BigDecimal(number.toString());
    }

    private BigDecimal decimal(String number) {
        return new BigDecimal(number);
    }

    private static LocalDateTime toLocalDateTime(String dateTime) {
        return Literals.getDateTime(dateTime);
    }

    private Object eval(String expression) {
        return eval(expression, null, Map.of());
    }

    private Object eval(String expression, Object object) {
        return eval(expression, object, Map.of());
    }

    private Object eval(String expression, Object object, Map<String, Object> vars) {
        EvaluationContext evaluationContext = createEvaluationContext(object, vars);
        mockInvocationContext(evaluationContext);
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        Expression e = new Expression(expression, ast);
        return expressionLanguage.evaluate(e, evaluationContext);
    }

    private Object evalNonStrict(String expression) {
        return evalNonStrict(expression, null, Map.of());
    }

    private Object evalNonStrict(String expression, Object object) {
        return evalNonStrict(expression, object, Map.of());
    }

    private Object evalNonStrict(String expression, Object object, Map<String, Object> vars) {
        EvaluationContext evaluationContext = createEvaluationContext(object, vars);
        mockInvocationContext(evaluationContext);
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        Expression e = new Expression(expression, ast);
        return nonStrictExpressionLanguage.evaluate(e, evaluationContext);
    }

    private EvaluationContext createEvaluationContext(Object object, Map<String, Object> vars) {
        Map<String, Object> variables = new HashMap<>(vars);
        TypeProvider typeProvider = new TypeProvider() {
            @Override
            public String getTypeOf(Object object) {
                return getTypeOf(object.getClass());
            }

            @Override
            public Collection<String> getInheritedTypesOf(Object object) {
                return getInheritedTypesOf(object.getClass());
            }

            private String getTypeOf(Class<?> type) {
                return type.getSimpleName();
            }

            private Collection<String> getInheritedTypesOf(Class<?> type) {
                Set<String> types = Arrays.stream(type.getInterfaces())
                    .map(this::getInheritedTypesOf)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());

                types.add(getTypeOf(type));

                return types;
            }
        };
        FunctionInvoker functionInvoker = new FunctionInvoker(
            Map.of(),
            new InterpretingExpressionEvaluator(new ExpressionLanguageConfiguration(false, true)),
            typeProvider
        );
        return new EvaluationContext(object, variables, typeProvider, functionInvoker, ZoneId.systemDefault());
    }

    private void mockInvocationContext(EvaluationContext evaluationContext) {
        InvocationContextHolder.setInvocationContext(new InvocationContext(evaluationContext));
        FunctionContextHolder.setFunctionContext(new FunctionContext(evaluationContext.getZoneId()));
    }
}
