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
package kraken.model.dsl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import kraken.model.Metadata;
import kraken.model.dsl.model.DSLEntryPoint;
import kraken.model.dsl.model.DSLEntryPoints;
import kraken.model.dsl.model.DSLModel;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;

import static kraken.model.dsl.KrakenDSLModelMetadataConverter.convertMetadata;
import static kraken.model.dsl.KrakenDSLModelMetadataConverter.withParentMetadata;

/**
 * @author mulevicius
 */
class KrakenDSLModelEntryPointConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelEntryPointConverter() {
    }

    static List<EntryPoint> convertEntryPoints(DSLModel dsl, URI resourceUri) {
        List<EntryPoint> entryPoints = convertEntryPointBlocks(
            dsl.getNamespace(),
            dsl.getEntryPointBlocks(),
            resourceUri,
            null,
            false
        );
        entryPoints.addAll(
            convertEntryPoints(dsl.getNamespace(), dsl.getEntryPoints(), resourceUri, null, false)
        );

        return Collections.unmodifiableList(entryPoints);
    }

    private static List<EntryPoint> convertEntryPointBlocks(String namespace,
                                                            Collection<DSLEntryPoints> entryPointBlocks,
                                                            URI resourceUri,
                                                            Metadata parentMetadata,
                                                            boolean serverSideOnly) {
        return entryPointBlocks.stream()
                .flatMap(entryPointBlock ->
                        convertEntryPointBlock(namespace, entryPointBlock, resourceUri, parentMetadata, serverSideOnly)
                            .stream())
                .collect(Collectors.toList());
    }

    private static List<EntryPoint> convertEntryPointBlock(String namespace,
                                                           DSLEntryPoints dsl,
                                                           URI resourceUri,
                                                           Metadata parentMetadata,
                                                           boolean serverSideOnly) {
        parentMetadata = KrakenDSLModelMetadataConverter.merge(
            parentMetadata,
            convertMetadata(dsl.getMetadata(), resourceUri)
        );
        serverSideOnly = serverSideOnly || dsl.isServerSideOnly();

        List<EntryPoint> entryPoints = convertEntryPointBlocks(
            namespace,
            dsl.getEntryPointBlocks(),
            resourceUri,
            parentMetadata,
            serverSideOnly
        );
        entryPoints.addAll(
            convertEntryPoints(namespace, dsl.getEntryPoints(), resourceUri, parentMetadata, serverSideOnly)
        );

        return Collections.unmodifiableList(entryPoints);
    }

    private static List<EntryPoint> convertEntryPoints(String namespace,
                                                       Collection<DSLEntryPoint> entryPoints,
                                                       URI resourceUri,
                                                       Metadata parentMetadata,
                                                       boolean serverSideOnly) {
        return entryPoints.stream()
                .map(entryPoint -> convert(namespace, entryPoint, resourceUri))
                .map(entryPoint -> {
                    entryPoint.setServerSideOnly(entryPoint.isServerSideOnly() || serverSideOnly);
                    return withParentMetadata(entryPoint, parentMetadata);
                })
                .collect(Collectors.toList());
    }

    private static EntryPoint convert(String namespace, DSLEntryPoint dslEntryPoint, URI resourceURi) {
        EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setPhysicalNamespace(namespace);
        entryPoint.setEntryPointVariationId(UUID.randomUUID().toString());
        entryPoint.setName(dslEntryPoint.getName());
        entryPoint.setRuleNames(new ArrayList<>(dslEntryPoint.getRuleNames()));
        entryPoint.setIncludedEntryPointNames(new ArrayList<>(dslEntryPoint.getIncludedEntryPointNames()));
        entryPoint.setServerSideOnly(dslEntryPoint.isServerSideOnly());
        entryPoint.setMetadata(convertMetadata(dslEntryPoint.getMetadata(), resourceURi));
        return entryPoint;
    }
}
