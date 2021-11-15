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
package kraken.el.ast.validation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.el.ast.AccessByIndex;
import kraken.el.ast.Addition;
import kraken.el.ast.And;
import kraken.el.ast.ArithmeticOperation;
import kraken.el.ast.BinaryExpression;
import kraken.el.ast.BinaryLogicalOperation;
import kraken.el.ast.CollectionFilter;
import kraken.el.ast.Division;
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
import kraken.el.ast.LessThan;
import kraken.el.ast.LessThanOrEquals;
import kraken.el.ast.MatchesRegExp;
import kraken.el.ast.Modulus;
import kraken.el.ast.MoreThan;
import kraken.el.ast.MoreThanOrEquals;
import kraken.el.ast.Multiplication;
import kraken.el.ast.Negation;
import kraken.el.ast.Negative;
import kraken.el.ast.NodeType;
import kraken.el.ast.NotEquals;
import kraken.el.ast.Null;
import kraken.el.ast.Or;
import kraken.el.ast.Path;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.Subtraction;
import kraken.el.ast.UnaryExpression;
import kraken.el.ast.visitor.AstTraversingVisitor;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class AstValidatingVisitor extends AstTraversingVisitor {

    private final Set<ReferenceValue> referenceValuesWithIdentifierErrors = new HashSet<>();

    private final Collection<AstError> syntaxErrors = new ArrayList<>();

    private final Deque<ReferenceValue> currentReferenceValue = new LinkedList<>();

    @Override
    public Expression visit(If anIf) {
        checkNotNullLiteral(anIf.getCondition(), anIf.getNodeType());

        String thenElseTemplate = "Return types must be the same between THEN and ELSE blocks in IF expression, " +
                "but return type of THEN is ''{0}'' while return type of ELSE is ''{1}''";
        Type thenExpressionType = anIf.getThenExpression().getEvaluationType();
        anIf.getElseExpression()
                .filter(elseExpression -> !areVersusAssignable(thenExpressionType, elseExpression.getEvaluationType()))
                .ifPresent(elseExpression -> syntaxErrors.add(
                        error(MessageFormat.format(thenElseTemplate, thenExpressionType, elseExpression.getEvaluationType()), anIf))
                );

        String conditionTemplate = "Condition in {0} operation must be of type ''{1}'' but is ''{2}''";
        if(!Type.BOOLEAN.isAssignableFrom(anIf.getCondition().getEvaluationType())) {
            syntaxErrors.add(error(MessageFormat.format(conditionTemplate, anIf.getNodeType(), Type.BOOLEAN, anIf.getCondition().getEvaluationType()), anIf));
        }

        return super.visit(anIf);
    }

    @Override
    public Expression visit(In in) {
        if(!in.getRight().getEvaluationType().equals(Type.ANY) && !(in.getRight().getEvaluationType() instanceof ArrayType)) {
            String template = "Right side of {0} operation must be array but was of type ''{1}''";
            syntaxErrors.add(error(MessageFormat.format(template, in.getNodeType(), in.getRight().getEvaluationType()), in));
        }

        if(in.getRight().getEvaluationType() instanceof ArrayType) {
            Type arrayType = ((ArrayType)in.getRight().getEvaluationType()).getElementType();
            validateLeftBinary(in, arrayType);
        }

        return super.visit(in);
    }

    @Override
    public Expression visit(MoreThanOrEquals moreThanOrEquals) {
        validateBinaryComparison(moreThanOrEquals);
        return super.visit(moreThanOrEquals);
    }

    @Override
    public Expression visit(MoreThan moreThan) {
        validateBinaryComparison(moreThan);
        return super.visit(moreThan);
    }

    @Override
    public Expression visit(LessThanOrEquals lessThanOrEquals) {
        validateBinaryComparison(lessThanOrEquals);
        return super.visit(lessThanOrEquals);
    }

    @Override
    public Expression visit(LessThan lessThan) {
        validateBinaryComparison(lessThan);
        return super.visit(lessThan);
    }

    @Override
    public Expression visit(And and) {
        validateBinaryLogicalOperation(and);
        return super.visit(and);
    }

    @Override
    public Expression visit(Or or) {
        validateBinaryLogicalOperation(or);
        return super.visit(or);
    }

    @Override
    public Expression visit(MatchesRegExp matchesRegExp) {
        checkNotNullLiteral(matchesRegExp.getLeft(), matchesRegExp.getNodeType());

        if(!Type.STRING.isAssignableFrom(matchesRegExp.getLeft().getEvaluationType())) {
            String template = "RegExp value must be of type 'STRING' but was ''{2}''";
            syntaxErrors.add(error(MessageFormat.format(template, matchesRegExp.getLeft().getEvaluationType()), matchesRegExp));
        }

        return super.visit(matchesRegExp);
    }

    @Override
    public Expression visit(Equals equals) {
        validateTypeCompatibility(equals);
        return super.visit(equals);
    }

    @Override
    public Expression visit(NotEquals notEquals) {
        validateTypeCompatibility(notEquals);
        return super.visit(notEquals);
    }

    private void validateTypeCompatibility(BinaryExpression expression) {
        if(!areVersusAssignable(expression.getLeft().getEvaluationType(), expression.getRight().getEvaluationType())) {
            String template = "Both sides of operator ''{0}'' must have same type, but left side was of type ''{1}'' and right side was of type ''{2}''";
            syntaxErrors.add(error(
                    MessageFormat.format(
                            template,
                            expression.getNodeType(),
                            expression.getLeft().getEvaluationType(),
                            expression.getRight().getEvaluationType()
                    ),
                    expression)
            );
        }
    }

    @Override
    public Expression visit(Modulus modulus) {
        validateArithmeticOperation(modulus);
        return super.visit(modulus);
    }

    @Override
    public Expression visit(Subtraction subtraction) {
        validateArithmeticOperation(subtraction);
        return super.visit(subtraction);
    }

    @Override
    public Expression visit(Multiplication multiplication) {
        validateArithmeticOperation(multiplication);
        return super.visit(multiplication);
    }

    @Override
    public Expression visit(Exponent exponent) {
        validateArithmeticOperation(exponent);
        return super.visit(exponent);
    }

    @Override
    public Expression visit(Division division) {
        validateArithmeticOperation(division);
        return super.visit(division);
    }

    @Override
    public Expression visit(Addition addition) {
        validateArithmeticOperation(addition);
        return super.visit(addition);
    }

    @Override
    public Expression visit(Negation negation) {
        checkNotNullLiteral(negation, negation.getNodeType());
        validateUnary(negation, Type.BOOLEAN);
        return super.visit(negation);
    }

    @Override
    public Expression visit(Negative negative) {
        checkNotNullLiteral(negative, negative.getNodeType());
        validateUnary(negative, Type.NUMBER);
        return super.visit(negative);
    }

    @Override
    public Expression visit(ReferenceValue reference) {
        currentReferenceValue.push(reference);
        Expression e = super.visit(reference);
        currentReferenceValue.pop();
        return e;
    }

    @Override
    public Expression visit(Function function) {
        String template = "Function {0} cannot be invoked in scope: {1}";
        function.getScope().resolveFunctionSymbol(function.getFunctionName(), function.getParameters().size())
                .ifPresentOrElse(
                        functionSymbol -> validateParameters(function, functionSymbol),
                        () -> syntaxErrors.add(error(MessageFormat.format(template, function, function.getScope()), function))
                );

        return super.visit(function);
    }

    @Override
    public Expression visit(Path path) {
        if(!(path.getProperty() instanceof Identifier) && !(path.getProperty() instanceof AccessByIndex)) {
            String messageTemplate = "Unsupported path expression ''{0}''. Property expression shall be identifier or access by index, but found: ''{1}''";
            String message = MessageFormat.format(messageTemplate, path.getToken(), path.getProperty().getNodeType());
            syntaxErrors.add(error(message, path));
        }
        return super.visit(path);
    }

    private void validateParameters(Function function, FunctionSymbol functionSymbol) {
        for(int i = 0; i < functionSymbol.getParameters().size(); i++) {
            Type expressionParameterType = function.getParameters().get(i).getEvaluationType();
            Type functionParameterType = functionSymbol.getParameters().get(i).getType();
            if(!functionParameterType.isAssignableFrom(expressionParameterType)) {
                String messageTemplate = "Incompatible type ''{0}'' of function parameter at index {1} when invoking function {2}. Expected type is ''{3}''";
                String message = MessageFormat.format(messageTemplate, expressionParameterType, i, function, functionParameterType);
                syntaxErrors.add(error(message, function));
            }
        }
    }

    @Override
    public Expression visit(AccessByIndex accessByIndex) {
        validateCollectionOperation(accessByIndex, accessByIndex.getCollection().getEvaluationType());
        return super.visit(accessByIndex);
    }

    @Override
    public Expression visit(CollectionFilter collectionFilter) {
        validateCollectionOperation(collectionFilter, collectionFilter.getCollection().getEvaluationType());
        checkCyclomaticComplexity(collectionFilter);
        return super.visit(collectionFilter);
    }

    @Override
    public Expression visit(ForEach forEach) {
        validateUniqueVariableNameInScope(forEach.getVar(), forEach);
        validateCollectionOperation(forEach, forEach.getCollection().getEvaluationType());
        checkCyclomaticComplexity(forEach);
        return super.visit(forEach);
    }

    @Override
    public Expression visit(ForSome forSome) {
        validateUniqueVariableNameInScope(forSome.getVar(), forSome);
        validateCollectionOperation(forSome, forSome.getCollection().getEvaluationType());
        validateBooleanReturnType(forSome, forSome.getReturnExpression());
        checkCyclomaticComplexity(forSome);
        return super.visit(forSome);
    }

    @Override
    public Expression visit(ForEvery forEvery) {
        validateUniqueVariableNameInScope(forEvery.getVar(), forEvery);
        validateCollectionOperation(forEvery, forEvery.getCollection().getEvaluationType());
        validateBooleanReturnType(forEvery, forEvery.getReturnExpression());
        checkCyclomaticComplexity(forEvery);
        return super.visit(forEvery);
    }

    private void validateUniqueVariableNameInScope(String variable, Expression expression) {
        if(expression.getScope().isReferenceInCurrentScope(variable)) {
            String variableNameClashTemplate = "Variable ''{0}'' defined in {1} operation is already defined in scope: {2}";
            syntaxErrors.add(
                    error(
                            MessageFormat.format(variableNameClashTemplate,
                                    variable,
                                    expression.getNodeType(),
                                    expression.getScope()
                            ),
                            expression
                    )
            );
        }
    }

    private void validateBooleanReturnType(Expression expression, Expression returnExpression) {
        if(!Type.BOOLEAN.isAssignableFrom(returnExpression.getEvaluationType())) {
            String returnTypeErrorTemplate = "Return type of {0} operation must be of type ''{1}'' but is ''{2}''";
            syntaxErrors.add(
                    error(
                            MessageFormat.format(returnTypeErrorTemplate,
                                    expression,
                                    Type.BOOLEAN,
                                    returnExpression.getEvaluationType()
                            ),
                            expression
                    )
            );
        }
    }

    private void validateCollectionOperation(Expression expression, Type evaluationType) {
        String template = "Operation {0} can only be performed on array, but was performed on type ''{1}''";
        if(!evaluationType.equals(Type.ANY) && !(evaluationType instanceof ArrayType)) {
            syntaxErrors.add(error(MessageFormat.format(template, expression.getNodeType(), evaluationType), expression));
        }
    }

    @Override
    public Expression visit(Identifier identifier) {
        String template = "Symbol ''{0}'' cannot be resolved in scope: {1}";
        if(identifier.getScope().resolveReferenceSymbol(identifier.getIdentifier()).isEmpty()) {
            if(!referenceValuesWithIdentifierErrors.contains(currentReferenceValue.peek())) {
                syntaxErrors.add(error(MessageFormat.format(template, identifier.getIdentifier(), identifier.getScope()), identifier));
                referenceValuesWithIdentifierErrors.add(currentReferenceValue.peek());
            }
        }
        return super.visit(identifier);
    }

    private void validateUnary(UnaryExpression e, Type expectedType) {
        checkNotNullLiteral(e, e.getNodeType());

        String template = "Operation {0} can only be performed on type ''{1}'' but was performed on ''{2}''";
        if(!expectedType.isAssignableFrom(e.getEvaluationType())) {
            syntaxErrors.add(error(MessageFormat.format(template, e.getNodeType(), expectedType, e.getEvaluationType()), e));
        }
    }

    private void validateBinaryComparison(BinaryExpression e) {
        checkNotNullLiteral(e.getLeft(), e.getNodeType());
        checkNotNullLiteral(e.getRight(), e.getNodeType());

        if(!e.getLeft().getEvaluationType().isComparableWith(e.getRight().getEvaluationType())) {
            String template = "Operation {0} can only be performed on comparable types, but was performed on ''{1}'' and ''{2}''";
            syntaxErrors.add(error(MessageFormat.format(template, e.getNodeType(), e.getLeft().getEvaluationType(), e.getRight().getEvaluationType()), e));
        }
    }

    private void validateArithmeticOperation(ArithmeticOperation e) {
        validateBinary(e, Type.NUMBER, Type.NUMBER);
    }

    private void validateBinaryLogicalOperation(BinaryLogicalOperation e) {
        validateBinary(e, Type.BOOLEAN, Type.BOOLEAN);
    }

    private void validateBinary(BinaryExpression e, Type expectedLeftType, Type expectedRightType) {
        validateLeftBinary(e, expectedLeftType);
        validateRightBinary(e, expectedRightType);
    }

    private void validateRightBinary(BinaryExpression e, Type expectedType) {
        checkNotNullLiteral(e, e.getNodeType());

        String template = "Right side of {0} operation must be of type ''{1}'' but was ''{2}''";
        if(!expectedType.isAssignableFrom(e.getRight().getEvaluationType())) {
            syntaxErrors.add(error(MessageFormat.format(template, e.getNodeType(), expectedType, e.getRight().getEvaluationType()), e));
        }
    }

    private void validateLeftBinary(BinaryExpression e, Type expectedType) {
        checkNotNullLiteral(e, e.getNodeType());

        String template = "Left side of {0} operation must be of type ''{1}'' but was ''{2}''";
        if(!expectedType.isAssignableFrom(e.getLeft().getEvaluationType())) {
            syntaxErrors.add(error(MessageFormat.format(template, e.getNodeType(), expectedType, e.getLeft().getEvaluationType()), e));
        }
    }

    private void checkNotNullLiteral(Expression e, NodeType nodeType) {
        if(e instanceof Null) {
            String template = "Operand cannot be 'null' literal for operation: {0}";
            syntaxErrors.add(error(MessageFormat.format(template, nodeType), e));
        }
    }

    private void checkCyclomaticComplexity(Expression e) {
        List<Expression> nestedLoops = astNodeQueue.stream().filter(
                node -> node instanceof CollectionFilter && ((CollectionFilter) node).getPredicate() != null
                        || node instanceof ForEach
                        || node instanceof ForSome
                        || node instanceof ForEvery)
                .collect(Collectors.toList());

        if(nestedLoops.size() > 3) {
            String message = "Cyclomatic complexity level is too high for expression. " +
                    "Maximum allowed cyclomatic complexity is 3";
            Expression firstLoopExpression = nestedLoops.get(nestedLoops.size() - 1);
            syntaxErrors.add(error(message, firstLoopExpression));
        }
    }

    private boolean areVersusAssignable(Type type1, Type type2) {
        return type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1);
    }

    public Collection<AstError> getSyntaxErrors() {
        return syntaxErrors;
    }

    private AstError error(String message, Expression expression) {
        Expression failedExpression = currentReferenceValue.peek() != null ? currentReferenceValue.peek() : expression;
        String template = "syntax error in ''{0}'' with message: {1}";
        String formattedMessage = MessageFormat.format(template, failedExpression.getToken(), message);
        return new AstError(formattedMessage, currentReferenceValue.peek(), expression);
    }
}
