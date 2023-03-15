package kraken.el.javascript;

import static java.util.List.of;
import static kraken.el.scope.type.Type.ANY;
import static kraken.el.scope.type.Type.BOOLEAN;
import static kraken.el.scope.type.Type.MONEY;
import static kraken.el.scope.type.Type.NUMBER;
import static kraken.el.scope.type.Type.STRING;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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

/**
 * @author mulevicius
 */
@RunWith(Parameterized.class)
public class JavascriptAstTranslatorTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<TestCase> testCases() {
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
            // DSL string literal to javascript string '"\ -> \'"\\
            tc("'\\'\\\"\\\\'",
                "'\\'\"\\\\'"),

            // REFERENCES
            tc("Coverage",
                "Coverage"),
            tc("Coverage.limit",
                "Coverage.limit"),
            tc("coverages",
                "coverages"),
            tc("this.coverages",
                "coverages"),
            tc("coverages",
                "coverages"),
            tc("coverages[0]",
                "__dataObject__.coverages[0]"),
            tc("coverages[0].limit",
                "__dataObject__.coverages[0].limit"),
            tc("coverages[this.limit > 20]",
                "(__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > 20))"),
            tc("coverages[limit > this.limit]",
                "(__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > this._nd(_x_.limit)))"),
            tc("coverages[*].limit",
                "(__dataObject__.coverages || []).map(_x_=>_x_.limit)"),
            tc("coverages[this.limit > 20].limit",
                "((__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > 20)) || []).map(_x_=>_x_.limit)"),
            tc("riskItems[*].coverages[*].limit",
                "((__dataObject__.riskItems || []).map(_x_=>_x_.coverages).reduce((p, n) => p.concat(n), []) || []).map(_x_=>_x_.limit)"),

            // NESTED FILTERS
            tc("riskItems[Count(coverages[limit > 10]) = 0]",
                "(__dataObject__.riskItems || []).filter(_x_=>this._eq(this.Count((_x_.coverages || []).filter(_y_=>(this._nd(_y_.limit) > 10))), 0))"),
            tc("context.riskItems[Count(context.coverages[riskItemLimit > this.limit]) = 0]",
                "(this._flatMap(context,_x_=>_x_.riskItems)"
                    + " || []).filter(_x_=>this._eq(this.Count((this._flatMap(context,_y_=>_y_.coverages)"
                    + " || []).filter(_y_=>(this._nd(this._o('riskItemLimit',[_y_, _x_]).riskItemLimit) > this._nd(this._flatMap(_y_,_z_=>_z_.limit))))), 0))"),

            // MATH
            tc("-Coverage.limit",
                "-this._n(Coverage.limit)"),
            tc("Coverage.limit + 5",
                "this._add(Coverage.limit, 5)"),
            tc("Coverage.limit - 5",
                "this._sub(Coverage.limit, 5)"),
            tc("Coverage.limit * 5",
                "this._mult(Coverage.limit, 5)"),
            tc("Coverage.limit / 5",
                "this._div(Coverage.limit, 5)"),
            tc("Coverage.limit ** 5",
                "this._pow(Coverage.limit, 5)"),
            tc("Coverage.limit % 5",
                "this._mod(Coverage.limit, 5)"),
            tc("Coverage.limit ** 5 + 10",
                "this._add(this._pow(Coverage.limit, 5), 10)"),
            tc("Coverage.limit ** (5 + 10)",
                "this._pow(Coverage.limit, this._add(5, 10))"),

            // LOGICAL
            tc("Coverage.limit > 5",
                "(this._nd(Coverage.limit) > 5)"),
            tc("Coverage.limit >= 5",
                "(this._nd(Coverage.limit) >= 5)"),
            tc("Coverage.limit < 5",
                "(this._nd(Coverage.limit) < 5)"),
            tc("Coverage.limit <= 5",
                "(this._nd(Coverage.limit) <= 5)"),
            tc("Coverage.limit = 5",
                "this._eq(Coverage.limit, 5)"),
            tc("Coverage.limit != 5",
                "this._neq(Coverage.limit, 5)"),
            tc("!(Coverage.limit = 5)",
                "!this._eq(Coverage.limit, 5)"),
            tc("not(Coverage.limit = 5)",
                "!this._eq(Coverage.limit, 5)"),
            tc("Coverage.coverageCd matches '[a..z]'",
                "(/[a..z]/.test(this._s(Coverage.coverageCd)))"),
            tc("Coverage.limit in {1, 2, 3}",
                "this._in([1,2,3], Coverage.limit)"),
            tc("!true",
                "!true"),

            tc("Coverage.selected and Coverage.included",
                "(this._b(Coverage.selected) && this._b(Coverage.included))"),
            tc("Coverage.selected or Coverage.included",
                "(this._b(Coverage.selected) || this._b(Coverage.included))"),
            tc("false or Coverage.selected and Coverage.included",
                "(false || (this._b(Coverage.selected) && this._b(Coverage.included)))"),
            tc("false and Coverage.selected or Coverage.included",
                "((false && this._b(Coverage.selected)) || this._b(Coverage.included))"),

            // IF
            tc("if Coverage.selected then Coverage.limit",
                "(this._b(Coverage.selected) ? Coverage.limit : undefined)"),
            tc("if Coverage.selected then Coverage.limit else RiskItem.limit",
                "(this._b(Coverage.selected) ? Coverage.limit : RiskItem.limit)"),

            // TYPE
            tc("coverages[this instanceof Coverage]",
                "(__dataObject__.coverages || []).filter(_x_=>this._i(_x_,'Coverage'))"),
            tc("coverages[this typeof Coverage]",
                "(__dataObject__.coverages || []).filter(_x_=>this._t(_x_,'Coverage'))"),
            tc("Coverage instanceof Coverage",
                "this._i(Coverage,'Coverage')"),
            tc("Coverage typeof Coverage",
                "this._t(Coverage,'Coverage')"),
            tc("(Coverage) Coverage",
                "Coverage"),

            // FUNCTIONS
            tc("Count(coverages)",
                "this.Count(__dataObject__.coverages)"),
            tc("riskItems[*].coverages[Count(coverages[*].limit)]",
                "(__dataObject__.riskItems || []).map(_x_=>_x_.coverages[this.Count((__dataObject__.coverages || []).map(_y_=>_y_.limit))])"),
            tc("context.additional.policies[Distinct(for p in {this.policyNumber} return p)[0] = policyNumber]",
                "(this._flatMap(this._flatMap(context,_x_=>_x_.additional),_x_=>_x_.policies)"
                    + " || []).filter(_x_=>this._eq(this.Distinct(([this._flatMap(_x_,_y_=>_y_.policyNumber)] || []).map(p=>p)"
                    + ".reduce((p, n) => p.concat(n), []))[0], _x_.policyNumber))"),

            // INLINE ARRAY AND MAP
            tc("{Coverage.limit, RiskItem.limit}",
                "[Coverage.limit,RiskItem.limit]"),
            tc("{'key1' : Coverage.limit, 'key2' : RiskItem.limit}",
                "{'key1':Coverage.limit,'key2':RiskItem.limit}"),
            tc("{}",
                "[]"),
            tc("GenericArray({Coverage.money, 1})",
                "this.GenericArray([this.FromMoney(Coverage.money),1])"),

            // COMPLEX
            tc("(coverages[limit > 20].deductibleAmounts)[0]",
                "((__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > 20)) || []).map(_x_=>_x_.deductibleAmounts).reduce((p, n) => p.concat(n), [])[0]"),
            tc("coverages[limit > 20].deductibleAmounts[0]",
                "((__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > 20)) || []).map(_x_=>_x_.deductibleAmounts[0])"),
            tc("coverages[*].deductibleAmounts[this > 20]",
                "((__dataObject__.coverages || []).map(_x_=>_x_.deductibleAmounts).reduce((p, n) => p.concat(n), []) || []).filter(_x_=>(this._nd(_x_) > 20))"),
            tc("(coverages[*].limit)[this > 20]",
                "((__dataObject__.coverages || []).map(_x_=>_x_.limit) || []).filter(_x_=>(this._nd(_x_) > 20))"),

            // CCR
            tc("coverages[RiskItem.limit > 20].limit",
                "((__dataObject__.coverages || []).filter(_x_=>(this._nd(RiskItem.limit) > 20)) || []).map(_x_=>_x_.limit)"),
            tc("coverages[RiskItem.limit > 20]",
                "(__dataObject__.coverages || []).filter(_x_=>(this._nd(RiskItem.limit) > 20))"),

            // ITERATION
            tc("for c in coverages[limit > 20] return c.limitAmount",
                "((__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > 20)) || []).map(c=>c.limitAmount)"),
            tc("Count(for c in coverages[limit > 20] return c.limitAmount)",
                "this.Count(((__dataObject__.coverages || []).filter(_x_=>(this._nd(_x_.limit) > 20)) || []).map(c=>c.limitAmount))"),
            tc("every c in coverages satisfies c.limitAmount > 10",
                "(__dataObject__.coverages || []).every(c=>(this._nd(c.limitAmount) > 10))"),
            tc("some c in coverages satisfies c.limitAmount > 10",
                "(__dataObject__.coverages || []).some(c=>(this._nd(c.limitAmount) > 10))"),
            tc("riskItems[every c in coverages satisfies c.limitAmount > 10]",
                "(__dataObject__.riskItems || []).filter(_x_=>(_x_.coverages || []).every(c=>(this._nd(c.limitAmount) > 10)))"),
            tc("every c in coverages satisfies coverages[c.limitAmount = 10]",
                "(__dataObject__.coverages || []).every(c=>(__dataObject__.coverages || []).filter(_x_=>this._eq(c.limitAmount, 10)))"),

            tc("Sum(for i in {2} return " +
                    "    i + Sum(for j in {2} return " +
                    "      j + Sum(for k in {2} return k) " +
                    "    )" +
                    "  )",
                "this.Sum(([2] || []).map(i=>this._add(i, this.Sum(([2] || []).map(j=>this._add(j, this.Sum(([2] || []).map(k=>k))))))))"),

            // Dynamic context
            tc("context.dynamic",
                "this._flatMap(context,_x_=>_x_.dynamic)"),
            tc("context.dynamic * context.dynamic",
                "this._mult(this._flatMap(context,_x_=>_x_.dynamic), this._flatMap(context,_x_=>_x_.dynamic))"),
            tc("context.value1 > context.value2",
                "(this._nd(this._flatMap(context,_x_=>_x_.value1)) > this._nd(this._flatMap(context,_x_=>_x_.value2)))"),
            tc("if(context.value1) then context.value2",
                "(this._b(this._flatMap(context,_x_=>_x_.value1)) ? this._flatMap(context,_x_=>_x_.value2) : undefined)"),
            tc("context.array[0].value1 > Coverage.limit",
                "(this._nd(this._flatMap(this._flatMap(context,_x_=>_x_.array[0]),_x_=>_x_.value1)) > this._nd(Coverage.limit))"),
            tc("every v in context.array[a > 10].value1 satisfies v < Coverage.limit ",
                "(this._flatMap((this._flatMap(context,_x_=>_x_.array) || []).filter(_x_=>(this._nd(_x_.a) > 10)),_x_=>_x_.value1) || []).every(v=>(this._nd(v) < this._nd(Coverage.limit)))"),

            // Function that returns array of complex types
            tc("GetCoverages(RiskItem)[0].limit",
                "this.GetCoverages(RiskItem)[0].limit"),
            tc("some l in GetCoverages(RiskItem).limit satisfies l == Coverage.limit",
                "((this.GetCoverages(RiskItem) || []).map(_x_=>_x_.limit) || []).some(l=>this._eq(l, Coverage.limit))"),

            // variables
            tc(""
                    + "set l to Coverage.limit "
                    + "return l > 100",
                ""
                    + "(()=>{"
                    + "const l=Coverage.limit;"
                    + "return (this._nd(l) > 100);"
                    + "})()"
            ),
            tc(""
                    + "set l to Coverage.limit "
                    + "set islimit to every v in context.array[a > 10].value satisfies v < l "
                    + "return islimit = true",
                ""
                    + "(()=>{"
                    + "const l=Coverage.limit;"
                    + "const islimit=(this._flatMap((this._flatMap(context,_x_=>_x_.array) || []).filter(_x_=>(this._nd(_x_.a) > 10)),_x_=>_x_.value) || []).every(v=>(this._nd(v) < this._nd(l)));"
                    + "return this._eq(islimit, true);"
                    + "})()"
            ),
            tc(""
                    + "set a to 100 "
                    + "set b to every c in coverages satisfies set limit to c.limitAmount return limit > a "
                    + "return if b then a",
                ""
                    + "(()=>{"
                    + "const a=100;"
                    + "const b=(__dataObject__.coverages || []).every(c=>(()=>{const limit=c.limitAmount;return (this._nd(limit) > this._nd(a));})());"
                    + "return (this._b(b) ? a : undefined);"
                    + "})()"
            ),
            tc(""
                    + "set a to 100 "
                    + "return if true then set b to 1 return a > 1",
                ""
                    + "(()=>{"
                    + "const a=100;"
                    + "return (true ? (()=>{const b=1;return (this._nd(a) > 1);})() : undefined);"
                    + "})()"
            )
        );
    }

    private JavascriptAstTranslator translator;

    private TestCase testCase;

    public JavascriptAstTranslatorTest(TestCase testCase) {
        this.testCase = testCase;
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
    public void shouldTranslateFromKELToJavascriptExpression() {
        assertThat(
            exp(ast(testCase.getKelExpression())),
            equalTo(testCase.getTranslatedExpression())
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

    private static TestCase tc(String kelExpression, String translatedExpression) {
        return new TestCase(kelExpression, translatedExpression);
    }

    private static final class TestCase {

        private String kelExpression;

        private String translatedExpression;

        public TestCase(String kelExpression, String translatedExpression) {
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
