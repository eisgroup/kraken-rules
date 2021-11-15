/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl;

import kraken.model.context.Cardinality;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.SystemDataTypes;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.resource.Resource;
import org.junit.Test;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.junit.Assert.assertThat;

/**
 * Context parsing test for {@link KrakenDSLModelParser} that verifies if {@link ContextDefinition} are parsed correctly
 *
 * @author mulevicius
 */
public class KrakenModelDSLParserContextTest {

    @Test
    public void shouldParseRootContext() {
        Resource model = parseResource("Namespace Foo Include Bar Contexts { Root Context Policy{} }");

        assertThat(model.getContextDefinitions(), hasSize(1));

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getName(), equalTo("Policy"));
        assertThat(contextDefinition.isStrict(), is(true));
        assertThat(contextDefinition.isRoot(), is(true));
        assertThat(contextDefinition.getChildren().values(), empty());
        assertThat(contextDefinition.getContextFields().values(), empty());
        assertThat(contextDefinition.getParentDefinitions(), empty());
    }

    @Test
    public void shouldParseEmptyContext() {
        Resource model = parseResource("Namespace Foo Include Bar Contexts{Context Policy{}}");

        assertThat(model.getContextDefinitions(), hasSize(1));

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getName(), equalTo("Policy"));
        assertThat(contextDefinition.isStrict(), is(true));
        assertThat(contextDefinition.getChildren().values(), empty());
        assertThat(contextDefinition.getContextFields().values(), empty());
        assertThat(contextDefinition.getParentDefinitions(), empty());
    }

    @Test
    public void shouldParseMultipleContexts() {
        Resource model = parseResource("Contexts{Context Policy{} Context RiskItem {}}");

        assertThat(model.getContextDefinitions(), hasSize(2));

        assertThat(model.getContextDefinitions().get(0).getName(), equalTo("Policy"));
        assertThat(model.getContextDefinitions().get(1).getName(), equalTo("RiskItem"));
    }

    @Test
    public void shouldParseContextField() {
        Resource model = parseResource("Contexts{Context Policy { String name Integer *ages : path.to.ages }}");

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getContextFields().values(), hasSize(2));

        ContextField name = contextDefinition.getContextFields().get("name");
        assertThat(name.getName(), equalTo("name"));
        assertThat(name.getCardinality(), is(Cardinality.SINGLE));
        assertThat(name.getFieldPath(), equalTo("name"));
        assertThat(name.getFieldType(), is(PrimitiveFieldDataType.STRING.toString()));

        ContextField ages = contextDefinition.getContextFields().get("ages");
        assertThat(ages.getName(), equalTo("ages"));
        assertThat(ages.getCardinality(), is(Cardinality.MULTIPLE));
        assertThat(ages.getFieldPath(), equalTo("path.to.ages"));
        assertThat(ages.getFieldType(), is(PrimitiveFieldDataType.INTEGER.toString()));
    }

    @Test
    public void shouldParseAllDataTypesFromContextField() {
        Resource model = parseResource("Contexts{Context Policy { " +
                "String string Integer integer Decimal decimal Date date DateTime dateTime Boolean boolean Money money }}");

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getContextFields().values(), hasSize(7));
        assertThat(contextDefinition.getContextFields().get("string").getFieldType(), is(PrimitiveFieldDataType.STRING.toString()));
        assertThat(contextDefinition.getContextFields().get("integer").getFieldType(), is(PrimitiveFieldDataType.INTEGER.toString()));
        assertThat(contextDefinition.getContextFields().get("decimal").getFieldType(), is(PrimitiveFieldDataType.DECIMAL.toString()));
        assertThat(contextDefinition.getContextFields().get("date").getFieldType(), is(PrimitiveFieldDataType.DATE.toString()));
        assertThat(contextDefinition.getContextFields().get("dateTime").getFieldType(), is(PrimitiveFieldDataType.DATETIME.toString()));
        assertThat(contextDefinition.getContextFields().get("boolean").getFieldType(), is(PrimitiveFieldDataType.BOOLEAN.toString()));
        assertThat(contextDefinition.getContextFields().get("money").getFieldType(), is(PrimitiveFieldDataType.MONEY.toString()));
    }

    @Test
    public void shouldParseComplexContextFieldType() {
        Resource model = parseResource("Context RiskItem { " +
                "Coverage *coverage }");

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getContextFields().values(), hasSize(1));

        ContextField coverageField = contextDefinition.getContextFields().get("coverage");
        assertThat(coverageField.getCardinality(), is(Cardinality.MULTIPLE));
        assertThat(coverageField.getFieldPath(), is("coverage"));
        assertThat(coverageField.getName(), is("coverage"));
        assertThat(coverageField.getFieldType(), is("Coverage"));
    }

    @Test
    public void shouldParseContextChild() {
        Resource model = parseResource("Contexts{Context Policy { " +
                "Child *Coverage : all.my.coverages Child RiskItem}}");

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getChildren().values(), hasSize(2));

        ContextNavigation coverage = contextDefinition.getChildren().get("Coverage");
        assertThat(coverage.getTargetName(), equalTo("Coverage"));
        assertThat(coverage.getCardinality(), is(Cardinality.MULTIPLE));
        assertThat(coverage.getNavigationExpression(), equalTo("all.my.coverages"));

        ContextNavigation riskItem = contextDefinition.getChildren().get("RiskItem");
        assertThat(riskItem.getTargetName(), equalTo("RiskItem"));
        assertThat(riskItem.getCardinality(), is(Cardinality.SINGLE));
        assertThat(riskItem.getNavigationExpression(), equalTo("riskItem"));
    }

    @Test
    public void shouldParseNotStrictContext() {
        Resource model = parseResource("Contexts{@NotStrict Context Policy {}}");

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.isStrict(), is(false));
    }

    @Test
    public void shouldParseContextParentDefinitions() {
        Resource model = parseResource("Contexts{Context Policy Is RootEntity, AbstractPolicy{}}");

        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getParentDefinitions(), hasItems("RootEntity", "AbstractPolicy"));
    }

    @Test
    public void shouldParseNonPropertyAttributeToUnknownDataType() {
        Resource model = parseResource("Contexts{" +
                "Context Policy {" +
                "Unknown fieldName" +
                "}}");
        final String fieldType = model.getContextDefinitions()
                .get(0)
                .getContextFields()
                .values()
                .iterator().next()
                .getFieldType();
        assertThat(fieldType, equalToIgnoringCase(SystemDataTypes.UNKNOWN.toString()));
    }

    @Test
    public void shouldParseExternalContextField() {
        Resource model = parseResource("Contexts{" +
                "Context Policy {" +
                "External String externalPrimitive " +
                "External Coverage externalComplex " +
                "Coverage notExternalComplex " +
                "Integer notExternalPrimitive" +
                "}}");
        ContextDefinition contextDefinition = model.getContextDefinitions().get(0);
        assertThat(contextDefinition.getContextFields().get("externalPrimitive").isExternal(), is(true));
        assertThat(contextDefinition.getContextFields().get("externalComplex").isExternal(), is(true));
        assertThat(contextDefinition.getContextFields().get("notExternalComplex").isExternal(), is(false));
        assertThat(contextDefinition.getContextFields().get("notExternalPrimitive").isExternal(), is(false));
    }
}
