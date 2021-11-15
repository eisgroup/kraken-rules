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

import java.io.Serializable;
import java.util.Collection;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class CompiledExpression implements Serializable {

    private static final long serialVersionUID = 7122032254357905188L;

    private final String expressionString;
    private final ExpressionType expressionType;
    private final Serializable compiledLiteralValue;
    private final String compiledLiteralValueType;
    private final Collection<ExpressionVariable> expressionVariables;

    public CompiledExpression(
            String expressionString,
            ExpressionType expressionType,
            Serializable compiledLiteralValue,
            String compiledLiteralValueType,
            Collection<ExpressionVariable> expressionVariables
    ) {
        this.expressionString = expressionString;
        this.expressionType = expressionType;
        this.compiledLiteralValue = compiledLiteralValue;
        this.compiledLiteralValueType = compiledLiteralValueType;
        this.expressionVariables = expressionVariables;
    }

    public String getExpressionString() {
        return expressionString;
    }

    public ExpressionType getExpressionType() {
        return expressionType;
    }

    public Serializable getCompiledLiteralValue() {
        return compiledLiteralValue;
    }

    /**
     *
     * @return type of literal value {@link #getCompiledLiteralValue()}. If literal value is null then type will be null.
     */
    public String getCompiledLiteralValueType() {
        return compiledLiteralValueType;
    }

    public Collection<ExpressionVariable> getExpressionVariables() {
        return expressionVariables;
    }
}
