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

import kraken.model.FunctionSignature;
import kraken.model.dsl.model.DSLFunction;
import kraken.model.dsl.model.DSLFunctionParameter;
import kraken.model.dsl.model.DSLModel;
import kraken.model.factory.RulesModelFactory;

/**
 * Converts every {@link DSLFunction} specified in DSL to Kraken Model {@link FunctionSignature} instance.
 *
 * @author mulevicius
 */
public class KrakenDSLModelFunctionConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelFunctionConverter() {
    }

    static List<FunctionSignature> convertFunctions(DSLModel dsl) {
        return dsl.getFunctions().stream()
            .map(f -> convertFunction(dsl.getNamespace(), f))
            .collect(Collectors.toList());
    }

    private static FunctionSignature convertFunction(String namespace, DSLFunction dslFunction) {
        FunctionSignature functionSignature = factory.createFunctionSignature();
        functionSignature.setName(dslFunction.getFunctionName());
        functionSignature.setPhysicalNamespace(namespace);
        functionSignature.setReturnType(dslFunction.getReturnType());
        functionSignature.setParameterTypes(dslFunction.getParameters().stream()
            .map(KrakenDSLModelFunctionConverter::convertParameter)
            .collect(Collectors.toList()));
        return functionSignature;
    }

    private static String convertParameter(DSLFunctionParameter dslFunctionParameter) {
        return dslFunctionParameter.getParameterType();
    }
}
