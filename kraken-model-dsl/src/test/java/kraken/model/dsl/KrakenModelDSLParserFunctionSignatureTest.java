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

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;

import org.junit.Test;

import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.resource.Resource;

/**
 * @author mulevicius
 */
public class KrakenModelDSLParserFunctionSignatureTest {

    @Test
    public void shouldParseFunctionSignatures() {
        Resource model = parseResource(""
            + "Namespace functions "
            + "Function NoArgs() : Any "
            + "Function PrimitiveTypes(Any, Number, Money, String, Date, DateTime, Boolean) : Any "
            + "Function WithArrayPrimitives(Any[], Number[], Money[], String[], Date[], DateTime[], Boolean[]) : Any[] "
            + "Function EntityType(Coverage) : RiskItem "
            + "Function ArrayEntityType(Coverage[]) : RiskItem[] "
            + "Function Generics(<T>, <N>, <M>) : <T> "
            + "Function ArrayGenerics(<T>[], <N>[], <M>[]) : <T>[] "
            + "Function UnionType(MEDCoverage | CollCoverage) : Date | DateTime "
            + "Function UnionTypeArray((MEDCoverage | CollCoverage)[]) : (Date | DateTime)[] "
            + "Function <T is String, N is Number, M is Date | DateTime> BoundedGenerics(<T>, <N>, <M>) : <T> ");

        assertFunctionSignature(
            model.getFunctionSignatures().get(0),
            "NoArgs", "Any",
            List.of()
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(1),
            "PrimitiveTypes", "Any",
            List.of("Any", "Number", "Money", "String", "Date", "DateTime", "Boolean")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(2),
            "WithArrayPrimitives", "Any[]",
            List.of("Any[]", "Number[]", "Money[]", "String[]", "Date[]", "DateTime[]", "Boolean[]")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(3),
            "EntityType", "RiskItem", List.of("Coverage")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(4),
            "ArrayEntityType", "RiskItem[]", List.of("Coverage[]")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(5),
            "Generics", "<T>", List.of("<T>", "<N>", "<M>")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(6),
            "ArrayGenerics", "<T>[]", List.of("<T>[]", "<N>[]", "<M>[]")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(7),
            "UnionType", "Date | DateTime",
            List.of("MEDCoverage | CollCoverage")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(8),
            "UnionTypeArray", "(Date | DateTime)[]",
            List.of("(MEDCoverage | CollCoverage)[]")
        );
        assertFunctionSignature(
            model.getFunctionSignatures().get(9),
            "BoundedGenerics", "<T>",
            List.of("<T>", "<N>", "<M>"),
            List.of("T is String", "N is Number", "M is Date|DateTime")
        );
    }

    private void assertFunctionSignature(FunctionSignature f, String name, String returnType, List<String> parameters) {
        assertFunctionSignature(f, name, returnType, parameters, List.of());
    }

    private void assertFunctionSignature(FunctionSignature f, String name, String returnType, List<String> parameters,
                                         List<String> bounds) {
        assertThat(f.getName(), equalTo(name));
        assertThat(f.getReturnType(), equalTo(returnType));
        assertThat(f.getParameterTypes(), equalTo(parameters));

        for(int i = 0; i < bounds.size(); i++) {
            String bound = bounds.get(i);
            GenericTypeBound genericTypeBound = f.getGenericTypeBounds().get(i);
            String actualBound = genericTypeBound.getGeneric() + " is " + genericTypeBound.getBound();
            assertThat(actualBound, equalTo(bound));
        }
    }

}
