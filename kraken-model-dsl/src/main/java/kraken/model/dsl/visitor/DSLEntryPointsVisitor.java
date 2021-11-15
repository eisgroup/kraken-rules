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
import kraken.model.dsl.model.DSLEntryPoints;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class DSLEntryPointsVisitor extends KrakenDSLBaseVisitor<DSLEntryPoints> {

    private DSLEntryPointVisitor entryPointVisitor = new DSLEntryPointVisitor();

    private DSLAnnotationVisitor annotationVisitor = new DSLAnnotationVisitor();

    @Override
    public DSLEntryPoints visitEntryPoints(KrakenDSL.EntryPointsContext ctx) {
        DSLAnnotation annotation = ctx.annotations() != null ?
                annotationVisitor.visit(ctx.annotations()) : DSLAnnotation.EMPTY;

        Collection<DSLEntryPoint> entryPoints = ctx.entryPoint().stream()
                .map(entryPointContext -> entryPointVisitor.visit(entryPointContext))
                .collect(Collectors.toList());

        Collection<DSLEntryPoints> entryPointBlocks = ctx.entryPoints().stream()
                .map(this::visitEntryPoints)
                .collect(Collectors.toList());

        return new DSLEntryPoints(entryPoints, annotation.getMetadata(), entryPointBlocks, annotation.isServerSideOnly());
    }

}
