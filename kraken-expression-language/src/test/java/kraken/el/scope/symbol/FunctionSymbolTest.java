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
package kraken.el.scope.symbol;

import static kraken.el.scope.type.Type.ANY;
import static kraken.el.scope.type.Type.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.hamcrest.core.IsEqual;
import org.junit.Test;

import kraken.el.ast.Expression;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.GenericType;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class FunctionSymbolTest {

    @Test
    public void shouldBeEqualWhenSymbolsAreTheSame() {
        var symbol1 = new FunctionSymbol(
            "FunctionName",
            new GenericType("T"),
            List.of(
                new FunctionParameter(0, ArrayType.of(new GenericType("T"))),
                new FunctionParameter(1, STRING)
            )
        );

        var symbol2 = new FunctionSymbol(
            "FunctionName",
            new GenericType("T"),
            List.of(
                new FunctionParameter(0, ArrayType.of(new GenericType("T"))),
                new FunctionParameter(1, STRING)
            )
        );

        assertThat(symbol1.equals(symbol2), is(true));
    }

    @Test
    public void shouldNotBeEqualWhenSymbolsDifferInParameterSequence() {
        var symbol1 = new FunctionSymbol(
            "FunctionName",
            new GenericType("T"),
            List.of(
                new FunctionParameter(0, ArrayType.of(new GenericType("T"))),
                new FunctionParameter(1, STRING)
            )
        );

        var symbol2 = new FunctionSymbol(
            "FunctionName",
            new GenericType("T"),
            List.of(
                new FunctionParameter(1, STRING),
                new FunctionParameter(0, ArrayType.of(new GenericType("T")))
            )
        );

        assertThat(symbol1.equals(symbol2), is(false));
    }

    @Test
    public void shouldNotBeEqualWhenSymbolsDifferInFunctionName() {
        var symbol1 = new FunctionSymbol(
            "FunctionName",
            new GenericType("T"),
            List.of(
                new FunctionParameter(0, ArrayType.of(new GenericType("T"))),
                new FunctionParameter(1, STRING)
            )
        );

        var symbol2 = new FunctionSymbol(
            "FunctionName2",
            new GenericType("T"),
            List.of(
                new FunctionParameter(1, STRING),
                new FunctionParameter(0, ArrayType.of(new GenericType("T")))
            )
        );

        assertThat(symbol1.equals(symbol2), is(false));
    }

    @Test
    public void shouldNotBeEqualWhenSymbolsDifferInGenericBound() {
        var symbol1 = new FunctionSymbol(
            "FunctionName",
            new GenericType("T"),
            List.of(
                new FunctionParameter(0, ArrayType.of(new GenericType("T"))),
                new FunctionParameter(1, STRING)
            )
        );

        var symbol2 = new FunctionSymbol(
            "FunctionName2",
            new GenericType("T", Type.NUMBER),
            List.of(
                new FunctionParameter(1, STRING),
                new FunctionParameter(0, ArrayType.of(new GenericType("T")))
            )
        );

        assertThat(symbol1.equals(symbol2), is(false));
    }

    @Test
    public void shouldResolveGenericRewrite() {
        var t = new GenericType("T");
        var symbol = new FunctionSymbol("F", t, List.of(new FunctionParameter(0, t)));
        var rewrites = symbol.resolveGenericRewrites(List.of(expression(STRING)));

        assertThat(rewrites.get(t), equalTo(STRING));
    }

    @Test
    public void shouldResolveGenericRewriteFromCollection() {
        var t = new GenericType("T");
        var symbol = new FunctionSymbol("F", t, List.of(new FunctionParameter(0, ArrayType.of(t))));
        var rewrites = symbol.resolveGenericRewrites(List.of(expression(ArrayType.of(STRING))));

        assertThat(rewrites.get(t), equalTo(STRING));
    }

    @Test
    public void shouldResolveGenericRewriteToCollection() {
        var t = new GenericType("T");
        var symbol = new FunctionSymbol("F", t, List.of(new FunctionParameter(0, t)));
        var rewrites = symbol.resolveGenericRewrites(List.of(expression(ArrayType.of(STRING))));

        assertThat(rewrites.get(t), equalTo(ArrayType.of(STRING)));
    }

    @Test
    public void shouldResolveGenericRewriteToNestedCollection() {
        var t = new GenericType("T");
        var symbol = new FunctionSymbol("F", t, List.of(new FunctionParameter(0, ArrayType.of(t))));
        var rewrites = symbol.resolveGenericRewrites(List.of(expression(ArrayType.of(ArrayType.of(STRING)))));

        assertThat(rewrites.get(t), equalTo(ArrayType.of(STRING)));
    }

    @Test
    public void shouldResolveGenericRewriteFromDynamic() {
        var t = new GenericType("T");
        var symbol = new FunctionSymbol("F", t, List.of(new FunctionParameter(0, ArrayType.of(t))));
        var rewrites = symbol.resolveGenericRewrites(List.of(expression(ANY)));

        assertThat(rewrites.get(t), equalTo(ANY));
    }

    private Expression expression(Type type) {
        Expression expression = mock(Expression.class);
        when(expression.getEvaluationType()).thenReturn(type);
        return expression;
    }
}
