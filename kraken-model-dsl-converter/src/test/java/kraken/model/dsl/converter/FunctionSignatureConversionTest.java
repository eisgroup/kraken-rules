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
package kraken.model.dsl.converter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;

import kraken.model.FunctionSignature;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.builder.ResourceBuilder;

/**
 * @author mulevicius
 */
public class FunctionSignatureConversionTest {
    
    private static final RulesModelFactory factory = RulesModelFactory.getInstance();
    
    private static final String SEPARATOR = System.lineSeparator();

    private final DSLModelConverter converter = new DSLModelConverter();

    @Test
    public void shouldConvertFunctionSignatures() {

        List<FunctionSignature> functions = List.of(
            function("FunctionWithNoArgs", "Any",
                List.of()),
            function("FunctionWithPrimitiveTypes", "Any",
                List.of("Any", "Number", "Money", "String", "Date", "DateTime", "Boolean")),
            function("FunctionWithArrayPrimitiveTypes", "Any[]",
                List.of("Any[]", "Number[]", "Money[]", "String[]", "Date[]", "DateTime[]", "Boolean[]")),
            function("FunctionWithEntityType", "RiskItem",
                List.of("Coverage")),
            function("FunctionWithArrayEntityType", "RiskItem[]",
                List.of("Coverage[]")),
            function("FunctionWithGenerics", "<T>",
                List.of("<T>", "<N>", "<M>")),
            function("FunctionWithArrayGenerics", "<T>[]",
                List.of("<T>[]", "<N>[]", "<M>[]")),
            function("FunctionWithUnionType", "Date | DateTime",
                List.of("MEDCoverage | CollCoverage", "Number | Money | Any"))
        );

        String convertedString = convert("functions", functions);

        assertThat(
            convertedString,
            equalTo(""
                + "Namespace functions" + SEPARATOR
                + SEPARATOR
                + "Function FunctionWithNoArgs() : Any" + SEPARATOR
                + "Function FunctionWithPrimitiveTypes(Any, Number, Money, String, Date, DateTime, Boolean) : Any" + SEPARATOR
                + "Function FunctionWithArrayPrimitiveTypes(Any[], Number[], Money[], String[], Date[], DateTime[], Boolean[]) : Any[]" + SEPARATOR
                + "Function FunctionWithEntityType(Coverage) : RiskItem" + SEPARATOR
                + "Function FunctionWithArrayEntityType(Coverage[]) : RiskItem[]" + SEPARATOR
                + "Function FunctionWithGenerics(<T>, <N>, <M>) : <T>" + SEPARATOR
                + "Function FunctionWithArrayGenerics(<T>[], <N>[], <M>[]) : <T>[]" + SEPARATOR
                + "Function FunctionWithUnionType(MEDCoverage | CollCoverage, Number | Money | Any) : Date | DateTime" + SEPARATOR
                + SEPARATOR
            )
        );
    }

    private String convert(String namespace, List<FunctionSignature> functions) {
        return converter.convert(
            ResourceBuilder.getInstance()
                .setNamespace(namespace)
                .addFunctionSignatures(functions)
                .build()
        );
    }

    public static FunctionSignature function(String name, String returnType, List<String> parameterTypes) {
        FunctionSignature functionSignature = factory.createFunctionSignature();
        functionSignature.setName(name);
        functionSignature.setReturnType(returnType);
        functionSignature.setParameterTypes(parameterTypes);
        functionSignature.setPhysicalNamespace("whatever");
        return functionSignature;
    }
}
