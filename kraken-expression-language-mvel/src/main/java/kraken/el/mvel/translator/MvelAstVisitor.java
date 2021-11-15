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

import java.util.stream.Collectors;

import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.*;
import kraken.el.ast.visitor.QueuedAstVisitor;
import kraken.el.functions.DateFunctions;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import org.apache.commons.lang3.StringUtils;

import static kraken.el.mvel.evaluator.MvelExpressionEvaluator.*;

/**
 * Converts Kraken Expression Language AST to MVEL expressions
 * by visiting over AST nodes and translating to MVEL constructs.
 *
 * @author mulevicius
 */
class MvelAstVisitor extends QueuedAstVisitor<String> {

    private final String invokeFunctionName;

    private final String pathDelimiter;

    private final boolean isStrictTypeMode;

    MvelAstVisitor(ExpressionLanguageConfiguration configuration) {
        this.pathDelimiter = configuration.isStrictTypeMode() ? "." : ".?";
        this.invokeFunctionName = configuration.isAllowAutomaticIterationWhenInvokingFunctions()
                ? INVOKE_WITH_ITERATION_FUNCTION_NAME
                : INVOKE_FUNCTION_NAME;
        this.isStrictTypeMode = configuration.isStrictTypeMode();
    }

    @Override
    public String visit(Cast cast) {
        return visit(cast.getReference());
    }

    @Override
    public String visit(InstanceOf instanceOf) {
        return "_i" + "(" +
                visit(instanceOf.getLeft()) +
                "," +
                "'" + instanceOf.getTypeLiteral() + "'" +
                ")"
                ;
    }

    @Override
    public String visit(TypeOf typeOf) {
        return "_t" + "(" +
                visit(typeOf.getLeft()) +
                "," +
                "'" + typeOf.getTypeLiteral() + "'" +
                ")"
                ;
    }

    @Override
    public String visit(CollectionFilter filter) {
        if(filter.getPredicate() == null) {
            return visit(filter.getCollection());
        }

        return FILTER_FUNCTION_NAME + "(" +
                visit(filter.getCollection()) +
                ",'" +
                encode(visit(filter.getPredicate())) +
                "')";
    }

    @Override
    public String visit(AccessByIndex accessByIndex) {
        return GET_ELEMENT_FUNCTION_NAME + "(" +
                visit(accessByIndex.getCollection()) +
                "," +
                visit(accessByIndex.getIndexExpression()) +
                ")";
    }

    @Override
    public String visit(ForEach forEach) {
        return FOREACH_FUNCTION_NAME + "(" +
                "'" + forEach.getVar() + "'" +
                "," +
                visit(forEach.getCollection()) +
                ",'" +
                encode(visit(forEach.getReturnExpression())) +
                "')";
    }

    @Override
    public String visit(ForSome forSome) {
        return FORSOME_FUNCTION_NAME + "(" +
                "'" + forSome.getVar() + "'" +
                "," +
                visit(forSome.getCollection()) +
                ",'" +
                encode(visit(forSome.getReturnExpression())) +
                "')";
    }

    @Override
    public String visit(ForEvery forEvery) {
        return FOREVERY_FUNCTION_NAME + "(" +
                "'" + forEvery.getVar() + "'" +
                "," +
                visit(forEvery.getCollection()) +
                ",'" +
                encode(visit(forEvery.getReturnExpression())) +
                "')";
    }

    @Override
    public String visit(Path path) {
        if(path.getObject().getEvaluationType() instanceof ArrayType || path.getObject() instanceof CollectionFilter) {
            return FLATMAP_FUNCTION_NAME + "(" +
                    visit(path.getObject()) +
                    ",'" +
                    encode(visit(path.getProperty())) +
                    "')";
        }

        if (path.getProperty() instanceof AccessByIndex) {
            AccessByIndex accessByIndex = (AccessByIndex) path.getProperty();

            return GET_ELEMENT_FUNCTION_NAME +
                    "(" +
                    visit(path.getObject()) + pathDelimiter + visit(accessByIndex.getCollection()) +
                    "," +
                    visit(accessByIndex.getIndexExpression()) +
                    ")";
        }

        return visit(path.getObject()) + pathDelimiter + visit(path.getProperty());
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
        if (numberLiteral.getValue().scale() > 0) {
            return numberLiteral + "B";
        }
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
        return "_in(" + visit(in.getRight()) + ", " + visit(in.getLeft()) + ")";
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
        return "(" + coerceString(matchesRegExp.getLeft()) + " ~= '" + matchesRegExp.getRegex() + "')";
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
                .collect(Collectors.joining(",", "[", "]"));
    }

    @Override
    public String visit(Function function) {
        String functionParameters = function.getParameters().stream()
                .map(node -> visit(node))
                .collect(Collectors.joining(",", "[", "]"));

        return invokeFunctionName + "('" + function.getFunctionName() + "'," + functionParameters + ")";
    }

    @Override
    public String visit(ReferenceValue reference) {
        return wrapIfMoney(visit(reference.getReference()), reference);
    }

    @Override
    public String visit(This aThis) {
        return wrapIfMoney("this", aThis);
    }

    private String wrapIfMoney(String string, Expression expression) {
        if(expression.getEvaluationType() == Type.MONEY) {
            return FROMMONEY_FUNCTION_NAME + "(" + string + ")";
        }
        return string;
    }

    @Override
    public String visit(Identifier identifier) {
        String identifierString = "";
        // first encountered identifier while visiting AST will consume parent ReferenceValue and will prefix 'this' if needed
        if(currentReferenceValue != null) {
            // force 'this' if we see that the user refers to property in filter scope object
            boolean refersToFilterScope = identifier.findScopeTypeOfReference() == ScopeType.FILTER;

            // or in local scope object
            boolean refersToLocalScope = identifier.findScopeTypeOfReference() == ScopeType.LOCAL;

            if(!currentReferenceValue.isStartsWithThis() && refersToLocalScope && identifier.getScope().findClosestScopeOfType(ScopeType.FILTER) != null) {
                // in case we are in filter scope but property actually refers to property of data object
                // then we assume that this is a data object reference, and __dataObject__ will be provided during
                // filter evaluation by NativeFunctions#filter
                identifierString += "__dataObject__" + pathDelimiter;
            } else if(currentReferenceValue.isStartsWithThis() || refersToLocalScope || refersToFilterScope) {
                identifierString += "this" + pathDelimiter;
            }

            // all subsequent identifiers in scope of this ReferenceValue will not prefix 'this'
            currentReferenceValue = null;
        }
        return identifierString + identifier.getIdentifier();
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
                .append(anIf.getElseExpression().map(this::visit).orElse("null"))
                .append(")");

        return sb.toString();
    }

    @Override
    public String visit(Null aNull) {
        return "null";
    }

    private String coerceBoolean(Expression expression) {
        if(isStrictTypeMode && isReference(expression)) {
            return "_b(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private String encode(String unencoded) {
        String encoded = StringUtils.replace(unencoded, "\\", "\\\\");
        encoded = StringUtils.replace(encoded, "'", "\\'");
        return encoded;
    }

    private String coerceNumberOrDate(Expression expression) {
        if(isStrictTypeMode && isReference(expression)) {
            return "_nd(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private String coerceNumber(Expression expression) {
        if(isStrictTypeMode && isReference(expression)) {
            return "_n(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private String coerceString(Expression expression) {
        if(isStrictTypeMode && isReference(expression)) {
            return "_s(" + visit(expression) + ")";
        }
        return visit(expression);
    }

    private boolean isReference(Expression expression) {
        return expression instanceof ReferenceValue || expression instanceof This;
    }

}
