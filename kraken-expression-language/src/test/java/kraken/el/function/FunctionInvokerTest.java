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
package kraken.el.function;

import com.google.common.collect.ImmutableList;
import kraken.el.ExpressionEvaluationException;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author mulevicius
 */
public class FunctionInvokerTest {

    @Test
    public void shouldInvokeFunction() {
        Coverage coverage = new Coverage();
        Object[] parameters = new Object[] {coverage, "limitAmount", 100};
        FunctionInvoker.invoke("set", parameters);

        assertThat(coverage.getLimitAmount(), equalTo(100));
    }

    @Test
    public void shouldInvokeFunctionThatReturnsResults() {
        Object[] parameters = new Object[] {new Coverage(100), "limitAmount"};
        Object result = FunctionInvoker.invoke("get", parameters);

        assertThat(result, equalTo(100));
    }

    @Test(expected = FunctionInvocationException.class)
    public void shouldThrowIfFunctionNameDoesNotExist() {
        FunctionInvoker.invoke("functionThatDoesNotExist", new Object[]{});
    }

    @Test(expected = FunctionInvocationException.class)
    public void shouldThrowIfFunctionParameterCountIsTooLow() {
        Object[] parameters = new Object[] {};
        FunctionInvoker.invoke("context", parameters);
    }

    @Test(expected = FunctionInvocationException.class)
    public void shouldThrowIfFunctionParameterCountIsTooHigh() {
        Object[] parameters = new Object[] {"1", "2", "3"};
        FunctionInvoker.invoke("context", parameters);
    }

    @Test
    public void shouldInvokeFunctionWithParameter() {
        Object[] parameters = new Object[] {"1"};
        FunctionInvoker.invoke("context", parameters);
    }

    @Test
    public void shouldInvokeFunctionOverloadWithMoreParameters() {
        Object[] parameters = new Object[] {"1", "2"};
        FunctionInvoker.invoke("context", parameters);
    }

    @Test(expected = FunctionInvocationException.class)
    public void shouldThrowIfFunctionParameterTypeIncorrect() {
        Object[] parameters = new Object[] {new Coverage(), 100};
        FunctionInvoker.invoke("get", parameters);
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldThrowIfFunctionParameterIsNull() {
        Object[] parameters = new Object[] { null, null};
        FunctionInvoker.invoke("get", parameters);
    }

    @Test
    public void shouldIterateOverFirstCollectionParameter() {
        Coverage coverage1 = new Coverage();
        Coverage coverage2 = new Coverage();
        Object[] parameters = new Object[] {ImmutableList.of(coverage1, coverage2), "limitAmount", 100};
        FunctionInvoker.invokeWithIteration("set", parameters);

        assertThat(coverage1.getLimitAmount(), equalTo(100));
        assertThat(coverage2.getLimitAmount(), equalTo(100));
    }

    @Test
    public void shouldInvokeFunctionByName() {
        Object result = FunctionInvoker.invokeWithIteration("Name", new Object[]{});

        assertThat(result,equalTo("result"));
    }

    @Test
    public void shouldIterateOverFirstCollectionParameterAndReturnResults() {
        Coverage coverage1 = new Coverage(100);
        Coverage coverage2 = new Coverage(200);
        Object[] parameters = new Object[] {ImmutableList.of(coverage1, coverage2), "limitAmount"};
        Collection<Integer> result = (Collection<Integer>) FunctionInvoker.invokeWithIteration("get", parameters);

        assertThat(result, hasSize(2));
        assertThat(result, hasItems(100, 200));
    }

    @Test
    public void shouldPassCollectionAsParameter() {
        Coverage coverage1 = new Coverage();
        Coverage coverage2 = new Coverage();
        Object[] parameters = new Object[] {ImmutableList.of(coverage1, coverage2)};
        Integer result = (Integer) FunctionInvoker.invokeWithIteration("count", parameters);

        assertThat(result, equalTo(2));
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
