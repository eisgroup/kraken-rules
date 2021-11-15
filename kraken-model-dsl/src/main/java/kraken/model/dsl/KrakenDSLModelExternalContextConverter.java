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

import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.dsl.model.DSLExternalContext;
import kraken.model.dsl.model.DSLModel;
import kraken.model.factory.RulesModelFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converter for extracting all {@code DSLExternalContext}'s from DSL model and converting
 * to {@code ExternalContext}'s.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
class KrakenDSLModelExternalContextConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();
    private static final String PREFIX = "ExternalContext_";
    private static final String ROOT = "root";

    private KrakenDSLModelExternalContextConverter() {
    }

    static ExternalContext convertExternalContext(DSLModel dsl, List<ExternalContextDefinition> allContextDefinitions) {
        return dsl.getExternalContext() != null
                ? toExternalContext(dsl.getNamespace(), null, true, dsl.getExternalContext(), allContextDefinitions) : null;
    }

    private static ExternalContext toExternalContext(String namespace,
                                                     String path,
                                                     Boolean isRoot,
                                                     DSLExternalContext dslExternalContext,
                                                     List<ExternalContextDefinition> allContextDefinitions) {
        ExternalContext externalContext = factory.createExternalContext();
        externalContext.setPhysicalNamespace(namespace);
        externalContext.setExternalContextDefinitions(resolveContextDefinitions(dslExternalContext,
                allContextDefinitions));

        if (isRoot) {
            externalContext.setName(PREFIX + ROOT);
        } else {
            externalContext.setName(PREFIX + path);
        }

        dslExternalContext.getContexts().forEach((key, value) -> externalContext.getContexts().put(key,
                toExternalContext(namespace, getPath(path, key), false, value, allContextDefinitions)));

        return externalContext;
    }

    private static Map<String, ExternalContextDefinitionReference> resolveContextDefinitions(DSLExternalContext dslExternalContext,
                                                                                             List<ExternalContextDefinition> allContextDefinitions) {
        Map<String, ExternalContextDefinition> extDefAsMap =
                allContextDefinitions.stream().collect(Collectors.toMap(ExternalContextDefinition::getName, e -> e));

        return dslExternalContext
                .getBoundedContextDefinitions().entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    if (extDefAsMap.containsKey(entry.getValue())) {
                        ExternalContextDefinitionReference ref = factory.createExternalContextDefinitionReference();
                        ref.setName(entry.getValue());

                        return ref;
                    } else {
                        throw new IllegalArgumentException("Cannot resolve External Context Definition named - " + entry.getValue());
                    }
                }));
    }

    private static String getPath(String current, String nextElement) {
        return StringUtils.isEmpty(current) ? nextElement : current + "_" + nextElement;
    }

}
