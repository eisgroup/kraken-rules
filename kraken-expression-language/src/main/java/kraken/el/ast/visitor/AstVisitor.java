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
 * Visits over Kraken Expression Language Abstract Syntax Tree nodes.
 *
 * Based on External Visitor Pattern to traverse Abstract Syntax Tree of Kraken Expression Language.
 * Abstract Syntax Tree implementation is based on Irregular Heterogeneous AST pattern as described by Terence Parr.
 *
 * @author mulevicius
 */
public interface AstVisitor<T> {

    T visit(Cast cast);

    T visit(InstanceOf instanceOf);

    T visit(TypeOf typeOf);
    
    T visit(BooleanLiteral booleanLiteral);

    T visit(StringLiteral stringLiteral);

    T visit(NumberLiteral numberLiteral);

    T visit(DateLiteral dateLiteral);

    T visit(DateTimeLiteral dateTimeLiteral);

    T visit(If anIf);

    T visit(ForEach forEach);

    T visit(ForSome forSome);

    T visit(ForEvery forEvery);

    T visit(In in);

    T visit(MoreThanOrEquals moreThanOrEquals);

    T visit(MoreThan moreThan);

    T visit(LessThanOrEquals lessThanOrEquals);

    T visit(LessThan lessThan);

    T visit(And and);

    T visit(Or or);

    T visit(MatchesRegExp matchesRegExp);

    T visit(Equals equals);

    T visit(NotEquals notEquals);

    T visit(Modulus modulus);

    T visit(Subtraction subtraction);

    T visit(Multiplication multiplication);

    T visit(Exponent exponent);

    T visit(Division division);

    T visit(Addition addition);

    T visit(Negation negation);

    T visit(Negative negative);

    T visit(Function function);

    T visit(InlineArray inlineArray);

    T visit(InlineMap inlineMap);

    T visit(Path path);

    T visit(AccessByIndex accessByIndex);

    T visit(CollectionFilter collectionFilter);

    T visit(ReferenceValue reference);

    T visit(Identifier identifier);

    T visit(Null aNull);

    T visit(Empty empty);

    T visit(This aThis);

    T visit(ValueBlock valueBlock);

    T visit(Variable variable);

    default T visit(Template template) {
        throw new IllegalStateException("Expression node is not supported: " + template.getNodeType());
    };
}
