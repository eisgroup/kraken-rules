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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.el.ast.AccessByIndex;
import kraken.el.ast.Addition;
import kraken.el.ast.And;
import kraken.el.ast.ArithmeticOperation;
import kraken.el.ast.BinaryExpression;
import kraken.el.ast.BinaryLogicalOperation;
import kraken.el.ast.Cast;
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
import kraken.el.ast.NumberLiteral;
import kraken.el.ast.Or;
import kraken.el.ast.Path;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.Subtraction;
import kraken.el.ast.UnaryExpression;
import kraken.el.ast.ValueBlock;
import kraken.el.ast.Variable;
import kraken.el.ast.builder.Literals;
import kraken.el.ast.validation.details.AstDetails;
import kraken.el.ast.validation.details.ComparisonTypeDetails;
import kraken.el.ast.validation.details.FunctionParameterTypeDetails;
import kraken.el.ast.visitor.AstTraversingVisitor;
import kraken.el.math.Numbers;
import kraken.el.scope.ScopeType;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class AstValidatingVisitor extends AstTraversingVisitor {

    private final Set<ReferenceValue> referenceValuesWithIdentifierErrors = new HashSet<>();

    private final Collection<AstMessage> messages = new ArrayList<>();

    private final Deque<ReferenceValue> currentReferenceValue = new LinkedList<>();

    @Override
    public Expression visit(If anIf) {
        checkNotNullLiteral(anIf.getCondition(), anIf.getNodeType());

        String conditionTemplate = "Condition in {0} operation must be of type ''{1}'' but is ''{2}''";
        if(!Type.BOOLEAN.isAssignableFrom(anIf.getCondition().getEvaluationType())) {
            messages.add(createError(MessageFormat.format(conditionTemplate, anIf.getNodeType(), Type.BOOLEAN, anIf.getCondition().getEvaluationType()), anIf));
        }

        return super.visit(anIf);
    }

    @Override
    public Expression visit(In in) {
        if(!in.getRight().getEvaluationType().isAssignableToArray()) {
            String template = "Right side of {0} operation must be array but was of type ''{1}''";
            messages.add(
                createError(MessageFormat.format(template, in.getNodeType(), in.getRight().getEvaluationType()), in));
        } else {
            validateLeftBinary(in, in.getRight().getEvaluationType().unwrapArrayType());
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
            messages.add(
                createError(MessageFormat.format(template, matchesRegExp.getLeft().getEvaluationType()), matchesRegExp));
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
            String template = "Both sides of operator ''{0}'' must have same type, but left side was of type ''{1}'' and right side was of type ''{2}''.";
            ComparisonTypeDetails details = new ComparisonTypeDetails(
                expression.getLeft(),
                expression.getRight()
            );

            String message = MessageFormat.format(
                template,
                expression.getNodeType(),
                expression.getLeft().getEvaluationType(),
                expression.getRight().getEvaluationType()
            );

            messages.add(createError(message, expression, details));
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
        if(reference.getReference().isEmpty()) {
            String messageTemplate = "Path is incomplete: ''{0}''. Property is missing.";
            String message = MessageFormat.format(messageTemplate, reference.getToken());
            messages.add(createError(message, reference));
        }
        Expression e = super.visit(reference);
        currentReferenceValue.pop();
        return e;
    }

    @Override
    public Expression visit(Function function) {
        String template = "Function ''{0}'' with {1} parameter(s) does not exist.";
        function.getScope().resolveFunctionSymbol(function.getFunctionName(), function.getParameters().size())
            .ifPresentOrElse(
                functionSymbol -> validateParameters(function, functionSymbol),
                () -> messages.add(
                    createError(
                        MessageFormat.format(
                            template,
                            function.getFunctionName(),
                            function.getParameters().size()),
                        function
                    )
                )
            );

        return super.visit(function);
    }

    private void validateParameters(Function function, FunctionSymbol functionSymbol) {
        for(FunctionParameter parameter : functionSymbol.getParameters()) {
            int i = parameter.getParameterIndex();
            Type argumentValueType = function.getParameters().get(i).getEvaluationType();
            if(parameter.getType().isGeneric()) {
                Type argumentTypeBoundsOnly = parameter.getType().rewriteGenericBounds();
                if(!argumentTypeBoundsOnly.isAssignableFrom(argumentValueType)) {
                    String template = "Incompatible type ''{0}'' of function parameter at index {1} "
                        + "when invoking function {2}. Type must be assignable to ''{3}''.";
                    String message = MessageFormat.format(template, argumentValueType, i, function, argumentTypeBoundsOnly);
                    FunctionParameterTypeDetails details = new FunctionParameterTypeDetails(
                        function.getParameters().get(i),
                        argumentTypeBoundsOnly
                    );
                    messages.add(createError(message, function, details));
                }
            } else if(!parameter.getType().isAssignableFrom(argumentValueType)) {
                String template = "Incompatible type ''{0}'' of function parameter at index {1} "
                    + "when invoking function {2}. Expected type is ''{3}''.";
                String message = MessageFormat.format(template, argumentValueType, i, function, parameter.getType());
                FunctionParameterTypeDetails details = new FunctionParameterTypeDetails(
                    function.getParameters().get(i),
                    parameter.getType()
                );
                messages.add(createError(message, function, details));
            }
        }
    }

    @Override
    public Expression visit(Path path) {
        if(path.getProperty().isEmpty()) {
            String messageTemplate = "Path is incomplete: ''{0}''. Property is missing.";
            String message = MessageFormat.format(messageTemplate, path.getToken());
            messages.add(createError(message, path));
        } else if(!(path.getProperty() instanceof Identifier) && !(path.getProperty() instanceof AccessByIndex)) {
            String messageTemplate = "Unsupported path expression ''{0}''. "
                + "Property expression shall be identifier or access by index, but found: ''{1}''";
            String message = MessageFormat.format(messageTemplate, path.getToken(), path.getProperty().getNodeType());
            messages.add(createError(message, path));
        }
        return super.visit(path);
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

    @Override
    public Expression visit(Variable variable) {
        validateUniqueVariableNameInScope(variable.getVariableName(), variable);
        if(variable.getValue().isEmpty()) {
            String template = "Variable assignment is incomplete because value statement is missing in: ''{0}''. "
                + "Variable name must be followed by keyword 'to' and then a value statement.";
            String message = MessageFormat.format(template, variable.getToken());
            messages.add(createError(message, variable));
        }

        return super.visit(variable);
    }

    @Override
    public Expression visit(ValueBlock valueBlock) {
        if(valueBlock.getValue().isEmpty()) {
            String messageTemplate = "Expression is incomplete because value statement is missing in: ''{0}''. "
                + "Variable assignments must be followed by keyword 'return' and then a value statement.";
            String message = MessageFormat.format(messageTemplate, valueBlock.getToken());
            messages.add(createError(message, valueBlock));
        }

        return super.visit(valueBlock);
    }

    private void validateUniqueVariableNameInScope(String variable, Expression expression) {
        if(expression.getScope().isReferenceStrictlyInScope(variable)) {
            String variableNameClashTemplate = "Variable ''{0}'' defined in {1} operation is already defined in scope: {2}";
            messages.add(
                    createError(
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
            messages.add(
                    createError(
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
        if(!evaluationType.isAssignableToArray()) {
            messages.add(
                createError(MessageFormat.format(template, expression.getNodeType(), evaluationType), expression));
        }
    }

    @Override
    public Expression visit(Identifier identifier) {
        if(identifier.getScope().resolveReferenceSymbol(identifier.getIdentifierToken()).isEmpty()) {
            if(!referenceValuesWithIdentifierErrors.contains(currentReferenceValue.peek())) {
                String message;
                if (identifier.getScope().getScopeType().equals(ScopeType.PATH)) {
                    message = MessageFormat.format(
                        "Attribute ''{0}'' not found in ''{1}''.",
                        identifier.getIdentifierToken(),
                        identifier.getScope()
                    );
                } else {
                    message = MessageFormat.format(
                        "Reference ''{0}'' not found.",
                        identifier.getIdentifierToken()
                    );
                }

                messages.add(createError(message, identifier));
                referenceValuesWithIdentifierErrors.add(currentReferenceValue.peek());
            }
        }
        return super.visit(identifier);
    }

    @Override
    public Expression visit(Cast cast) {
        Type referenceType = cast.getReference().getEvaluationType();

        if (referenceType.isKnown() && !referenceType.isDynamic()) {
            Type castType = cast.getScope().resolveTypeOf(cast.getTypeLiteral().getValue());
            if(!castType.isKnown()) {
                String template = "Unknown type: '%s'";
                String message = String.format(template, cast.getTypeLiteral().getValue());
                messages.add(createError(message, cast.getTypeLiteral()));
            } else if(castType.isGeneric()) {
                String message = "Casting to generic type is not allowed";
                messages.add(createError(message, cast.getTypeLiteral()));
            } else if(castType.isUnion()) {
                String message = "Casting to union type is not allowed";
                messages.add(createError(message, cast.getTypeLiteral()));
            } else if(castType.equals(referenceType)) {
                String template = "Cast is redundant because type of object is already '%s'";
                String message = String.format(template, referenceType);
                messages.add(createInfo(message, cast.getTypeLiteral()));
            } else if(castType.isAssignableFrom(referenceType)) {
                String template = "Cast to '%s' is redundant because object type is '%s' and it extends '%1$s'";
                String message = String.format(template, castType, referenceType);
                messages.add(createInfo(message, cast.getTypeLiteral()));
            } else if(!referenceType.isAssignableFrom(castType)) {
                String template = "Cast to '%s' could be an error because object type is '%s' and it is not "
                    + "a supertype of '%1$s'";
                String message = String.format(template, castType, referenceType);
                messages.add(createWarning(message, cast.getTypeLiteral()));
            }
        }
        return super.visit(cast);
    }

    @Override
    public Expression visit(NumberLiteral numberLiteral) {
        if(numberLiteral.getValue().stripTrailingZeros().precision() > Numbers.DEFAULT_MATH_CONTEXT.getPrecision()) {
            String template = "Number '%s' cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to '%s'";
            String message = String.format(
                template,
                numberLiteral.getValue().toPlainString(),
                Numbers.normalized(numberLiteral.getValue()).toPlainString()
            );
            messages.add(createWarning(message, numberLiteral));
        }
        return super.visit(numberLiteral);
    }

    private void validateUnary(UnaryExpression e, Type expectedType) {
        checkNotNullLiteral(e, e.getNodeType());

        String template = "Operation {0} can only be performed on type ''{1}'' but was performed on ''{2}''";
        if(!expectedType.isAssignableFrom(e.getEvaluationType())) {
            messages.add(
                createError(MessageFormat.format(template, e.getNodeType(), expectedType, e.getEvaluationType()), e));
        }
    }

    private void validateBinaryComparison(BinaryExpression e) {
        checkNotNullLiteral(e.getLeft(), e.getNodeType());
        checkNotNullLiteral(e.getRight(), e.getNodeType());

        if(!e.getLeft().getEvaluationType().isComparableWith(e.getRight().getEvaluationType())) {
            String template = "Operation {0} can only be performed on comparable types, but was performed on ''{1}'' and ''{2}''";
            messages.add(createError(MessageFormat.format(template, e.getNodeType(), e.getLeft().getEvaluationType(), e.getRight().getEvaluationType()), e));
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
            messages.add(createError(MessageFormat.format(template, e.getNodeType(), expectedType, e.getRight().getEvaluationType()), e));
        }
    }

    private void validateLeftBinary(BinaryExpression e, Type expectedType) {
        checkNotNullLiteral(e, e.getNodeType());

        String template = "Left side of {0} operation must be of type ''{1}'' but was ''{2}''";
        if(!expectedType.isAssignableFrom(e.getLeft().getEvaluationType())) {
            messages.add(createError(MessageFormat.format(template, e.getNodeType(), expectedType, e.getLeft().getEvaluationType()), e));
        }
    }

    private void checkNotNullLiteral(Expression e, NodeType nodeType) {
        if(e instanceof Null) {
            String template = "Operand cannot be 'null' literal for operation: {0}";
            messages.add(createError(MessageFormat.format(template, nodeType), e));
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
            messages.add(createError(message, firstLoopExpression));
        }
    }

    private boolean areVersusAssignable(Type type1, Type type2) {
        return type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1);
    }

    public Collection<AstMessage> getMessages() {
        return messages;
    }

    private AstMessage createError(String message, Expression expression) {
        return createError(message, expression, null);
    }

    private AstMessage createError(String message, Expression expression, AstDetails details) {
        String template = "error in ''{0}'' with message: {1}";
        return createAstMessage(message, template, expression, AstMessageSeverity.ERROR, details);
    }

    private AstMessage createWarning(String message, Expression expression) {
        String template = "warning about ''{0}'' with message: {1}";
        return createAstMessage(message, template, expression, AstMessageSeverity.WARNING, null);
    }

    private AstMessage createInfo(String message, Expression expression) {
        String template = "info about ''{0}'' with message: {1}";
        return createAstMessage(message, template, expression, AstMessageSeverity.INFO, null);
    }

    private AstMessage createAstMessage(String message, String template, Expression expression,
                                        AstMessageSeverity severity, AstDetails details) {
        Expression failedExpression = currentReferenceValue.peek() != null ? currentReferenceValue.peek() : expression;
        String formattedMessage = MessageFormat.format(template, failedExpression.getToken(), message);
        return new AstMessage(formattedMessage, currentReferenceValue.peek(), expression, severity, details);
    }

}
