package kraken.el.javascript;

import static kraken.el.scope.type.Type.ANY;
import static kraken.el.scope.type.Type.BOOLEAN;
import static kraken.el.scope.type.Type.MONEY;
import static kraken.el.scope.type.Type.NUMBER;
import static kraken.el.scope.type.Type.STRING;
import static kraken.utils.SnapshotMatchers.matches;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.Ast;
import kraken.el.ast.Expression;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.javascript.translator.JavascriptAstTranslator;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.GenericType;
import kraken.el.scope.type.Type;
import kraken.utils.Snapshot;
import kraken.utils.SnapshotTestRunner;

/**
 * @author mulevicius
 */
@RunWith(SnapshotTestRunner.class)
public class JavascriptAstTranslatorTest {

    private JavascriptAstTranslator translator;
    
    private String[] expressions;

    private Snapshot snapshot;
    
    @Before
    public void setUp() {
        this.expressions = new String[] {
            // LITERALS
            "true",
            "false",
            "null",
            "1",
            "-1",
            "1.0",
            "1.5",
            "-1.5",
            "'string'",
            "\"string\"",
            // DSL string literal to javascript string '"\ -> \'"\\
            "'\\'\\\"\\\\'",

            // REFERENCES
            "Coverage",
            "Coverage.limit",
            "coverages",
            "this.coverages",
            "coverages",
            "coverages[0]",
            "coverages[0].limit",
            "coverages[this.limit > 20]",
            "coverages[limit > this.limit]",
            "coverages[*].limit",
            "coverages[this.limit > 20].limit",
            "riskItems[*].coverages[*].limit",

            // NESTED FILTERS
            "riskItems[Count(coverages[limit > 10]) = 0]",
            "context.riskItems[Count(context.coverages[riskItemLimit > this.limit]) = 0]",

            // MATH
            "-Coverage.limit",
            "Coverage.limit + 5",
            "Coverage.limit - 5",
            "Coverage.limit * 5",
            "Coverage.limit / 5",
            "Coverage.limit ** 5",
            "Coverage.limit % 5",
            "Coverage.limit ** 5 + 10",
            "Coverage.limit ** (5 + 10)",

            // LOGICAL
            "Coverage.limit > 5",
            "Coverage.limit >= 5",
            "Coverage.limit < 5",
            "Coverage.limit <= 5",
            "Coverage.limit = 5",
            "Coverage.limit != 5",
            "!(Coverage.limit = 5)",
            "not(Coverage.limit = 5)",
            "Coverage.coverageCd matches '[a..z]'",
            "Coverage.limit in {1, 2, 3}",
            "!true",

            "Coverage.selected and Coverage.included",
            "Coverage.selected or Coverage.included",
            "false or Coverage.selected and Coverage.included",
            "false and Coverage.selected or Coverage.included",

            // IF
            "if Coverage.selected then Coverage.limit",
            "if Coverage.selected then Coverage.limit else RiskItem.limit",

            // TYPE
            "coverages[this instanceof Coverage]",
            "coverages[this typeof Coverage]",
            "Coverage instanceof Coverage",
            "Coverage typeof Coverage",
            "(Coverage) Coverage",

            // FUNCTIONS
            "Count(coverages)",
            "riskItems[*].coverages[Count(coverages[*].limit)]",
            "context.additional.policies[Distinct(for p in {this.policyNumber} return p)[0] = policyNumber]",
            "FromMoney(Coverage.money) > 100",
            "Coverage.money > 100",

            // INLINE ARRAY AND MAP
            "{Coverage.limit, RiskItem.limit}",
            "{'key1' : Coverage.limit, 'key2' : RiskItem.limit}",
            "{}",
            "GenericArray({Coverage.money, 1})",

            // COMPLEX
            "(coverages[limit > 20].deductibleAmounts)[0]",
            "coverages[limit > 20].deductibleAmounts[0]",
            "coverages[*].deductibleAmounts[this > 20]",
            "(coverages[*].limit)[this > 20]",

            // CCR
            "coverages[RiskItem.limit > 20].limit",
            "coverages[RiskItem.limit > 20]",

            // ITERATION
            "for c in coverages[limit > 20] return c.limitAmount",
            "Count(for c in coverages[limit > 20] return c.limitAmount)",
            "every c in coverages satisfies c.limitAmount > 10",
            "some c in coverages satisfies c.limitAmount > 10",
            "riskItems[every c in coverages satisfies c.limitAmount > 10]",
            "every c in coverages satisfies coverages[c.limitAmount = 10]",

            "Sum(for i in {2} return " +
                    "    i + Sum(for j in {2} return " +
                    "      j + Sum(for k in {2} return k) " +
                    "    )" +
                    "  )",

            // Dynamic context
            "context.dynamic",
            "context.dynamic * context.dynamic",
            "context.value1 > context.value2",
            "if(context.value1) then context.value2",
            "context.array[0].value1 > Coverage.limit",
            "every v in context.array[a > 10].value1 satisfies v < Coverage.limit ",

            // Function that returns array of complex types
            "GetCoverages(RiskItem)[0].limit",
            "some l in GetCoverages(RiskItem).limit satisfies l == Coverage.limit",

            // variables
            "set l to Coverage.limit "
                + "return l > 100",
            "set l to Coverage.limit "
                + "set islimit to every v in context.array[a > 10].value "
                + "satisfies v < l return islimit = true",
            "set a to 100 "
                + "set b to every c in coverages satisfies set limit to c.limitAmount return limit > a "
                + "return if b then a",
            "set a to 100 "
                + "return if true then set b to 1 return a > 1",
        };

        this.translator = new JavascriptAstTranslator(
            ExpressionLanguageConfiguration.builder()
                .strictTypeMode()
                .build()
        );
    }

    @Test
    public void shouldMatchTranslations() {
        var output = Arrays.stream(this.expressions)
            .map(expression -> expression + System.lineSeparator() + "=>" + System.lineSeparator() + exp(ast(expression)))
            .collect(Collectors.joining(System.lineSeparator() + System.lineSeparator()));

        assertThat(output, matches(snapshot));
    }

    private Expression ast(String expression) {
        return AstBuilder.from(expression, getCoverageScopeMock()).getExpression();
    }

    private String exp(Expression expression) {
        return translator.translate(new Ast(expression));
    }

    private Scope getCoverageScopeMock() {
        Type coverageType = type("Coverage",
            var("limit", NUMBER),
            var("money", MONEY),
            var("coverageCd", STRING),
            var("selected", BOOLEAN),
            var("included", BOOLEAN),
            var("deductibleAmounts", ArrayType.of(NUMBER)));

        Type riskItemType = type("RiskItem",
            var("limit", NUMBER),
            var("coverages", ArrayType.of(coverageType)));

        VariableSymbol coverage = new VariableSymbol(coverageType.getName(), coverageType);
        VariableSymbol riskItem = new VariableSymbol(riskItemType.getName(), riskItemType);
        VariableSymbol coverages = var("coverages", ArrayType.of(coverageType));
        VariableSymbol riskItems = var("riskItems", ArrayType.of(riskItemType));
        VariableSymbol arrayOfCoverages = var("arrayOfArrayOfCoverages", ArrayType.of(coverages.getType()));

        VariableSymbol context = new VariableSymbol("context", ANY);

        FunctionSymbol count = new FunctionSymbol("Count", NUMBER,
            List.of(new FunctionParameter(0, ArrayType.of(ANY))));
        FunctionSymbol sum = new FunctionSymbol("Sum", ArrayType.of(NUMBER),
            List.of(new FunctionParameter(0, ArrayType.of(NUMBER))));
        FunctionSymbol getCoverages = new FunctionSymbol("GetCoverages", ArrayType.of(coverageType),
            List.of(new FunctionParameter(0, riskItemType)));
        FunctionSymbol distinct = new FunctionSymbol("Distinct", ArrayType.of(ANY),
            List.of(new FunctionParameter(0, ArrayType.of(ANY))));
        FunctionSymbol genericArray = new FunctionSymbol("GenericArray", new GenericType("T"),
            List.of(new FunctionParameter(0, ArrayType.of(new GenericType("T")))));

        return new Scope(
            ScopeType.LOCAL,
            new Scope(
                new Type(
                    "TEST",
                    toSymbolTable(
                        List.of(count, sum, getCoverages, distinct, genericArray),
                        List.of(coverage, riskItem, context)
                    )
                ),
                Map.of(coverageType.getName(), coverageType, riskItemType.getName(), riskItemType)
            ),
            new Type(
                "LOCAL",
                toSymbolTable(
                    List.of(),
                    List.of(riskItems, coverages, arrayOfCoverages)
                )
            )
        );
    }

    private SymbolTable toSymbolTable(Collection<FunctionSymbol> functions, Collection<VariableSymbol> variables) {
        return new SymbolTable(
            functions,
            variables.stream().collect(Collectors.toMap(VariableSymbol::getName, symbol -> symbol))
        );
    }

    private Type type(String name, VariableSymbol... properties) {
        return new Type(name,
            new SymbolTable(
                List.of(),
                Arrays.stream(properties).collect(Collectors.toMap(VariableSymbol::getName, property -> property))
            )
        );
    }

    private VariableSymbol var(String name, Type type) {
        return new VariableSymbol(name, type);
    }
}
