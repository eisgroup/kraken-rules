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
package kraken.runtime.engine.result.reducers.validation;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.TestRuleBuilder;
import kraken.model.context.Cardinality;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataReference;
import kraken.runtime.engine.dto.OverrideDependency;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mulevicius
 */
public class OverrideDependencyExtractorTest {

    private OverrideDependencyExtractor overrideDependencyExtractor;

    @Before
    public void setUp() {
        this.overrideDependencyExtractor = new OverrideDependencyExtractor(new KrakenExpressionEvaluator());
    }

    @Test
    public void shouldExtractMultipleDependencies() {
        Map<String, OverrideDependency> overrideDependencies = overrideDependencyExtractor.extractOverrideDependencies(
                rule(
                        ccrDependency("RiskItem", "itemName"),
                        selfDependency("Address", "street")
                ),
                context(
                        "Policy",
                        List.of(),
                        List.of(
                                ref(context("RiskItem", List.of(field("itemName")), List.of())),
                                ref(
                                        "Address",
                                        context("BillingAddress", List.of(field("street")), List.of()),
                                        Cardinality.SINGLE
                                )
                        )
                )
        );

        OverrideDependency itemNameDependency = overrideDependencies.get("RiskItem.itemName");
        assertThat(itemNameDependency.getName(), equalTo("RiskItem.itemName"));
        assertThat(itemNameDependency.getValue(), equalTo("itemNameValue"));

        OverrideDependency streetDependency = overrideDependencies.get("BillingAddress.street");
        assertThat(streetDependency.getName(), equalTo("BillingAddress.street"));
        assertThat(streetDependency.getValue(), equalTo("streetValue"));
    }

    @Test
    public void shouldNotExtractFieldDependency() {
        Map<String, OverrideDependency> overrideDependencies = overrideDependencyExtractor.extractOverrideDependencies(
                rule(
                        fieldDependency("RiskItem", "itemName")
                ),
                context(
                        "Policy",
                        List.of(),
                        List.of(ref(context("RiskItem", List.of(field("itemName")), List.of())))
                )
        );

        assertThat(overrideDependencies.values(), empty());
    }

    @Test
    public void shouldNotExtractComplexDependency() {
        Map<String, OverrideDependency> overrideDependencies = overrideDependencyExtractor.extractOverrideDependencies(
                rule(
                        ccrDependency("RiskItem", "coverage")
                ),
                context(
                        "Policy",
                        List.of(),
                        List.of(ref(context("RiskItem", List.of(field("coverage", Cardinality.SINGLE, "Coverage")), List.of())))
                )
        );

        assertThat(overrideDependencies.values(), empty());
    }

    @Test
    public void shouldNotExtractDependencyOfCollectionCrossContext() {
        Map<String, OverrideDependency> overrideDependencies = overrideDependencyExtractor.extractOverrideDependencies(
                rule(
                        ccrDependency("RiskItem", "itemName")
                ),
                context(
                        "Policy",
                        List.of(),
                        List.of(ref(context("RiskItem", List.of(field("itemName")), List.of()), Cardinality.MULTIPLE))
                )
        );

        assertThat(overrideDependencies.values(), empty());
    }

    @Test
    public void shouldNotExtractDependencyOfCollectionField() {
        Map<String, OverrideDependency> overrideDependencies = overrideDependencyExtractor.extractOverrideDependencies(
                rule(
                        ccrDependency("RiskItem", "itemName")
                ),
                context(
                        "Policy",
                        List.of(),
                        List.of(ref(context("RiskItem", List.of(field("itemName", Cardinality.MULTIPLE)), List.of())))
                )
        );

        assertThat(overrideDependencies.values(), empty());
    }

    private ContextField field(String name) {
        return field(name, Cardinality.SINGLE);
    }

    private ContextField field(String name, Cardinality cardinality) {
        return field(name, cardinality, PrimitiveFieldDataType.STRING.toString());
    }

    private ContextField field(String name, Cardinality cardinality, String type) {
        return new ContextField(name, type, name, cardinality, false);
    }

    private DataContext context(
            String name,
            Collection<ContextField> fields,
            Collection<DataReference> contextReferences
    ) {
        DataContext dataContext = new DataContext();
        dataContext.setContextName(name);
        dataContext.setDataObject(
            fields.stream().collect(Collectors.toMap(ContextField::getFieldPath, c -> c.getFieldPath() + "Value"))
        );
        contextReferences.forEach(dataContext::updateReference);
        Map<String, ContextField> fieldMap = fields.stream().collect(Collectors.toMap(ContextField::getName, f -> f));
        dataContext.setContextDefinition(new RuntimeContextDefinition(name, Map.of(), fieldMap, List.of(), false));
        return dataContext;
    }

    private DataReference ref(DataContext dataContext) {
        return ref(dataContext, Cardinality.SINGLE);
    }

    private DataReference ref(DataContext dataContext, Cardinality cardinality) {
        return ref(dataContext.getContextName(), dataContext, cardinality);
    }

    private DataReference ref(String refName, DataContext dataContext, Cardinality cardinality) {
        return new DataReference(refName, List.of(dataContext), cardinality);
    }

    private RuntimeRule rule(Dependency... dependencies) {
        return TestRuleBuilder.getInstance()
                .addDependencies(List.of(dependencies))
                .build();
    }

    private Dependency ccrDependency(String context, String attribute) {
        return new Dependency(context, attribute, true, false);
    }

    private Dependency selfDependency(String context, String attribute) {
        return new Dependency(context, attribute, false, true);
    }

    private Dependency fieldDependency(String context, String attribute) {
        return new Dependency(context, attribute, false, false);
    }
}
