/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.scope;

import static kraken.model.project.scope.TypeBuilder.buildType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.el.scope.type.Type;
import kraken.el.scope.type.TypeRefResolver;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.project.KrakenProject;

/**
 * @author mulevicius
 */
class TypeRegistry {

    /**
     * ALL types of {@link ContextDefinition} and {@link ExternalContextDefinition}
     * that are available in {@link KrakenProject}.
     * <p/>
     * Note, that key is equal to {@link ContextDefinition#getName()} or {@link ExternalContextDefinition#getName()}
     * which may not be equal to {@link Type#getName()} when {@link ContextDefinition#isStrict()} is false,
     * because in that case {@link Type#getName()} is ANY
     */
    private final Map<String, Type> types;

    /**
     * Types of {@link ContextDefinition} that are available in {@link KrakenProject} AND can be referenced as Cross Context Reference.
     * This excludes {@link ContextDefinition} that are only complex field but not children.
     * <p/>
     * Note, that key is equal to {@link ContextDefinition#getName()} which may not be equal to {@link Type#getName()}
     * when {@link ContextDefinition#isStrict()} is not true, because in that case {@link Type#getName()} is ANY
     */
    private final Map<String, Type> globalTypes;

    TypeRegistry(KrakenProject krakenProject) {

        Map<String, Type> allTypes = new HashMap<>();
        TypeRefResolver typeRefResolver = allTypes::get;

        krakenProject.getContextDefinitions().values()
                .forEach(c -> allTypes.put(c.getName(), buildType(c, krakenProject, typeRefResolver)));

        Map<String, Type> allGlobalTypes = krakenProject.getConnectedContextDefinitions()
                .stream()
                .distinct()
                .collect(Collectors.toMap(t -> t, allTypes::get));

        this.globalTypes = Collections.unmodifiableMap(allGlobalTypes);

        if (krakenProject.getExternalContext() != null) {
            krakenProject.getExternalContextDefinitions().values()
                .forEach(c -> allTypes.put(c.getName(), buildType(c, typeRefResolver)));
        }

        this.types = Collections.unmodifiableMap(allTypes);
    }

    /**
     *
     * @param type name of complex type (non primitive and non system) available in the {@link KrakenProject}
     * @return
     */
    Type get(String type) {
        return types.get(type);
    }

    Map<String, Type> getAllTypes() {
        return types;
    }

    Map<String, Type> getGlobalTypes() {
        return globalTypes;
    }

}
