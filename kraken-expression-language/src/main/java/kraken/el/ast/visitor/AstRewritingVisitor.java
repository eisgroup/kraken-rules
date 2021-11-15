/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import java.util.List;
import java.util.stream.Collectors;

/**
 * Visitor that rewrites AST in immutable way from one structure to another.
 * Should be used by extending and overwriting relevant visitor steps that you need to rewrite.
 * Overwritten steps must guarantee immutability.
 *
 * @author mulevicius
 */
public abstract class AstRewritingVisitor extends QueuedAstVisitor<Expression> {

    @Override
    public Expression visit(Cast cast) {
        Reference reference = (Reference) visit(cast.getReference());

        return new Cast(cast.getTypeLiteral(), reference, cast.getScope(), cast.getToken());
    }

    @Override
    public Expression visit(InstanceOf instanceOf) {
        Expression left = visit(instanceOf.getLeft());

        return new InstanceOf(left, instanceOf.getTypeLiteral(), instanceOf.getScope(), instanceOf.getToken());
    }

    @Override
    public Expression visit(TypeOf typeOf) {
        Expression left = visit(typeOf.getLeft());

        return new TypeOf(left, typeOf.getTypeLiteral(), typeOf.getScope(), typeOf.getToken());
    }

    @Override
    public Expression visit(CollectionFilter filter) {
        final Reference collection = (Reference) visit(filter.getCollection());
        Expression predicate = null;
        if(filter.getPredicate() != null) {
            predicate = visit(filter.getPredicate());
        }
        return new CollectionFilter(collection, predicate, filter.getScope(), filter.getToken());
    }

    @Override
    public Expression visit(BooleanLiteral booleanLiteral) {
        return new BooleanLiteral(booleanLiteral.getValue(), booleanLiteral.getScope(), booleanLiteral.getToken());
    }

    @Override
    public Expression visit(StringLiteral stringLiteral) {
        return new StringLiteral(stringLiteral.getValue(), stringLiteral.getScope(), stringLiteral.getToken());
    }

    @Override
    public Expression visit(NumberLiteral numberLiteral) {
        return new NumberLiteral(numberLiteral.getValue(), numberLiteral.getScope(), numberLiteral.getToken());
    }

    @Override
    public Expression visit(DateLiteral dateLiteral) {
        return new DateLiteral(dateLiteral.getValue(), dateLiteral.getScope(), dateLiteral.getToken());
    }

    @Override
    public Expression visit(DateTimeLiteral dateTimeLiteral) {
        return new DateTimeLiteral(dateTimeLiteral.getValue(), dateTimeLiteral.getScope(), dateTimeLiteral.getToken());
    }

    @Override
    public Expression visit(If anIf) {
        Expression elseExpression = anIf.getElseExpression()
                .map(this::visit)
                .orElse(null);

        return new If(visit(anIf.getCondition()), visit(anIf.getThenExpression()), elseExpression, anIf.getScope(), anIf.getToken());
    }

    @Override
    public Expression visit(ForEach forEach) {
        return new ForEach(
                forEach.getVar(),
                visit(forEach.getCollection()),
                visit(forEach.getReturnExpression()),
                forEach.getScope(),
                forEach.getToken()
        );
    }

    @Override
    public Expression visit(ForSome forSome) {
        return new ForSome(
                forSome.getVar(),
                visit(forSome.getCollection()),
                visit(forSome.getReturnExpression()),
                forSome.getScope(),
                forSome.getToken()
        );
    }

    @Override
    public Expression visit(ForEvery forEvery) {
        return new ForEvery(
                forEvery.getVar(),
                visit(forEvery.getCollection()),
                visit(forEvery.getReturnExpression()),
                forEvery.getScope(),
                forEvery.getToken()
        );
    }

    @Override
    public Expression visit(In in) {
        return new In(visit(in.getLeft()), visit(in.getRight()), in.getScope(), in.getToken());
    }

    @Override
    public Expression visit(MoreThanOrEquals moreThanOrEquals) {
        return new MoreThanOrEquals(
                visit(moreThanOrEquals.getLeft()),
                visit(moreThanOrEquals.getRight()),
                moreThanOrEquals.getScope(),
                moreThanOrEquals.getToken()
        );
    }

    @Override
    public Expression visit(MoreThan moreThan) {
        return new MoreThan(visit(moreThan.getLeft()), visit(moreThan.getRight()), moreThan.getScope(), moreThan.getToken());
    }

    @Override
    public Expression visit(LessThanOrEquals lessThanOrEquals) {
        return new LessThanOrEquals(
                visit(lessThanOrEquals.getLeft()),
                visit(lessThanOrEquals.getRight()),
                lessThanOrEquals.getScope(),
                lessThanOrEquals.getToken()
        );
    }

    @Override
    public Expression visit(LessThan lessThan) {
        return new LessThan(visit(lessThan.getLeft()), visit(lessThan.getRight()), lessThan.getScope(), lessThan.getToken());
    }

    @Override
    public Expression visit(And and) {
        return new And(visit(and.getLeft()), visit(and.getRight()), and.getScope(), and.getToken());
    }

    @Override
    public Expression visit(Or or) {
        return new Or(visit(or.getLeft()), visit(or.getRight()), or.getScope(), or.getToken());
    }

    @Override
    public Expression visit(MatchesRegExp matchesRegExp) {
        return new MatchesRegExp(
                visit(matchesRegExp.getLeft()),
                matchesRegExp.getRegex(),
                matchesRegExp.getScope(),
                matchesRegExp.getToken());
    }

    @Override
    public Expression visit(Equals equals) {
        return new Equals(visit(equals.getLeft()), visit(equals.getRight()), equals.getScope(), equals.getToken());
    }

    @Override
    public Expression visit(NotEquals notEquals) {
        return new NotEquals(visit(notEquals.getLeft()), visit(notEquals.getRight()), notEquals.getScope(), notEquals.getToken());
    }

    @Override
    public Expression visit(Modulus modulus) {
        return new Modulus(visit(modulus.getLeft()), visit(modulus.getRight()), modulus.getScope(), modulus.getToken());
    }

    @Override
    public Expression visit(Subtraction subtraction) {
        return new Subtraction(visit(subtraction.getLeft()), visit(subtraction.getRight()), subtraction.getScope(), subtraction.getToken());
    }

    @Override
    public Expression visit(Multiplication multiplication) {
        return new Multiplication(
                visit(multiplication.getLeft()),
                visit(multiplication.getRight()),
                multiplication.getScope(),
                multiplication.getToken()
        );
    }

    @Override
    public Expression visit(Exponent exponent) {
        return new Exponent(visit(exponent.getLeft()), visit(exponent.getRight()), exponent.getScope(), exponent.getToken());
    }

    @Override
    public Expression visit(Division division) {
        return new Division(visit(division.getLeft()), visit(division.getRight()), division.getScope(), division.getToken());
    }

    @Override
    public Expression visit(Addition addition) {
        return new Addition(visit(addition.getLeft()), visit(addition.getRight()), addition.getScope(), addition.getToken());
    }

    @Override
    public Expression visit(Negation negation) {
        return new Negation(visit(negation.getExpression()), negation.getScope(), negation.getToken());
    }

    @Override
    public Expression visit(Negative negative) {
        return new Negative(visit(negative.getExpression()), negative.getScope(), negative.getToken());
    }

    @Override
    public Expression visit(Function function) {
        List<Expression> parameters = function.getParameters()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList());

        return new Function(function.getFunctionName(), parameters, function.getScope(), function.getEvaluationType(), function.getToken());
    }

    @Override
    public Expression visit(InlineArray inlineArray) {
        List<Expression> items = inlineArray.getItems()
                .stream()
                .map(this::visit)
                .collect(Collectors.toList());

        return new InlineArray(items, inlineArray.getScope(), inlineArray.getToken());
    }

    @Override
    public Expression visit(InlineMap inlineMap) {
        List<InlineMap.KeyValuePair> keyValuePairs = inlineMap.getKeyValuePairs()
                .stream()
                .map(keyValuePair -> new InlineMap.KeyValuePair(keyValuePair.getKey(), visit(keyValuePair.getValue())))
                .collect(Collectors.toList());

        return new InlineMap(keyValuePairs, inlineMap.getScope(), inlineMap.getToken());
    }

    @Override
    public Expression visit(Path path) {
        return new Path(
                (Reference) visit(path.getObject()),
                (Reference) visit(path.getProperty()),
                path.getScope(),
                path.getToken());
    }

    @Override
    public Expression visit(AccessByIndex accessByIndex) {
        return new AccessByIndex(
                (Reference) visit(accessByIndex.getCollection()),
                visit(accessByIndex.getIndexExpression()),
                accessByIndex.getScope(),
                accessByIndex.getToken());
    }

    @Override
    public Expression visit(ReferenceValue reference) {
        return new ReferenceValue(
                reference.isStartsWithThis(),
                (Reference) visit(reference.getReference()),
                reference.getScope(),
                reference.getEvaluationType(),
                reference.getToken());
    }

    @Override
    public Expression visit(Identifier identifier) {
        return new Identifier(identifier.getIdentifierToken(), identifier.getIdentifier(), identifier.getScope(), identifier.getToken());
    }

    @Override
    public Expression visit(Null aNull) {
        return new Null(aNull.getScope(), aNull.getToken());
    }

    @Override
    public Expression visit(This aThis) {
        return new This(aThis.getScope(), aThis.getToken());
    }

    @Override
    public Expression visit(Template template) {
        List<Expression> templateExpressions = template.getTemplateExpressions().stream()
            .map(this::visit)
            .collect(Collectors.toList());

        return new Template(template.getTemplateParts(), templateExpressions, template.getScope(), template.getToken());
    }
}
