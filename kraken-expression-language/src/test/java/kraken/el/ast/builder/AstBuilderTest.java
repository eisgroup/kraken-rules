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

import static kraken.el.scope.type.Type.ANY;
import static kraken.el.scope.type.Type.BOOLEAN;
import static kraken.el.scope.type.Type.MONEY;
import static kraken.el.scope.type.Type.NUMBER;
import static kraken.el.scope.type.Type.STRING;
import static kraken.el.scope.type.Type.UNKNOWN;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import kraken.el.ast.AccessByIndex;
import kraken.el.ast.Addition;
import kraken.el.ast.And;
import kraken.el.ast.Ast;
import kraken.el.ast.AstType;
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
import kraken.el.ast.Reference;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.StringLiteral;
import kraken.el.ast.Subtraction;
import kraken.el.ast.Template;
import kraken.el.ast.This;
import kraken.el.ast.ValueBlock;
import kraken.el.ast.token.Token;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.GenericType;
import kraken.el.scope.type.Type;
import kraken.el.scope.type.UnionType;

/**
 * @author mulevicius
 */
public class AstBuilderTest {

    @Test
    public void shouldBuildEmptyAstWhenExpressionIsSemanticallyEmpty() {
        Scope scope = Scope.dynamic();

        assertThat(ast("", scope).isEmpty(), is(true));
        assertThat(ast(" ", scope).isEmpty(), is(true));
        assertThat(ast("// comment", scope).isEmpty(), is(true));
        assertThat(ast("\n", scope).isEmpty(), is(true));
    }

    @Test
    public void shouldNotBuildEmptyAstWhenExpressionHasLogic() {
        Scope scope = Scope.dynamic();

        assertThat(ast("attribute", scope).isEmpty(), is(false));
        assertThat(ast("10", scope).isEmpty(), is(false));
    }

    @Test
    public void shouldReturnAstErrorForMissingBrackets() {
        Scope scope = Scope.dynamic();

        assertThat(rawAst("attribute[", scope).getGenerationErrors(), hasSize(1));
        assertThat(rawAst("attribute?[", scope).getGenerationErrors(), hasSize(1));
        assertThat(rawAst("attribute[this.path.", scope).getGenerationErrors(), hasSize(1));
        assertThat(rawAst("attribute?[this.path. = true", scope).getGenerationErrors(), hasSize(1));
        assertThat(rawAst("attribute[]", scope).getGenerationErrors(), hasSize(1));
        assertThat(rawAst("attribute?[]", scope).getGenerationErrors(), hasSize(1));

        assertThat(rawAst("attribute[index]", scope).getGenerationErrors(), empty());
        assertThat(rawAst("attribute?[filter = true]", scope).getGenerationErrors(), empty());
    }

    @Test
    public void shouldBuildIncompleteThisReference() {
        Scope scope = Scope.dynamic();
        ReferenceValue reference = (ReferenceValue) ast("this.", scope);

        assertThat(reference.getThisNode(), instanceOf(This.class));
        assertThat(reference.getReference(), instanceOf(Path.class));
        assertThat(reference.getReference().getToken().getText(), is("this."));

        Reference object = ((Path)reference.getReference()).getObject();
        Reference property = ((Path)reference.getReference()).getProperty();
        assertThat(object, instanceOf(This.class));
        assertThat(property, instanceOf(Empty.class));
        assertThat(property.getToken().getText(), is(""));
    }

    @Test
    public void shouldBuildIncompletePath() {
        Scope scope = Scope.dynamic();
        ReferenceValue reference = (ReferenceValue) ast("Policy.", scope);

        assertThat(reference.getReference(), instanceOf(Path.class));
        Path path = (Path) reference.getReference();

        assertThat(path.getProperty(), instanceOf(Empty.class));
        assertThat(path.getProperty().getToken().getText(), is(""));
    }

    @Test
    public void shouldTypeGuardWithinIf() {
        Scope scope = getCoverageSymbolMock();

        If ifExpression = (If) ast("if(RiskItem instanceof Coverage) then RiskItem.limitAmount", scope);
        assertThat(ifExpression.getEvaluationType(), is(NUMBER));
    }

    @Test
    public void shouldBuildIfEvaluationType() {
        Scope scope = Scope.dynamic();

        assertThat(ast("if true then 1 else 'a'", scope).getEvaluationType(), is(UNKNOWN));
        assertThat(ast("if true then 1 else 2", scope).getEvaluationType(), is(NUMBER));
    }

    @Test
    public void shouldTypeGuardWithinNestedConjunction() {
        Scope scope = getCoverageSymbolMock();

        And and = (And) ast(
            "RiskItem[0] instanceof Coverage and RiskItem[0].limitAmount > 10",
            scope
        );
        Type limitType = ((ReferenceValue)((MoreThan)and.getRight()).getLeft()).getReference().getEvaluationType();
        assertThat(limitType, is(NUMBER));
    }

    @Test
    public void shouldTypeGuardWithinFilter() {
        Scope scope = getCoverageSymbolMock();

        assertThat(
            ast("coverages[this instanceof RiskItem].itemCd", scope).getEvaluationType(),
            is(ArrayType.of(STRING))
        );
        assertThat(
            ast("coverages[this instanceof Base and this instanceof RiskItem].itemCd", scope).getEvaluationType(),
            is(ArrayType.of(STRING))
        );
        assertThat(
            ast("coverages[this instanceof RiskItem][0].itemCd", scope).getEvaluationType(),
            is(STRING)
        );
        assertThat(
            ast("coverages[this instanceof Base or this instanceof RiskItem].itemCd", scope).getEvaluationType(),
            is(ArrayType.of(UNKNOWN))
        );
    }

    @Test
    public void shouldHandleScopeResetInNestedTypeGuardAndFilter() {
        Scope scope = getCoverageSymbolMock();

        If ifExpression = (If) ast(
            "if(this instanceof Coverage) then this.limitAmount + coverages[this instanceof RiskItem][0].maxLimitAmount",
            scope
        );
        Expression leftOfAddition = ((Addition)ifExpression.getThenExpression()).getLeft();
        Expression rightOfAddition = ((Addition)ifExpression.getThenExpression()).getRight();

        assertThat(leftOfAddition.getEvaluationType(), is(NUMBER));
        assertThat(rightOfAddition.getEvaluationType(), is(MONEY));
    }

    @Test
    public void shouldBuildInNode() {
        Scope scope = getCoverageSymbolMock();

        assertThat(ast("Coverage.coverageCd in {'name'}", scope), instanceOf(In.class));
    }

    @Test
    public void shouldBuildCastOutsideOfReference() {
        Expression expression = ast("(COLLCoverage)coverages[0]", Scope.dynamic());

        assertThat(((ReferenceValue) expression).getReference(), instanceOf(Cast.class));
    }

    @Test
    public void shouldThrowIfCannotTokenize() {
        assertThrows(AstBuildingException.class, () -> rawAst("\"\"\""));
    }

    @Test
    public void shouldBuildCollectionFoldNode() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("coverages[*].limitAmount", scope);
        Path path = (Path) reference.getReference();
        CollectionFilter filter = (CollectionFilter) path.getObject();
        Identifier property = (Identifier) path.getProperty();
        assertThat(((Identifier) filter.getCollection()).getIdentifier(), equalTo("coverages"));
        assertThat(filter.getPredicate(), nullValue());
        assertThat(property.getIdentifier(), equalTo("limitAmount"));
        assertThat(reference.getEvaluationType(), is(ArrayType.of(NUMBER)));
    }

    @Test
    public void shouldBuildForEachNode() {
        Scope scope = getCoverageSymbolMock();

        ForEach forEach = (ForEach) ast("for coverage in coverages return coverage.limitAmount", scope);
        Identifier collection = (Identifier) ((ReferenceValue) forEach.getCollection()).getReference();
        Path path = (Path) ((ReferenceValue) forEach.getReturnExpression()).getReference();
        assertThat(forEach.getVar(), equalTo("coverage"));
        assertThat(collection.getIdentifier(), equalTo("coverages"));
        assertThat(((Identifier)path.getObject()).getIdentifier(), equalTo("coverage"));
        assertThat(((Identifier)path.getProperty()).getIdentifier(), equalTo("limitAmount"));

        assertThat(forEach.getEvaluationType(), is(ArrayType.of(NUMBER)));
    }

    @Test
    public void shouldThrowErrorWhenKeywordIsUsedAsIdentifierInForExpressions() {
        Scope scope = getCoverageSymbolMock();

        assertThrows(AstBuildingException.class, () -> ast("for for in coverages satisfies coverage.isPrimary", scope));
    }

    @Test
    public void shouldThrowErrorWhenKeywordIsUsedAsIdentifierInTypeComparison() {
        Scope scope = getCoverageSymbolMock();

        assertThrows(AstBuildingException.class, () -> ast("coverage instanceof return", scope));
    }

    @Test
    public void shouldThrowErrorWhenKeywordIsUsedAsIdentifierInReference() {
        Scope scope = getCoverageSymbolMock();

        assertThrows(AstBuildingException.class, () -> ast("coverage.in", scope));
    }

    @Test
    public void shouldBuildForSomeNode() {
        Scope scope = getCoverageSymbolMock();

        ForSome forSome = (ForSome) ast("some coverage in coverages satisfies coverage.isPrimary", scope);
        Identifier collection = (Identifier) ((ReferenceValue) forSome.getCollection()).getReference();
        Path path = (Path) ((ReferenceValue) forSome.getReturnExpression()).getReference();
        assertThat(forSome.getVar(), equalTo("coverage"));
        assertThat(collection.getIdentifier(), equalTo("coverages"));
        assertThat(((Identifier)path.getObject()).getIdentifier(), equalTo("coverage"));
        assertThat(((Identifier)path.getProperty()).getIdentifier(), equalTo("isPrimary"));

        assertThat(forSome.getEvaluationType(), is(BOOLEAN));
    }

    @Test
    public void shouldBuildForEveryNode() {
        Scope scope = getCoverageSymbolMock();

        ForEvery forEvery = (ForEvery) ast("every coverage in coverages satisfies coverage.isPrimary", scope);
        Identifier collection = (Identifier) ((ReferenceValue) forEvery.getCollection()).getReference();
        Path path = (Path) ((ReferenceValue) forEvery.getReturnExpression()).getReference();
        assertThat(forEvery.getVar(), equalTo("coverage"));
        assertThat(collection.getIdentifier(), equalTo("coverages"));
        assertThat(((Identifier)path.getObject()).getIdentifier(), equalTo("coverage"));
        assertThat(((Identifier)path.getProperty()).getIdentifier(), equalTo("isPrimary"));

        assertThat(forEvery.getEvaluationType(), is(BOOLEAN));
    }

    @Test
    public void shouldAddFilterScopeToAccessByIndex() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("coverages[coverages[1] > 99]", scope);
        assertThat(reference.getReference(), CoreMatchers.instanceOf(CollectionFilter.class));
        assertThat(((CollectionFilter) reference.getReference()).getPredicate(), instanceOf(MoreThan.class));
        ScopeType scopeType = ((MoreThan) ((CollectionFilter) reference
                .getReference())
                .getPredicate())
                .getLeft()
                .getScope()
                .getScopeType();
        assertThat(scopeType, is(ScopeType.FILTER));
        assertThat(reference.getEvaluationType(), instanceOf(ArrayType.class));
        assertThat(((ArrayType)reference.getEvaluationType()).getElementType().getName(), is("Coverage"));
    }

    @Test
    public void shouldBuildCollectionFilterNode() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("coverages[limitAmount > 99 and limitAmount < 199]", scope);
        assertThat(reference.getReference(), CoreMatchers.instanceOf(CollectionFilter.class));
        CollectionFilter filter = (CollectionFilter) reference.getReference();
        assertThat(filter.getPredicate().getNodeType(), is(NodeType.AND));
        assertThat(reference.getEvaluationType(), instanceOf(ArrayType.class));
        assertThat(((ArrayType)reference.getEvaluationType()).getElementType().getName(), is("Coverage"));
    }

    @Test
    public void shouldFailWhenBuildCollectionFilterNode() {
        assertThrows(AstBuildingException.class,
                () -> ast("coverages{limitAmount > 99 and limitAmount < 199]", getCoverageSymbolMock()));
    }

    @Test
    public void shouldBuildCollectionFoldNodeWithIndices() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("arrayOfArrayOfCoverages[1][*].deductibleAmounts[2]", scope);
        Path path = (Path) reference.getReference();
        CollectionFilter filter = (CollectionFilter) path.getObject();
        AccessByIndex firstAccessByIndex = (AccessByIndex) filter.getCollection();
        assertThat(index(firstAccessByIndex), equalTo(1));

        AccessByIndex secondAccessByIndex = (AccessByIndex) path.getProperty();
        assertThat(index(secondAccessByIndex), equalTo(2));
        assertThat(reference.getEvaluationType(), is(ArrayType.of(NUMBER)));
    }

    @Test
    public void shouldBuildAccessByIndexNodes() {
        Scope scope = getCoverageSymbolMock();

        Ast ast = rawAst("coverages[1]", scope);
        ReferenceValue reference = (ReferenceValue) ast.getExpression();

        assertThat(reference.getReference(), instanceOf(AccessByIndex.class));
        assertThat(reference.getEvaluationType().getName(), is("Coverage"));
        assertThat(ast.getAstType(), is(AstType.COMPLEX));
    }

    @Test
    public void shouldBuildNestedAccessByIndexNodes() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("coverages[coverages[5]]", scope);
        assertThat(reference.getReference(), instanceOf(AccessByIndex.class));
        AccessByIndex outerAccessByIndex = (AccessByIndex) reference.getReference();
        ReferenceValue indexReference = (ReferenceValue) outerAccessByIndex.getIndexExpression();
        assertThat(indexReference.getReference(), instanceOf(AccessByIndex.class));
        ReferenceValue innerPath = (ReferenceValue) outerAccessByIndex.getIndexExpression();
        assertThat(innerPath.getReference(), instanceOf(AccessByIndex.class));
        AccessByIndex innerAccessByIndex = (AccessByIndex) innerPath.getReference();
        assertThat(index(innerAccessByIndex), equalTo(5));
        assertThat(reference.getEvaluationType().getName(), is("Coverage"));
    }

    @Test
    public void shouldBuildSequentialAccessByIndexNodes() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("arrayOfArrayOfCoverages[1][2]", scope);
        AccessByIndex secondAccessByIndex = (AccessByIndex) reference.getReference();
        assertThat(index(secondAccessByIndex), equalTo(2));
        AccessByIndex firstAccessByIndex = (AccessByIndex) secondAccessByIndex.getCollection();
        assertThat(index(firstAccessByIndex), equalTo(1));
        assertThat(reference.getEvaluationType().getName(), is("Coverage"));
    }

    @Test
    public void shouldBuildInPathAccessByIndex() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference = (ReferenceValue) ast("coverages[1].limitAmount", scope);
        Path path = (Path) reference.getReference();
        AccessByIndex firstAccessByIndex = (AccessByIndex) path.getObject();
        assertThat(firstAccessByIndex.getEvaluationType().getName(), is("Coverage"));
        assertThat(index(firstAccessByIndex), equalTo(1));
        assertThat(reference.getEvaluationType(), is(NUMBER));
    }

    @Test
    public void shouldBuildNumericalComparisonNodes() {
        Scope scope = getCoverageSymbolMock();

        assertThat(ast("Coverage.limitAmount > 10", scope), instanceOf(MoreThan.class));
        assertThat(ast("Coverage.limitAmount >= 10", scope), instanceOf(MoreThanOrEquals.class));
        assertThat(ast("Coverage.limitAmount < 10", scope), instanceOf(LessThan.class));
        assertThat(ast("Coverage.limitAmount <= 10", scope), instanceOf(LessThanOrEquals.class));
    }

    @Test
    public void shouldBuildBinaryLogicalNodes() {
        Scope scope = newScope(new SymbolTable(
                List.of(),
                Map.of(
                        "white", new VariableSymbol("white", Type.BOOLEAN),
                        "black", new VariableSymbol("black", Type.BOOLEAN)
                )
        ));

        assertThat(ast("white and black", scope), instanceOf(And.class));
        assertThat(ast("white or black", scope), instanceOf(Or.class));
    }

    @Test
    public void shouldBuildMatchesRegExpNode() {
        Scope scope = getCoverageSymbolMock();

        assertThat(ast("Coverage.coverageCd matches '[a-z]'", scope), instanceOf(MatchesRegExp.class));
    }

    @Test
    public void shouldBuildComparisonNodes() {
        Scope scope = getCoverageSymbolMock();

        assertThat(ast("Coverage.limitAmount = 10", scope), instanceOf(Equals.class));
        assertThat(ast("Coverage.limitAmount != 10", scope), instanceOf(NotEquals.class));
    }

    @Test
    public void shouldBuildArithmeticNodes() {
        Scope scope = getCoverageSymbolMock();

        assertThat(ast("Coverage.limitAmount + 10", scope), instanceOf(Addition.class));
        assertThat(ast("Coverage.limitAmount - 10", scope), instanceOf(Subtraction.class));
        assertThat(ast("Coverage.limitAmount-10", scope), instanceOf(Subtraction.class));
        assertThat(ast("Coverage.limitAmount * 10", scope), instanceOf(Multiplication.class));
        assertThat(ast("Coverage.limitAmount ** 10", scope), instanceOf(Exponent.class));
        assertThat(ast("Coverage.limitAmount / 10", scope), instanceOf(Division.class));
        assertThat(ast("Coverage.limitAmount % 10", scope), instanceOf(Modulus.class));
    }

    @Test
    public void shouldBuildDateLiteralFromDateNode() {
        Scope scope = newScope(new SymbolTable(
                List.of(date().getValue()),
                Map.of()
        ));

        Ast ast = rawAst("2018-01-01", scope);
        Expression expression = ast.getExpression();
        assertThat(expression, instanceOf(DateLiteral.class));
        assertThat(((DateLiteral) expression).getValue(), equalTo(LocalDate.parse("2018-01-01")));
        assertThat(((DateLiteral) expression).toString(), equalTo("2018-01-01"));

        assertThat(ast.getCompiledLiteralValue(), equalTo(LocalDate.parse("2018-01-01")));
        assertThat(ast.getCompiledLiteralValueType(), equalTo("Date"));
    }

    @Test
    public void shouldBuildDateTimeLiteralFromDateNode() {
        Scope scope = newScope(new SymbolTable(
                List.of(dateTime().getValue()),
                Map.of()
        ));

        Ast ast = rawAst("2018-01-01T10:00:00Z", scope);
        Expression expression = ast.getExpression();
        assertThat(expression, instanceOf(DateTimeLiteral.class));
        assertThat(((DateTimeLiteral) expression).getValue(), equalTo(Literals.getDateTime("2018-01-01T10:00:00Z")));
        assertThat(((DateTimeLiteral) expression).toString(), equalTo("2018-01-01T10:00:00Z"));

        assertThat(ast.getCompiledLiteralValue(), equalTo(Literals.getDateTime("2018-01-01T10:00:00Z")));
        assertThat(ast.getCompiledLiteralValueType(), equalTo("DateTime"));
    }

    @Test
    public void shouldBuildFunctionNode() {
        Scope scope = newScope(new SymbolTable(
                List.of(date().getValue()),
                Map.of()
        ));

        ReferenceValue reference = (ReferenceValue) ast("Date('2020-01-01')", scope);
        assertThat(reference.getReference(), instanceOf(Function.class));
    }

    @Test
    public void shouldBuildGenericFunctionNode() {
        Scope scope = newScope(new SymbolTable(
            List.of(new FunctionSymbol(
                "First",
                new GenericType("T"),
                List.of(new FunctionParameter(0, ArrayType.of(new GenericType("T"))))
            )),
            Map.of()
        ));

        ReferenceValue reference = (ReferenceValue) ast("First({'string'})", scope);
        assertThat(reference.getReference(), instanceOf(Function.class));
        assertThat(reference.getEvaluationType(), equalTo(STRING));
    }

    @Test
    public void shouldBuildBoundedGenericFunctionNode() {
        Scope scope = newScope(new SymbolTable(
            List.of(new FunctionSymbol(
                "First",
                new GenericType("T", STRING),
                List.of(new FunctionParameter(0, ArrayType.of(new GenericType("T", STRING))))
            )),
            Map.of()
        ));

        ReferenceValue reference = (ReferenceValue) ast("First({'string'})", scope);
        assertThat(reference.getReference(), instanceOf(Function.class));
        assertThat(reference.getEvaluationType(), equalTo(STRING));
    }

    @Test
    public void shouldBuildFunctionBodyNodeWithGenericParameters() {
        Scope scope = newScope(new SymbolTable(
            List.of(new FunctionSymbol(
                "First",
                new GenericType("T"),
                List.of(
                    new FunctionParameter(0, ArrayType.of(new GenericType("T"))),
                    new FunctionParameter(1, new GenericType("T"))
                )
            )),
            Map.of("arr", new VariableSymbol("arr", ArrayType.of(new GenericType("N", NUMBER))))
        ));

        var evaluationType = ast("set p to arr[0] return First(arr, p)", scope).getEvaluationType();
        assertThat(evaluationType, equalTo(new GenericType("N")));
        assertThat(((GenericType) evaluationType).getBound(), equalTo(NUMBER));
    }

    @Test
    public void shouldNotThrowExceptionForUnknownFunction() {
        ReferenceValue reference = (ReferenceValue) ast("UnknownFunction('a', 2)", emptyScope());
        assertThat(reference.getReference(), instanceOf(Function.class));

        Function function = (Function) reference.getReference();

        assertThat(function.getEvaluationType(), is(UNKNOWN));
    }

    @Test
    public void shouldBuildPathNodeFromPaths() {
        Scope scope = newScope(new SymbolTable(
                List.of(),
                Map.of("a", new VariableSymbol("a", new Type("A",
                        new SymbolTable(
                                List.of(),
                                    Map.of("b",
                                            new VariableSymbol("b", new Type("B",
                                            new SymbolTable(
                                                    List.of(),
                                                    Map.of("d",
                                                            new VariableSymbol("d", new Type("D",
                                                            new SymbolTable()))
                                                    )
                                            )
                                    )))
                        ))
                ))
        ));

        Ast ast = rawAst("a.b.d", scope);
        ReferenceValue reference = (ReferenceValue) ast.getExpression();
        assertThat(reference.getReference(), instanceOf(Path.class));
        assertThat(reference.getEvaluationType().getName(), is("D"));
        assertThat(ast.getAstType(), is(AstType.PATH));
        assertThat(ast.getCompiledLiteralValue(), nullValue());
    }

    @Test
    public void shouldBePathIfIdentifierIsRewrittenToPath() {
        Identifier identifier = new Identifier("field", "path.to.field", Scope.dynamic(), Type.ANY, new Token(0, 0, ""));
        Ast ast = new Ast(identifier);
        assertThat(ast.getAstType(), is(AstType.PATH));
    }

    @Test
    public void shouldFlatMapPath() {
        Scope scope = getCoverageSymbolMock();

        ReferenceValue reference1 = (ReferenceValue) ast("((Coverage | Coverage[]) coverage).limitAmount", scope);
        assertThat(reference1.getEvaluationType(), is(new UnionType(NUMBER, ArrayType.of(NUMBER))));

        ReferenceValue reference2 = (ReferenceValue) ast("((Any | Coverage) coverage).limitAmount", scope);
        assertThat(reference2.getEvaluationType(), is(ANY));

        ReferenceValue reference3 = (ReferenceValue) ast("((Any | Coverage | Coverage[]) coverage).limitAmount", scope);
        assertThat(reference3.getEvaluationType(), is(ANY));

        ReferenceValue reference4 = (ReferenceValue) ast("((Coverage[]) coverage).limitAmount", scope);
        assertThat(reference4.getEvaluationType(), is(ArrayType.of(NUMBER)));

        ReferenceValue reference5 = (ReferenceValue) ast("((RiskItem | Coverage) coverage).limitAmount", scope);
        assertThat(reference5.getEvaluationType(), is(UNKNOWN));

        ReferenceValue reference6 = (ReferenceValue) ast("((Coverage[]) coverage).deductibleAmounts", scope);
        assertThat(reference6.getEvaluationType(), is(ArrayType.of(NUMBER)));
    }

    @Test
    public void shouldBuildPathNodeWithUnknownVariables() {
        ReferenceValue reference = (ReferenceValue) ast("a.b.c", emptyScope());
        assertThat(reference.getReference(), instanceOf(Path.class));
        assertThat(reference.getEvaluationType(), is(Type.UNKNOWN));
    }

    @Test
    public void shouldBuildInlineArrayNode() {
        Expression expression = ast("{'key', 'value'}");

        assertThat(expression, instanceOf(InlineArray.class));
        assertThat(expression.getEvaluationType(), is(ArrayType.of(STRING)));
    }

    @Test
    public void shouldBuildInlineArrayNodeAndDetermineType() {
        Scope scope = getCoverageSymbolMock();

        Expression expression = ast("{Coverage, RiskItem}", scope);

        assertThat(expression, instanceOf(InlineArray.class));
        assertThat(expression.getEvaluationType(), is(ArrayType.of(new Type("Base"))));
    }

    @Test
    public void shouldBuildInlineArrayNodeAndDetermineComplexArrayType() {
        Scope scope = getCoverageSymbolMock();

        Expression expression = ast("{(Coverage[])Coverage, (Any[])RiskItem}", scope);

        assertThat(expression, instanceOf(InlineArray.class));
        assertThat(expression.getEvaluationType(), is(ArrayType.of(ArrayType.of(ANY))));
    }

    @Test
    public void shouldBuildInlineArrayNodeAndDetermineComplexUnionType() {
        Scope scope = getCoverageSymbolMock();

        Expression expression = ast("{(Coverage | String[])Coverage, (Coverage | String[])RiskItem}", scope);

        assertThat(expression, instanceOf(InlineArray.class));
        assertThat(expression.getEvaluationType().getName(), is("(Coverage | String[])[]"));
    }

    @Test
    public void shouldBuildIdentifierNode() {
        Scope scope = getCoverageSymbolMock();

        Ast ast = rawAst("Coverage", scope);
        ReferenceValue reference = (ReferenceValue) ast.getExpression();

        assertThat(reference.getReference(), instanceOf(Identifier.class));
        assertThat(ast.getAstType(), is(AstType.PROPERTY));
        assertThat(ast.getCompiledLiteralValue(), nullValue());
    }

    @Test
    public void shouldBuildBooleanLiteral() {
        assertThat(ast("true"), instanceOf(BooleanLiteral.class));
    }

    @Test
    public void shouldBuildBooleanLiteralValue() {
        Ast ast = rawAst("true");
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((BooleanLiteral) ast.getExpression()).getValue(), equalTo(true));
        assertThat(ast.getCompiledLiteralValue(), equalTo(true));
        assertThat(ast.getCompiledLiteralValueType(), equalTo("Boolean"));

        ast = rawAst("false");
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((BooleanLiteral) ast.getExpression()).getValue(), equalTo(false));
        assertThat(ast.getCompiledLiteralValue(), equalTo(false));
    }

    @Test
    public void shouldBuildDecimalLiteral() {
        assertThat(ast("0.25"), instanceOf(NumberLiteral.class));
        assertThat(ast("0.6666666666666667"), instanceOf(NumberLiteral.class));
        assertThat(ast("10.66666666666667"), instanceOf(NumberLiteral.class));
        assertThat(ast("6666666666666667"), instanceOf(NumberLiteral.class));
    }

    @Test
    public void shouldBuildDecimalLiteralValue() {
        Ast ast = rawAst("0.25");
        Expression expression = ast.getExpression();

        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((NumberLiteral)expression).getValue(), equalTo(new BigDecimal("0.25")));
        assertThat(ast.getCompiledLiteralValue(), equalTo(new BigDecimal("0.25")));
        assertThat(ast.getCompiledLiteralValueType(), equalTo("Number"));

        ast = rawAst("-5");
        expression = ast.getExpression();
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((NumberLiteral)expression).getValue(), equalTo(new BigDecimal("-5")));
        assertThat(ast.getCompiledLiteralValue(), equalTo(new BigDecimal("-5")));

        ast = rawAst("10000000000000000");
        expression = ast.getExpression();
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        BigDecimal literalValue = (BigDecimal) ((NumberLiteral)expression).getValue();
        assertThat(literalValue.toPlainString(), equalTo("10000000000000000"));
        BigDecimal compileValue = (BigDecimal) ast.getCompiledLiteralValue();
        assertThat(compileValue.toPlainString(), equalTo("10000000000000000"));
    }

    @Test
    public void shouldBuildStringLiteral() {
        assertThat(ast("'str'"), instanceOf(StringLiteral.class));
    }

    @Test
    public void shouldBuildStringLiteralValue() {
        Ast ast = rawAst("'str'");
        Expression expression = ast.getExpression();
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((StringLiteral)expression).getValue(), equalTo("str"));
        assertThat(ast.getCompiledLiteralValue(), equalTo("str"));
        assertThat(ast.getCompiledLiteralValueType(), equalTo("String"));

        ast = rawAst("\"st\\'r\"");
        expression = ast.getExpression();
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((StringLiteral)expression).getValue(), equalTo("st'r"));
        assertThat(ast.getCompiledLiteralValue(), equalTo("st'r"));
    }

    @Test
    public void shouldBuildNullLiteral() {
        Ast ast = rawAst("null");

        Expression expression = ast.getExpression();
        assertThat(expression, instanceOf(Null.class));
        assertThat(ast.getAstType(), is(AstType.LITERAL));
        assertThat(((Null)expression).getValue(), nullValue());
        assertThat(ast.getCompiledLiteralValue(), nullValue());
        assertThat(ast.getCompiledLiteralValueType(), nullValue());
    }

    @Test
    public void shouldBuildNegationNode() {
        assertThat(ast("not true"), instanceOf(Negation.class));
    }

    @Test
    public void shouldBuildNegativeNode() {
        Scope scope = getCoverageSymbolMock();

        assertThat(ast("-Coverage.limitAmount", scope), instanceOf(Negative.class));
    }

    @Test
    public void shouldBuildWithDynamicScopeType() {
        assertThat(ast("coverages[*].limitAmount", Scope.dynamic()).getEvaluationType(), equalTo(ANY));
        assertThat(ast("coverages.limitAmount", Scope.dynamic()).getEvaluationType(), equalTo(ANY));
        assertThat(ast("a + b - c * d / e", Scope.dynamic()).getEvaluationType(), equalTo(NUMBER));
        assertThat(ast("t and f or z", Scope.dynamic()).getEvaluationType(), equalTo(BOOLEAN));
        assertThat(ast("coverages[0][1][2][3].limitAmount", Scope.dynamic()).getEvaluationType(), equalTo(ANY));
        assertThat(ast("some n in numbers satisfies n*2 > max", Scope.dynamic()).getEvaluationType(), equalTo(BOOLEAN));
        assertThat(ast("for n in numbers return n.path", Scope.dynamic()).getEvaluationType(), equalTo(ANY));
    }

    @Test
    public void shouldBuildTemplate() {
        Template e = (Template) ast("`this is template!`", Scope.dynamic());

        assertThat(e.getEvaluationType(), equalTo(STRING));
        assertThat(e.getNodeType(), equalTo(NodeType.TEMPLATE));
        assertThat(e.getToken().getText(), equalTo("`this is template!`"));
        assertThat(e.getTemplateParts(), hasItems("this is template!"));
        assertThat(e.getTemplateExpressions(), empty());
    }

    @Test
    public void shouldBuildTemplateFromEmptyMessage() {
        Template e = (Template) ast("``", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems(""));
        assertThat(e.getTemplateExpressions(), empty());
    }

    @Test
    public void shouldBuildTemplateWithExpression() {
        Template e = (Template) ast("`I'm ${User.name}`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("I'm ", ""));
        assertThat(e.getTemplateExpressions().get(0), instanceOf(ReferenceValue.class));
        assertThat(asStrings(e.getTemplateExpressions()), hasItems("User.name"));
    }

    @Test
    public void shouldBuildTemplateWithExpressionOnlyAndIncludeEmptyTemplateParts() {
        Template e = (Template) ast("`${User.name}`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("", ""));
        assertThat(asStrings(e.getTemplateExpressions()), hasItems("User.name"));
    }

    @Test
    public void shouldBuildTemplateWithMultipleExpressions() {
        Template e = (Template) ast("`I'm ${name} ${surname} and my phone is ${code}${number}`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("I'm ", " ", " and my phone is ", "", ""));
        assertThat(asStrings(e.getTemplateExpressions()), hasItems("name", "surname", "code", "number"));
    }

    @Test
    public void shouldBuildTemplateWithComplexExpression() {
        Template e = (Template) ast("`Status: ${Policy.countryCd in {'CAD', 'LT'} and User.age > 10}`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("Status: ", ""));
        assertThat(asStrings(e.getTemplateExpressions()), hasItems("Policy.countryCd in {'CAD', 'LT'} and User.age > 10"));
    }

    @Test
    public void shouldAllowDollarSignInTemplate() {
        Template e = (Template) ast("`Worth ${limitAmount}$`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("Worth ", "$"));
        assertThat(asStrings(e.getTemplateExpressions()), hasItems("limitAmount"));
    }

    @Test
    public void shouldAllowEmptyTemplate() {
        Template e = (Template) ast("`${}smth${}`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("smth"));
        assertThat(asStrings(e.getTemplateExpressions()), empty());
    }

    @Test
    public void shouldAllowToEscapeReservedExpressionMarkingsInTemplate() {
        Template e = (Template) ast("`hahaha\\`\\${limit\\`${limitAmount}Amount}`", Scope.dynamic());
        assertThat(e.getTemplateParts(), hasItems("hahaha`${limit`", "Amount}"));
        assertThat(asStrings(e.getTemplateExpressions()), hasItems("limitAmount"));
    }

    private List<String> asStrings(List<Expression> expressions) {
        return expressions.stream().map(Expression::getToken).map(Token::getText).collect(Collectors.toList());
    }

    private static Map.Entry<FunctionHeader, FunctionSymbol> dateTime() {
        return Pair.of(
                new FunctionHeader("DateTime", 1),
                new FunctionSymbol("DateTime", Type.DATETIME, List.of(
                        new FunctionParameter(0, STRING)
                ))
        );
    }

    private static Map.Entry<FunctionHeader, FunctionSymbol> date() {
        return Pair.of(
                new FunctionHeader("Date", 1),
                new FunctionSymbol("Date", Type.DATE, List.of(
                        new FunctionParameter(0, STRING)
                ))
        );
    }

    private Scope getCoverageSymbolMock() {
        Type base = new Type("Base");

        VariableSymbol coverage = var("Coverage",
                base,
                var("limitAmount", NUMBER),
                var("coverageCd", STRING),
                var("isPrimary", BOOLEAN),
                var("deductibleAmounts", ArrayType.of(NUMBER)));

        VariableSymbol coverages = var("coverages", ArrayType.of(coverage.getType()));
        VariableSymbol coverageOrCoverages = var("coverageOrCoverages",
            new UnionType(coverage.getType(), ArrayType.of(coverage.getType())));

        VariableSymbol arrayOfCoverages = var("arrayOfArrayOfCoverages", ArrayType.of(coverages.getType()));

        VariableSymbol riskItem = var("RiskItem", base,
            coverage,
            var("itemCd", STRING),
            var("maxLimitAmount", MONEY)
        );

        return newScope(asGlobalSymbolTable(coverage, coverages, arrayOfCoverages, riskItem, coverageOrCoverages));
    }

    private SymbolTable asGlobalSymbolTable(VariableSymbol... symbols) {
        return new SymbolTable(
                List.of(),
                Arrays.stream(symbols).collect(Collectors.toMap(VariableSymbol::getName, symbol -> symbol))
        );
    }

    private VariableSymbol var(String name, Type parent, VariableSymbol... properties) {
        return new VariableSymbol(name,
                new Type(name,
                        new SymbolTable(
                                List.of(),
                                Arrays.stream(properties).collect(Collectors.toMap(VariableSymbol::getName, property -> property))
                        ),
                        List.of(parent)
                )
        );
    }

    private VariableSymbol var(String name, Type type) {
        return new VariableSymbol(name, type);
    }

    private Ast rawAst(String expression, Scope scope) {
        return AstBuilder.from(expression, scope);
    }

    private Ast rawAst(String expression) {
        return AstBuilder.from(expression, emptyScope());
    }

    private Expression ast(String expression) {
        return ast(expression, emptyScope());
    }

    private Expression ast(String expression, Scope scope) {
        return AstBuilder.from(expression, scope).getExpression();
    }

    private Integer index(AccessByIndex accessByIndex) {
        NumberLiteral numberLiteral = (NumberLiteral) accessByIndex.getIndexExpression();
        return numberLiteral.getValue().intValue();
    }

    private Scope newScope(SymbolTable symbolTable) {
        Map<String, Type> allTypes = symbolTable.getReferences().values().stream()
            .map(VariableSymbol::getType)
            .filter(type -> !type.isPrimitive())
            .filter(type -> type.isKnown())
            .distinct()
            .collect(Collectors.toMap(Type::getName, t -> t));

        return new Scope(new Type("TEST", symbolTable), allTypes);
    }

    private Scope emptyScope() {
        return newScope(new SymbolTable());
    }

}
