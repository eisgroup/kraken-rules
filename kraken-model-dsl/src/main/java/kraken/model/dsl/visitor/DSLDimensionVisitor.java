/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.dsl.visitor;

import static kraken.el.ast.builder.Literals.escape;
import static kraken.el.ast.builder.Literals.stripQuotes;

import java.util.Arrays;
import java.util.stream.Collectors;

import kraken.model.DimensionDataType;
import kraken.model.dsl.KrakenDSL.DimensionContext;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLDimension;

/**
 * A visitor for performing operation on {@code DimensionContext} object.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public class DSLDimensionVisitor extends KrakenDSLBaseVisitor<DSLDimension> {

    @Override
    public DSLDimension visitDimension(DimensionContext ctx) {
        String name = escape(stripQuotes(ctx.dimensionName().getText()));
        String type = parseDimensionType(ctx.dimensionType.getText());

        return new DSLDimension(name, type);
    }

    private String parseDimensionType(String typeText) {
        if (DimensionDataType.isDimensionType(typeText.toUpperCase())) {
            return typeText.toUpperCase();
        }

        throw new IllegalStateException("Invalid dimension data type '"
            + typeText.toUpperCase() + "' supported data types: "
            + Arrays.stream(DimensionDataType.values()).map(Enum::name).collect(Collectors.joining(", ")));
    }

}
