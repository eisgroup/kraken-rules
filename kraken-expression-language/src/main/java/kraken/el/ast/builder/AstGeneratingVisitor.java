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
package kraken.el.ast.builder;

import kraken.el.Kel;
import kraken.el.Kel.TemplateContext;
import kraken.el.Kel.TemplateTextContext;
import kraken.el.KelBaseVisitor;
import kraken.el.ast.*;
import kraken.el.ast.InlineMap.KeyValuePair;
import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.GenericType;
import kraken.el.scope.type.Type;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Traverses over Kraken Expression Language Parse Tree which is parsed by ANTLR4 from grammar
 *
 * @author mulevicius
 */
public class AstGeneratingVisitor extends KelBaseVisitor<Expression> {

    private Map<String, Function> functions = new HashMap<>();

    private Collection<Reference> references = new ArrayList<>();

    private Deque<Scope> currentScope = new ArrayDeque<>();

    public AstGeneratingVisitor(Scope scope) {
        this.currentScope.add(scope);
    }

    // Precedence
    @Override
    public Expression visitPrecedence(Kel.PrecedenceContext ctx) {
        return visit(ctx.value());
    }

    @Override
    public Expression visitPrecedencePredicate(Kel.PrecedencePredicateContext ctx) {
        return visit(ctx.valuePredicate());
    }

    @Override
    public Expression visitPrecedenceValue(Kel.PrecedenceValueContext ctx) {
        return visit(ctx.indexValue());
    }

    @Override
    public Expression visitReferencePrecedence(Kel.ReferencePrecedenceContext ctx) {
        return visit(ctx.reference());
    }

    @Override
    public Expression visitCast(Kel.CastContext ctx) {
        Reference reference = (Reference) visit(ctx.reference());

        Scope scope = scope();

        TypeLiteral typeLiteral = new TypeLiteral(ctx.type().getText(), scope, token(ctx));
        return new Cast(typeLiteral, reference, scope, token(ctx));
    }

    @Override
    public Expression visitTypeComparison(Kel.TypeComparisonContext ctx) {
        Expression left = visit(ctx.value());
        Identifier typeIdentifier = (Identifier) visit(ctx.identifier());

        Scope scope = scope();
        TypeLiteral type = new TypeLiteral(typeIdentifier.getIdentifier(), scope, token(ctx));

        if(ctx.OP_INSTANCEOF() != null) {
            return new InstanceOf(left, type, scope(), token(ctx));
        }
        if(ctx.OP_TYPEOF() != null) {
            return new TypeOf(left, type, scope(), token(ctx));
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    @Override
    public Expression visitTypeComparisonPredicate(Kel.TypeComparisonPredicateContext ctx) {
        Expression left = visit(ctx.value());
        Identifier typeIdentifier = (Identifier) visit(ctx.identifier());

        Scope scope = scope();
        TypeLiteral type = new TypeLiteral(typeIdentifier.getIdentifier(), scope, token(ctx));

        if(ctx.OP_INSTANCEOF() != null) {
            return new InstanceOf(left, type, scope(), token(ctx));
        }
        if(ctx.OP_TYPEOF() != null) {
            return new TypeOf(left, type, scope(), token(ctx));
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    // RegExp
    @Override
    public Expression visitMatchesRegExp(Kel.MatchesRegExpContext ctx) {
        Expression left = visit(ctx.value());
        String regexp = Literals.stripQuotes(ctx.STRING().getText());
        return new MatchesRegExp(left, regexp, scope(), token(ctx));
    }

    @Override
    public Expression visitMatchesRegExpPredicate(Kel.MatchesRegExpPredicateContext ctx) {
        Expression left = visit(ctx.value());
        String regexp = Literals.stripQuotes(ctx.STRING().getText());
        return new MatchesRegExp(left, regexp, scope(), token(ctx));
    }

    // Multiplication Division Modulus
    private Expression resolveMultiplicationOrDivision(
            Kel.ValueContext leftCtx,
            Kel.ValueContext rightCtx,
            TerminalNode division,
            TerminalNode multiplication,
            TerminalNode modulus,
            Token token
    ) {
        Expression left = visit(leftCtx);
        Expression right = visit(rightCtx);
        if (division != null) {
            return new Division(left, right, scope(), token);
        }
        if (multiplication != null) {
            return new Multiplication(left, right, scope(), token);
        }
        if (modulus != null) {
            return new Modulus(left, right, scope(), token);
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    @Override
    public Expression visitMultiplicationOrDivisionValue(Kel.MultiplicationOrDivisionValueContext ctx) {
        return resolveMultiplicationOrDivision(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_DIV(),
                ctx.OP_MULT(),
                ctx.OP_MOD(),
                token(ctx)
        );
    }

    @Override
    public Expression visitMultiplicationOrDivision(Kel.MultiplicationOrDivisionContext ctx) {
        return resolveMultiplicationOrDivision(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_DIV(),
                ctx.OP_MULT(),
                ctx.OP_MOD(),
                token(ctx)
        );
    }

    // Subtraction Addition
    private Expression resolveSubtractionOrAddition(
            Kel.ValueContext leftCtx,
            Kel.ValueContext rightCtx,
            TerminalNode subtraction,
            TerminalNode addition,
            Token token
    ) {
        Expression left = visit(leftCtx);
        Expression right = visit(rightCtx);
        if (subtraction != null) {
            return new Subtraction(left, right, scope(), token);
        }
        if (addition != null) {
            return new Addition(left, right, scope(), token);
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    @Override
    public Expression visitSubtractionOrAddition(Kel.SubtractionOrAdditionContext ctx) {
        return resolveSubtractionOrAddition(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_MINUS(),
                ctx.OP_ADD(),
                token(ctx)
        );
    }

    @Override
    public Expression visitSubtractionOrAdditionValue(Kel.SubtractionOrAdditionValueContext ctx) {
        return resolveSubtractionOrAddition(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_MINUS(),
                ctx.OP_ADD(),
                token(ctx)
        );
    }

    private ReferenceValue resolveReferenceValueValue(Kel.ReferenceContext ctx, TerminalNode thiz) {
        Reference reference = (Reference) visit(ctx);
        references.add(reference);
        return new ReferenceValue(thiz != null, reference, scope(), reference.getEvaluationType(), token(ctx));
    }

    @Override
    public Expression visitReferenceValueValue(Kel.ReferenceValueValueContext ctx) {
        return resolveReferenceValueValue(ctx.reference(), ctx.THIS());
    }

    @Override
    public Expression visitReferenceValue(Kel.ReferenceValueContext ctx) {
        return resolveReferenceValueValue(ctx.reference(), ctx.THIS());
    }

    // This
    @Override
    public Expression visitThisValue(Kel.ThisValueContext ctx) {
        return new This(scope(), token(ctx));
    }

    @Override
    public Expression visitThis(Kel.ThisContext ctx) {
        return new This(scope(), token(ctx));
    }

    // Negation
    @Override
    public Expression visitNegationPredicate(Kel.NegationPredicateContext ctx) {
        return new Negation(visit(ctx.value()), scope(), token(ctx));
    }

    @Override
    public Expression visitNegation(Kel.NegationContext ctx) {
        return new Negation(visit(ctx.value()), scope(), token(ctx));
    }

    // Numerical Comparison
    private Expression resolveNumericalComparisonPredicate(
            Kel.ValueContext leftCtx,
            Kel.ValueContext rightCtx,
            TerminalNode less,
            TerminalNode lessEq,
            TerminalNode more,
            TerminalNode moreEq,
            Token token
    ) {
        Expression left = visit(leftCtx);
        Expression right = visit(rightCtx);
        if (less != null) {
            return new LessThan(left, right, scope(), token);
        }
        if (lessEq != null) {
            return new LessThanOrEquals(left, right, scope(), token);
        }
        if (more != null) {
            return new MoreThan(left, right, scope(), token);
        }
        if (moreEq != null) {
            return new MoreThanOrEquals(left, right, scope(), token);
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    @Override
    public Expression visitNumericalComparisonPredicate(Kel.NumericalComparisonPredicateContext ctx) {
        return resolveNumericalComparisonPredicate(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_LESS(),
                ctx.OP_LESS_EQUALS(),
                ctx.OP_MORE(),
                ctx.OP_MORE_EQUALS(),
                token(ctx)
        );
    }

    @Override
    public Expression visitNumericalComparison(Kel.NumericalComparisonContext ctx) {
        return resolveNumericalComparisonPredicate(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_LESS(),
                ctx.OP_LESS_EQUALS(),
                ctx.OP_MORE(),
                ctx.OP_MORE_EQUALS(),
                token(ctx)
        );
    }

    // Equality comparison
    private Expression resolveEqualityComparisonPredicate(
            Kel.ValueContext leftCtx,
            Kel.ValueContext rightCtx,
            TerminalNode equals,
            TerminalNode notEquals,
            Token token
    ) {
        Expression left = visit(leftCtx);
        Expression right = visit(rightCtx);
        if (equals != null) {
            return new Equals(left, right, scope(), token);
        }
        if (notEquals != null) {
            return new NotEquals(left, right, scope(), token);
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    @Override
    public Expression visitEqualityComparisonPredicate(Kel.EqualityComparisonPredicateContext ctx) {
        return resolveEqualityComparisonPredicate(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_EQUALS(),
                ctx.OP_NOT_EQUALS(),
                token(ctx)
        );
    }

    @Override
    public Expression visitEqualityComparison(Kel.EqualityComparisonContext ctx) {
        return resolveEqualityComparisonPredicate(
                ctx.value(0),
                ctx.value(1),
                ctx.OP_EQUALS(),
                ctx.OP_NOT_EQUALS(),
                token(ctx)
        );
    }

    // Conjunction
    @Override
    public Expression visitConjunctionPredicate(Kel.ConjunctionPredicateContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new And(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitConjunction(Kel.ConjunctionContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new And(left, right, scope(), token(ctx));
    }

    // Disjunction
    @Override
    public Expression visitDisjunctionPredicate(Kel.DisjunctionPredicateContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new Or(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitDisjunction(Kel.DisjunctionContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new Or(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitAccessByIndex(Kel.AccessByIndexContext ctx) {
        Reference collection = (Reference) visit(ctx.collection);
        Deque<Scope> pathScopes = unwrapParentScopeOfPath();
        Expression index = visit(ctx.indices().indexValue());
        wrapPathScopes(pathScopes);
        return new AccessByIndex(collection, index, scope(), token(ctx));
    }

    private void wrapPathScopes(Deque<Scope> pathScopes) {
        for(Scope pathScope : pathScopes) {
            currentScope.push(pathScope);
        }
    }

    private Deque<Scope> unwrapParentScopeOfPath() {
        Deque<Scope> pathScopes = new LinkedList<>();
        while(currentScope.peek().getScopeType().equals(ScopeType.PATH)) {
            pathScopes.addFirst(currentScope.pop());
        }
        return pathScopes;
    }
    @Override
    public Expression visitExpression(Kel.ExpressionContext ctx) {
        if (ctx.value() == null) {
            return new Null(scope(), token(ctx));
        }
        return visit(ctx.value());
    }

    @Override
    public Expression visitNegative(Kel.NegativeContext ctx) {
        return unwrapNegatedNumberLiteralIfNeeded(visit(ctx.value()), token(ctx));
    }

    @Override
    public Expression visitNegativeValue(Kel.NegativeValueContext ctx) {
        return unwrapNegatedNumberLiteralIfNeeded(visit(ctx.value()), token(ctx));
    }

    private Expression unwrapNegatedNumberLiteralIfNeeded(Expression negatedExpression, Token token) {
        if(negatedExpression instanceof NumberLiteral) {
            BigDecimal positiveDecimal = ((NumberLiteral) negatedExpression).getValue();
            return new NumberLiteral(positiveDecimal.negate(), scope(), token);
        }
        return new Negative(negatedExpression, scope(), token);
    }

    @Override
    public Expression visitExponent(Kel.ExponentContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new Exponent(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitExponentValue(Kel.ExponentValueContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new Exponent(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitIn(Kel.InContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new In(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitInPredicate(Kel.InPredicateContext ctx) {
        Expression left = visit(ctx.value(0));
        Expression right = visit(ctx.value(1));
        return new In(left, right, scope(), token(ctx));
    }

    @Override
    public Expression visitPath(Kel.PathContext ctx) {
        Reference object = (Reference) visit(ctx.object);

        Type evaluationType = object.getEvaluationType();
        if(object.getEvaluationType() instanceof ArrayType) {
            evaluationType = ((ArrayType) object.getEvaluationType()).getElementType();
        }

        currentScope.push(new Scope(ScopeType.PATH, null, evaluationType));
        Reference property = (Reference) visit(ctx.property);
        currentScope.pop();

        return new Path(object, property, scope(), token(ctx));
    }

    @Override
    public Expression visitFilter(Kel.FilterContext ctx) {
        Reference collection = (Reference) visit(ctx.filterCollection);

        Expression predicate = null;
        if(ctx.predicate() != null) {
            currentScope.push(new Scope(ScopeType.FILTER, scope(), unwrapTypeForIteration(collection.getEvaluationType())));
            predicate = ctx.predicate().valuePredicate() != null
                    ? visit(ctx.predicate().valuePredicate())
                    : visit(ctx.predicate().value());
            currentScope.pop();
        }

        return new CollectionFilter(collection, predicate, scope(), token(ctx));
    }

    @Override
    public Expression visitIfValue(Kel.IfValueContext ctx) {
        Expression condition = visit(ctx.condition);
        Expression then = visit(ctx.thenExpression);
        Expression ifElse = ctx.elseExpression != null ? visit(ctx.elseExpression) : null;

        return new If(condition, then, ifElse, scope(), token(ctx));
    }

    @Override
    public Expression visitForEach(Kel.ForEachContext ctx) {
        Expression collection = visit(ctx.collection);

        String var = ctx.var.getText();
        Type forScopeType = buildTypeForIterationContext(collection, var);

        currentScope.push(new Scope(ScopeType.FOR_RETURN_EXPRESSION, scope(), forScopeType));
        Expression returnExpression = visit(ctx.returnExpression);
        currentScope.pop();

        return new ForEach(var, collection, returnExpression, scope(), token(ctx));
    }

    @Override
    public Expression visitForEvery(Kel.ForEveryContext ctx) {
        return buildForEvery(ctx.var, ctx.collection, ctx.returnExpression, token(ctx));
    }

    @Override
    public Expression visitForEveryPredicate(Kel.ForEveryPredicateContext ctx) {
        return buildForEvery(ctx.var, ctx.collection, ctx.returnExpression, token(ctx));
    }

    @Override
    public Expression visitForSome(Kel.ForSomeContext ctx) {
        return buildForSome(ctx.var, ctx.collection, ctx.returnExpression, token(ctx));
    }

    @Override
    public Expression visitForSomePredicate(Kel.ForSomePredicateContext ctx) {
        return buildForSome(ctx.var, ctx.collection, ctx.returnExpression, token(ctx));
    }

    private Expression buildForSome(Kel.IdentifierContext varCtx,
                                    Kel.ValueContext collectionCtx,
                                    Kel.ValueContext returnExpressionCtx,
                                    Token token) {

        Expression collection = visit(collectionCtx);

        String var = varCtx.getText();
        Type forScopeType = buildTypeForIterationContext(collection, var);

        currentScope.push(new Scope(ScopeType.FOR_RETURN_EXPRESSION, scope(), forScopeType));
        Expression returnExpression = visit(returnExpressionCtx);
        currentScope.pop();

        return new ForSome(var, collection, returnExpression, scope(), token);
    }

    private Expression buildForEvery(Kel.IdentifierContext varCtx,
                                    Kel.ValueContext collectionCtx,
                                    Kel.ValueContext returnExpressionCtx,
                                    Token token) {

        Expression collection = visit(collectionCtx);

        String var = varCtx.getText();
        Type forScopeType = buildTypeForIterationContext(collection, var);

        currentScope.push(new Scope(ScopeType.FOR_RETURN_EXPRESSION, scope(), forScopeType));
        Expression returnExpression = visit(returnExpressionCtx);
        currentScope.pop();

        return new ForEvery(var, collection, returnExpression, scope(), token);
    }

    private Type buildTypeForIterationContext(Expression collection, String varName) {
        return new Type("for_" + varName + "_" + UUID.randomUUID().toString(),
                new SymbolTable(Collections.emptyList(),
                        Map.of(varName,
                                new VariableSymbol(varName,
                                        unwrapTypeForIteration(collection.getEvaluationType())
                                )
                        )
                )
        );
    }

    @Override
    public Expression visitInlineArray(Kel.InlineArrayContext ctx) {
        List<Expression> items = ctx.valueList() == null
                ? Collections.emptyList()
                : ctx.valueList().value().stream()
                    .map(valueContext -> visit(valueContext))
                    .collect(Collectors.toList());

        return new InlineArray(items, scope(), token(ctx));
    }

    @Override
    public Expression visitInlineMap(Kel.InlineMapContext ctx) {
        List<KeyValuePair> keyValuePairs = ctx.keyValuePairs().keyValuePair().stream()
                .map(this::toKeyValuePair)
                .collect(Collectors.toList());

        return new InlineMap(keyValuePairs, scope(), token(ctx));
    }

    private KeyValuePair toKeyValuePair(Kel.KeyValuePairContext ctx) {
        return new KeyValuePair(Literals.stripQuotes(ctx.key.getText()), visit(ctx.value()));
    }

    @Override
    public Expression visitIdentifierReference(Kel.IdentifierReferenceContext ctx) {
        return visit(ctx.identifier());
    }

    @Override
    public Expression visitIdentifier(Kel.IdentifierContext ctx) {
        return new Identifier(ctx.getText(), scope(), token(ctx));
    }

    @Override
    public Expression visitBoolean(Kel.BooleanContext ctx) {
        return new BooleanLiteral(Literals.getBoolean(ctx.BOOL().getText()), scope(), token(ctx));
    }

    @Override
    public Expression visitDecimal(Kel.DecimalContext ctx) {
        try {
            return new NumberLiteral(Literals.getDecimal(ctx.decimalLiteral().getText()), scope(), token(ctx));
        } catch (ArithmeticException e) {
            throw new IllegalStateException(
                    "Cannot parse decimal literal without loss of precision, " +
                            "because decimal literal exceeds 64bit Decimal prevision: " + ctx.decimalLiteral().getText()
            );
        }
    }

    @Override
    public Expression visitString(Kel.StringContext ctx) {
        return new StringLiteral(Literals.stripQuotes(ctx.STRING().getText()), scope(), token(ctx));
    }

    @Override
    public Expression visitNull(Kel.NullContext ctx) {
        return new Null(scope(), token(ctx));
    }

    @Override
    public Expression visitDateTime(Kel.DateTimeContext ctx) {
        LocalDateTime dateTime = Literals.getDateTime(ctx.TIME_TOKEN().getText());
        return new DateTimeLiteral(dateTime, scope(), token(ctx));
    }

    @Override
    public Expression visitDate(Kel.DateContext ctx) {
        LocalDate date = Literals.getDate(ctx.DATE_TOKEN().getText());
        return new DateLiteral(date, scope(), token(ctx));
    }

    @Override
    public Expression visitFunction(Kel.FunctionContext ctx) {
        String functionName = ctx.functionCall().functionName.getText();
        List<Expression> parameters = ctx.functionCall().arguments != null
                ? parseArguments(ctx.functionCall().arguments)
                : Collections.emptyList();
        Scope scope = scope();
        FunctionSymbol functionSymbol = resolveFunctionOrThrow(scope, functionName, parameters.size());

        Type evaluationType = functionSymbol.getType();
        Type scalarReturnType = unwrapScalarType(functionSymbol.getType());
        if(scalarReturnType instanceof GenericType) {
            FunctionParameter parameter = functionSymbol.findGenericParameter((GenericType)scalarReturnType);
            Type type = parameters.get(parameter.getParameterIndex()).getEvaluationType();
            evaluationType = calculateGenericEvaluationType(functionSymbol.getType(), unwrapScalarType(type));
        }
        Function function = new Function(functionName, parameters, scope, evaluationType, token(ctx));
        functions.put(function.getFunctionName(), function);
        return function;
    }

    private Type calculateGenericEvaluationType(Type functionEvaluationType, Type genericType) {
        if(functionEvaluationType instanceof ArrayType) {
            Type subtype = ((ArrayType) functionEvaluationType).getElementType();
            return ArrayType.of(calculateGenericEvaluationType(subtype, genericType));
        }
        if(functionEvaluationType instanceof GenericType) {
            return genericType;
        }
        return functionEvaluationType;
    }

    @Override
    public Expression visitTemplate(TemplateContext ctx) {
        List<Expression> templateExpressions = new ArrayList<>();
        List<String> templateParts = new ArrayList<>();

        StringBuilder currentTemplatePart = new StringBuilder();
        for(TemplateTextContext t : ctx.templateText()) {
            if(t.TEMPLATE_TEXT() != null) {
                String text = escapeTemplate(t.TEMPLATE_TEXT().getText());
                currentTemplatePart.append(text);
            } else if(t.templateExpression().value() != null) {
                templateParts.add(currentTemplatePart.toString());
                currentTemplatePart = new StringBuilder();
                Expression templateExpression = visit(t.templateExpression().value());
                templateExpressions.add(templateExpression);
            }
        }
        templateParts.add(currentTemplatePart.toString());

        return new Template(templateParts, templateExpressions, scope(), token(ctx));
    }

    private String escapeTemplate(String templatePart) {
        return templatePart.replace("\\`", "`")
            .replace("\\$", "$")
            .replace("\\\\", "\\");
    }

    private Token token(ParserRuleContext ctx) {
        int startIndex = ctx.getStart() != null ? ctx.getStart().getStartIndex() : 0;
        int endIndex = ctx.getStop() != null ? ctx.getStop().getStopIndex() : 0;
        String text = ctx.getStart().getInputStream().getText(new Interval(startIndex, endIndex));
        return new Token(startIndex, endIndex, text);
    }

    private FunctionSymbol resolveFunctionOrThrow(Scope scope, String functionName, int paramCount) {
        return scope.resolveFunctionSymbol(functionName, paramCount)
                .orElseThrow(() -> new IllegalStateException(
                        functionName + " function with " + paramCount + " parameter(s) does not exist in system"
                ));
    }

    private Scope scope() {
        return currentScope.peek();
    }

    private Type unwrapScalarType(Type type) {
        if(type instanceof ArrayType) {
            return unwrapScalarType(((ArrayType) type).getElementType());
        }
        return type;
    }

    private Type unwrapTypeForIteration(Type type) {
        if(type instanceof ArrayType) {
            return ((ArrayType) type).getElementType();
        }
        if(type.equals(Type.ANY)) {
            return Type.ANY;
        }
        return Type.UNKNOWN;
    }

    private List<Expression> parseArguments(Kel.ValueListContext arguments) {
        return arguments.value()
                .stream()
                .map(valueContext -> visit(valueContext))
                .collect(Collectors.toList());
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public Collection<Reference> getReferences() {
        return references;
    }
}
