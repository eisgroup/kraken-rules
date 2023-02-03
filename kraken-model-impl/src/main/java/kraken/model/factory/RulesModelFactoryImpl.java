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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import kraken.model.*;
import kraken.model.context.*;
import kraken.model.context.external.*;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultValuePayloadImpl;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.entrypoint.EntryPointImpl;
import kraken.model.impl.*;
import kraken.model.state.*;
import kraken.model.validation.*;
import kraken.namespace.Namespaced;

/**
 * Default implementation of {@link RulesModelFactory}
 *
 * @author psurinin
 * @since 1.0
 */
public class RulesModelFactoryImpl implements RulesModelFactory {

    private final List<ClassHolder> classNameHolders = new ArrayList<>();

    public RulesModelFactoryImpl() {
        classNameHolders.add(new ClassHolder(ContextDefinition.class, ContextDefinitionImpl.class));
        classNameHolders.add(new ClassHolder(LengthPayload.class, LengthPayloadImpl.class));
        classNameHolders.add(new ClassHolder(ContextNavigation.class, ContextNavigationImpl.class));
        classNameHolders.add(new ClassHolder(DefaultValuePayload.class, DefaultValuePayloadImpl.class));
        classNameHolders.add(new ClassHolder(EntryPoint.class, EntryPointImpl.class));
        classNameHolders.add(new ClassHolder(AccessibilityPayload.class, AccessibilityPayloadImpl.class));
        classNameHolders.add(new ClassHolder(VisibilityPayload.class, VisibilityPayloadImpl.class));
        classNameHolders.add(new ClassHolder(AssertionPayload.class, AssertionPayloadImpl.class));
        classNameHolders.add(new ClassHolder(RegExpPayload.class, RegExpPayloadImpl.class));
        classNameHolders.add(new ClassHolder(Condition.class, ConditionImpl.class));
        classNameHolders.add(new ClassHolder(ErrorMessage.class, ErrorMessageImpl.class));
        classNameHolders.add(new ClassHolder(Expression.class, ExpressionImpl.class));
        classNameHolders.add(new ClassHolder(Metadata.class, MetadataImpl.class));
        classNameHolders.add(new ClassHolder(Rule.class, RuleImpl.class));
        classNameHolders.add(new ClassHolder(UsagePayload.class, UsagePayloadImpl.class));
        classNameHolders.add(new ClassHolder(ContextField.class, ContextFieldImpl.class));
        classNameHolders.add(new ClassHolder(SizePayload.class, SizePayloadImpl.class));
        classNameHolders.add(new ClassHolder(SizeRangePayload.class, SizeRangePayloadImpl.class));
        classNameHolders.add(new ClassHolder(ExternalContext.class, ExternalContextImpl.class));
        classNameHolders.add(new ClassHolder(ExternalContextDefinition.class, ExternalContextDefinitionImpl.class));
        classNameHolders.add(new ClassHolder(ExternalContextDefinitionAttribute.class, ExternalContextDefinitionAttributeImpl.class));
        classNameHolders.add(new ClassHolder(ExternalContextDefinitionAttributeType.class, ExternalContextDefinitionAttributeTypeImpl.class));
        classNameHolders.add(new ClassHolder(ExternalContextDefinitionReference.class, ExternalContextDefinitionReferenceImpl.class));
        classNameHolders.add(new ClassHolder(FunctionSignature.class, FunctionSignatureImpl.class));
        classNameHolders.add(new ClassHolder(Function.class, FunctionImpl.class));
        classNameHolders.add(new ClassHolder(FunctionParameter.class, FunctionParameterImpl.class));
        classNameHolders.add(new ClassHolder(GenericTypeBound.class, GenericTypeBoundImpl.class));
        classNameHolders.add(new ClassHolder(FunctionDocumentation.class, FunctionDocumentationImpl.class));
        classNameHolders.add(new ClassHolder(ParameterDocumentation.class, ParameterDocumentationImpl.class));
        classNameHolders.add(new ClassHolder(FunctionExample.class, FunctionExampleImpl.class));
    }

    @Override
    public ExternalContext createExternalContext() {
        return new ExternalContextImpl();
    }

    @Override
    public ExternalContext cloneExternalContext(ExternalContext externalContext) {
        ExternalContext cloned = createExternalContext();
        cloned.setName(externalContext.getName());
        cloned.setPhysicalNamespace(externalContext.getPhysicalNamespace());
        cloned.setContexts(
            externalContext.getContexts().values()
                .stream()
                .map(this::cloneExternalContext)
                .collect(Collectors.toMap(Namespaced::getName, ex -> ex)));
        cloned.setExternalContextDefinitions(
            externalContext.getExternalContextDefinitions().values()
                .stream()
                .map(this::cloneExternalContextDefinitionReference)
                .collect(Collectors.toMap(ExternalContextDefinitionReference::getName, ex -> ex)));

        return cloned;
    }

    @Override
    public ExternalContextDefinition createExternalContextDefinition() {
        return new ExternalContextDefinitionImpl();
    }

    @Override
    public ExternalContextDefinition cloneExternalContextDefinition(
        ExternalContextDefinition externalContextDefinition) {
        var cloned = createExternalContextDefinition();
        cloned.setName(externalContextDefinition.getName());
        cloned.setPhysicalNamespace(externalContextDefinition.getPhysicalNamespace());
        cloned.setAttributes(
            externalContextDefinition.getAttributes().values()
                .stream()
                .map(this::cloneExternalContextDefinitionAttribute)
                .collect(Collectors.toMap(ExternalContextDefinitionAttribute::getName, o -> o)));

        return cloned;
    }

    @Override
    public ExternalContextDefinitionAttribute createExternalContextDefinitionAttribute() {
        return new ExternalContextDefinitionAttributeImpl();
    }

    @Override
    public ExternalContextDefinitionAttribute cloneExternalContextDefinitionAttribute(
        ExternalContextDefinitionAttribute externalContextDefinitionAttribute) {
        var cloned = createExternalContextDefinitionAttribute();
        cloned.setName(externalContextDefinitionAttribute.getName());
        cloned.setType(cloneExternalContextDefinitionAttributeType(externalContextDefinitionAttribute.getType()));
        return cloned;
    }

    @Override
    public ExternalContextDefinitionAttributeType createExternalContextDefinitionAttributeType() {
        return new ExternalContextDefinitionAttributeTypeImpl();
    }

    @Override
    public ExternalContextDefinitionAttributeType cloneExternalContextDefinitionAttributeType(
        ExternalContextDefinitionAttributeType externalContextDefinitionAttributeType) {
        var cloned = createExternalContextDefinitionAttributeType();
        cloned.setType(externalContextDefinitionAttributeType.getType());
        cloned.setCardinality(externalContextDefinitionAttributeType.getCardinality());
        cloned.setPrimitive(externalContextDefinitionAttributeType.getPrimitive());
        return cloned;
    }

    @Override
    public ExternalContextDefinitionReference createExternalContextDefinitionReference() {
        return new ExternalContextDefinitionReferenceImpl();
    }

    @Override
    public ExternalContextDefinitionReference cloneExternalContextDefinitionReference(
        ExternalContextDefinitionReference externalContextDefinitionReference) {
        var cloned = createExternalContextDefinitionReference();
        cloned.setName(externalContextDefinitionReference.getName());
        return cloned;
    }

    @Override
    public ContextDefinition createContextDefinition() {
        return new ContextDefinitionImpl();
    }

    @Override
    public ContextDefinition cloneContextDefinition(ContextDefinition contextDefinition) {
         var cloned = createContextDefinition();
         cloned.setName(contextDefinition.getName());
         cloned.setPhysicalNamespace(contextDefinition.getPhysicalNamespace());
         cloned.setRoot(contextDefinition.isRoot());
         cloned.setStrict(contextDefinition.isStrict());
         cloned.setSystem(contextDefinition.isSystem());
         cloned.setParentDefinitions(new ArrayList<>(contextDefinition.getParentDefinitions()));
         cloned.setContextFields(contextDefinition.getContextFields().values().stream()
             .map(this::cloneContextField)
             .collect(Collectors.toMap(ContextField::getName, c -> c)));
         cloned.setChildren(contextDefinition.getChildren().values().stream()
             .map(this::cloneContextNavigation)
             .collect(Collectors.toMap(ContextNavigation::getTargetName, o -> o)));

         return cloned;
    }

    @Override
    public ContextNavigation createContextNavigation() {
        return new ContextNavigationImpl();
    }

    @Override
    public ContextNavigation cloneContextNavigation(ContextNavigation contextNavigation) {
        var cloned = createContextNavigation();
        cloned.setCardinality(contextNavigation.getCardinality());
        cloned.setNavigationExpression(contextNavigation.getNavigationExpression());
        cloned.setTargetName(contextNavigation.getTargetName());
        return cloned;
    }

    @Override
    public ContextField createContextField() {
        return new ContextFieldImpl();
    }

    @Override
    public ContextField cloneContextField(ContextField contextField) {
        var cloned = createContextField();
        cloned.setName(contextField.getName());
        cloned.setCardinality(contextField.getCardinality());
        cloned.setFieldPath(contextField.getFieldPath());
        cloned.setFieldType(contextField.getFieldType());
        cloned.setExternal(contextField.isExternal());
        return cloned;
    }

    @Override
    public EntryPoint createEntryPoint() {
        return new EntryPointImpl();
    }

    @Override
    public EntryPoint cloneEntryPoint(EntryPoint entryPoint) {
        var cloned = createEntryPoint();
        cloned.setName(entryPoint.getName());
        cloned.setPhysicalNamespace(entryPoint.getPhysicalNamespace());
        cloned.setEntryPointVariationId(entryPoint.getEntryPointVariationId());
        cloned.setServerSideOnly(entryPoint.isServerSideOnly());
        cloned.setIncludedEntryPointNames(new ArrayList<>(entryPoint.getIncludedEntryPointNames()));
        cloned.setRuleNames(new ArrayList<>(entryPoint.getRuleNames()));
        cloned.setMetadata(entryPoint.getMetadata() != null ? cloneMetadata(entryPoint.getMetadata()) : null);
        return cloned;
    }

    @Override
    public DefaultValuePayload createDefaultValuePayload() {
        return new DefaultValuePayloadImpl();
    }

    @Override
    public DefaultValuePayload cloneDefaultValuePayload(DefaultValuePayload defaultValuePayload) {
        var cloned = createDefaultValuePayload();
        cloned.setDefaultingType(defaultValuePayload.getDefaultingType());
        cloned.setValueExpression(cloneExpression(defaultValuePayload.getValueExpression()));
        return cloned;
    }

    @Override
    public LengthPayload createLengthPayload() {
        return new LengthPayloadImpl();
    }

    @Override
    public LengthPayload cloneLengthPayload(LengthPayload lengthPayload) {
        var cloned = createLengthPayload();
        cloned.setLength(lengthPayload.getLength());
        copyValidationPayload(lengthPayload, cloned);
        return cloned;
    }

    @Override
    public AccessibilityPayload createAccessibilityPayload() {
        return new AccessibilityPayloadImpl();
    }

    @Override
    public AccessibilityPayload cloneAccessibilityPayload(AccessibilityPayload accessibilityPayload) {
        var cloned = createAccessibilityPayload();
        cloned.setAccessible(accessibilityPayload.isAccessible());
        return cloned;
    }

    @Override
    public VisibilityPayload createVisibilityPayload() {
        return new VisibilityPayloadImpl();
    }

    @Override
    public VisibilityPayload cloneVisibilityPayload(VisibilityPayload visibilityPayload) {
        var cloned = createVisibilityPayload();
        cloned.setVisible(visibilityPayload.isVisible());
        return cloned;
    }

    @Override
    public AssertionPayload createAssertionPayload() {
        return new AssertionPayloadImpl();
    }

    @Override
    public AssertionPayload cloneAssertionPayload(AssertionPayload assertionPayload) {
        var cloned = createAssertionPayload();
        cloned.setAssertionExpression(cloneExpression(assertionPayload.getAssertionExpression()));
        copyValidationPayload(assertionPayload, cloned);
        return cloned;
    }

    @Override
    public SizePayload createSizePayload() {
        return new SizePayloadImpl();
    }

    @Override
    public SizePayload cloneSizePayload(SizePayload sizePayload) {
        var cloned = createSizePayload();
        cloned.setSize(sizePayload.getSize());
        cloned.setOrientation(sizePayload.getOrientation());
        copyValidationPayload(sizePayload, cloned);
        return cloned;
    }

    @Override
    public SizeRangePayload createSizeRangePayload() {
        return new SizeRangePayloadImpl();
    }

    @Override
    public SizeRangePayload cloneSizeRangePayload(SizeRangePayload sizeRangePayload) {
        var cloned = createSizeRangePayload();
        cloned.setMax(sizeRangePayload.getMax());
        cloned.setMin(sizeRangePayload.getMin());
        copyValidationPayload(sizeRangePayload, cloned);
        return cloned;
    }

    @Override
    public RegExpPayload createRegExpPayload() {
        return new RegExpPayloadImpl();
    }

    @Override
    public RegExpPayload cloneRegExpPayload(RegExpPayload regExpPayload) {
        var cloned = createRegExpPayload();
        cloned.setRegExp(regExpPayload.getRegExp());
        copyValidationPayload(regExpPayload, cloned);
        return cloned;
    }

    @Override
    public UsagePayload createUsagePayload() {
        return new UsagePayloadImpl();
    }

    @Override
    public UsagePayload cloneUsagePayload(UsagePayload usagePayload) {
        var cloned = createUsagePayload();
        cloned.setUsageType(usagePayload.getUsageType());
        copyValidationPayload(usagePayload, cloned);
        return cloned;
    }

    @Override
    public Condition createCondition() {
        return new ConditionImpl();
    }

    @Override
    public Condition cloneCondition(Condition condition) {
        var cloned = createCondition();
        cloned.setExpression(cloneExpression(condition.getExpression()));
        return cloned;
    }

    @Override
    public ErrorMessage createErrorMessage() {
        return new ErrorMessageImpl();
    }

    @Override
    public ErrorMessage cloneErrorMessage(ErrorMessage errorMessage) {
        var cloned = createErrorMessage();
        cloned.setErrorMessage(errorMessage.getErrorMessage());
        cloned.setErrorCode(errorMessage.getErrorCode());
        return cloned;
    }

    @Override
    public Expression createExpression() {
        return new ExpressionImpl();
    }

    @Override
    public Expression cloneExpression(Expression expression) {
        var cloned = createExpression();
        cloned.setExpressionString(expression.getExpressionString());
        return cloned;
    }

    @Override
    public Rule createRule() {
        return new RuleImpl();
    }

    @Override
    public Rule cloneRule(Rule rule) {
        var cloned = createRule();
        cloned.setName(rule.getName());
        cloned.setPhysicalNamespace(rule.getPhysicalNamespace());
        cloned.setRuleVariationId(rule.getRuleVariationId());
        cloned.setContext(rule.getContext());
        cloned.setTargetPath(rule.getTargetPath());
        cloned.setDescription(rule.getDescription());
        cloned.setDimensional(rule.isDimensional());
        cloned.setCondition(rule.getCondition() != null ? cloneCondition(rule.getCondition()) : null);
        cloned.setPayload(clonePayload(rule.getPayload()));
        cloned.setMetadata(rule.getMetadata() != null ? cloneMetadata(rule.getMetadata()) : null);
        cloned.setPriority(rule.getPriority());
        return cloned;
    }

    @Override
    public Payload clonePayload(Payload payload) {
        if(payload instanceof DefaultValuePayload) {
            return cloneDefaultValuePayload((DefaultValuePayload) payload);
        }
        if(payload instanceof AccessibilityPayload) {
            return cloneAccessibilityPayload((AccessibilityPayload) payload);
        }
        if(payload instanceof VisibilityPayload) {
            return cloneVisibilityPayload((VisibilityPayload) payload);
        }
        if(payload instanceof SizePayload) {
            return cloneSizePayload((SizePayload) payload);
        }
        if(payload instanceof SizeRangePayload) {
            return cloneSizeRangePayload((SizeRangePayload) payload);
        }
        if(payload instanceof RegExpPayload) {
            return cloneRegExpPayload((RegExpPayload) payload);
        }
        if(payload instanceof UsagePayload) {
            return cloneUsagePayload((UsagePayload) payload);
        }
        if(payload instanceof LengthPayload) {
            return cloneLengthPayload((LengthPayload) payload);
        }
        if(payload instanceof AssertionPayload) {
            return cloneAssertionPayload((AssertionPayload) payload);
        }
        throw new IllegalArgumentException(
            "Error while cloning payload object because payload object is instance of unknown class: "
                + payload.getClass().getName()
        );
    }

    private void copyValidationPayload(ValidationPayload from, ValidationPayload to) {
        to.setErrorMessage(from.getErrorMessage() != null ? cloneErrorMessage(from.getErrorMessage()) : null);
        to.setSeverity(from.getSeverity());
        to.setOverridable(from.isOverridable());
        to.setOverrideGroup(from.getOverrideGroup());
    }

    @Override
    public Metadata createMetadata() {
        return new MetadataImpl();
    }

    @Override
    public Metadata cloneMetadata(Metadata metadata) {
        var cloned = createMetadata();
        metadata.asMap().forEach(cloned::setProperty);
        return cloned;
    }

    @Override
    public FunctionSignature createFunctionSignature() {
        return new FunctionSignatureImpl();
    }

    @Override
    public FunctionSignature cloneFunctionSignature(FunctionSignature functionSignature) {
        var cloned = createFunctionSignature();
        cloned.setName(functionSignature.getName());
        cloned.setPhysicalNamespace(functionSignature.getPhysicalNamespace());
        cloned.setReturnType(functionSignature.getReturnType());
        cloned.setParameterTypes(new ArrayList<>(functionSignature.getParameterTypes()));
        cloned.setGenericTypeBounds(functionSignature.getGenericTypeBounds().stream().map(this::cloneGenericTypeBound)
            .collect(Collectors.toList()));
        return cloned;
    }

    @Override
    public FunctionDocumentation createFunctionDocumentation() {
        return new FunctionDocumentationImpl();
    }

    @Override
    public FunctionDocumentation cloneFunctionDocumentation(FunctionDocumentation documentation) {
        var cloned = new FunctionDocumentationImpl();
        cloned.setDescription(documentation.getDescription());
        cloned.setSince(documentation.getSince());
        cloned.setExamples(documentation.getExamples().stream()
            .map(this::cloneFunctionExample)
            .collect(Collectors.toList()));
        cloned.setParameterDocumentations(documentation.getParameterDocumentations().stream()
            .map(this::cloneParameterDocumentation)
            .collect(Collectors.toList()));
        return cloned;
    }

    @Override
    public ParameterDocumentation createParameterDocumentation() {
        return new ParameterDocumentationImpl();
    }

    @Override
    public ParameterDocumentation cloneParameterDocumentation(ParameterDocumentation parameterDocumentation) {
        var cloned = new ParameterDocumentationImpl();
        cloned.setParameterName(parameterDocumentation.getParameterName());
        cloned.setDescription(parameterDocumentation.getDescription());
        return cloned;
    }

    @Override
    public kraken.model.Function createFunction() {
        return new FunctionImpl();
    }

    @Override
    public kraken.model.Function cloneFunction(kraken.model.Function function) {
        var cloned = createFunction();
        cloned.setName(function.getName());
        cloned.setPhysicalNamespace(function.getPhysicalNamespace());
        cloned.setReturnType(function.getReturnType());
        cloned.setParameters(function.getParameters().stream().map(this::cloneFunctionParameter).collect(Collectors.toList()));
        cloned.setGenericTypeBounds(function.getGenericTypeBounds().stream().map(this::cloneGenericTypeBound)
            .collect(Collectors.toList()));
        cloned.setBody(cloneExpression(function.getBody()));
        cloned.setDocumentation(
            function.getDocumentation() != null
                ? cloneFunctionDocumentation(function.getDocumentation())
                : null
        );
        return cloned;
    }

    @Override
    public FunctionParameter createFunctionParameter() {
        return new FunctionParameterImpl();
    }

    @Override
    public FunctionParameter cloneFunctionParameter(FunctionParameter functionParameter) {
        var cloned = createFunctionParameter();
        cloned.setName(functionParameter.getName());
        cloned.setType(functionParameter.getType());
        return cloned;
    }

    @Override
    public GenericTypeBound createGenericTypeBound() {
        return new GenericTypeBoundImpl();
    }

    @Override
    public GenericTypeBound cloneGenericTypeBound(GenericTypeBound genericTypeBound) {
        var cloned = new GenericTypeBoundImpl();
        cloned.setGeneric(genericTypeBound.getGeneric());
        cloned.setBound(genericTypeBound.getBound());
        return cloned;
    }

    @Override
    public FunctionExample createFunctionExample() {
        return new FunctionExampleImpl();
    }

    @Override
    public FunctionExample cloneFunctionExample(FunctionExample functionExample) {
        var cloned = new FunctionExampleImpl();
        cloned.setExample(functionExample.getExample());
        cloned.setResult(functionExample.getResult());
        cloned.setValid(functionExample.isValid());
        return cloned;
    }

    @Override
    public Class getImplClass(Class<?> clazz) {
        return classNameHolders.stream()
                .filter(h -> h.getInterfaceName().equals(clazz))
                .findFirst()
                .orElse(new ClassHolder(Object.class, Object.class))
                .getImplementationName();
    }
}

