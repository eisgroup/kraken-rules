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
package kraken.benchmarks.evaluation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguage;
import kraken.el.accelerated.PropertyExpressionEvaluator;
import kraken.el.ast.Ast;
import kraken.el.ast.AstType;
import kraken.el.mvel.evaluator.MvelExpressionEvaluator;

/**
 * @author mulevicius
 */
public class AcceleratedExpressionEvaluator implements ExpressionLanguage {

    private MvelExpressionEvaluator evaluator;

    private PropertyExpressionEvaluator propertyExpressionEvaluator;

    public static final Map<String, ExpressionMetadata> expressionCache = new ConcurrentHashMap<>();

    public AcceleratedExpressionEvaluator(MvelExpressionEvaluator evaluator) {
        this.evaluator = evaluator;
        this.propertyExpressionEvaluator = new PropertyExpressionEvaluator();
    }

    @Override
    public Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException {
        ExpressionMetadata e = expressionCache.get(expression);
        if(e.getAstType() == AstType.LITERAL) {
            return e.getCompiledLiteralValue();
        }
        if(e.getAstType() == AstType.PROPERTY) {
            return propertyExpressionEvaluator.evaluate(e.getExpression(), dataObject);
        }
        return evaluator.evaluate(expression, dataObject, vars);
    }

    @Override
    public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
        evaluator.evaluateSetExpression(valueToSet, path, dataObject);
    }

    @Override
    public String translate(Ast ast) {
        throw new UnsupportedOperationException();
    }
}
