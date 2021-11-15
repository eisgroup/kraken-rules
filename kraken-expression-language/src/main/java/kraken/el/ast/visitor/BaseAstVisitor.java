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
package kraken.el.ast.visitor;

import kraken.el.ast.*;

/**
 * A base generic abstract visitor based on External Tree Visitor pattern that traverses over all nodes
 * and allows to produce generic result from given Abstract Syntax Tree
 *
 * @author mulevicius
 */
public abstract class BaseAstVisitor<T> implements AstVisitor<T> {

    public T visit(Expression expression) {
        switch (expression.getNodeType()) {
            case CAST:
                return visit((Cast) expression);
            case INSTANCEOF:
                return visit((InstanceOf)expression);
            case TYPEOF:
                return visit((TypeOf)expression);
            case COLLECTION_FILTER:
                return visit((CollectionFilter) expression);
            case REFERENCE:
                return visit((ReferenceValue) expression);
            case ACCESS_BY_INDEX:
                return visit((AccessByIndex) expression);
            case ADDITION:
                return visit((Addition) expression);
            case SUBTRACTION:
                return visit((Subtraction) expression);
            case MULTIPLICATION:
                return visit((Multiplication) expression);
            case DIVISION:
                return visit((Division) expression);
            case MODULUS:
                return visit((Modulus) expression);
            case EXPONENT:
                return visit((Exponent) expression);
            case AND:
                return visit((And) expression);
            case OR:
                return visit((Or) expression);
            case EQUALS:
                return visit((Equals) expression);
            case NOT_EQUALS:
                return visit((NotEquals) expression);
            case MORE_THAN:
                return visit((MoreThan) expression);
            case MORE_THAN_OR_EQUALS:
                return visit((MoreThanOrEquals) expression);
            case LESS_THAN:
                return visit((LessThan) expression);
            case LESS_THAN_OR_EQUALS:
                return visit((LessThanOrEquals) expression);
            case IN:
                return visit((In) expression);
            case MATCHES_REG_EXP:
                return visit((MatchesRegExp) expression);
            case NEGATION:
                return visit((Negation) expression);
            case NEGATIVE:
                return visit((Negative) expression);
            case STRING:
                return visit((StringLiteral) expression);
            case BOOLEAN:
                return visit((BooleanLiteral) expression);
            case DECIMAL:
                return visit((NumberLiteral) expression);
            case DATE:
                return visit((DateLiteral) expression);
            case DATETIME:
                return visit((DateTimeLiteral) expression);
            case NULL:
                return visit((Null) expression);
            case INLINE_ARRAY:
                return visit((InlineArray) expression);
            case INLINE_MAP:
                return visit((InlineMap) expression);
            case IDENTIFIER:
                return visit((Identifier) expression);
            case FUNCTION:
                return visit((Function) expression);
            case IF:
                return visit((If) expression);
            case FOR:
                return visit((ForEach) expression);
            case SOME:
                return visit((ForSome) expression);
            case EVERY:
                return visit((ForEvery) expression);
            case PATH:
                return visit((Path) expression);
            case THIS:
                return visit((This) expression);
            case TEMPLATE:
                return visit((Template) expression);
            default: throw new IllegalStateException("Cannot identify AST node: " + expression.getNodeType());
        }

    }

}




