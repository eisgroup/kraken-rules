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
package kraken.el.interpreter.evaluator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;

import javax.money.MonetaryAmount;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.accelerated.ReflectionsCache;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.math.Numbers;
import kraken.el.scope.Scope;

/**
 * @author mulevicius
 */
public class InterpretingExpressionEvaluator {

    private final ExpressionLanguageConfiguration configuration;

    public InterpretingExpressionEvaluator(ExpressionLanguageConfiguration configuration) {
        this.configuration = configuration;
    }

    public Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException {
        InterpretingAstVisitor interpretingAstVisitor = new InterpretingAstVisitor(dataObject, vars, configuration);
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        Value value = interpretingAstVisitor.visit(ast.getExpression());
        return value.getValue();
    }

    public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
        try {
            if (!path.contains(".")) {
                set(valueToSet, path, dataObject);
                return;
            }

            String expression = path.substring(0, path.lastIndexOf('.'));
            InterpretingAstVisitor interpretingAstVisitor = new InterpretingAstVisitor(dataObject, Map.of(), configuration);
            Ast ast = AstBuilder.from(expression, Scope.dynamic());
            Value value = interpretingAstVisitor.visit(ast.getExpression());
            String property = path.substring(expression.length() + 1);
            set(valueToSet, property, value.getValue());
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            throw new ExpressionEvaluationException("Error while evaluating set expression: " + path, e);
        }
    }

    private void set(Object valueToSet, String property, Object dataObject) throws InvocationTargetException, IllegalAccessException {
        if(dataObject instanceof Map) {
            ((Map)dataObject).put(property, valueToSet);
            return;
        }
        Method setter = ReflectionsCache.getSettersOrCompute(dataObject.getClass()).get(property);
        if(setter == null) {
            String template = "Cannot set property '%s' in object of type '%s', because setter does not exist in type";
            String message = String.format(template, property, dataObject.getClass());
            throw new IllegalStateException(message);
        }
        Object coercedValue = coerce(valueToSet, setter.getParameterTypes()[0]);
        setter.invoke(dataObject, coercedValue);
    }

    private <T> T coerce(Object value, Class<T> expectedType) {
        if(value == null) {
            return null;
        }
        if(expectedType.isInstance(value)) {
            return expectedType.cast(value);
        }
        if(Number.class.isAssignableFrom(expectedType)) {
            if(value instanceof MonetaryAmount) {
                Number number = ((MonetaryAmount) value).getNumber().numberValue(BigDecimal.class);
                return coerceNumber(number, expectedType);
            }
            if(value instanceof Number) {
                return coerceNumber((Number) value, expectedType);
            }
        }
        throw new IllegalArgumentException("Cannot convert value of type " + value.getClass() + " to type " + expectedType);
    }

    private <T> T coerceNumber(Number value, Class<T> expectedType) {
        if (BigDecimal.class == expectedType) {
            return (T) Numbers.normalized(value);
        }
        if (Long.class == expectedType) {
            return (T) Long.valueOf((value).longValue());
        }
        if (Integer.class == expectedType) {
            return (T) Integer.valueOf((value).intValue());
        }
        throw new IllegalArgumentException("Cannot convert number of type " + value.getClass() + " to type " + expectedType);
    }
}
