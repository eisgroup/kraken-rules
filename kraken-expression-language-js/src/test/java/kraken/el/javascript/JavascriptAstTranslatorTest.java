package kraken.el.javascript;

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
import kraken.el.scope.type.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.List.of;
import static kraken.el.scope.type.Type.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author mulevicius
 */
@RunWith(Parameterized.class)
public class JavascriptAstTranslatorTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<MvelAstTestCase> testCases() {
        return of(
                // LITERALS
                tc("true",
                        "true"),
                tc("false",
                        "false"),
                tc("null",
                        "undefined"),
                tc("1",
                        "1"),
                tc("-1",
                        "-1"),
                tc("1.0",
                        "1.0"),
                tc("1.5",
                        "1.5"),
                tc("-1.5",
                        "-1.5"),
                tc("'string'",
                        "'string'"),
                tc("\"string\"",
                        "'string'"),

                // REFERENCES
                tc("Coverage",
                        "Coverage"),
                tc("Coverage.limit",
                        "Coverage.limit"),
                tc("this.coverages",
                        "__dataObject__.coverages"),
                tc("coverages",
                        "coverages"),
                tc("coverages[0]",
                        "coverages[0]"),
                tc("coverages[0].limit",
                        "coverages[0].limit"),
                tc("coverages[this.limit > 20]",
                        "coverages.filter(function(_x_) { return (_nd(_x_.limit) > 20) })"),
                tc("coverages[*].limit",
                        "coverages.map(function(_x_) { return _x_.limit })"),
                tc("coverages[this.limit > 20].limit",
                        "coverages.filter(function(_x_) { return (_nd(_x_.limit) > 20) })" +
                                ".map(function(_x_) { return _x_.limit })"),
                tc("riskItems[*].coverages[*].limit",
                        "riskItems.map(function(_x_) { return _x_.coverages })" +
                                ".reduce(function(p, n) { return p.concat(n) }, [])" +
                                ".map(function(_x_) { return _x_.limit })"),

                // MATH
                tc("-Coverage.limit",
                        "-_n(Coverage.limit)"),
                tc("Coverage.limit + 5",
                        "_add(Coverage.limit, 5)"),
                tc("Coverage.limit - 5",
                        "_sub(Coverage.limit, 5)"),
                tc("Coverage.limit * 5",
                        "_mult(Coverage.limit, 5)"),
                tc("Coverage.limit / 5",
                        "_div(Coverage.limit, 5)"),
                tc("Coverage.limit ** 5",
                        "_pow(Coverage.limit, 5)"),
                tc("Coverage.limit % 5",
                        "_mod(Coverage.limit, 5)"),
                tc("Coverage.limit ** 5 + 10",
                        "_add(_pow(Coverage.limit, 5), 10)"),
                tc("Coverage.limit ** (5 + 10)",
                        "_pow(Coverage.limit, _add(5, 10))"),

                // LOGICAL
                tc("Coverage.limit > 5",
                        "(_nd(Coverage.limit) > 5)"),
                tc("Coverage.limit >= 5",
                        "(_nd(Coverage.limit) >= 5)"),
                tc("Coverage.limit < 5",
                        "(_nd(Coverage.limit) < 5)"),
                tc("Coverage.limit <= 5",
                        "(_nd(Coverage.limit) <= 5)"),
                tc("Coverage.limit = 5",
                        "_eq(Coverage.limit, 5)"),
                tc("Coverage.limit != 5",
                        "_neq(Coverage.limit, 5)"),
                tc("!(Coverage.limit = 5)",
                        "!_eq(Coverage.limit, 5)"),
                tc("not(Coverage.limit = 5)",
                        "!_eq(Coverage.limit, 5)"),
                tc("Coverage.coverageCd matches '[a..z]'",
                        "(/[a..z]/.test(_s(Coverage.coverageCd)))"),
                tc("Coverage.limit in {1, 2, 3}",
                        "(([1,2,3] || []).indexOf(Coverage.limit) > -1)"),
                tc("!true",
                        "!true"),

                tc("Coverage.selected and Coverage.included",
                        "(_b(Coverage.selected) && _b(Coverage.included))"),
                tc("Coverage.selected or Coverage.included",
                        "(_b(Coverage.selected) || _b(Coverage.included))"),
                tc("false or Coverage.selected and Coverage.included",
                        "(false || (_b(Coverage.selected) && _b(Coverage.included)))"),
                tc("false and Coverage.selected or Coverage.included",
                        "((false && _b(Coverage.selected)) || _b(Coverage.included))"),

                // IF
                tc("if Coverage.selected then Coverage.limit",
                        "(_b(Coverage.selected) ? Coverage.limit : undefined)"),
                tc("if Coverage.selected then Coverage.limit else RiskItem.limit",
                        "(_b(Coverage.selected) ? Coverage.limit : RiskItem.limit)"),

                // TYPE
                tc("coverages[this instanceof Coverage]",
                        "coverages.filter(function(_x_) { return _i(_x_,'Coverage') })"),
                tc("coverages[this typeof Coverage]",
                        "coverages.filter(function(_x_) { return _t(_x_,'Coverage') })"),
                tc("Coverage instanceof Coverage",
                        "_i(Coverage,'Coverage')"),
                tc("Coverage typeof Coverage",
                        "_t(Coverage,'Coverage')"),
                tc("(Coverage) Coverage",
                        "Coverage"),

                // FUNCTIONS
                tc("Count(coverages)",
                        "Count(coverages)"),
                tc("riskItems[*].coverages[Count(coverages[*].limit)]",
                        "riskItems.map(function(_x_) { " +
                                "return _x_.coverages[Count(coverages.map(function(_y_) { return _y_.limit }))] " +
                                "})"),

                // INLINE ARRAY AND MAP
                tc("{Coverage.limit, RiskItem.limit}",
                        "[Coverage.limit,RiskItem.limit]"),
                tc("{'key1' : Coverage.limit, 'key2' : RiskItem.limit}",
                        "{'key1':Coverage.limit,'key2':RiskItem.limit}"),
                tc("{}",
                        "[]"),

                // COMPLEX
                tc("(coverages[limit > 20].deductibleAmounts)[0]",
                        "coverages.filter(function(_x_) { return (_nd(_x_.limit) > 20) })" +
                                ".map(function(_x_) { return _x_.deductibleAmounts })" +
                                ".reduce(function(p, n) { return p.concat(n) }, [])[0]"),
                tc("coverages[limit > 20].deductibleAmounts[0]",
                        "coverages.filter(function(_x_) { return (_nd(_x_.limit) > 20) })" +
                                ".map(function(_x_) { return _x_.deductibleAmounts[0] })"),
                tc("coverages[*].deductibleAmounts[this > 20]",
                        "coverages.map(function(_x_) { return _x_.deductibleAmounts })" +
                                ".reduce(function(p, n) { return p.concat(n) }, [])" +
                                ".filter(function(_x_) { return (_nd(_x_) > 20) })"),
                tc("(coverages[*].limit)[this > 20]",
                        "coverages.map(function(_x_) { return _x_.limit })" +
                                ".filter(function(_x_) { return (_nd(_x_) > 20) })"),

                // CCR
                tc("coverages[RiskItem.limit > 20].limit",
                        "coverages.filter(function(_x_) { return (_nd(RiskItem.limit) > 20) })" +
                                ".map(function(_x_) { return _x_.limit })"),
                tc("coverages[RiskItem.limit > 20]",
                        "coverages.filter(function(_x_) { return (_nd(RiskItem.limit) > 20) })"),

                // ITERATION
                tc("for c in coverages[limit > 20] return c.limitAmount",
                        "(coverages.filter(function(_x_) { return (_nd(_x_.limit) > 20) }) || [])" +
                                ".map(function(c) { return c.limitAmount })"),
                tc("Count(for c in coverages[limit > 20] return c.limitAmount)",
                        "Count((coverages.filter(function(_x_) { return (_nd(_x_.limit) > 20) }) || [])" +
                                ".map(function(c) { return c.limitAmount }))"),
                tc("every c in coverages satisfies c.limitAmount > 10",
                        "(coverages || []).every(function(c) { return (_nd(c.limitAmount) > 10) })"),
                tc("some c in coverages satisfies c.limitAmount > 10",
                        "(coverages || []).some(function(c) { return (_nd(c.limitAmount) > 10) })"),
                tc("riskItems[every c in coverages satisfies c.limitAmount > 10]",
                        "riskItems.filter(function(_x_) { return (_x_.coverages || [])" +
                                ".every(function(c) { return (_nd(c.limitAmount) > 10) }) })"),

                tc("Sum(for i in {2} return " +
                                "    i + Sum(for j in {2} return " +
                                "      j + Sum(for k in {2} return k) " +
                                "    )" +
                                "  )",
                        "Sum(([2] || [])" +
                                ".map(function(i) { return _add(i, Sum(([2] || [])" +
                                ".map(function(j) { return _add(j, Sum(([2] || [])" +
                                ".map(function(k) { return k }))) }))) }))"),

                // Dynamic context
                tc("context.dynamic",
                        "context.dynamic"),
                tc("context.dynamic * context.dynamic",
                        "_mult(context.dynamic, context.dynamic)"),
                tc("context.value1 > context.value2",
                        "(_nd(context.value1) > _nd(context.value2))"),
                tc("if(context.value1) then context.value2",
                        "(_b(context.value1) ? context.value2 : undefined)"),
                tc("context.array[0].value1 > Coverage.limit",
                        "(_nd(context.array[0].value1) > _nd(Coverage.limit))"),
                tc("every v in context.array[a > 10].value1 satisfies v < Coverage.limit ",
                        "(context.array.filter(function(_x_) { return (_nd(_x_.a) > 10) })" +
                                ".map(function(_x_) { return _x_.value1 })" +
                                ".reduce(function(p, n) { return p.concat(n) }, []) || [])" +
                                ".every(function(v) { return (_nd(v) < _nd(Coverage.limit)) })"),

                // Function that returns array of complex types
                tc("GetCoverages(RiskItem)[0].limit",
                        "GetCoverages(RiskItem)[0].limit"),
                tc("some l in GetCoverages(RiskItem).limit satisfies l == Coverage.limit",
                        "(GetCoverages(RiskItem).map(function(_x_) { return _x_.limit }) || [])" +
                                ".some(function(l) { return _eq(l, Coverage.limit) })")
        );
    }

    private JavascriptAstTranslator translator;

    private MvelAstTestCase mvelAstTestCase;

    public JavascriptAstTranslatorTest(MvelAstTestCase mvelAstTestCase) {
        this.mvelAstTestCase = mvelAstTestCase;
    }

    @Before
    public void setUp() {
        this.translator = new JavascriptAstTranslator(
                ExpressionLanguageConfiguration.builder()
                        .strictTypeMode()
                        .build()
        );
    }

    @Test
    public void shouldTranslateFromKELToMvelExpression() {
        assertThat(
                exp(ast(mvelAstTestCase.getKelExpression())),
                equalTo(mvelAstTestCase.getTranslatedExpression())
        );
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

        return new Scope(ScopeType.LOCAL,
                new Scope(
                        new Type("TEST",
                                toSymbolTable(
                                        List.of(count, sum, getCoverages),
                                        List.of(coverage, riskItems, coverages, arrayOfCoverages, riskItem, context)
                                )
                        ),
                        Map.of(coverageType.getName(), coverageType, riskItemType.getName(), riskItemType)
                ),
                new Type("LOCAL"));
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

    private static MvelAstTestCase tc(String kelExpression, String translatedExpression) {
        return new MvelAstTestCase(kelExpression, translatedExpression);
    }

    public static final class MvelAstTestCase {

        private String kelExpression;

        private String translatedExpression;

        public MvelAstTestCase(String kelExpression, String translatedExpression) {
            this.kelExpression = kelExpression;
            this.translatedExpression = translatedExpression;
        }

        public String getKelExpression() {
            return kelExpression;
        }

        public String getTranslatedExpression() {
            return translatedExpression;
        }

        @Override
        public String toString() {
            return kelExpression + " => " + translatedExpression;
        }
    }
}
