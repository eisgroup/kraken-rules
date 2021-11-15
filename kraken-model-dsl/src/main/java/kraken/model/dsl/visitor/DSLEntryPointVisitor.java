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
import kraken.model.dsl.model.DSLAnnotation;
import kraken.model.dsl.model.DSLEntryPoint;

import java.util.ArrayList;
import java.util.List;

import static kraken.el.ast.builder.Literals.escape;
import static kraken.el.ast.builder.Literals.stripQuotes;

/**
 * @author mulevicius
 */
public class DSLEntryPointVisitor extends KrakenDSLBaseVisitor<DSLEntryPoint> {

    private DSLAnnotationVisitor annotationVisitor = new DSLAnnotationVisitor();

    @Override
    public DSLEntryPoint visitEntryPoint(KrakenDSL.EntryPointContext ctx) {
        String name = escape(stripQuotes(ctx.entryPointName.getText()));

        DSLAnnotation annotation = ctx.annotations() != null ?
                annotationVisitor.visit(ctx.annotations()) : DSLAnnotation.EMPTY;

        List<String> ruleNames = new ArrayList<>();
        List<String> includedEntryPointNames = new ArrayList<>();

        if (ctx.entryPointItems() != null) {
            for (KrakenDSL.EntryPointItemContext ruleNameContext : ctx.entryPointItems().entryPointItem()) {
                if(ruleNameContext.entryPointName != null){
                    includedEntryPointNames.add(escape(stripQuotes(ruleNameContext.entryPointName.getText())));
                } else {
                    ruleNames.add(escape(stripQuotes(ruleNameContext.getText())));
                }
            }
        }

        return new DSLEntryPoint(name, annotation.getMetadata(),
                ruleNames, includedEntryPointNames, annotation.isServerSideOnly());
    }

}
