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
package kraken.el;

import java.util.Map;

import kraken.el.ast.Ast;

/**
 * Represents Expression Language which is pluggable per Target Environment
 *
 * @author mulevicius
 */
public interface ExpressionLanguage {

    /**
     * Translates AST to target environment expression language
     *
     * @param ast of Kraken Expression Language
     * @return translated expression
     */
    String translate(Ast ast);
    
    /**
     * Evaluates expression on provided data object
     *
     * @param expression
     * @param dataObject
     * @param vars
     * @return evaluated result
     * @throws ExpressionEvaluationException
     */
    Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException;

    /**
     * Evaluates set expression which sets value in data object by path expression
     *
     * @param valueToSet
     * @param path expression that indicates where to set the value
     * @param dataObject
     * @throws ExpressionEvaluationException
     */
    void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException;

}
