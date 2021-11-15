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

import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.context.PrimitiveFieldDataType.INTEGER;
import static kraken.model.context.PrimitiveFieldDataType.STRING;
import static kraken.model.project.KrakenProjectMocks.arrayChild;
import static kraken.model.project.KrakenProjectMocks.arrayField;
import static kraken.model.project.KrakenProjectMocks.child;
import static kraken.model.project.KrakenProjectMocks.contextDefinition;
import static kraken.model.project.KrakenProjectMocks.entryPoints;
import static kraken.model.project.KrakenProjectMocks.field;
import static kraken.model.project.KrakenProjectMocks.function;
import static kraken.model.project.KrakenProjectMocks.rule;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Condition;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
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
                allOf(
                        hasProperty("path", is("coverages")),
                        hasProperty("contextName", is("Vehicle")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limitAmount")),
                        hasProperty("contextName", is("Coverage")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("happyNumber")),
                        hasProperty("contextName", is("Info")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(false))
                )
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

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("coverages")),
                        hasProperty("contextName", is("Vehicle")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("amount")),
                        hasProperty("contextName", is("Info")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("vehicleInfo")),
                        hasProperty("contextName", is("Vehicle")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("happyNumber")),
                        hasProperty("contextName", is("Info")),
                        hasProperty("contextDependency", is(false))
                )
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

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("streetNumber")),
                        hasProperty("contextName", is("Address")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is(nullValue())),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(true))
                )
        ));
    }

    @Test
    public void shouldExtractDependenciesFromExpressionWithComplexFunctionReturnType() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "GetAddress(SomeContext.addressField).streetNumber";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("streetNumber")),
                        hasProperty("contextName", is("Address")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("addressField")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(true))
                )
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

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("streetNumber")),
                        hasProperty("contextName", is("Address")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is(nullValue())),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(true))
                )
        ));
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

        assertThat(dependencies, hasSize(9));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("vehicle")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limitAmount")),
                        hasProperty("contextName", is("Coverage")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("vehicleInfo")),
                        hasProperty("contextName", is("Vehicle")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("salary")),
                        hasProperty("contextName", is("Info")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("coverages")),
                        hasProperty("contextName", is("Vehicle")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("amount")),
                        hasProperty("contextName", is("Info")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("price")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("price")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("happyNumber")),
                        hasProperty("contextName", is("Info")),
                        hasProperty("contextDependency", is(false))
                )
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

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("vehicle")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limitAmount")),
                        hasProperty("contextName", is("Coverage")),
                        hasProperty("contextDependency", is(false))
                )
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
        assertThat(dependencies, contains(
                allOf(
                        hasProperty("path", is("coverages")),
                        hasProperty("contextName", is("Vehicle")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limitAmount")),
                        hasProperty("contextName", is("Coverage")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(false))
                )
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
                allOf(
                        hasProperty("path", is("vehicle")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limitAmount")),
                        hasProperty("contextName", is("Coverage")),
                        hasProperty("contextDependency", is(false))
                )
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

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is(nullValue())),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(false))
                )
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

        assertThat(dependencies, hasSize(4));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(false))
                )
        ));
    }

    @Test
    public void extractFromConditionMultipleOccurs() {
        Rule rule = rule("TESTRULE", "SomeContext", "target");
        String expressionString = "SomeContext.source = SomeContext.source*10 / SomeContext.target";
        addCondition(expressionString, rule);

        KrakenProject krakenProject = krakenProject(rule);
        RuleDependencyExtractor ruleDependencyExtractor = new RuleDependencyExtractor(krakenProject);
        Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(rule);

        assertThat(dependencies, hasSize(2));
        assertThat(dependencies, containsInAnyOrder(
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("source")),
                        hasProperty("contextName", is("SomeContext")),
                        hasProperty("contextDependency", is(false))
                )
        ));
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
                allOf(
                        hasProperty("path", is("price")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("price")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(false))
                )
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
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("ThirdContext")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("ThirdContext")),
                        hasProperty("contextDependency", is(true))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(false))
                ),
                allOf(
                        hasProperty("path", is("limit")),
                        hasProperty("contextName", is("OtherContext")),
                        hasProperty("contextDependency", is(true))
                )
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
            allOf(
                hasProperty("path", is("price")),
                hasProperty("contextName", is("OtherContext")),
                hasProperty("contextDependency", is(true))
            ),
            allOf(
                hasProperty("path", is("price")),
                hasProperty("contextName", is("OtherContext")),
                hasProperty("contextDependency", is(false))
            )
        ));
    }

    private void addCondition(String expressionString, Rule rule) {
        Condition condition = factory.createCondition();
        Expression expression = factory.createExpression();
        expression.setExpressionString(expressionString);
        condition.setExpression(expression);
        rule.setCondition(condition);
    }

    private KrakenProject krakenProject(Rule rule) {
        return KrakenProjectMocks.krakenProject(contextDefinitions, entryPoints(), List.of(rule), functionSignatures());
    }

    private static List<FunctionSignature> functionSignatures() {
        return List.of(
            function("GetAddress", "Address", List.of("Any")),
            function("GetAddresses", "Address[]", List.of("Any"))
        );
    }
}
