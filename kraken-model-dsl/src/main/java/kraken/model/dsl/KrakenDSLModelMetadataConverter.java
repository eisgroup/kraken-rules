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

import kraken.model.Metadata;
import kraken.model.MetadataAware;
import kraken.model.dsl.model.DSLMetadata;
import kraken.model.factory.RulesModelFactory;

/**
 * @author mulevicius
 */
class KrakenDSLModelMetadataConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    private KrakenDSLModelMetadataConverter() {
    }

    static Metadata convertMetadata(DSLMetadata dslMetadata) {
        if (dslMetadata == null || dslMetadata.getMetadata().isEmpty()) {
            return null;
        }
        Metadata metadata = factory.createMetadata();
        dslMetadata.getMetadata().entrySet()
                .stream()
                .forEach(e -> metadata.setProperty(e.getKey(), e.getValue()));
        return metadata;
    }

    /**
     * Applies metadata over MetadataAware model by preserving duplicate metadata properties in model
     *
     * @param model
     * @param parentMetadata
     * @param <T>
     * @return
     */
    static <T extends MetadataAware> T withParentMetadata(T model, Metadata parentMetadata) {
        if(parentMetadata == null) {
            return model;
        }
        if(model.getMetadata() == null) {
            model.setMetadata(parentMetadata);
        } else {
            parentMetadata.asMap().entrySet().stream()
                    .filter(e -> !model.getMetadata().hasProperty(e.getKey()))
                    .forEach(e -> model.getMetadata().setProperty(e.getKey(), e.getValue()));
        }
        return model;
    }

    /**
     * Merges two metadata objects so that duplicate metadata properties in child metadata takes precedence
     *
     * @param parentMetadata
     * @param childMetadata
     * @return
     */
    public static Metadata merge(Metadata parentMetadata, Metadata childMetadata) {
        if(parentMetadata == null && childMetadata == null) {
            return null;
        }

        Metadata metadata = factory.createMetadata();

        if(parentMetadata != null) {
            parentMetadata.asMap().entrySet().stream()
                    .forEach(e -> metadata.setProperty(e.getKey(), e.getValue()));
        }

        if(childMetadata != null) {
            childMetadata.asMap().entrySet().stream()
                    .forEach(e -> metadata.setProperty(e.getKey(), e.getValue()));
        }

        return metadata;
    }
}
