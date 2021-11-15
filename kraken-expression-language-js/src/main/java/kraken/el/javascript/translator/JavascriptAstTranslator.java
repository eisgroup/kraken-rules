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
package kraken.el.javascript.translator;

import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.*;

/**
 * @author mulevicius
 */
public class JavascriptAstTranslator {

    private final ExpressionLanguageConfiguration configuration;

    public JavascriptAstTranslator(ExpressionLanguageConfiguration configuration) {
        this.configuration = configuration;
    }

    public String translate(Ast ast) {
        if(!configuration.isStrictTypeMode()) {
            throw new UnsupportedOperationException("Non Strict mode is not supported for Javascript translation");
        }
        if(ast.getAstType() == AstType.PROPERTY || ast.getAstType() == AstType.PATH) {
            return resolvePathExpression(ast.getExpression());
        }
        JavascriptAstVisitor visitor = new JavascriptAstVisitor();
        return visitor.visit(ast.getExpression());
    }

    private String resolvePathExpression(Expression expression) {
        if(expression instanceof ReferenceValue) {
            return resolvePathExpression(((ReferenceValue) expression).getReference());
        }
        return expression.toString();
    }
}
