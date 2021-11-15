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
package kraken.converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import kraken.el.TargetEnvironment;
import kraken.model.derive.DefaultingType;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProjectFactory;
import kraken.model.project.ResourceKrakenProjectFactoryHolder;
import kraken.model.resource.Resource;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.project.RuntimeKrakenProject;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author mulevicius
 */
public class KrakenProjectConverterTest {

    private DSLReader reader;

    @Before
    public void setUp() throws Exception {
        this.reader = new DSLReader();
    }

    @Test
    public void shouldConvertSimpleKrakenProjectUsingInterpreterByDefault() {
        Collection<Resource> resources = reader.read(
                List.of("rules/KrakenProjectConverter/shouldConvertSimpleKrakenProject")
        );
        KrakenProjectFactory factory = ResourceKrakenProjectFactoryHolder.getInstance()
                .createKrakenProjectFactory(resources);
        KrakenProjectConverter krakenProjectConverter = new KrakenProjectConverter(
                factory.createKrakenProject("AutoPolicy"),
                TargetEnvironment.JAVA
        );
        RuntimeKrakenProject krakenProject = krakenProjectConverter.convert();

        // Assert names
        assertThat(krakenProject.getNamespace(), is("AutoPolicy"));
        assertThat(krakenProject.getRootContextName(), is("AutoPolicy"));

        // Assert ContextDefinitions
        assertThat(krakenProject.getContextDefinitions().size(), is(6));

        RuntimeContextDefinition autoPolicy = krakenProject.getContextDefinitions().get("AutoPolicy");
        assertThat(autoPolicy.getChildren().size(), is(2));
        assertThat(autoPolicy.getFields().size(), is(3));
        assertThat(autoPolicy.getInheritedContexts().size(), is(2));
        assertThat(autoPolicy.getChildren().get("CarCoverage").getNavigationExpression().getExpressionString(),
                is("carCoverage"));
        assertThat(autoPolicy.getChildren().get("PackageDetails").getNavigationExpression().getExpressionString(),
                is("package.packageDetails"));

        RuntimeContextDefinition carCoverage = krakenProject.getContextDefinitions().get("CarCoverage");
        assertThat(carCoverage.getChildren().size(), is(0));
        assertThat(carCoverage.getFields().size(), is(2));
        assertThat(carCoverage.getInheritedContexts().size(), is(2));

        // Assert EntryPoints
        assertThat(krakenProject.getEntryPoints().size(), is(2));

        RuntimeEntryPoint validationEntryPoint = krakenProject.getEntryPoints().stream()
                .filter(ep -> ep.getName().equals("Validation")).findFirst().get();
        assertThat(validationEntryPoint.getRuleNames().size(), is(1));
        assertThat(validationEntryPoint.getIncludedEntryPoints().size(), is(1));

        // Assert Rules
        assertThat(krakenProject.getRules().size(), is(3));

        RuntimeRule defaultRule = krakenProject.getRules().stream()
                .filter(r -> r.getName().equals("DefaultRule")).findFirst().get();
        assertThat(defaultRule.getContext(), is("Policy"));
        assertThat(defaultRule.getTargetPath(), is("policyCd"));
        assertThat(defaultRule.getCondition(), nullValue());
        assertThat(defaultRule.getMetadata().getProperties().values(), empty());
        assertThat(defaultRule.getDependencies(), empty());
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getDefaultingType(),
                is(DefaultingType.defaultValue));
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getValueExpression().getExpressionString(),
                is("'cd'"));
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getValueExpression().getExpressionType(),
                is(ExpressionType.LITERAL));
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getValueExpression().getCompiledLiteralValue(),
                is("cd"));

        List<RuntimeRule> assertRules = krakenProject.getRules().stream()
                .filter(r -> r.getName().equals("AssertRule")).collect(Collectors.toList());

        RuntimeRule baseAssertRule = assertRules.stream()
                .filter(r -> r.getMetadata().getProperties().isEmpty()).findFirst().get();

        assertThat(baseAssertRule.getContext(), is("AutoPolicy"));
        assertThat(baseAssertRule.getTargetPath(), is("policyCd"));
        assertThat(baseAssertRule.getCondition(), nullValue());
        assertThat(baseAssertRule.getDependencies(), hasSize(4));
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getExpressionString(),
                is("(Sum(CarCoverage[*].limitAmount) <= maxLimitAmount)"));
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getExpressionType(),
                is(ExpressionType.COMPLEX));
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getCompiledLiteralValue(),
                nullValue());
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getExpressionVariables(),
                hasSize(1));

        RuntimeRule pizzaAssertRule = assertRules.stream()
                .filter(r -> "Pizza".equals(r.getMetadata().getProperties().get("PackageCd"))).findFirst().get();

        assertThat(pizzaAssertRule.getContext(), is("AutoPolicy"));
        assertThat(pizzaAssertRule.getTargetPath(), is("policyCd"));
        assertThat(pizzaAssertRule.getCondition().getExpression().getExpressionString(),
                is("(PackageDetails.path.to.planCd == 'Pizza')"));
        assertThat(pizzaAssertRule.getDependencies(), hasSize(5));
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getExpressionString(),
                is("every limit in CarCoverage[*].limitAmount satisfies (limit < maxLimitAmount)"));
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getExpressionType(),
                is(ExpressionType.COMPLEX));
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getCompiledLiteralValue(),
                nullValue());
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getExpressionVariables(),
                hasSize(1));
    }

    @Test
    public void shouldConvertSimpleKrakenProjectUsingMvel() {
        System.setProperty("kraken.el.evaluator.java", "mvel");

        Collection<Resource> resources = reader.read(
                List.of("rules/KrakenProjectConverter/shouldConvertSimpleKrakenProject")
        );
        KrakenProjectFactory factory = ResourceKrakenProjectFactoryHolder.getInstance()
                .createKrakenProjectFactory(resources);
        KrakenProjectConverter krakenProjectConverter = new KrakenProjectConverter(
                factory.createKrakenProject("AutoPolicy"),
                TargetEnvironment.JAVA
        );
        RuntimeKrakenProject krakenProject = krakenProjectConverter.convert();

        // Assert names
        assertThat(krakenProject.getNamespace(), is("AutoPolicy"));
        assertThat(krakenProject.getRootContextName(), is("AutoPolicy"));

        // Assert ContextDefinitions
        assertThat(krakenProject.getContextDefinitions().size(), is(6));

        RuntimeContextDefinition autoPolicy = krakenProject.getContextDefinitions().get("AutoPolicy");
        assertThat(autoPolicy.getChildren().size(), is(2));
        assertThat(autoPolicy.getFields().size(), is(3));
        assertThat(autoPolicy.getInheritedContexts().size(), is(2));
        assertThat(autoPolicy.getChildren().get("CarCoverage").getNavigationExpression().getExpressionString(),
                is("carCoverage"));
        assertThat(autoPolicy.getChildren().get("PackageDetails").getNavigationExpression().getExpressionString(),
                is("package.packageDetails"));

        RuntimeContextDefinition carCoverage = krakenProject.getContextDefinitions().get("CarCoverage");
        assertThat(carCoverage.getChildren().size(), is(0));
        assertThat(carCoverage.getFields().size(), is(2));
        assertThat(carCoverage.getInheritedContexts().size(), is(2));

        // Assert EntryPoints
        assertThat(krakenProject.getEntryPoints().size(), is(2));

        RuntimeEntryPoint validationEntryPoint = krakenProject.getEntryPoints().stream()
                .filter(ep -> ep.getName().equals("Validation")).findFirst().get();
        assertThat(validationEntryPoint.getRuleNames().size(), is(1));
        assertThat(validationEntryPoint.getIncludedEntryPoints().size(), is(1));

        // Assert Rules
        assertThat(krakenProject.getRules().size(), is(3));

        RuntimeRule defaultRule = krakenProject.getRules().stream()
                .filter(r -> r.getName().equals("DefaultRule")).findFirst().get();
        assertThat(defaultRule.getContext(), is("Policy"));
        assertThat(defaultRule.getTargetPath(), is("policyCd"));
        assertThat(defaultRule.getCondition(), nullValue());
        assertThat(defaultRule.getMetadata().getProperties().values(), empty());
        assertThat(defaultRule.getDependencies(), empty());
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getDefaultingType(),
                is(DefaultingType.defaultValue));
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getValueExpression().getExpressionString(),
                is("'cd'"));
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getValueExpression().getExpressionType(),
                is(ExpressionType.LITERAL));
        assertThat(((DefaultValuePayload)defaultRule.getPayload()).getValueExpression().getCompiledLiteralValue(),
                is("cd"));

        List<RuntimeRule> assertRules = krakenProject.getRules().stream()
                .filter(r -> r.getName().equals("AssertRule")).collect(Collectors.toList());

        RuntimeRule baseAssertRule = assertRules.stream()
                .filter(r -> r.getMetadata().getProperties().isEmpty()).findFirst().get();

        assertThat(baseAssertRule.getContext(), is("AutoPolicy"));
        assertThat(baseAssertRule.getTargetPath(), is("policyCd"));
        assertThat(baseAssertRule.getCondition(), nullValue());
        assertThat(baseAssertRule.getDependencies(), hasSize(4));
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getExpressionString(),
                is("(_nd(Invoke('Sum',[FlatMap(CarCoverage,'limitAmount')])) <= _nd(this.maxLimitAmount))"));
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getExpressionType(),
                is(ExpressionType.COMPLEX));
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getCompiledLiteralValue(),
                nullValue());
        assertThat(((AssertionPayload)baseAssertRule.getPayload()).getAssertionExpression().getExpressionVariables(),
                hasSize(1));

        RuntimeRule pizzaAssertRule = assertRules.stream()
                .filter(r -> "Pizza".equals(r.getMetadata().getProperties().get("PackageCd"))).findFirst().get();

        assertThat(pizzaAssertRule.getContext(), is("AutoPolicy"));
        assertThat(pizzaAssertRule.getTargetPath(), is("policyCd"));
        assertThat(pizzaAssertRule.getCondition().getExpression().getExpressionString(),
                is("_eq(PackageDetails.path.to.planCd, 'Pizza')"));
        assertThat(pizzaAssertRule.getDependencies(), hasSize(5));
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getExpressionString(),
                is("ForEvery('limit',FlatMap(CarCoverage,'limitAmount'),'(_nd(FromMoney(limit)) < _nd(this.maxLimitAmount))')"));
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getExpressionType(),
                is(ExpressionType.COMPLEX));
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getCompiledLiteralValue(),
                nullValue());
        assertThat(((AssertionPayload)pizzaAssertRule.getPayload()).getAssertionExpression().getExpressionVariables(),
                hasSize(1));
    }

    @Test(expected = KrakenProjectConvertionException.class)
    public void shouldThrowIfFunctionDoesNotExist() {
        Collection<Resource> resources = reader.read(
            List.of("rules/KrakenProjectConverter/shouldThrowIfFunctionDoesNotExist")
        );
        KrakenProjectFactory factory = ResourceKrakenProjectFactoryHolder.getInstance()
            .createKrakenProjectFactory(resources);
        KrakenProjectConverter krakenProjectConverter = new KrakenProjectConverter(
            factory.createKrakenProject("Policy"),
            TargetEnvironment.JAVA
        );
        krakenProjectConverter.convert();
    }
}
