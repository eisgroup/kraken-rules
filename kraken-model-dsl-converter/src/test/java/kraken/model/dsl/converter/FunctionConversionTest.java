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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;

import org.junit.Test;

import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.FunctionExample;
import kraken.model.FunctionParameter;
import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.ParameterDocumentation;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.builder.ResourceBuilder;

/**
 * @author mulevicius
 */
public class FunctionConversionTest {
    
    private static final RulesModelFactory factory = RulesModelFactory.getInstance();
    
    private static final String SEPARATOR = System.lineSeparator();

    private final DSLModelConverter converter = new DSLModelConverter();

    @Test
    public void shouldConvertFunctions() {

        List<Function> functions = List.of(
            function("FunctionWithNoArgs",
                "String",
                List.of(),
                List.of(),
                "'USD'",
                null
            ),
            function("FunctionWithArgs",
                "<T>",
                List.of(
                    parameter("c", "<T>[]"),
                    parameter("n", "Number")
                ),
                List.of(
                    genericTypeBound("T", "Coverage")
                ),
                "set first to c[n] return n",
                functionDocumentation(
                    "Returns coverage from array",
                    "1.0.0",
                    List.of(
                        example("FunctionWithArgs(coverages, 1)", "Coverage", true),
                        example("FunctionWithArgs(coverages, -1)", null, false)
                    ),
                    List.of(
                        parameterDocumentation("c", "Array of coverages"),
                        parameterDocumentation("n", "Index of coverage")
                    )
                )
            )
        );

        String convertedString = convert("functions", functions);

        assertThat(
            convertedString,
            equalTo(""
                + "Namespace functions" + SEPARATOR
                + SEPARATOR
                + "Function FunctionWithNoArgs() : String {" + SEPARATOR
                + "  'USD'" + SEPARATOR
                + "}" + SEPARATOR
                + "/**" + SEPARATOR
                + "  Returns coverage from array" + SEPARATOR
                + "  @since 1.0.0" + SEPARATOR
                + "  @example FunctionWithArgs(coverages, 1)" + SEPARATOR
                + "    @result Coverage" + SEPARATOR
                + "  @invalidExample FunctionWithArgs(coverages, -1)" + SEPARATOR
                + "  @parameter c - Array of coverages" + SEPARATOR
                + "  @parameter n - Index of coverage" + SEPARATOR
                + " */" + SEPARATOR
                + "Function <T is Coverage> FunctionWithArgs(<T>[] c, Number n) : <T> {" + SEPARATOR
                + "  set first to c[n] return n" + SEPARATOR
                + "}" + SEPARATOR
                + SEPARATOR
            )
        );
    }

    private String convert(String namespace, List<Function> functions) {
        return converter.convert(
            ResourceBuilder.getInstance()
                .setNamespace(namespace)
                .addFunctions(functions)
                .build()
        );
    }

    private static FunctionParameter parameter(String name, String type) {
        FunctionParameter functionParameter = factory.createFunctionParameter();
        functionParameter.setName(name);
        functionParameter.setType(type);
        return functionParameter;
    }

    private static Function function(String name,
                                     String returnType,
                                     List<FunctionParameter> parameters,
                                     List<GenericTypeBound> genericTypeBounds,
                                     String body,
                                     FunctionDocumentation documentation) {
        Function function = factory.createFunction();
        function.setName(name);
        function.setReturnType(returnType);
        function.setParameters(parameters);
        function.setGenericTypeBounds(genericTypeBounds);

        Expression expression = factory.createExpression();
        expression.setExpressionString(body);
        function.setBody(expression);

        function.setDocumentation(documentation);

        function.setPhysicalNamespace("whatever");
        return function;
    }

    private static GenericTypeBound genericTypeBound(String generic, String bound) {
        GenericTypeBound genericTypeBound = factory.createGenericTypeBound();
        genericTypeBound.setGeneric(generic);
        genericTypeBound.setBound(bound);
        return genericTypeBound;
    }

    private static FunctionExample example(String example, String result, boolean valid) {
        FunctionExample functionExample = factory.createFunctionExample();
        functionExample.setExample(example);
        functionExample.setResult(result);
        functionExample.setValid(valid);
        return functionExample;
    }

    private static ParameterDocumentation parameterDocumentation(String parameterName, String description) {
        ParameterDocumentation parameterDocumentation = factory.createParameterDocumentation();
        parameterDocumentation.setParameterName(parameterName);
        parameterDocumentation.setDescription(description);
        return parameterDocumentation;
    }

    private static FunctionDocumentation functionDocumentation(String description,
                                                               String since,
                                                               List<FunctionExample> examples,
                                                               List<ParameterDocumentation> parameterDocumentations) {
        FunctionDocumentation documentation = factory.createFunctionDocumentation();
        documentation.setDescription(description);
        documentation.setSince(since);
        documentation.setExamples(examples);
        documentation.setParameterDocumentations(parameterDocumentations);
        return documentation;
    }
}
