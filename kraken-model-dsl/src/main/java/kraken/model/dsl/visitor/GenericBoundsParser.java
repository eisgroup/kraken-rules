/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.visitor;

import java.util.List;
import java.util.stream.Collectors;

import kraken.model.dsl.KrakenDSL.GenericBoundContext;
import kraken.model.dsl.KrakenDSL.GenericBoundsContext;
import kraken.model.dsl.model.DSLGenericTypeBound;

/**
 * Parses {@link DSLGenericTypeBound} from ANTLR4 context
 *
 * @author mulevicius
 */
public class GenericBoundsParser {

    public static List<DSLGenericTypeBound> parseBounds(GenericBoundsContext ctx) {
        return ctx != null
            ? ctx.genericBound().stream().map(GenericBoundsParser::toGenericBound).collect(Collectors.toList())
            : List.of();
    }

    private static DSLGenericTypeBound toGenericBound(GenericBoundContext ctx) {
        return new DSLGenericTypeBound(ctx.generic.getText(), ctx.type().getText());
    }
}
