package kraken.el.mvel;

import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.ast.Ast;
import kraken.el.ast.Expression;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.mvel.translator.MvelAstTranslator;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

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
public class MvelAstTranslatorTest {

    @Parameters(name = "{index}: {0}")
    public static Iterable<MvelAstTestCase> testCases() {
        return of(
                // LITERALS
                tc("true",
                        "true"),
                tc("false",
                        "false"),
                tc("null",
                        "null"),
                tc("1",
                        "1"),
                tc("-1",
                        "-1"),
                tc("1.0",
                        "1.0B"),
                tc("1.5",
                        "1.5B"),
                tc("-1.5",
                        "-1.5B"),
                tc("'string'",
                        "'string'"),
                tc("\"string\"",
                        "'string'"),
                tc("2020-01-01",
                        "Date('2020-01-01')"),
                tc("2020-01-01T10:00:00Z",
                        "DateTime('2020-01-01T10:00:00Z')"),
                // REFERENCES
                tc("Coverage",
                        "Coverage"),
                tc("Coverage.limit",
                        "Coverage.limit"),
                tc("this.coverages",
                        "this.coverages"),
                tc("coverages[0]",
                        "GetElement(coverages,0)"),
                tc("coverages[0].limit",
                        "GetElement(coverages,0).limit"),
                tc("Policy.riskItems[0].coverages[0]",
                        "GetElement(GetElement(Policy.riskItems,0).coverages,0)"),
                tc("Policy.riskItems[0].coverages[coverages[1].limit].limit",
                        "GetElement(GetElement(Policy.riskItems,0).coverages,GetElement(coverages,1).limit).limit"),
                tc("Policy.riskItems[0].insured",
                        "GetElement(Policy.riskItems,0).insured"),
                tc("RiskItem.coverages[Count(coverages)]",
                        "GetElement(RiskItem.coverages,Invoke('Count',[coverages]))"),
                tc("this.coverages[limit > 20]",
                        "Filter(this.coverages,'(_nd(this.limit) > 20)')"),
                tc("coverages[this.limit > 20]",
                        "Filter(coverages,'(_nd(this.limit) > 20)')"),
                tc("coverages[*].limit",
                        "FlatMap(coverages,'limit')"),
                tc("riskItems[*].coverages[*].limit",
                        "FlatMap(FlatMap(riskItems,'coverages'),'limit')"),
                tc("coverages[limit > 20].limit",
                        "FlatMap(Filter(coverages,'(_nd(this.limit) > 20)'),'limit')"),

                tcNonStrict( "Coverage.limit",
                        "Coverage.?limit"),
                tcNonStrict("this.coverages",
                        "this.?coverages"),
                tcNonStrict("coverages[0].limit",
                        "GetElement(coverages,0).?limit"),

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
                        "(_s(Coverage.coverageCd) ~= '[a..z]')"),
                tc("Coverage.limit in {1, 2, 3}",
                        "_in([1,2,3], Coverage.limit)"),
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
                        "(_b(Coverage.selected) ? Coverage.limit : null)"),
                tc("if Coverage.selected then Coverage.limit else RiskItem.limit",
                        "(_b(Coverage.selected) ? Coverage.limit : RiskItem.limit)"),

                // TYPE
                tc("coverages[this instanceof Coverage]",
                        "Filter(coverages,'_i(this,\\'Coverage\\')')"),
                tc("coverages[this typeof Coverage]",
                        "Filter(coverages,'_t(this,\\'Coverage\\')')"),
                tc("Coverage instanceof Coverage",
                        "_i(Coverage,'Coverage')"),
                tc("Coverage typeof Coverage",
                        "_t(Coverage,'Coverage')"),
                tc("(Coverage) Coverage",
                        "Coverage"),

                // FUNCTIONS
                tc("Count(coverages)",
                        "Invoke('Count',[coverages])"),
                tc("coverages[Count(coverages[*].limit)]",
                        "GetElement(coverages,Invoke('Count',[FlatMap(coverages,'limit')]))"),
                tc(true, "Count(coverages)",
                        "InvokeWithIteration('Count',[coverages])"),
                tc(true, "riskItems[*].coverages[Count(coverages[*].limit)]",
                        "FlatMap(riskItems,'GetElement(coverages,InvokeWithIteration(\\'Count\\',[FlatMap(coverages,\\'limit\\')]))')"),

                // INLINE ARRAY AND MAP
                tc("{Coverage.limit, RiskItem.limit}",
                        "[Coverage.limit,RiskItem.limit]"),
                tc("{'key1' : Coverage.limit, 'key2' : RiskItem.limit}",
                        "['key1':Coverage.limit,'key2':RiskItem.limit]"),
                tc("{}",
                        "[]"),

                // COMPLEX
                tc("(coverages[limit > 20].deductibleAmounts)[0]",
                        "GetElement(FlatMap(Filter(coverages,'(_nd(this.limit) > 20)'),'deductibleAmounts'),0)"),
                tc("coverages[limit > 20].deductibleAmounts[0]",
                        "FlatMap(Filter(coverages,'(_nd(this.limit) > 20)'),'GetElement(deductibleAmounts,0)')"),
                tc("coverages[*].deductibleAmounts[this > 20]",
                        "Filter(FlatMap(coverages,'deductibleAmounts'),'(_nd(this) > 20)')"),
                tc("(coverages[*].limit)[this > 20]",
                        "Filter(FlatMap(coverages,'limit'),'(_nd(this) > 20)')"),

                // CCR
                tc("coverages[RiskItem.limit > 20].limit",
                        "FlatMap(Filter(coverages,'(_nd(RiskItem.limit) > 20)'),'limit')"),
                tc("coverages[RiskItem.limit > 20]",
                        "Filter(coverages,'(_nd(RiskItem.limit) > 20)')"),

                // ITERATION
                tc("every c in coverages satisfies c.limitAmount == Count(coverages)",
                        "ForEvery('c',coverages,'_eq(c.limitAmount, Invoke(\\'Count\\',[coverages]))')"),

                tc("for c in coverages[limit > 20] return c.limitAmount",
                        "ForEach('c',Filter(coverages,'(_nd(this.limit) > 20)'),'c.limitAmount')"),
                tc("Count(for c in coverages[limit > 20] return c.limitAmount)",
                        "Invoke('Count',[ForEach('c',Filter(coverages,'(_nd(this.limit) > 20)'),'c.limitAmount')])"),
                tc("every c in coverages satisfies c.limitAmount > 10",
                        "ForEvery('c',coverages,'(_nd(c.limitAmount) > 10)')"),
                tc("some c in coverages satisfies c.limitAmount > 10",
                        "ForSome('c',coverages,'(_nd(c.limitAmount) > 10)')"),
                tc("riskItems[every c in coverages satisfies c.limitAmount > 10]",
                        "Filter(riskItems,'ForEvery(\\'c\\',this.coverages,\\'(_nd(c.limitAmount) > 10)\\')')"),

                // NESTED ITERATION
                tc(
                        "Sum(for i in {1} return " +
                        "  i + Sum(for j in {10} return " +
                        "    i + j + Sum(for k in {100} return i + j + k) " +
                        "  )" +
                        ")",
                        "Invoke('Sum',[" +
                                  "ForEach('i',[1]," +
                                    "'_add(i, Invoke(\\'Sum\\',[ForEach(\\'j\\',[10]," +
                                      "\\'_add(_add(i, j), Invoke(\\\\\\'Sum\\\\\\',[" +
                                        "ForEach(\\\\\\'k\\\\\\',[100],\\\\\\'_add(_add(i, j), k)\\\\\\')" +
                                      "]))" +
                                    "\\')]))'" +
                                  ")" +
                                "])"),

                // Dynamic context
                tc("context.dynamic",
                        "context.dynamic"),
                tc("context.dynamic * context.dynamic",
                        "_mult(context.dynamic, context.dynamic)"),
                tc("context.value1 > context.value2",
                        "(_nd(context.value1) > _nd(context.value2))"),
                tc("if(context.value1) then context.value2",
                        "(_b(context.value1) ? context.value2 : null)"),
                tc("context.array[0].value1 > Coverage.limit",
                        "(_nd(GetElement(context.array,0).value1) > _nd(Coverage.limit))"),
                tc("every v in context.array[a > 10].value1 satisfies v < Coverage.limit ",
                        "ForEvery('v'," +
                                "FlatMap(" +
                                  "Filter(context.array,'(_nd(this.a) > 10)'),'value1')," +
                                  "'(_nd(v) < _nd(Coverage.limit))'" +
                                ")"),

                // Function that returns array of complex types
                tc("GetCoverages(RiskItem)[0].limit",
                        "GetElement(Invoke('GetCoverages',[RiskItem]),0).limit"),
                tc("some l in GetCoverages(RiskItem).limit satisfies l == Coverage.limit",
                        "ForSome('l',FlatMap(Invoke('GetCoverages',[RiskItem]),'limit'),'_eq(l, Coverage.limit)')")
        );
    }

    private MvelAstTestCase mvelAstTestCase;

    public MvelAstTranslatorTest(MvelAstTestCase mvelAstTestCase) {
        this.mvelAstTestCase = mvelAstTestCase;
    }

    @Test
    public void shouldTranslateFromKELToMvelExpression() {
        assertThat(
                exp(ast(mvelAstTestCase.getKelExpression()), mvelAstTestCase.isStrictTypeMode(), mvelAstTestCase.isAutomaticIteration()),
                equalTo(mvelAstTestCase.getTranslatedExpression())
        );
    }

    private Expression ast(String expression) {
        return AstBuilder.from(expression, getCoverageScopeMock()).getExpression();
    }

    private String exp(Expression expression, boolean strictTypeMode, boolean automaticIteration) {
        var configuration = new ExpressionLanguageConfiguration(null, automaticIteration, strictTypeMode);
        MvelAstTranslator translator = new MvelAstTranslator(configuration);
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

    private static MvelAstTestCase tc(boolean automaticIteration, String kelExpression, String translatedExpression) {
        return new MvelAstTestCase(true, kelExpression, translatedExpression, automaticIteration);
    }

    private static MvelAstTestCase tcNonStrict(String kelExpression, String translatedExpression) {
        return new MvelAstTestCase(false, kelExpression, translatedExpression, false);
    }

    private static MvelAstTestCase tc(String kelExpression, String translatedExpression) {
        return new MvelAstTestCase(true, kelExpression, translatedExpression, false);
    }

    public static final class MvelAstTestCase {

        private boolean automaticIteration;

        private boolean strictTypeMode;

        private String kelExpression;

        private String translatedExpression;

        public MvelAstTestCase(boolean strictTypeMode, String kelExpression, String translatedExpression, boolean automaticIteration) {
            this.strictTypeMode = strictTypeMode;
            this.kelExpression = kelExpression;
            this.translatedExpression = translatedExpression;
            this.automaticIteration = automaticIteration;
        }

        public boolean isAutomaticIteration() {
            return automaticIteration;
        }

        public boolean isStrictTypeMode() {
            return strictTypeMode;
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
