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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.el.scope.type.Type;
import kraken.el.scope.type.TypeRefResolver;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextNavigation;
import kraken.model.project.KrakenProject;

import static kraken.model.project.scope.TypeBuilder.buildType;

/**
 * @author mulevicius
 */
class TypeRegistry {

    /**
     * ALL types of {@link ContextDefinition} that are available in {@link KrakenProject}.
     * <p/>
     * Note, that key is equal to {@link ContextDefinition#getName()} which may not be equal to {@link Type#getName()}
     * when {@link ContextDefinition#isStrict()} is false, because in that case {@link Type#getName()} is ANY
     */
    private final Map<String, Type> types;

    /**
     * All types of {@code ExternalContextDefinition} that are available in {@code KrakenProject}.
     */
    private final Map<String, Type> externalTypes;

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
        TypeRefResolver typeRefResolver = typeName -> allTypes.get(typeName);

        krakenProject.getContextDefinitions().values()
                .forEach(c -> allTypes.put(c.getName(), buildType(c, krakenProject, typeRefResolver)));

        this.types = Collections.unmodifiableMap(allTypes);

        Map<String, Type> allGlobalTypes = collectUniqueGlobalTypes(krakenProject.getRootContextName(), krakenProject)
                .stream()
                .distinct()
                .collect(Collectors.toMap(t -> t, allTypes::get));

        this.globalTypes = Collections.unmodifiableMap(allGlobalTypes);
        this.externalTypes = collectExternalTypes(krakenProject);
    }

    private Map<String, Type> collectExternalTypes(KrakenProject krakenProject) {
        Map<String, Type> allTypes = new HashMap<>();

        if (krakenProject.getExternalContext() != null) {
            TypeRefResolver typeRefResolver = allTypes::get;

            krakenProject.getExternalContextDefinitions().values()
                    .forEach(c -> allTypes.put(c.getName(), buildType(c, krakenProject, typeRefResolver)));
        }

        return Collections.unmodifiableMap(allTypes);
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

    /**
     * Returns external type for given name. Will return an empty optional if no external type
     * if available in {@code KrakenProject} for given name.
     *
     * @param name External type name.
     * @return External type for given name.
     */
    Optional<Type> getExternalType(String name) {
        return Optional.ofNullable(externalTypes.get(name));
    }

    /**
     * Returns all external types available in {@code KrakenProject}.
     *
     * @return All available types.
     */
    Map<String, Type> getAllExternalTypes() {
        return externalTypes;
    }


    Map<String, Type> getGlobalTypes() {
        return globalTypes;
    }

    private static Collection<String> collectUniqueGlobalTypes(String rootContextName,
                                                               KrakenProject krakenProject) {
        Set<String> collectedTypes = new HashSet<>();
        collectTypes(rootContextName, krakenProject, collectedTypes);
        return collectedTypes;
    }

    private static void collectTypes(String contextName,
                                     KrakenProject krakenProject,
                                     Set<String> collectedTypes) {
        collectedTypes.add(contextName);

        ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(contextName);
        collectInheritedTypes(contextDefinition, krakenProject, collectedTypes);
        collectChildTypes(contextDefinition, krakenProject, collectedTypes);
    }

    private static void collectInheritedTypes(ContextDefinition contextDefinition,
                                              KrakenProject krakenProject,
                                              Set<String> collectedTypes) {
        krakenProject.getContextProjection(contextDefinition.getName()).getParentDefinitions()
                .forEach(c -> {
                    collectedTypes.add(c);
                    collectChildTypes(krakenProject.getContextDefinitions().get(c), krakenProject, collectedTypes);
                });
    }

    private static void collectChildTypes(ContextDefinition contextDefinition,
                                          KrakenProject krakenProject,
                                          Set<String> collectedTypes) {
        Map<String, ContextNavigation> childProjection = krakenProject.getContextProjection(contextDefinition.getName()).getChildren();
        for(ContextNavigation contextNavigation : childProjection.values()) {
            if (!collectedTypes.contains(contextNavigation.getTargetName())) {
                collectTypes(contextNavigation.getTargetName(), krakenProject, collectedTypes);
            }
        }
    }

}
