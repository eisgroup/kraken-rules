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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLAnnotation;
import kraken.model.dsl.model.DSLMetadata;

/**
 * Specific implementation of DSL visitor to resolve all DSL model
 * annotations.
 *
 * @author Tomas Dapkunas
 * @since 1.0.36
 */
public class DSLAnnotationVisitor extends KrakenDSLBaseVisitor<DSLAnnotation> {

    private DSLMetadataVisitor metadataVisitor = new DSLMetadataVisitor();

    @Override
    public DSLAnnotation visitAnnotations(KrakenDSL.AnnotationsContext ctx) {
        List<KrakenDSL.AnnotationEntryContext> aCtx = getAnnotations(ctx);

        return new DSLAnnotation(isServerSideOnly(aCtx), visitMetadata(aCtx));
    }

    private List<KrakenDSL.AnnotationEntryContext> getAnnotations(KrakenDSL.AnnotationsContext ctx) {
        return Optional.ofNullable(ctx)
                .map(KrakenDSL.AnnotationsContext::annotationEntry)
                .orElseGet(List::of);
    }

    private boolean isServerSideOnly(List<KrakenDSL.AnnotationEntryContext> annotations) {
        return annotations.stream()
                .anyMatch(annotationEntryContext -> annotationEntryContext.serverSideOnlyEntry() != null);
    }

    private DSLMetadata visitMetadata(List<KrakenDSL.AnnotationEntryContext> annotations) {
        return new DSLMetadata(annotations.stream()
                .filter(annotationEntryContext -> annotationEntryContext.metadataEntry() != null)
                .map(mCtx -> metadataVisitor.visit(mCtx.metadataEntry()))
                .flatMap(metadata -> metadata.getMetadata().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

}