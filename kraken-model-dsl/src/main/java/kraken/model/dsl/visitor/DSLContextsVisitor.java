/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.model.DSLContext;
import kraken.model.dsl.model.DSLContexts;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class DSLContextsVisitor extends KrakenDSLBaseVisitor<DSLContexts> {

    private DSLContextVisitor contextVisitor = new DSLContextVisitor();

    @Override
    public DSLContexts visitContexts(KrakenDSL.ContextsContext ctx) {
        Collection<DSLContext> contexts = ctx.context().stream()
                .map(contextContext -> contextVisitor.visit(contextContext))
                .collect(Collectors.toList());

        Collection<DSLContexts> contextBlocks = ctx.contexts().stream()
                .map(contextBlock -> this.visitContexts(contextBlock))
                .collect(Collectors.toList());

        return new DSLContexts(contexts, contextBlocks);
    }
}
