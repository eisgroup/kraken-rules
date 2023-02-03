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
package kraken.model.dsl;

import java.util.List;
import java.util.stream.Collectors;

import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.FunctionExample;
import kraken.model.FunctionParameter;
import kraken.model.GenericTypeBound;
import kraken.model.ParameterDocumentation;
import kraken.model.dsl.model.DSLExpression;
import kraken.model.dsl.model.DSLFunction;
import kraken.model.dsl.model.DSLFunctionDocumentation;
import kraken.model.dsl.model.DSLFunctionExample;
import kraken.model.dsl.model.DSLFunctionParameter;
import kraken.model.dsl.model.DSLGenericTypeBound;
import kraken.model.dsl.model.DSLModel;
import kraken.model.dsl.model.DSLParameterDocumentation;
import kraken.model.factory.RulesModelFactory;

/**
 * Converts every {@link DSLFunction} specified in DSL to Kraken Model {@link Function} instance.
 *
 * @author mulevicius
 */
public class KrakenDSLModelFunctionConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelFunctionConverter() {
    }

    static List<Function> convertFunctions(DSLModel dsl) {
        return dsl.getFunctions().stream()
            .map(f -> convertFunctions(dsl.getNamespace(), f))
            .collect(Collectors.toList());
    }

    private static Function convertFunctions(String namespace, DSLFunction dslFunction) {
        Function function = factory.createFunction();
        function.setName(dslFunction.getFunctionName());
        function.setPhysicalNamespace(namespace);
        function.setReturnType(dslFunction.getReturnType());
        function.setParameters(dslFunction.getParameters().stream()
            .map(KrakenDSLModelFunctionConverter::convertParameter)
            .collect(Collectors.toList()));
        function.setGenericTypeBounds(dslFunction.getGenericTypeBounds().stream()
            .map(KrakenDSLModelFunctionConverter::convertBound)
            .collect(Collectors.toList()));
        function.setBody(convertExpression(dslFunction.getBody()));
        function.setDocumentation(convertDocumentation(dslFunction.getDocumentation()));
        return function;
    }

    private static FunctionParameter convertParameter(DSLFunctionParameter dslFunctionParameter) {
        FunctionParameter functionParameter = factory.createFunctionParameter();
        functionParameter.setName(dslFunctionParameter.getName());
        functionParameter.setType(dslFunctionParameter.getType());
        return functionParameter;
    }

    private static Expression convertExpression(DSLExpression dslExpression) {
        Expression expression = factory.createExpression();
        expression.setExpressionString(dslExpression.getExpression());
        return expression;
    }

    private static GenericTypeBound convertBound(DSLGenericTypeBound dslGenericTypeBound) {
        var bound = factory.createGenericTypeBound();
        bound.setGeneric(dslGenericTypeBound.getGeneric());
        bound.setBound(dslGenericTypeBound.getBound());
        return bound;
    }

    private static FunctionDocumentation convertDocumentation(DSLFunctionDocumentation dslDocumentation) {
        if(dslDocumentation == null) {
            return null;
        }
        FunctionDocumentation documentation = factory.createFunctionDocumentation();
        documentation.setDescription(dslDocumentation.getDescription());
        documentation.setSince(dslDocumentation.getSince());
        documentation.setExamples(dslDocumentation.getExamples().stream()
            .map(KrakenDSLModelFunctionConverter::convertFunctionExample)
            .collect(Collectors.toList()));
        documentation.setParameterDocumentations(dslDocumentation.getParameterDocumentations().stream()
            .map(KrakenDSLModelFunctionConverter::convertParameterDocumentation)
            .collect(Collectors.toList()));
        return documentation;
    }

    private static FunctionExample convertFunctionExample(DSLFunctionExample dslExample) {
        FunctionExample example = factory.createFunctionExample();
        example.setExample(dslExample.getExample());
        example.setResult(dslExample.getResult());
        example.setValid(dslExample.isValid());
        return example;
    }

    private static ParameterDocumentation convertParameterDocumentation(DSLParameterDocumentation dslDocumentation) {
        ParameterDocumentation documentation = factory.createParameterDocumentation();
        documentation.setParameterName(dslDocumentation.getParameterName());
        documentation.setDescription(dslDocumentation.getDescription());
        return documentation;
    }
}
