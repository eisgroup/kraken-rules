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
package kraken.el.mvel.translator;

import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.*;

/**
 * @author mulevicius
 */
public class MvelAstTranslator {

    private final ExpressionLanguageConfiguration configuration;

    public MvelAstTranslator(ExpressionLanguageConfiguration configuration) {
        this.configuration = configuration;
    }

    public String translate(Ast ast) {
        if(ast.getAstType() == AstType.PROPERTY || configuration.isStrictTypeMode() && ast.getAstType() == AstType.PATH) {
            return resolvePathExpression(ast.getExpression());
        }

        MvelAstVisitor visitor = new MvelAstVisitor(configuration);
        return visitor.visit(ast.getExpression());
    }

    private String resolvePathExpression(Expression expression) {
        if(expression instanceof ReferenceValue) {
            return resolvePathExpression(((ReferenceValue) expression).getReference());
        }
        return expression.toString();
    }

}
