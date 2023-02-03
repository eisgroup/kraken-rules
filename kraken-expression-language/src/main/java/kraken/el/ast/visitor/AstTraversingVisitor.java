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
package kraken.el.ast.visitor;

import kraken.el.ast.*;
import kraken.el.ast.InlineMap.KeyValuePair;

/**
 * @author mulevicius
 */
public class AstTraversingVisitor extends QueuedAstVisitor<Expression> {

    @Override
    public Expression visit(Cast cast) {
        visit(cast.getReference());

        return cast;
    }

    @Override
    public Expression visit(InstanceOf instanceOf) {
        visit(instanceOf.getLeft());

        return instanceOf;
    }

    @Override
    public Expression visit(TypeOf typeOf) {
        visit(typeOf.getLeft());

        return typeOf;
    }

    @Override
    public Expression visit(CollectionFilter collectionFilter) {
        visit(collectionFilter.getCollection());
        if(collectionFilter.getPredicate() != null) {
            visit(collectionFilter.getPredicate());
        }
        return collectionFilter;
    }

    @Override
    public Expression visit(BooleanLiteral booleanLiteral) {
        return booleanLiteral;
    }

    @Override
    public Expression visit(StringLiteral stringLiteral) {
        return stringLiteral;
    }

    @Override
    public Expression visit(NumberLiteral numberLiteral) {
        return numberLiteral;
    }

    @Override
    public Expression visit(DateLiteral dateLiteral) {
        return dateLiteral;
    }

    @Override
    public Expression visit(DateTimeLiteral dateTimeLiteral) {
        return dateTimeLiteral;
    }

    @Override
    public Expression visit(If anIf) {
        visit(anIf.getCondition());
        visit(anIf.getThenExpression());
        anIf.getElseExpression().ifPresent(this::visit);
        return anIf;
    }

    @Override
    public Expression visit(In in) {
        return visitBinaryExpression(in);
    }

    @Override
    public Expression visit(ForEach forEach) {
        visit(forEach.getCollection());
        visit(forEach.getReturnExpression());

        return forEach;
    }

    @Override
    public Expression visit(ForSome forSome) {
        visit(forSome.getCollection());
        visit(forSome.getReturnExpression());

        return forSome;
    }

    @Override
    public Expression visit(ForEvery forEvery) {
        visit(forEvery.getCollection());
        visit(forEvery.getReturnExpression());

        return forEvery;
    }

    @Override
    public Expression visit(MoreThanOrEquals moreThanOrEquals) {
        return visitBinaryExpression(moreThanOrEquals);
    }

    @Override
    public Expression visit(MoreThan moreThan) {
        return visitBinaryExpression(moreThan);
    }

    @Override
    public Expression visit(LessThanOrEquals lessThanOrEquals) {
        return visitBinaryExpression(lessThanOrEquals);
    }

    @Override
    public Expression visit(LessThan lessThan) {
        return visitBinaryExpression(lessThan);
    }

    @Override
    public Expression visit(And and) {
        return visitBinaryExpression(and);
    }

    @Override
    public Expression visit(Or or) {
        return visitBinaryExpression(or);
    }

    @Override
    public Expression visit(MatchesRegExp matchesRegExp) {
        visit(matchesRegExp.getLeft());
        return matchesRegExp;
    }

    @Override
    public Expression visit(Equals equals) {
        return visitBinaryExpression(equals);
    }

    @Override
    public Expression visit(NotEquals notEquals) {
        return visitBinaryExpression(notEquals);
    }

    @Override
    public Expression visit(Modulus modulus) {
        return visitBinaryExpression(modulus);
    }

    @Override
    public Expression visit(Subtraction subtraction) {
        return visitBinaryExpression(subtraction);
    }

    @Override
    public Expression visit(Multiplication multiplication) {
        return visitBinaryExpression(multiplication);
    }

    @Override
    public Expression visit(Exponent exponent) {
        return visitBinaryExpression(exponent);
    }

    @Override
    public Expression visit(Division division) {
        return visitBinaryExpression(division);
    }

    @Override
    public Expression visit(Addition addition) {
        return visitBinaryExpression(addition);
    }

    @Override
    public Expression visit(Negation negation) {
        visit(negation.getExpression());
        return negation;
    }

    @Override
    public Expression visit(Negative negative) {
        visit(negative.getExpression());
        return negative;
    }

    @Override
    public Expression visit(Function function) {
        function.getParameters().stream().forEach(this::visit);
        return function;
    }

    @Override
    public Expression visit(InlineArray inlineArray) {
        inlineArray.getItems().stream().forEach(this::visit);
        return inlineArray;
    }

    @Override
    public Expression visit(InlineMap inlineMap) {
        inlineMap.getKeyValuePairs().stream().map(KeyValuePair::getValue).forEach(this::visit);
        return inlineMap;
    }

    @Override
    public Expression visit(Path path) {
        visit(path.getObject());
        visit(path.getProperty());
        return path;
    }

    @Override
    public Expression visit(AccessByIndex accessByIndex) {
        visit(accessByIndex.getCollection());
        visit(accessByIndex.getIndexExpression());
        return accessByIndex;
    }

    @Override
    public Expression visit(ReferenceValue reference) {
        if(reference.getThisNode() != null) {
            visit(reference.getThisNode());
        }
        visit(reference.getReference());
        return reference;
    }

    @Override
    public Expression visit(Identifier identifier) {
        return identifier;
    }

    @Override
    public Expression visit(Null aNull) {
        return aNull;
    }

    @Override
    public Expression visit(Empty empty) {
        return empty;
    }

    @Override
    public Expression visit(This aThis) {
        return aThis;
    }

    @Override
    public Expression visit(Template template) {
        template.getTemplateExpressions().forEach(this::visit);
        return template;
    }

    @Override
    public Expression visit(ValueBlock valueBlock) {
        valueBlock.getVariables().forEach(this::visit);

        visit(valueBlock.getValue());

        return valueBlock;
    }

    @Override
    public Expression visit(Variable variable) {
        visit(variable.getValue());

        return variable;
    }

    private Expression visitBinaryExpression(BinaryExpression e) {
        visit(e.getLeft());
        visit(e.getRight());
        return e;
    }
}
