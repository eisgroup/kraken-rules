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

package kraken.runtime.model.expression;

import java.beans.Transient;
import java.io.Serializable;
import java.util.Collection;

import kraken.el.ast.Ast;
import kraken.el.scope.Scope;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class CompiledExpression implements Serializable {

    private static final long serialVersionUID = 7122032254357905188L;

    private final String expressionString;
    private final String originalExpressionString;
    private final ExpressionType expressionType;
    private final Serializable compiledLiteralValue;
    private final String expressionEvaluationType;
    private final Collection<ExpressionVariable> expressionVariables;
    private final transient Ast ast;

    public CompiledExpression(
            String expressionString,
            String originalExpressionString,
            ExpressionType expressionType,
            Serializable compiledLiteralValue,
            String expressionEvaluationType,
            Collection<ExpressionVariable> expressionVariables,
            Ast ast
    ) {
        this.expressionString = expressionString;
        this.originalExpressionString = originalExpressionString;
        this.expressionType = expressionType;
        this.compiledLiteralValue = compiledLiteralValue;
        this.expressionEvaluationType = expressionEvaluationType;
        this.expressionVariables = expressionVariables;
        this.ast = ast;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public String getOriginalExpressionString() {
        return originalExpressionString;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public Serializable getCompiledLiteralValue() {
        return compiledLiteralValue;
    }

    public String getExpressionEvaluationType() {
        return expressionEvaluationType;
    }

    public Collection<ExpressionVariable> getExpressionVariables() {
        return expressionVariables;
    }

    @Transient
    public Ast getAst() {
        return ast;
    }
}
