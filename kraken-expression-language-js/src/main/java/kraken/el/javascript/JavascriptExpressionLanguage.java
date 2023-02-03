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
package kraken.el.javascript;

import kraken.el.EvaluationContext;
import kraken.el.Expression;
import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguage;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.Ast;
import kraken.el.javascript.translator.JavascriptAstTranslator;

/**
 * @author mulevicius
 */
public class JavascriptExpressionLanguage implements ExpressionLanguage {

    private JavascriptAstTranslator translator;

    public JavascriptExpressionLanguage(ExpressionLanguageConfiguration configuration) {
        this.translator = new JavascriptAstTranslator(configuration);
    }

    @Override
    public Expression translate(Ast ast) {
        return new Expression(translator.translate(ast), ast);
    }

    @Override
    public Object evaluate(Expression expression, EvaluationContext evaluationContext) throws ExpressionEvaluationException {
        throw new UnsupportedOperationException("Javascript evaluation in Java is not supported");
    }

    @Override
    public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
        throw new UnsupportedOperationException("Javascript evaluation in Java is not supported");
    }
}
