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
import kraken.model.GenericTypeBound;
import kraken.model.dsl.model.DSLFunctionSignature;
import kraken.model.dsl.model.DSLFunctionSignatureParameter;
import kraken.model.dsl.model.DSLGenericTypeBound;
import kraken.model.dsl.model.DSLModel;
import kraken.model.factory.RulesModelFactory;

/**
 * Converts every {@link DSLFunctionSignature} specified in DSL to Kraken Model {@link FunctionSignature} instance.
 *
 * @author mulevicius
 */
public class KrakenDSLModelFunctionSignatureConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelFunctionSignatureConverter() {
    }

    static List<FunctionSignature> convertFunctionSignatures(DSLModel dsl) {
        return dsl.getFunctionSignatures().stream()
            .map(f -> convertFunctionSignature(dsl.getNamespace(), f))
            .collect(Collectors.toList());
    }

    private static FunctionSignature convertFunctionSignature(String namespace, DSLFunctionSignature dslFunctionSignature) {
        FunctionSignature functionSignature = factory.createFunctionSignature();
        functionSignature.setName(dslFunctionSignature.getFunctionName());
        functionSignature.setPhysicalNamespace(namespace);
        functionSignature.setReturnType(dslFunctionSignature.getReturnType());
        functionSignature.setParameterTypes(dslFunctionSignature.getParameters().stream()
            .map(KrakenDSLModelFunctionSignatureConverter::convertParameter)
            .collect(Collectors.toList()));
        functionSignature.setGenericTypeBounds(dslFunctionSignature.getGenericTypeBounds().stream()
            .map(KrakenDSLModelFunctionSignatureConverter::convertBound)
            .collect(Collectors.toList()));

        return functionSignature;
    }

    private static String convertParameter(DSLFunctionSignatureParameter dslFunctionSignatureParameter) {
        return dslFunctionSignatureParameter.getParameterType();
    }

    private static GenericTypeBound convertBound(DSLGenericTypeBound dslGenericTypeBound) {
        var bound = factory.createGenericTypeBound();
        bound.setGeneric(dslGenericTypeBound.getGeneric());
        bound.setBound(dslGenericTypeBound.getBound());
        return bound;
    }
}
