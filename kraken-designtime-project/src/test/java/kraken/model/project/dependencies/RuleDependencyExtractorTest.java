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
package kraken.model.project.dependencies;

import static kraken.model.context.PrimitiveFieldDataType.BOOLEAN;
import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.context.PrimitiveFieldDataType.INTEGER;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.project.KrakenProjectMocks.arrayChild;
import static kraken.model.project.KrakenProjectMocks.arrayField;
import static kraken.model.project.KrakenProjectMocks.attribute;
import static kraken.model.project.KrakenProjectMocks.child;
import static kraken.model.project.KrakenProjectMocks.contextDefinition;
import static kraken.model.project.KrakenProjectMocks.createExternalContextDefinitionReference;
import static kraken.model.project.KrakenProjectMocks.entryPoints;
import static kraken.model.project.KrakenProjectMocks.externalContext;
import static kraken.model.project.KrakenProjectMocks.externalContextDefinition;
import static kraken.model.project.KrakenProjectMocks.field;
import static kraken.model.project.KrakenProjectMocks.function;
import static kraken.model.project.KrakenProjectMocks.functionSignature;
import static kraken.model.project.KrakenProjectMocks.parameter;
import static kraken.model.project.KrakenProjectMocks.rule;
import static kraken.model.project.KrakenProjectMocks.type;
import static kraken.model.project.dependencies.RuleDependencyExtractorTest.DependencyMatcher.contextRef;
import static kraken.model.project.dependencies.RuleDependencyExtractorTest.DependencyMatcher.fieldRef;
import static kraken.model.project.dependencies.RuleDependencyExtractorTest.DependencyMatcher.selfRef;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;

import kraken.el.scope.type.Type;
import kraken.model.Condition;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectMocks;
import kraken.model.validation.AssertionPayload;

/**
 * @author mulevicius
 */
public class RuleDependencyExtractorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private List<ContextDefinition> contextDefinitions;

    @Before
    public void setUp() throws Exception {
        ContextDefinition someContext = contextDefinition("SomeContext",
                List.of(
                        field("target", DECIMAL),
                        field("source", DECIMAL),
                        field("otherField", DECIMAL),
                        field("addressField", "Address"),
                        field("vehicle", "Vehicle"),
                        arrayField("someContextInfo", "Info")
                ),
                List.of(),
                List.of(
                        child("Vehicle")
                )
        );

        ContextDefinition otherContext = contextDefinition("OtherContext",
                List.of(
                        field("limit", DECIMAL),
                        field("price", DECIMAL),
                        field("vehicle", "Vehicle"),
                        arrayField("someContextInfo", "Info")
                ),
                List.of(),
                List.of(
                        child("Vehicle")
                )
        );

        ContextDefinition vehicle = contextDefinition("Vehicle",
                List.of(
                        field("vin", STRING),
                        field("addressField", "Address"),
                        field("vehicleInfo", "Info"),
                        arrayField("coverages", "Coverage"),
                        arrayField("someContextInfo", "Info")
                ),
                List.of(),
                List.of(
                        child("Info"),
                        arrayChild("Coverage")
                )
        );

        ContextDefinition address = contextDefinition("Address",
                List.of(
                        field("streetNumber", INTEGER)
                ),
                List.of(),
                List.of()
        );

        ContextDefinition info = contextDefinition("Info",
                List.of(
                        field("amount", DECIMAL),
                        field("happyNumber", INTEGER),
                        field("salary", DECIMAL)
                ),
                List.of(),
                List.of()
        );

        ContextDefinition coverage = contextDefinition("Coverage",
                List.of(
                        field("limitAmount", DECIMAL),
                        field("coverageInfo", "Info"),
                        arrayField("coverageDeductibles", "Deductible")
                ),
                List.of(),
                List.of(
                        child("Info"),
                        arrayChild("Deductible")
                )
        );

        ContextDefinition deductible = contextDefinition("Deductible",
                List.of(
                        field("deductibleAmount", DECIMAL),
                        arrayField("deductibleInfo", "Info")
                ),
                List.of(),
                List.of(
                        arrayChild("Info")
                )
        );

        ContextDefinition thirdContext = contextDefinition("ThirdContext",
                List.of(
                        field("limit", DECIMAL)
                ),
                List.of(),
                List.of()
        );

        ContextDefinition anotherContext = contextDefinition("AnotherContext",
                List.of(),
                List.of(),
                List.of()
        );

        ContextDefinition rootContext = contextDefinition("RootContext",
                List.of(),
                List.of(),
                List.of(
                        child("SomeContext"),
                        child("OtherContext"),
                        child("ThirdContext"),
                        child("AnotherContext")
                )
        );

        this.contextDefinitions = List.of(
                rootContext,
                anotherContext,
                thirdContext,
                deductible,
                coverage,
                info,
                address,
                vehicle,
                otherContext,
                someContext
        );
    }

    @Test
    public void shouldNotExtractDependencyFromExternalContextReference() {
        var rule = rule("TESTRULE", "SomeContext", "target");
        var expressionString = "every e in { context.external } satisfies e.previousPolicy.state != null";
        addCondition(expressionString, rule);

        var previousPolicyExternal = externalContextDefinition(
            "PreviousPolicy_external",
            List.of(attribute("state", type(STRING.name(), Cardinality.SINGLE, true)))
        );

        var externalContext = externalContext("root_external", Map.of("context",
            externalContext("context_map", Map.of("external",
                externalContext("external_map", Map.of(), Map.of("previousPolicy",
                    createExternalContextDefinitionReference(previousPolicyExternal.getName()))
                )),
                Map.of()
            )),
            Map.of()
        );

        var krakenProject = krakenProjectWithExternalData(rule, externalContext, List.of(previousPolicyExternal));
        var dependencyExtractor = new RuleDependencyExtractor(krakenProject);
        var dependencies = dependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, empty());
    }

    @Test
    public void extractDependencyFromNestedForExpressions() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "for vehicleCoverage in Vehicle.coverages return "
                + "every deductionInfo in vehicleCoverage.coverageDeductibles[*].deductibleInfo satisfies "
                + "deductionInfo.happyNumber > vehicleCoverage.limitAmount + source";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("Vehicle"),
            fieldRef("Coverage", "limitAmount"),
            fieldRef("Info", "happyNumber"),
            selfRef("SomeContext", "source")
        ));
    }

    @Test
    public void extractDependencyFromForReferencingComplexPath() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "every c in Vehicle.coverages satisfies c.coverageInfo.amount > Vehicle.vehicleInfo.happyNumber";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            fieldRef("Info", "amount"),
            fieldRef("Info", "happyNumber"),
            contextRef("Vehicle")
        ));
    }

    @Test
    public void extractDependenciesFromExpressionWithComplexFunctionReturnType() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "GetAddress(SomeContext).streetNumber";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(1));
        assertThat(dependencies, contains(fieldRef("Address", "streetNumber")));
    }

    @Test
    public void shouldExtractDependenciesFromExpressionWithComplexFunctionReturnType() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "GetAddress(SomeContext.addressField).streetNumber";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(1));
        assertThat(dependencies, contains(
            fieldRef("Address", "streetNumber")
        ));
    }

    @Test
    public void extractDependenciesFromExpressionWithCollectionComplexFunctionReturnType() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "GetAddresses(SomeContext)[0].streetNumber";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(1));
        assertThat(dependencies, contains(fieldRef("Address", "streetNumber")));
    }

    @Test
    public void extractDependenciesFromExpressionWithCollectionPredicate() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString =
                "Count(OtherContext.vehicle.coverages[limitAmount > Vehicle.vehicleInfo.salary])"
                        + " > Sum(Vehicle.coverages[coverageInfo.amount > OtherContext.price].coverageInfo.happyNumber)";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(7));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            contextRef("OtherContext", "price"),
            contextRef("Vehicle"),
            fieldRef("Coverage", "limitAmount"),
            fieldRef("Info", "happyNumber"),
            fieldRef("Info", "salary"),
            fieldRef("Info", "amount")
        ));
    }

    @Test
    public void extractExpressionDependencyOnlyOfFieldThatAreDefinedAsChildren() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "Sum(OtherContext.vehicle.coverages[*].limitAmount) > Sum(OtherContext.vehicle.addressField.streetNumber)";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            fieldRef("Coverage", "limitAmount"),
            fieldRef("Address", "streetNumber")
        ));
    }

    @Test
    public void extractDependenciesFromExpressionEvery() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "every c in Vehicle.coverages satisfies c.limitAmount > source";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("Vehicle"),
            fieldRef("Coverage", "limitAmount"),
            selfRef("SomeContext", "source")
        ));
    }

    @Test
    public void extractDependenciesFromExpressionCount() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "Count(OtherContext.vehicle.coverages[*].limitAmount)";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            fieldRef("Coverage", "limitAmount")
        ));
    }

    @Test
    public void extractFromCondition() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "for s in {SomeContext} return s.source == OtherContext.limit";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            contextRef("OtherContext", "limit"),
            fieldRef("SomeContext", "source")
        ));
    }

    @Test
    public void extractFromForExpressionInCondition() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "SomeContext.target = SomeContext.source / OtherContext.limit";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            selfRef("SomeContext", "source"),
            contextRef("OtherContext"),
            contextRef("OtherContext", "limit")
        ));
    }

    @Test
    public void extractFromConditionMultipleOccurs() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "source = SomeContext.source*10 / SomeContext.target";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(1));
        assertThat(dependencies, contains(selfRef("SomeContext", "source")));
    }

    @Test
    public void extractFromAssertion() {
        String expressionString = "SomeContext.target = OtherContext.price";
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(expression);
        rule.setPayload(payload);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            contextRef("OtherContext", "price")
        ));
    }

    @Test
    public void extractFromDefault() {
        String expressionString = "SomeContext.target + OtherContext.limit/ThirdContext.limit";
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setValueExpression(expression);
        rule.setPayload(payload);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("ThirdContext"),
            contextRef("ThirdContext", "limit"),
            contextRef("OtherContext"),
            contextRef("OtherContext", "limit")
        ));
    }

    @Test
    public void extractFromTemplate() {
        ErrorMessage errorMessage = factory.createErrorMessage();
        errorMessage.setErrorCode("CODE");
        errorMessage.setErrorMessage("Are equal: ${SomeContext.target = OtherContext.price}");
        Expression expression = factory.createExpression();
        expression.setExpressionString("true");

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(expression);
        payload.setErrorMessage(errorMessage);
        rule.setPayload(payload);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext", "price"),
            contextRef("OtherContext")
        ));
    }

    @Test
    public void shouldExtractNoDependenciesFromUnparseableExpression() {
        String expressionString = "SomeContext.target = \"\"\"";
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(expression);
        rule.setPayload(payload);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, empty());
    }

    @Test
    public void extractFromUsedFunction() {
        String expressionString = "CalculateTotalPrice(OtherContext)";
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(expression);
        rule.setPayload(payload);

        List<Function> functions = List.of(
            function(
                "CalculateTotalPrice",
                List.of(parameter("ctx", "OtherContext")),
                "ctx.price + ctx.limit"
            )
        );

        KrakenProject krakenProject = krakenProjectWithFunctions(rule, functions);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            fieldRef("OtherContext", "price"),
            fieldRef("OtherContext", "limit")
        ));
    }

    @Test
    public void extractFromUsedFunctionWithRecursion() {
        String expressionString = "CalculateRecursive(OtherContext, 3)";
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(expression);
        rule.setPayload(payload);

        List<Function> functions = List.of(
            function(
                "CalculateTotalPrice",
                List.of(parameter("ctx", "OtherContext")),
                "ctx.price + ctx.limit"
            ),
            function(
                "CalculateRecursive",
                List.of(
                    parameter("ctx", "OtherContext"),
                    parameter("depth", Type.NUMBER.getName())
                ),
                "CalculateRecursive(ctx, depth-1) + CalculateTotalPrice(ctx)"
            )
        );

        KrakenProject krakenProject = krakenProjectWithFunctions(rule, functions);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(3));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            fieldRef("OtherContext", "price"),
            fieldRef("OtherContext", "limit")
        ));
    }

    @Test
    public void extractFromUsedFunctionByActualArgumentTypes() {
        String expressionString = "CalculateTotalPriceDynamic(OtherContext) "
            + "+ CalculateTotalPriceDynamic(ThirdContext) "
            + "+ CalculateTotalPriceDynamic(SomeContext)";
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);

        Rule rule = rule("TESTRULE", "SomeContext", "target");
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(expression);
        rule.setPayload(payload);

        List<Function> functions = List.of(
            function(
                "CalculateTotalPriceDynamic",
                List.of(parameter("ctx", Type.ANY.getName())),
                "ctx.price + ctx.limit + ctx.unknown"
            )
        );

        KrakenProject krakenProject = krakenProjectWithFunctions(rule, functions);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(5));
        assertThat(dependencies, containsInAnyOrder(
            contextRef("OtherContext"),
            contextRef("ThirdContext"),
            fieldRef("OtherContext", "price"),
            fieldRef("OtherContext", "limit"),
            fieldRef("ThirdContext", "limit")
        ));
    }

    private void addCondition(String expressionString, Rule rule) {
        Condition condition = factory.createCondition();
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);
        condition.setExpression(expression);
        rule.setCondition(condition);
    }

    private KrakenProject krakenProjectWithExternalData(
        Rule rule,
        ExternalContext externalContext,
        List<ExternalContextDefinition> externalContextDefinitions
    ) {
        return KrakenProjectMocks.krakenProject(
            contextDefinitions,
            externalContext,
            externalContextDefinitions,
            entryPoints(),
            List.of(rule),
            functionSignatures()
        );
    }

    private KrakenProject krakenProjectWithFunctions(Rule rule, List<Function> functions) {
        return KrakenProjectMocks.krakenProject(
            contextDefinitions,
            null,
            List.of(),
            entryPoints(),
            List.of(rule),
            functionSignatures(),
            functions
        );
    }

    private KrakenProject krakenProject(Rule rule) {
        return krakenProjectWithFunctions(rule, List.of());
    }

    private static List<FunctionSignature> functionSignatures() {
        return List.of(
            functionSignature("GetAddress", "Address", List.of("Any")),
            functionSignature("GetAddresses", "Address[]", List.of("Any")),
            functionSignature("ReturnMe", "Any", List.of("Any"))
        );
    }

    static class DependencyMatcher extends BaseMatcher<FieldDependency> {
        private final String contextName;
        private final String fieldName;
        private final boolean ccrDependency;
        private final boolean selfDependency;

        public DependencyMatcher(String contextName, String fieldName, boolean ccrDependency, boolean selfDependency) {
            this.contextName = contextName;
            this.fieldName = fieldName;
            this.ccrDependency = ccrDependency;
            this.selfDependency = selfDependency;
        }

        @Override
        public boolean matches(Object item) {
            FieldDependency dependency = (FieldDependency) item;
            return Objects.equals(dependency.getFieldName(), fieldName)
                && Objects.equals(dependency.getContextName(), contextName)
                && dependency.isCcrDependency() == ccrDependency
                && dependency.isSelfDependency() == selfDependency;
        }

        @Override
        public void describeTo(Description description) {
            String ref = fieldName != null
                ? contextName + "." + fieldName
                : contextName;

            if(ccrDependency) {
                description.appendText("Expected context dependency to " + ref);
            } else if(selfDependency) {
                description.appendText("Expected self dependency to " + ref);
            } else {
                description.appendText("Expected field dependency to " + ref);
            }
        }

        public static DependencyMatcher fieldRef(String contextName, String fieldName) {
            return new DependencyMatcher(contextName, fieldName, false, false);
        }

        public static DependencyMatcher selfRef(String contextName, String fieldName) {
            return new DependencyMatcher(contextName, fieldName, false, true);
        }

        public static DependencyMatcher contextRef(String contextName, String fieldName) {
            return new DependencyMatcher(contextName, fieldName, true, false);
        }

        public static DependencyMatcher contextRef(String contextName) {
            return new DependencyMatcher(contextName, null, true, false);
        }
    }
}
