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

import kraken.el.ast.AstType;

/**
 * @author mulevicius
 */
public class ExpressionMetadata {

    private String expression;

    private AstType astType;

    private Object compiledLiteralValue;

    public ExpressionMetadata(String expression, AstType astType, Object compiledLiteralValue) {
        this.expression = expression;
        this.astType = astType;
        this.compiledLiteralValue = compiledLiteralValue;
    }

    public String getExpression() {
        return expression;
    }

    public AstType getAstType() {
        return astType;
    }

    public Object getCompiledLiteralValue() {
        return compiledLiteralValue;
    }
}
