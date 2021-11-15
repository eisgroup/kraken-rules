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
package kraken.model.factory;

import kraken.annotations.API;
import kraken.model.Condition;
import kraken.model.ErrorMessage;
import kraken.model.Expression;
import kraken.model.FunctionSignature;
import kraken.model.Metadata;
import kraken.model.Payload;
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
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;

/**
 * Provides factory methods for creating new instances of kraken models or for deep cloning existing kraken models.
 */
@API
public interface RulesModelFactory {

    /**
     * Returns factory instance
     */
    static RulesModelFactory getInstance() {
        return FactoryProvider.getInstance();
    }

    /**
     * Find implementing class by passed interface in param
     *
     * @param interfaceClazz    Interface class
     * @return implementing class of that interface, if there is no registered implementing class
     * for this interface method will return {@link Object}.class
     */
    Class getImplClass(Class<?> interfaceClazz);

    ContextDefinition createContextDefinition();
    ContextDefinition cloneContextDefinition(ContextDefinition contextDefinition);

    ExternalContext createExternalContext();
    ExternalContext cloneExternalContext(ExternalContext externalContext);

    ExternalContextDefinition createExternalContextDefinition();
    ExternalContextDefinition cloneExternalContextDefinition(ExternalContextDefinition externalContextDefinition);


    ExternalContextDefinitionReference createExternalContextDefinitionReference();
    ExternalContextDefinitionReference cloneExternalContextDefinitionReference(
        ExternalContextDefinitionReference externalContextDefinitionReference
    );

    ExternalContextDefinitionAttribute createExternalContextDefinitionAttribute();
    ExternalContextDefinitionAttribute cloneExternalContextDefinitionAttribute(
        ExternalContextDefinitionAttribute externalContextDefinitionAttribute
    );

    ExternalContextDefinitionAttributeType createExternalContextDefinitionAttributeType();
    ExternalContextDefinitionAttributeType cloneExternalContextDefinitionAttributeType(
        ExternalContextDefinitionAttributeType externalContextDefinitionAttributeType
    );

    ContextNavigation createContextNavigation();
    ContextNavigation cloneContextNavigation(ContextNavigation contextNavigation);

    DefaultValuePayload createDefaultValuePayload();
    DefaultValuePayload cloneDefaultValuePayload(DefaultValuePayload defaultValuePayload);

    EntryPoint createEntryPoint();
    EntryPoint cloneEntryPoint(EntryPoint entryPoint);

    LengthPayload createLengthPayload();
    LengthPayload cloneLengthPayload(LengthPayload lengthPayload);

    AccessibilityPayload createAccessibilityPayload();
    AccessibilityPayload cloneAccessibilityPayload(AccessibilityPayload accessibilityPayload);

    VisibilityPayload createVisibilityPayload();
    VisibilityPayload cloneVisibilityPayload(VisibilityPayload visibilityPayload);

    AssertionPayload createAssertionPayload();
    AssertionPayload cloneAssertionPayload(AssertionPayload assertionPayload);

    SizePayload createSizePayload();
    SizePayload cloneSizePayload(SizePayload sizePayload);

    SizeRangePayload createSizeRangePayload();
    SizeRangePayload cloneSizeRangePayload(SizeRangePayload sizeRangePayload);

    RegExpPayload createRegExpPayload();
    RegExpPayload cloneRegExpPayload(RegExpPayload regExpPayload);

    UsagePayload createUsagePayload();
    UsagePayload cloneUsagePayload(UsagePayload usagePayload);

    Condition createCondition();
    Condition cloneCondition(Condition condition);

    ErrorMessage createErrorMessage();
    ErrorMessage cloneErrorMessage(ErrorMessage errorMessage);

    Expression createExpression();
    Expression cloneExpression(Expression expression);

    Rule createRule();
    Rule cloneRule(Rule rule);

    Payload clonePayload(Payload payload);

    Metadata createMetadata();
    Metadata cloneMetadata(Metadata metadata);

    ContextField createContextField();
    ContextField cloneContextField(ContextField contextField);

    FunctionSignature createFunctionSignature();
    FunctionSignature cloneFunctionSignature(FunctionSignature functionSignature);
}
