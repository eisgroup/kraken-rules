/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.el.functionregistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.TypeProvider;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.functionregistry.KelFunction.Parameter;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;
import kraken.el.scope.Scope;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class FunctionInvokerTest {

    private FunctionInvoker functionInvoker;

    @Before
    public void setUp() throws Exception {
        this.functionInvoker = new FunctionInvoker(
            Map.of(
                new FunctionHeader("multiply", 2),
                new KelFunction(
                    "multiply",
                    List.of(
                        new Parameter("n1"),
                        new Parameter("n2")
                    ),
                    AstBuilder.from("n1 * n2", Scope.dynamic())
                ),
                new FunctionHeader("Fibonacci", 1),
                new KelFunction(
                    "Fibonacci",
                    List.of(
                        new Parameter("n")
                    ),
                    AstBuilder.from("if n=0 or n=1 then n else Fibonacci(n-2) + Fibonacci(n-1)", Scope.dynamic())
                )
            ),
            new InterpretingExpressionEvaluator(new ExpressionLanguageConfiguration(false, true)),
            new TypeProvider() {
                @Override
                public String getTypeOf(Object object) {
                    return Type.ANY.getName();
                }

                @Override
                public Collection<String> getInheritedTypesOf(Object object) {
                    return List.of();
                }
            }
        );
    }

    @Test
    public void shouldInvokeFunction() {
        Coverage coverage = new Coverage();
        Object[] parameters = new Object[] {coverage, "limitAmount", 100};
        functionInvoker.invoke("set", parameters);

        assertThat(coverage.getLimitAmount(), equalTo(100));
    }

    @Test
    public void shouldInvokeFunctionThatReturnsResults() {
        Object[] parameters = new Object[] {new Coverage(100), "limitAmount"};
        Object result = functionInvoker.invoke("get", parameters);

        assertThat(result, equalTo(100));
    }

    @Test
    public void shouldThrowIfFunctionNameDoesNotExist() {
        assertThrows(FunctionInvocationException.class,
                () -> functionInvoker.invoke("functionThatDoesNotExist", new Object[]{}));
    }

    @Test
    public void shouldThrowIfFunctionParameterCountIsTooLow() {
        Object[] parameters = new Object[] {};

        assertThrows(FunctionInvocationException.class,
                () -> functionInvoker.invoke("context", parameters));
    }

    @Test
    public void shouldThrowIfFunctionParameterCountIsTooHigh() {
        Object[] parameters = new Object[] {"1", "2", "3"};

        assertThrows(FunctionInvocationException.class,
                () -> functionInvoker.invoke("context", parameters));
    }

    @Test
    public void shouldInvokeFunctionWithParameter() {
        Object[] parameters = new Object[] {"1"};
        functionInvoker.invoke("context", parameters);
    }

    @Test
    public void shouldInvokeFunctionOverloadWithMoreParameters() {
        Object[] parameters = new Object[] {"1", "2"};
        functionInvoker.invoke("context", parameters);
    }

    @Test
    public void shouldThrowIfFunctionParameterTypeIncorrect() {
        Object[] parameters = new Object[] {new Coverage(), 100};
        assertThrows(ExpressionEvaluationException.class,
                () -> functionInvoker.invoke("get", parameters));
    }

    @Test
    public void shouldThrowIfFunctionParameterIsNull() {
        Object[] parameters = new Object[] { null, null};

        assertThrows(ExpressionEvaluationException.class,
                () -> functionInvoker.invoke("get", parameters));
    }

    @Test
    public void shouldIterateOverFirstCollectionParameter() {
        Coverage coverage1 = new Coverage();
        Coverage coverage2 = new Coverage();
        Object[] parameters = new Object[] { List.of(coverage1, coverage2), "limitAmount", 100};
        functionInvoker.invokeWithIteration("set", parameters);

        assertThat(coverage1.getLimitAmount(), equalTo(100));
        assertThat(coverage2.getLimitAmount(), equalTo(100));
    }

    @Test
    public void shouldInvokeFunctionByName() {
        Object result = functionInvoker.invokeWithIteration("Name", new Object[]{});

        assertThat(result, equalTo("result"));
    }

    @Test
    public void shouldIterateOverFirstCollectionParameterAndReturnResults() {
        Coverage coverage1 = new Coverage(100);
        Coverage coverage2 = new Coverage(200);
        Object[] parameters = new Object[] { List.of(coverage1, coverage2), "limitAmount"};
        Collection<Integer> result = (Collection<Integer>) functionInvoker.invokeWithIteration("get", parameters);

        assertThat(result, hasSize(2));
        assertThat(result, hasItems(100, 200));
    }

    @Test
    public void shouldPassCollectionAsParameter() {
        Coverage coverage1 = new Coverage();
        Coverage coverage2 = new Coverage();
        Object[] parameters = new Object[] { List.of(coverage1, coverage2)};
        Integer result = (Integer) functionInvoker.invokeWithIteration("count", parameters);

        assertThat(result, equalTo(2));
    }

    @Test
    public void shouldInvokeKelFunction() {
        BigDecimal result = (BigDecimal) functionInvoker.invoke("multiply", new Object[]{2, 3});

        assertThat(result, equalTo(new BigDecimal("6")));
    }

    @Test
    public void shouldInvokeKelFunctionRecursively() {
        BigDecimal result = (BigDecimal) functionInvoker.invoke("Fibonacci", new Object[]{10});

        assertThat(result, equalTo(new BigDecimal("55")));
    }

    public static class Coverage {

        private Integer limitAmount;

        public Coverage() {
        }

        public Coverage(Integer limitAmount) {
            this.limitAmount = limitAmount;
        }

        public Integer getLimitAmount() {
            return limitAmount;
        }

        public void setLimitAmount(Integer limitAmount) {
            this.limitAmount = limitAmount;
        }
    }
}
