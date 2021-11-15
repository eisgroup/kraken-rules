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

import java.util.Map;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguage;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.Ast;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;

/**
 * @author mulevicius
 */
public class InterpretingExpressionLanguage implements ExpressionLanguage {

    private InterpretingExpressionEvaluator evaluator;

    public InterpretingExpressionLanguage(ExpressionLanguageConfiguration configuration) {
        this.evaluator = new InterpretingExpressionEvaluator(configuration);
    }

    @Override
    public String translate(Ast ast) {
        return ast.getExpression().toString();
    }

    @Override
    public Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException {
        return evaluator.evaluate(expression, dataObject, vars);
    }

    @Override
    public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
        evaluator.evaluateSetExpression(valueToSet, path, dataObject);
    }
}
