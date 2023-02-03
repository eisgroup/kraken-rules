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

import static kraken.el.scope.type.Type.UNKNOWN;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;

import kraken.el.Kel;
import kraken.el.Kel.IncompletePathContext;
import kraken.el.Kel.PathSeparatorContext;
import kraken.el.Kel.ReferenceContext;
import kraken.el.Kel.TemplateContext;
import kraken.el.Kel.TemplateTextContext;
import kraken.el.Kel.ValueBlockContext;
import kraken.el.Kel.ValueWithVariablesContext;
import kraken.el.Kel.VariableContext;
import kraken.el.KelBaseVisitor;
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
import kraken.el.ast.InlineMap.KeyValuePair;
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
import kraken.el.ast.Reference;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.StringLiteral;
import kraken.el.ast.Subtraction;
import kraken.el.ast.Template;
import kraken.el.ast.This;
import kraken.el.ast.TypeLiteral;
import kraken.el.ast.TypeOf;
import kraken.el.ast.ValueBlock;
import kraken.el.ast.Variable;
import kraken.el.ast.token.Token;
import kraken.el.ast.typeguard.TypeFact;
import kraken.el.ast.typeguard.TypeGuardContext;
import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.AstMessageSeverity;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

/**
 * Traverses over Kraken Expression Language Parse Tree which is parsed by ANTLR4 from grammar
 *
 * @author mulevicius
 */
public class AstGeneratingVisitor extends KelBaseVisitor<Expression> {

    private final Map<String, Function> functions = new HashMap<>();

    private final Collection<Reference> references = new ArrayList<>();

    private final Deque<Scope> currentScope = new ArrayDeque<>();

    private final Deque<TypeGuardContext> currentTypeGuardContext = new ArrayDeque<>();

    private final Collection<AstMessage> generationErrors = new ArrayList<>();

    public AstGeneratingVisitor(Scope scope) {
        this.currentScope.push(scope);
        this.currentTypeGuardContext.push(TypeGuardContext.empty());
    }

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

        TypeLiteral typeLiteral = new TypeLiteral(ctx.type().getText(), scope, token(ctx.type()));
        return new Cast(typeLiteral, reference, scope, token(ctx));
    }

    @Override
    public Expression visitTypeComparison(Kel.TypeComparisonContext ctx) {
        return resolveTypeComparison(ctx.value(), ctx.identifier(), ctx.OP_INSTANCEOF(), ctx.OP_TYPEOF(), token(ctx));
    }

    @Override
    public Expression visitTypeComparisonPredicate(Kel.TypeComparisonPredicateContext ctx) {
        return resolveTypeComparison(ctx.value(), ctx.identifier(), ctx.OP_INSTANCEOF(), ctx.OP_TYPEOF(), token(ctx));
    }

    private Expression resolveTypeComparison(Kel.ValueContext leftCtx,
                                             Kel.IdentifierContext typeCtx,
                                             TerminalNode instanceOf,
                                             TerminalNode typeOf,
                                             Token token) {
        Expression left = visit(leftCtx);
        Identifier typeIdentifier = (Identifier) visit(typeCtx);

        Scope scope = scope();
        TypeLiteral type = new TypeLiteral(typeIdentifier.getIdentifier(), scope, token(typeCtx));

        if(instanceOf != null) {
            return new InstanceOf(left, type, scope(), token);
        }
        if(typeOf != null) {
            return new TypeOf(left, type, scope(), token);
        }
        throw new IllegalStateException("Unexpected state when parsing expression");
    }

    @Override
    public Expression visitMatchesRegExp(Kel.MatchesRegExpContext ctx) {
        return resolveMatchesRegExp(ctx.value(), ctx.STRING(), token(ctx));
    }

    @Override
    public Expression visitMatchesRegExpPredicate(Kel.MatchesRegExpPredicateContext ctx) {
        return resolveMatchesRegExp(ctx.value(), ctx.STRING(), token(ctx));
    }

    private Expression resolveMatchesRegExp(Kel.ValueContext leftCtx, TerminalNode regExp, Token token) {
        String regexp = Literals.stripQuotes(regExp.getText());
        return new MatchesRegExp(visit(leftCtx), regexp, scope(), token);
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
    public Expression visitReferenceValueValue(Kel.ReferenceValueValueContext ctx) {
        return resolveReferenceValue(ctx.reference(), token(ctx));
    }

    @Override
    public Expression visitReferenceValue(Kel.ReferenceValueContext ctx) {
        return resolveReferenceValue(ctx.reference(), token(ctx));
    }

    private ReferenceValue resolveReferenceValue(Kel.ReferenceContext ctx, Token token) {
        Reference reference = (Reference) visit(ctx);
        references.add(reference);
        return new ReferenceValue(reference, scope(), token);
    }

    @Override
    public Expression visitThisValue(Kel.ThisValueContext ctx) {
        Token token = token(ctx);
        Scope scope = scope();
        Type evaluationType = findTypeOf(token)
            .orElseGet(() -> scope.findClosestReferencableScope().getType());

        return new This(scope, evaluationType, token);
    }

    @Override
    public Expression visitNegationPredicate(Kel.NegationPredicateContext ctx) {
        return resolveNegation(ctx.value(), token(ctx));
    }

    @Override
    public Expression visitNegation(Kel.NegationContext ctx) {
        return resolveNegation(ctx.value(), token(ctx));
    }

    private Expression resolveNegation(Kel.ValueContext negationCtx, Token token) {
        return new Negation(visit(negationCtx), scope(), token);
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
    public Expression visitConjunctionPredicate(Kel.ConjunctionPredicateContext ctx) {
        return resolveConjunction(ctx.value(0), ctx.value(1), token(ctx));
    }

    @Override
    public Expression visitConjunction(Kel.ConjunctionContext ctx) {
        return resolveConjunction(ctx.value(0), ctx.value(1), token(ctx));
    }

    private And resolveConjunction(Kel.ValueContext leftCtx, Kel.ValueContext rightCtx, Token token) {
        Expression left = visit(leftCtx);

        pushTypeFacts(left.getDeducedTypeFacts());
        Expression right = visit(rightCtx);
        popTypeFacts();

        return new And(left, right, scope(), token);
    }

    @Override
    public Expression visitDisjunctionPredicate(Kel.DisjunctionPredicateContext ctx) {
        return resolveDisjunction(ctx.value(0), ctx.value(1), token(ctx));
    }

    @Override
    public Expression visitDisjunction(Kel.DisjunctionContext ctx) {
        return resolveDisjunction(ctx.value(0), ctx.value(1), token(ctx));
    }

    private Or resolveDisjunction(Kel.ValueContext leftCtx, Kel.ValueContext rightCtx, Token token) {
        return new Or(visit(leftCtx), visit(rightCtx), scope(), token);
    }

    @Override
    public Expression visitAccessByIndex(Kel.AccessByIndexContext ctx) {
        Reference collection = (Reference) visit(ctx.collection);
        Deque<Scope> pathScopes = unwrapParentScopeOfPath();

        Expression index = ctx.indices().indexValue() != null
            ? visit(ctx.indices().indexValue())
            : new Empty(scope());

        wrapPathScopes(pathScopes);

        Token token = token(ctx);
        Type evaluationType = findTypeOf(token)
            .orElseGet(() -> collection.getEvaluationType().unwrapArrayType());

        Expression e = new AccessByIndex(collection, index, scope(), evaluationType, token);

        if(ctx.indices().R_SQUARE_BRACKETS() == null) {
            generationErrors.add(new AstMessage(
                "Access by index is missing closing square brackets ']'", null, e, AstMessageSeverity.ERROR
            ));
        } else if(ctx.indices().indexValue() == null) {
            generationErrors.add(new AstMessage(
                "Access by index is empty. There must be an expression between square brackets '[]'", null, e, AstMessageSeverity.ERROR
            ));
        }

        return e;
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
        if(ctx.template() != null) {
            return visit(ctx.template());
        }
        var valueBlock = ctx.valueBlock();
        if (valueBlock == null) {
            return new Empty(scope());
        }
        return visit(valueBlock);
    }

    @Override
    public Expression visitValueBlock(ValueBlockContext ctx) {
        if(ctx.valueWithVariables() != null) {
            return this.visit(ctx.valueWithVariables());
        }
        return this.visit(ctx.value());
    }

    @Override
    public Expression visitValueWithVariables(ValueWithVariablesContext ctx) {
        List<Variable> variables = new ArrayList<>();
        for(VariableContext variableContext : ctx.variable()) {
            Variable variable = (Variable) this.visit(variableContext);
            variables.add(variable);
            Type currentVariables = buildTypeWithVariable(variable.getVariableName(), variable.getEvaluationType());
            pushScope(ScopeType.VARIABLES_MAP, currentVariables);
        }

        var valueContext = ctx.value();
        Expression value = valueContext != null ? this.visit(valueContext) : new Empty(scope());

        variables.forEach(v -> popScope());

        ValueBlock valueBlock = new ValueBlock(value, variables, scope(), token(ctx));

        if(!variables.isEmpty() && ctx.RETURN() == null) {
            generationErrors.add(new AstMessage(
                "Missing keyword 'return'. "
                    + "Variable assignments must be followed by keyword 'return' and then a value statement.",
                null,
                valueBlock,
                AstMessageSeverity.ERROR
            ));
        } else if(variables.isEmpty() && ctx.RETURN() != null) {
            generationErrors.add(new AstMessage(
                "Keyword 'return' is redundant. Value statement can be specified without keyword 'return'.",
                null,
                valueBlock,
                AstMessageSeverity.ERROR
            ));
        }

        return valueBlock;
    }

    private Type buildTypeWithVariable(String variableName, Type variableType) {
        return new Type("VARIABLES_MAP_" + variableName,
            new SymbolTable(
                Collections.emptyList(),
                Map.of(variableName, new VariableSymbol(variableName, variableType))
            )
        );
    }

    @Override
    public Expression visitVariable(VariableContext ctx) {
        var valueContext = ctx.value();

        Variable variable = new Variable(
            ctx.identifier().getText(),
            valueContext != null ? visit(valueContext) : new Empty(scope()),
            scope(),
            token(ctx)
        );

        if(ctx.TO() == null) {
            generationErrors.add(new AstMessage(
                "Variable assignment is missing keyword 'to'. "
                    + "Variable name must be followed by keyword 'to' and then a value statement.",
                null,
                variable,
                AstMessageSeverity.ERROR
            ));
        }

        return variable;
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
        return resolvePath(ctx.object, ctx.property, ctx.pathSeparator(), token(ctx));
    }

    @Override
    public Expression visitIncompletePath(IncompletePathContext ctx) {
        return resolvePath(ctx.object, null, ctx.pathSeparator(), token(ctx));
    }

    private Expression resolvePath(ReferenceContext objectCtx,
                                   ReferenceContext propertyCtx,
                                   PathSeparatorContext pathCtx,
                                   Token token) {
        Reference object = (Reference) visit(objectCtx);

        Type objectEvaluationType = object.getEvaluationType().unwrapArrayType();

        pushScope(ScopeType.PATH, null, objectEvaluationType);
        Reference property = propertyCtx != null
            ? (Reference) this.visit(propertyCtx)
            : new Empty(scope());
        popScope();

        Type evaluationType = findTypeOf(token)
            .orElseGet(() -> object.getEvaluationType().isAssignableToArray()
                ? object.getEvaluationType().mapTo(property.getEvaluationType().unwrapArrayType())
                : property.getEvaluationType()
            );

        boolean nullSafe = pathCtx.QDOT() != null;

        return new Path(object, property, nullSafe, scope(), evaluationType, token);
    }

    @Override
    public Expression visitFilter(Kel.FilterContext ctx) {
        Reference collection = (Reference) visit(ctx.filterCollection);

        Expression predicate = null;
        if(ctx.predicate() != null) {
            pushScope(ScopeType.FILTER, collection.getEvaluationType().unwrapArrayType());
            if(ctx.predicate().valuePredicate() != null) {
                predicate = visit(ctx.predicate().valuePredicate());
            } else if(ctx.predicate().value() != null) {
                predicate = visit(ctx.predicate().value());
            } else {
                predicate = new Empty(scope());
            }
            popScope();
        }

        Type collectionEvaluationType = collection.getEvaluationType();
        if(predicate != null) {
            Map<String, TypeFact> typeFactsInFilter = predicate.getDeducedTypeFacts();
            if(typeFactsInFilter.containsKey("this")) {
                TypeFact fact = typeFactsInFilter.get("this");
                collectionEvaluationType = ArrayType.of(fact.getType());
            }
        }

        Token token = token(ctx);
        Type evaluationType = findTypeOf(token).orElse(collectionEvaluationType);

        var e = new CollectionFilter(collection, predicate, scope(), evaluationType, token);

        if(ctx.predicate() != null) {
            if(ctx.predicate().R_SQUARE_BRACKETS() == null) {
                generationErrors.add(new AstMessage(
                    "Filter is missing closing square brackets ']'", null, e, AstMessageSeverity.ERROR
                ));
            } else if(ctx.predicate().value() == null && ctx.predicate().valuePredicate() == null) {
                generationErrors.add(new AstMessage(
                    "Filter is empty. There must be an expression between square brackets '[]'", null, e, AstMessageSeverity.ERROR
                ));
            }
        }

        return e;
    }

    @Override
    public Expression visitIfValue(Kel.IfValueContext ctx) {
        Expression condition = visit(ctx.condition);

        pushTypeFacts(condition.getDeducedTypeFacts());
        Expression then = visit(ctx.thenExpression);
        popTypeFacts();

        Expression ifElse = ctx.elseExpression != null ? visit(ctx.elseExpression) : null;

        return new If(condition, then, ifElse, scope(), token(ctx));
    }

    @Override
    public Expression visitForEach(Kel.ForEachContext ctx) {
        Expression collection = visit(ctx.collection);

        String var = ctx.var.getText();
        Type forScopeType = buildTypeWithVariable(var, collection.getEvaluationType().unwrapArrayType());

        pushScope(ScopeType.VARIABLES_MAP, forScopeType);
        Expression returnExpression = visit(ctx.returnExpression);
        popScope();

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

    private Expression buildForEvery(Kel.IdentifierContext varCtx,
                                     Kel.ValueContext collectionCtx,
                                     Kel.ValueBlockContext returnExpressionCtx,
                                     Token token) {

        Expression collection = visit(collectionCtx);

        String var = varCtx.getText();
        Type forScopeType = buildTypeWithVariable(var, collection.getEvaluationType().unwrapArrayType());

        pushScope(ScopeType.VARIABLES_MAP, forScopeType);
        Expression returnExpression = visit(returnExpressionCtx);
        popScope();

        return new ForEvery(var, collection, returnExpression, scope(), token);
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
                                    Kel.ValueBlockContext returnExpressionCtx,
                                    Token token) {

        Expression collection = visit(collectionCtx);

        String var = varCtx.getText();
        Type forScopeType = buildTypeWithVariable(var, collection.getEvaluationType().unwrapArrayType());

        pushScope(ScopeType.VARIABLES_MAP, forScopeType);
        Expression returnExpression = visit(returnExpressionCtx);
        popScope();

        return new ForSome(var, collection, returnExpression, scope(), token);
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
        Token token = token(ctx);
        Scope scope = scope();
        String identifier = ctx.getText();

        Type evaluationType = findTypeOf(token)
            .orElseGet(() -> scope.resolveReferenceSymbol(identifier).map(VariableSymbol::getType).orElse(UNKNOWN));

        return new Identifier(identifier, scope, evaluationType, token);
    }

    @Override
    public Expression visitBoolean(Kel.BooleanContext ctx) {
        return new BooleanLiteral(Literals.getBoolean(ctx.BOOL().getText()), scope(), token(ctx));
    }

    @Override
    public Expression visitDecimal(Kel.DecimalContext ctx) {
        try {
            return new NumberLiteral(Literals.getDecimal(ctx.positiveDecimalLiteral().getText()), scope(), token(ctx));
        } catch (ArithmeticException e) {
            throw new IllegalStateException(
                "Cannot parse decimal literal without loss of precision, " +
                    "because decimal literal exceeds 64bit Decimal precision: " + ctx.positiveDecimalLiteral().getText()
            );
        }
    }

    @Override
    public Expression visitString(Kel.StringContext ctx) {
        String text = Literals.stripQuotes(ctx.STRING().getText());
        return new StringLiteral(Literals.escape(text), scope(), token(ctx));
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
        List<Expression> arguments = ctx.functionCall().arguments != null
                ? parseArguments(ctx.functionCall().arguments)
                : Collections.emptyList();

        Scope scope = scope();
        Token token = token(ctx);
        Type evaluationType = findTypeOf(token)
            .orElseGet(() -> scope.resolveFunctionSymbol(functionName, arguments.size())
                .map(f -> f.getType().rewriteGenericTypes(f.resolveGenericRewrites(arguments)))
                .orElse(UNKNOWN));

        Function function = new Function(functionName, arguments, scope, evaluationType, token);
        functions.put(function.getFunctionName(), function);
        return function;
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

    private Scope scope() {
        return currentScope.peek();
    }

    private void pushScope(ScopeType scopeType, Type evaluationType) {
        pushScope(scopeType, scope(), evaluationType);
    }

    private void pushScope(ScopeType scopeType, Scope parentScope, Type evaluationType) {
        currentScope.push(new Scope(scopeType, parentScope, evaluationType));
        currentTypeGuardContext.push(TypeGuardContext.empty());
    }

    private void popScope() {
        popTypeFacts();
        currentScope.pop();
    }

    private Optional<Type> findTypeOf(Token token) {
        return Optional.ofNullable(currentTypeGuardContext.peek().getFacts().get(token.getText()))
            .map(TypeFact::getType);
    }

    private void pushTypeFacts(Map<String, TypeFact> facts) {
        Map<String, TypeFact> previousFacts = currentTypeGuardContext.peek().getFacts();
        Map<String, TypeFact> unionOfFacts = new HashMap<>(previousFacts);
        unionOfFacts.putAll(facts);

        currentTypeGuardContext.push(new TypeGuardContext(unionOfFacts));
    }

    private void popTypeFacts() {
        currentTypeGuardContext.pop();
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

    public Collection<AstMessage> getGenerationErrors() {
        return generationErrors;
    }

}
