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
package kraken.model.impl;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import kraken.model.Condition;
import kraken.model.Dimension;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.FunctionExample;
import kraken.model.FunctionParameter;
import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.Metadata;
import kraken.model.ParameterDocumentation;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionAttribute;
import kraken.model.context.external.ExternalContextDefinitionAttributeType;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.*;
import org.junit.Test;

/**
 * Unit test for {@link RulesModelFactory}
 *
 * @author psurinin
 * @since 1.0
 */
public class RulesModelFactoryTest {

    /**
     * List of supported model interfaces, any new elements added must be registered here
     */
    List<Class> MODELS_INTERFACE_NAMES = Arrays.asList(
        ContextNavigation.class,
        LengthPayload.class,
        DefaultValuePayload.class,
        DefaultValuePayload.class,
        EntryPoint.class,
        AccessibilityPayload.class,
        VisibilityPayload.class,
        AssertionPayload.class,
        NumberSetPayload.class,
        RegExpPayload.class,
        UsagePayload.class,
        Condition.class,
        ErrorMessage.class,
        Expression.class,
        Rule.class,
        Metadata.class,
        SizePayload.class,
        SizeRangePayload.class,
        ContextField.class,
        ExternalContext.class,
        ExternalContextDefinition.class,
        ExternalContextDefinitionAttribute.class,
        ExternalContextDefinitionAttributeType.class,
        ExternalContextDefinitionReference.class,
        FunctionSignature.class,
        Function.class,
        FunctionParameter.class,
        GenericTypeBound.class,
        FunctionDocumentation.class,
        FunctionExample.class,
        ParameterDocumentation.class,
        ValueListPayload.class,
        Dimension.class
    );

    @Test
    public void shouldCreateExternalContext() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createExternalContext(), instanceOf(instance.getImplClass(ExternalContext.class)));
    }

    @Test
    public void shouldCreateExternalContextDefinition() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createExternalContextDefinition(),
                instanceOf(instance.getImplClass(ExternalContextDefinition.class)));
    }

    @Test
    public void shouldCreateExternalContextDefinitionAttribute() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createExternalContextDefinitionAttribute(),
                instanceOf(instance.getImplClass(ExternalContextDefinitionAttribute.class)));
    }

    @Test
    public void shouldCreateExternalContextDefinitionAttributeType() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createExternalContextDefinitionAttributeType(),
                instanceOf(instance.getImplClass(ExternalContextDefinitionAttributeType.class)));
    }

    @Test
    public void shouldCreateExternalContextDefinitionReference() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createExternalContextDefinitionReference(),
                instanceOf(instance.getImplClass(ExternalContextDefinitionReference.class)));
    }

    @Test
    public void shouldCreateContextDefinition() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createContextDefinition(), instanceOf(instance.getImplClass(ContextDefinition.class)));
    }

    @Test
    public void shouldCreateContextNavigation() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createContextNavigation(), instanceOf(instance.getImplClass(ContextNavigation.class)));
    }

    @Test
    public void shouldContextField() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createContextField(), instanceOf(instance.getImplClass(ContextField.class)));
    }

    @Test
    public void shouldCreateDefaultValuePayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createDefaultValuePayload(), instanceOf(instance.getImplClass(DefaultValuePayload.class)));
    }

    @Test
    public void shouldCreateEntryPoint() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createEntryPoint(), instanceOf(instance.getImplClass(EntryPoint.class)));
    }

    @Test
    public void shouldCreateAccessibilityPayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createAccessibilityPayload(), instanceOf(instance.getImplClass(AccessibilityPayload.class)));
    }

    @Test
    public void shouldCreateSizePayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createSizePayload(), instanceOf(instance.getImplClass(SizePayload.class)));
    }

    @Test
    public void shouldCreateVisibilityPayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createVisibilityPayload(), instanceOf(instance.getImplClass(VisibilityPayload.class)));
    }

    @Test
    public void shouldCreateAssertionPayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createAssertionPayload(), instanceOf(instance.getImplClass(AssertionPayload.class)));
    }

    @Test
    public void shouldCreateRegExpPayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createRegExpPayload(), instanceOf(instance.getImplClass(RegExpPayload.class)));
    }

    @Test
    public void shouldCreateUsagePayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createUsagePayload(), instanceOf(instance.getImplClass(UsagePayload.class)));
    }

    @Test
    public void shouldCreateValueListPayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createValueListPayload(), instanceOf(instance.getImplClass(ValueListPayload.class)));
    }

    @Test
    public void shouldCreateCondition() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createCondition(), instanceOf(instance.getImplClass(Condition.class)));
    }

    @Test
    public void shouldCreateErrorMessage() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createErrorMessage(), instanceOf(instance.getImplClass(ErrorMessage.class)));
    }

    @Test
    public void shouldCreateExpression() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createExpression(), instanceOf(instance.getImplClass(Expression.class)));
    }

    @Test
    public void shouldCreateRule() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createRule(), instanceOf(instance.getImplClass(Rule.class)));
    }

    @Test
    public void shouldCreateMetadata() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createMetadata(), instanceOf(instance.getImplClass(Metadata.class)));
    }

    @Test
    public void shouldCreateLengthPayload() {
        final RulesModelFactory instance = RulesModelFactory.getInstance();
        final LengthPayload lengthPayload = RulesModelFactory.getInstance().createLengthPayload();
        assertThat(lengthPayload, notNullValue());
        assertThat(lengthPayload, instanceOf(instance.getImplClass(LengthPayload.class)));
    }

    @Test
    public void shouldCreateNumberSetPayload() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        NumberSetPayload payload = RulesModelFactory.getInstance().createNumberSetPayload();
        assertThat(payload, notNullValue());
        assertThat(payload, instanceOf(instance.getImplClass(NumberSetPayload.class)));
    }

    @Test
    public void shouldGetImplementationClassNameByInterface() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        Class clazzRule = instance.getImplClass(Rule.class);
        assertThat(clazzRule.getSimpleName(), is(instance.createRule().getClass().getSimpleName()));
    }

    @Test
    public void shouldCheckIsAllImplementedClassNames() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(RulesModelFactory.class.getMethods().length, is(MODELS_INTERFACE_NAMES.size() * 2 + 3));
        MODELS_INTERFACE_NAMES.forEach( interfaceClazz ->
                assertThat(
                        "Didn't found implementing class for " + interfaceClazz.getName(),
                        instance.getImplClass(interfaceClazz),
                        is(not(Object.class))));
    }

    @Test
    public void shouldGetObjectClassByNonRegisteredInterface() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        Class clazzRule = instance.getImplClass(String.class);
        assertThat(clazzRule.getName(), is(Object.class.getName()));
    }

    @Test
    public void shouldReturnSameFactoryInstance(){
        assertThat(RulesModelFactory.getInstance(), is(RulesModelFactory.getInstance()));
    }

    @Test
    public void shouldCreateFunctionSignature() {
        RulesModelFactory instance = RulesModelFactory.getInstance();
        assertThat(instance.createFunctionSignature(), instanceOf(instance.getImplClass(FunctionSignature.class)));
    }
}
