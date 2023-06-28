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
import java.util.Map;

import kraken.el.EvaluationContext;
import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.coercer.KelCoercer;
import kraken.el.accelerated.ReflectionsCache;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;

/**
 * @author mulevicius
 */
public class InterpretingExpressionEvaluator {

    private final ExpressionLanguageConfiguration configuration;

    public InterpretingExpressionEvaluator(ExpressionLanguageConfiguration configuration) {
        this.configuration = configuration;
    }

    public Object evaluate(Ast ast, EvaluationContext evaluationContext) throws ExpressionEvaluationException {
        var visitor = new InterpretingAstVisitor(ast.getExpression().getScope(), evaluationContext, configuration);
        Value value = visitor.visit(ast.getExpression());
        return value.getValue();
    }

    public void evaluateSetExpression(Object valueToSet, String path, Object object) throws ExpressionEvaluationException {
        try {
            if (!path.contains(".")) {
                set(valueToSet, path, object);
                return;
            }

            String expression = path.substring(0, path.lastIndexOf('.'));
            Ast ast = AstBuilder.from(expression, Scope.dynamic());
            EvaluationContext evaluationContext = new EvaluationContext(object);
            var visitor = new InterpretingAstVisitor(ast.getExpression().getScope(), evaluationContext, configuration);
            Value value = visitor.visit(ast.getExpression());
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
        Object coercedValue = KelCoercer.coerce(valueToSet, setter.getParameters()[0].getParameterizedType());
        setter.invoke(dataObject, coercedValue);
    }

}
