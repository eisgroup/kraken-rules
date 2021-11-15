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

import kraken.el.ast.*;
import kraken.el.ast.visitor.QueuedAstVisitor;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts Kraken Expression Language AST to Javascript expressions
 * by visiting over AST nodes and translating to Javascript constructs.
 *
 * @author mulevicius
 */
public class JavascriptAstVisitor extends QueuedAstVisitor<String> {

    private static final String NESTED_ARRAY_FLATTING_FUNCTION = ".reduce(function(p, n) { return p.concat(n) }, [])";

    private final Deque<String> availablePredicateSubject;

    private final Deque<String> currentPredicateSubject;

    public JavascriptAstVisitor() {
        this.availablePredicateSubject = new ArrayDeque(List.of("_x_", "_y_", "_z_", "_a_", "_b_", "_c_", "_d_", "_e_"));
        this.currentPredicateSubject = new ArrayDeque<>();
    }

    @Override
    public String visit(Cast cast) {
        return visit(cast.getReference());
    }

    @Override
    public String visit(InstanceOf instanceOf) {
        return "_i" + "(" +
                visit(instanceOf.getLeft()) +
                ",'" +
                instanceOf.getTypeLiteral() +
                "')"
                ;
    }

    @Override
    public String visit(TypeOf typeOf) {
        return "_t" + "(" +
                visit(typeOf.getLeft()) +
                ",'" +
                typeOf.getTypeLiteral() +
                "')"
                ;
    }

    @Override
    public String visit(CollectionFilter filter) {
        if(filter.getPredicate() == null) {
            return visit(filter.getCollection());
        }

        currentPredicateSubject.push(availablePredicateSubject.pop());
        String subject = currentPredicateSubject.peek();
        String filterExpression = ".filter(function(" + subject + ") { return " + visit(filter.getPredicate()) + " })";
        availablePredicateSubject.push(currentPredicateSubject.pop());

        return visit(filter.getCollection()) + filterExpression;
    }

    @Override
    public String visit(BooleanLiteral booleanLiteral) {
        return booleanLiteral.getValue().toString();
    }

    @Override
    public String visit(StringLiteral stringLiteral) {
        return "'" + stringLiteral.getValue() + "'";
    }

    @Override
    public String visit(NumberLiteral numberLiteral) {
        return numberLiteral.toString();
    }

    @Override
    public String visit(DateLiteral dateLiteral) {
        return "Date('" + dateLiteral + "')";
    }

    @Override
    public String visit(DateTimeLiteral dateTimeLiteral) {
        return "DateTime('" + dateTimeLiteral + "')";
    }

    @Override
    public String visit(In in) {
        String object = visit(in.getLeft());
        String collection = visit(in.getRight());

        return "((" + collection + " || []).indexOf(" + object + ") > -1)";
    }

    @Override
    public String visit(MoreThanOrEquals moreThanOrEquals) {
        return "(" + coerceNumberOrDate(moreThanOrEquals.getLeft()) + " >= " + coerceNumberOrDate(moreThanOrEquals.getRight()) + ")";
    }

    @Override
    public String visit(MoreThan moreThan) {
        return "(" + coerceNumberOrDate(moreThan.getLeft()) + " > " + coerceNumberOrDate(moreThan.getRight()) + ")";
    }

    @Override
    public String visit(LessThanOrEquals lessThanOrEquals) {
        return "(" + coerceNumberOrDate(lessThanOrEquals.getLeft()) + " <= " + coerceNumberOrDate(lessThanOrEquals.getRight()) + ")";
    }

    @Override
    public String visit(LessThan lessThan) {
        return "(" + coerceNumberOrDate(lessThan.getLeft()) + " < " + coerceNumberOrDate(lessThan.getRight()) + ")";
    }

    @Override
    public String visit(And and) {
        return "(" + coerceBoolean(and.getLeft()) + " && " + coerceBoolean(and.getRight()) + ")";
    }

    @Override
    public String visit(Or or) {
        return "(" + coerceBoolean(or.getLeft()) + " || " + coerceBoolean(or.getRight()) + ")";
    }

    @Override
    public String visit(MatchesRegExp matchesRegExp) {
        return "(/" + matchesRegExp.getRegex() + "/.test(" + coerceString(matchesRegExp.getLeft()) + "))";
    }

    @Override
    public String visit(Equals equals) {
        return "_eq(" + visit(equals.getLeft()) + ", " + visit(equals.getRight()) + ")";
    }

    @Override
    public String visit(NotEquals notEquals) {
        return "_neq(" + visit(notEquals.getLeft()) + ", " + visit(notEquals.getRight()) + ")";
    }

    @Override
    public String visit(Modulus modulus) {
        return "_mod(" + visit(modulus.getLeft()) + ", " + visit(modulus.getRight()) + ")";
    }

    @Override
    public String visit(Subtraction subtraction) {
        return "_sub(" + visit(subtraction.getLeft()) + ", " + visit(subtraction.getRight()) + ")";
    }

    @Override
    public String visit(Multiplication multiplication) {
        return "_mult(" + visit(multiplication.getLeft()) + ", " + visit(multiplication.getRight()) + ")";
    }

    @Override
    public String visit(Exponent exponent) {
        return "_pow(" + visit(exponent.getLeft()) + ", " + visit(exponent.getRight()) + ")";
    }

    @Override
    public String visit(Division division) {
        return "_div(" + visit(division.getLeft()) + ", " + visit(division.getRight()) + ")";
    }

    @Override
    public String visit(Addition addition) {
        return "_add(" + visit(addition.getLeft()) + ", " + visit(addition.getRight()) + ")";
    }

    @Override
    public String visit(Negation negation) {
        return "!" + coerceBoolean(negation.getExpression());
    }

    @Override
    public String visit(Negative negative) {
        return "-" + coerceNumber(negative.getExpression());
    }

    @Override
    public String visit(InlineArray inlineArray) {
        return inlineArray.getItems().stream()
                .map(node -> visit(node))
                .collect(Collectors.joining(",", "[", "]"));
    }

    @Override
    public String visit(InlineMap inlineMap) {
        return inlineMap.getKeyValuePairs().stream()
                .map(keyValuePair -> "'" + keyValuePair.getKey() + "':" + visit(keyValuePair.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    @Override
    public String visit(Function function) {
        return function.getParameters().stream()
                .map(node -> visit(node))
                .collect(Collectors.joining(",", function.getFunctionName() + "(", ")"));
    }

    @Override
    public String visit(Path path) {
        String pathString = visit(path.getObject());
        if(path.getObject().getEvaluationType() instanceof ArrayType || path.getObject() instanceof CollectionFilter) {

            currentPredicateSubject.push(availablePredicateSubject.pop());
            String subject = currentPredicateSubject.peek();
            pathString += ".map(function(" + subject + ") { return " + subject + "." + visit(path.getProperty()) + " })";
            availablePredicateSubject.push(currentPredicateSubject.pop());

            if(path.getProperty().getEvaluationType() instanceof ArrayType || path.getProperty().getEvaluationType() == Type.ANY) {
                pathString += NESTED_ARRAY_FLATTING_FUNCTION;
            }
        } else {
            pathString += "." + visit(path.getProperty());
        }
        return pathString;
    }

    @Override
    public String visit(ReferenceValue reference) {
        String referenceString = "";

        // force 'this' if we see that the user refers to property in filter scope object
        boolean refersToFilterScope = reference.getReference().findScopeTypeOfReference() == ScopeType.FILTER;

        // or in local scope object
        boolean refersToLocalScope = reference.getReference().findScopeTypeOfReference() == ScopeType.LOCAL;

        if (reference.isStartsWithThis()) {
            // if user specified explicit 'this', then we have to check the scope type and if we are within filter
            // then we need to prepend filter predicate variable, otherwise wee assume that user is referring to data object
            if(reference.getReference().getScope().findClosestScopeOfType(ScopeType.FILTER) != null) {
                referenceString = currentPredicateSubject.peek() + ".";
            } else {
                referenceString = "__dataObject__.";
            }
        } else {
            // if 'this' is not specified then we have to guess what is the target of the reference.
            // if reference is contained within some filter scope then we prepend filter predicate variable,
            // if reference is contained in local scope then we assume that it refers to data object and we transform it to data object reference
            if (refersToFilterScope) {
                referenceString = currentPredicateSubject.peek() + ".";
            } else if (refersToLocalScope) {
                referenceString = "__dataObject__.";
            }
        }

        referenceString += visit(reference.getReference());
        return wrapIfMoney(referenceString, reference);
    }

    @Override
    public String visit(AccessByIndex accessByIndex) {
        return visit(accessByIndex.getCollection()) + "[" + visit(accessByIndex.getIndexExpression()) + "]";
    }

    @Override
    public String visit(ForEach forEach) {
        String forEachString = "(" + visit(forEach.getCollection()) + " || []).map(function(" + forEach.getVar() + ") { return " + visit(forEach.getReturnExpression()) + " })";
        if(forEach.getReturnExpression().getEvaluationType() instanceof ArrayType || forEach.getReturnExpression().getEvaluationType() == Type.ANY) {
            forEachString += NESTED_ARRAY_FLATTING_FUNCTION;
        }
        return forEachString;
    }

    @Override
    public String visit(ForSome forSome) {
        return "(" + visit(forSome.getCollection()) + " || []).some(function(" + forSome.getVar() + ") { return " + visit(forSome.getReturnExpression()) + " })";
    }

    @Override
    public String visit(ForEvery forEvery) {
        return "(" + visit(forEvery.getCollection()) + " || []).every(function(" + forEvery.getVar() + ") { return " + visit(forEvery.getReturnExpression()) + " })";
    }

    @Override
    public String visit(This aThis) {
        String string = ScopeType.FILTER == aThis.getScope().getScopeType()
                ? currentPredicateSubject.peek()
                : "__dataObject__";
        return wrapIfMoney(string, aThis);
    }

    private String wrapIfMoney(String string, Expression expression) {
        if(expression.getEvaluationType() == Type.MONEY) {
            return "FromMoney(" + string + ")";
        }
        return string;
    }

    @Override
    public String visit(Identifier identifier) {
        return identifier.getIdentifier();
    }

    @Override
    public String visit(If anIf) {
        String thenNode = visit(anIf.getThenExpression());

        StringBuilder sb = new StringBuilder();
        sb.append("(")
                .append(coerceBoolean(anIf.getCondition()))
                .append(" ? ")
                .append(thenNode)
                .append(" : ")
                .append(anIf.getElseExpression().map(this::visit).orElse("undefined"))
                .append(")");

        return sb.toString();
    }

    @Override
    public String visit(Null aNull) {
        return "undefined";
    }

    private String coerceBoolean(Expression expression) {
        if(isReference(expression)) {
            return "_b(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private String coerceNumberOrDate(Expression expression) {
        if(isReference(expression)) {
            return "_nd(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private String coerceNumber(Expression expression) {
        if(isReference(expression)) {
            return "_n(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private String coerceString(Expression expression) {
        if(isReference(expression)) {
            return "_s(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private boolean isReference(Expression expression) {
        return expression instanceof ReferenceValue || expression instanceof This;
    }
}
