/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLExternalContext;

import java.util.HashMap;
import java.util.Objects;

/**
 * A visitor for visiting External Context parser rule context.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class DSLExternalContextVisitor extends KrakenDSLBaseVisitor<DSLExternalContext> {

    @Override
    public DSLExternalContext visitExternalContext(KrakenDSL.ExternalContextContext ctx) {
        DSLExternalContext rootExternalContext = new DSLExternalContext(new HashMap<>(), new HashMap<>());

        if(ctx.externalContextItems() != null) {
            for(var c : ctx.externalContextItems().externalContextItem()) {
                if(c.externalContextNodeItem() != null) {
                    rootExternalContext.getContexts()
                        .put(c.externalContextNodeItem().key.getText(), createContext(c.externalContextNodeItem()));
                }
                if(c.externalContextEntityItem() != null) {
                    rootExternalContext.getBoundedContextDefinitions()
                        .put(c.externalContextEntityItem().key.getText(),
                            c.externalContextEntityItem().externalContextDefinitionName.getText());
                }
            }
        }
        return rootExternalContext;
    }

    private DSLExternalContext createContext(KrakenDSL.ExternalContextNodeItemContext ctx) {
        DSLExternalContext externalContext = new DSLExternalContext(new HashMap<>(), new HashMap<>());

        ctx.externalContextItems().externalContextItem().stream()
            .map(KrakenDSL.ExternalContextItemContext::externalContextEntityItem)
            .filter(Objects::nonNull)
            .forEach(c -> externalContext.getBoundedContextDefinitions().put(c.key.getText(),
                c.externalContextDefinitionName.getText()));

        ctx.externalContextItems().externalContextItem().stream()
            .map(KrakenDSL.ExternalContextItemContext::externalContextNodeItem)
            .filter(Objects::nonNull)
            .forEach(c -> externalContext.getContexts().put(c.key.getText(), createContext(c)));

        return externalContext;
    }

}
