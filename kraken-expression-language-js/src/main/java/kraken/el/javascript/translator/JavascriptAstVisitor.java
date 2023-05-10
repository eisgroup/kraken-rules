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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import kraken.el.ast.AccessByIndex;
import kraken.el.ast.Addition;
import kraken.el.ast.And;
import kraken.el.ast.BooleanLiteral;
import kraken.el.ast.Cast;
import kraken.el.ast.CollectionFilter;
import kraken.el.ast.DateLiteral;
import kraken.el.ast.DateTimeLiteral;
import kraken.el.ast.Division;
import kraken.el.ast.Empty;
import kraken.el.ast.Equals;
import kraken.el.ast.Exponent;
import kraken.el.ast.Expression;
import kraken.el.ast.ForEach;
import kraken.el.ast.ForEvery;
import kraken.el.ast.ForSome;
import kraken.el.ast.Function;
import kraken.el.ast.Identifier;
import kraken.el.ast.If;
import kraken.el.ast.In;
import kraken.el.ast.InlineArray;
import kraken.el.ast.InlineMap;
import kraken.el.ast.InstanceOf;
import kraken.el.ast.LessThan;
import kraken.el.ast.LessThanOrEquals;
import kraken.el.ast.MatchesRegExp;
import kraken.el.ast.Modulus;
import kraken.el.ast.MoreThan;
import kraken.el.ast.MoreThanOrEquals;
import kraken.el.ast.Multiplication;
import kraken.el.ast.Negation;
import kraken.el.ast.Negative;
import kraken.el.ast.NotEquals;
import kraken.el.ast.Null;
import kraken.el.ast.NumberLiteral;
import kraken.el.ast.Or;
import kraken.el.ast.Path;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.StringLiteral;
import kraken.el.ast.Subtraction;
import kraken.el.ast.This;
import kraken.el.ast.TypeOf;
import kraken.el.ast.ValueBlock;
import kraken.el.ast.Variable;
import kraken.el.ast.builder.Literals;
import kraken.el.ast.visitor.QueuedAstVisitor;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.Type;

/**
 * Converts Kraken Expression Language AST to Javascript expressions
 * by visiting over AST nodes and translating to Javascript constructs.
 *
 * @author mulevicius
 */
public class JavascriptAstVisitor extends QueuedAstVisitor<String> {

    private static final String PATH_SEPARATOR = ".";
    private static final String NULL_SAFE_PATH_SEPARATOR = "?.";

    private static final String NESTED_ARRAY_FLATTING_FUNCTION = "reduce((p, n) => p.concat(n), [])";

    private final Deque<String> availableSubjectNames;

    private final Deque<Subject> currentPredicateSubject;

    public JavascriptAstVisitor() {
        this.availableSubjectNames = new ArrayDeque(List.of("_x_", "_y_", "_z_", "_a_", "_b_", "_c_", "_d_", "_e_"));
        this.currentPredicateSubject = new ArrayDeque<>();
    }

    @Override
    public String visit(Cast cast) {
        return visit(cast.getReference());
    }

    @Override
    public String visit(InstanceOf instanceOf) {
        return f("_i" + "(" +
                visit(instanceOf.getLeft()) +
                ",'" +
                instanceOf.getTypeLiteral() +
                "')")
                ;
    }

    @Override
    public String visit(TypeOf typeOf) {
        return f("_t" + "(" +
                visit(typeOf.getLeft()) +
                ",'" +
                typeOf.getTypeLiteral() +
                "')")
                ;
    }

    @Override
    public String visit(CollectionFilter filter) {
        String collectionString = visit(filter.getCollection());

        if(filter.getPredicate() != null) {
            String subjectName = availableSubjectNames.pop();
            Type type = filter.getCollection().getEvaluationType().unwrapArrayType();
            currentPredicateSubject.push(new Subject(subjectName, type));
            String filterPredicateString = PATH_SEPARATOR
                + "filter(" + subjectName + "=>" + visit(filter.getPredicate()) + ")";
            availableSubjectNames.push(currentPredicateSubject.pop().getSubjectName());
            return "(" + collectionString + " || [])" + filterPredicateString;
        }

        return collectionString;
    }

    @Override
    public String visit(BooleanLiteral booleanLiteral) {
        return booleanLiteral.getValue().toString();
    }

    @Override
    public String visit(StringLiteral stringLiteral) {
        return "'" + escape(stringLiteral.getValue()) + "'";
    }

    private static String escape(String str) {
        return str.replace("\\", "\\\\")
            .replace("'", "\\'");
    }

    @Override
    public String visit(NumberLiteral numberLiteral) {
        return numberLiteral.toString();
    }

    @Override
    public String visit(DateLiteral dateLiteral) {
        return f("Date('" + dateLiteral + "')");
    }

    @Override
    public String visit(DateTimeLiteral dateTimeLiteral) {
        return f("DateTime('" + dateTimeLiteral + "')");
    }

    @Override
    public String visit(In in) {
        String object = visit(in.getLeft());
        String collection = visit(in.getRight());
        return "this._in("+ collection +", " + object + ")";
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
        return f("_eq(" + visit(equals.getLeft()) + ", " + visit(equals.getRight()) + ")");
    }

    @Override
    public String visit(NotEquals notEquals) {
        return f("_neq(" + visit(notEquals.getLeft()) + ", " + visit(notEquals.getRight()) + ")");
    }

    @Override
    public String visit(Modulus modulus) {
        return f("_mod(" + visit(modulus.getLeft()) + ", " + visit(modulus.getRight()) + ")");
    }

    @Override
    public String visit(Subtraction subtraction) {
        return f("_sub(" + visit(subtraction.getLeft()) + ", " + visit(subtraction.getRight()) + ")");
    }

    @Override
    public String visit(Multiplication multiplication) {
        return f("_mult(" + visit(multiplication.getLeft()) + ", " + visit(multiplication.getRight()) + ")");
    }

    @Override
    public String visit(Exponent exponent) {
        return f("_pow(" + visit(exponent.getLeft()) + ", " + visit(exponent.getRight()) + ")");
    }

    @Override
    public String visit(Division division) {
        return f("_div(" + visit(division.getLeft()) + ", " + visit(division.getRight()) + ")");
    }

    @Override
    public String visit(Addition addition) {
        return f("_add(" + visit(addition.getLeft()) + ", " + visit(addition.getRight()) + ")");
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
                .map(this::visit)
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
        return f(
            function.getParameters().stream()
                .map(this::visit)
                .collect(Collectors.joining(",", function.getFunctionName() + "(", ")"))
        );
    }

    @Override
    public String visit(Path path) {
        String separator = path.isNullSafe() ? NULL_SAFE_PATH_SEPARATOR : PATH_SEPARATOR;

        String object = visit(path.getObject());
        if(path.getObject().getEvaluationType().isAssignableToArray() || path.getObject() instanceof CollectionFilter) {
            String subjectName = availableSubjectNames.pop();
            Type type = path.getObject().getEvaluationType().unwrapArrayType();
            currentPredicateSubject.push(new Subject(subjectName, type));
            String property = visit(path.getProperty());
            String expression;
            if(path.getObject().getEvaluationType().isDynamic()) {
                expression = f("_flatMap(" + object + "," + subjectName + "=>" + subjectName + separator + property + ")");
            } else {
                expression = "(" + object + " || [])" + PATH_SEPARATOR
                    + "map(" + subjectName + "=>" + subjectName + separator + property + ")";
                if(path.getProperty().getEvaluationType().isAssignableToArray()) {
                    expression = expression + PATH_SEPARATOR + NESTED_ARRAY_FLATTING_FUNCTION;
                }
            }
            availableSubjectNames.push(currentPredicateSubject.pop().getSubjectName());
            return expression;
        } else {
            String property = visit(path.getProperty());
            return object + separator + property;
        }
    }

    @Override
    public String visit(ReferenceValue reference) {
        return wrapIfMoney(visit(reference.getReference()), reference);
    }

    @Override
    public String visit(Identifier identifier) {
        String prefix = "";

        if(currentReferenceValue != null) {
            ScopeType scopeType = currentReferenceValue.findScopeTypeOfReference();
            if(currentReferenceValue.getThisNode() == null) {
                if(scopeType == ScopeType.FILTER) {
                    // if reference is without this, then resolve property from the closest element that has it.
                    // If a dynamic type can have that property, then property is resolved dynamically at runtime
                    // from the closest stack element that has the property.
                    prefix = findStaticSubjectWithProperty(identifier.getIdentifierToken())
                        .map(Subject::getSubjectName)
                        .orElseGet(() -> resolvePropertyDynamically(identifier)) + PATH_SEPARATOR;
                }
                if(scopeType == ScopeType.LOCAL) {
                    prefix = "__dataObject__"  + PATH_SEPARATOR;
                }
            }
            currentReferenceValue = null;
        }

        // propagate path null safety to field path
        String fieldPathSeparator = findFirstPathFromClosestReference()
            .filter(Path::isNullSafe)
            .map(nullSafePath -> NULL_SAFE_PATH_SEPARATOR)
            .orElse(PATH_SEPARATOR);

        return Arrays.stream(identifier.getIdentifierParts())
            .collect(Collectors.joining(fieldPathSeparator, prefix, ""));
    }

    private String resolvePropertyDynamically(Identifier identifier) {
        String property = identifier.getIdentifierToken();
        boolean localScopeCouldHaveProperty = Optional
            .ofNullable(currentReferenceValue.getScope().findClosestScopeOfType(ScopeType.LOCAL))
            .map(local -> local.isDynamic() || local.isReferenceStrictlyInImmediateScope(property))
            .orElse(false);
        boolean globalScopeCouldHaveProperty = Optional
            .ofNullable(currentReferenceValue.getScope().findClosestScopeOfType(ScopeType.GLOBAL))
            .map(global -> global.isDynamic() || global.isReferenceStrictlyInImmediateScope(property))
            .orElse(false);

        List<String> subjectsThatCouldHaveProperty = currentPredicateSubject.stream()
            .filter(s -> s.getType().isDynamic() || s.getType().getProperties().getReferences().containsKey(property))
            .map(Subject::getSubjectName)
            .collect(Collectors.toList());
        if(localScopeCouldHaveProperty) {
            subjectsThatCouldHaveProperty.add("__dataObject__");
        }
        if(globalScopeCouldHaveProperty) {
            subjectsThatCouldHaveProperty.add("__references__");
        }

        if(subjectsThatCouldHaveProperty.size() == 1) {
            return subjectsThatCouldHaveProperty.get(0);
        }
        return f("_o('" + identifier.getIdentifierParts()[0] + "',[" + String.join(", ", subjectsThatCouldHaveProperty) + "])");
    }

    private Optional<Subject> findStaticSubjectWithProperty(String property) {
        for(Subject subject : currentPredicateSubject) {
            if(subject.getType().getProperties().getReferences().containsKey(property)) {
                return Optional.of(subject);
            }
            if(subject.getType().isDynamic()) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public String visit(AccessByIndex accessByIndex) {
        return visit(accessByIndex.getCollection()) + "[" + visit(accessByIndex.getIndexExpression()) + "]";
    }

    @Override
    public String visit(ForEach forEach) {
        String collectionString = visit(forEach.getCollection());
        String returnString = visit(forEach.getReturnExpression());

        String forEachString = "(" + collectionString + " || []).map(" + forEach.getVar() + "=>" + returnString + ")";
        if(forEach.getReturnExpression().getEvaluationType().isAssignableToArray()) {
            forEachString  = forEachString + PATH_SEPARATOR + NESTED_ARRAY_FLATTING_FUNCTION;
        }
        return forEachString;
    }

    @Override
    public String visit(ForSome forSome) {
        String collectionString = visit(forSome.getCollection());
        String returnString = visit(forSome.getReturnExpression());

        return "(" + collectionString + " || []).some(" + forSome.getVar() + "=>" + returnString + ")";
    }

    @Override
    public String visit(ForEvery forEvery) {
        String collectionString = visit(forEvery.getCollection());
        String returnString = visit(forEvery.getReturnExpression());

        return "(" + collectionString + " || []).every(" + forEvery.getVar() + "=>" + returnString + ")";
    }

    @Override
    public String visit(This aThis) {
        ScopeType scopeType = currentReferenceValue.findScopeTypeOfReference();
        if(scopeType == ScopeType.FILTER) {
            return currentPredicateSubject.peek().getSubjectName();
        }
        return "__dataObject__";
    }

    @Override
    public String visit(If anIf) {
        String conditionString = coerceBoolean(anIf.getCondition());
        String thenString = visit(anIf.getThenExpression());
        String elseString = anIf.getElseExpression()
            .map(elseNode -> visit(elseNode))
            .orElse("undefined");

        StringBuilder sb = new StringBuilder();
        sb.append("(")
            .append(conditionString)
            .append(" ? ")
            .append(thenString)
            .append(" : ")
            .append(elseString)
            .append(")");

        return sb.toString();
    }

    @Override
    public String visit(Null aNull) {
        return "undefined";
    }

    @Override
    public String visit(Empty empty) {
        return "undefined";
    }

    @Override
    public String visit(ValueBlock valueBlock) {
        return "(()=>{"
            + valueBlock.getVariables().stream().map(v -> this.visit(v) + ";").collect(Collectors.joining())
            + "return " + visit(valueBlock.getValue()) + ";"
            + "})()";
    }

    @Override
    public String visit(Variable variable) {
        return "const " + variable.getVariableName() + "=" + visit(variable.getValue());
    }

    private String coerceBoolean(Expression expression) {
        if(isReference(expression)) {
            return f("_b(" + visit(expression) + ")");
        }
        return visit(expression);
    }

    private String coerceNumberOrDate(Expression expression) {
        if(isReference(expression)) {
            return f("_nd(" + visit(expression) + ")");
        }
        return visit(expression);
    }

    private String coerceNumber(Expression expression) {
        if(isReference(expression)) {
            return f("_n(" + visit(expression) + ")");
        }
        return visit(expression);
    }

    private String coerceString(Expression expression) {
        if(isReference(expression)) {
            return f("_s(" + visit(expression) + ")");
        }
        return visit(expression);
    }

    private boolean isReference(Expression expression) {
        return expression instanceof ReferenceValue;
    }

    private String wrapIfMoney(String string, Expression expression) {
        if (expression.getEvaluationType() == Type.MONEY) {
            Iterator<Expression> nodeIterator = astNodeQueue.iterator();
            nodeIterator.next(); // last is expression in the parameters
            Expression previousNode = null;

            if (nodeIterator.hasNext()) {
                previousNode = nodeIterator.next();
            }

            boolean alreadyWrapped = previousNode instanceof Function
                && ((Function) previousNode).getFunctionName().equals("FromMoney");

            if (!alreadyWrapped) {
                return f("FromMoney(" + string + ")");
            }
        }

        return string;
    }

    private String f(String functionCall) {
        // at runtime, 'this' object is a function container which has every function assigned
        return "this" + PATH_SEPARATOR + functionCall;
    }

    static class Subject {
        private final String subjectName;
        private final Type type;

        public Subject(@Nonnull String subjectName, @Nonnull Type type) {
            this.subjectName = subjectName;
            this.type = type;
        }

        @Nonnull
        public String getSubjectName() {
            return subjectName;
        }

        @Nonnull
        public Type getType() {
            return type;
        }
    }

}
