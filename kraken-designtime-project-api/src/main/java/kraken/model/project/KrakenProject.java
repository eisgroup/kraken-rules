/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

package kraken.model.project;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import kraken.annotations.API;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.entrypoint.EntryPoint;

/**
 * Represents a self-contained designtime model of {@link Rule}, {@link EntryPoint}, {@link ContextDefinition}
 * and {@link ExternalContext} in scope of a namespace.
 *
 * @author mulevicius
 */
@API
public interface KrakenProject {

    /**
     * @return uniquely identifies contents of this project instance;
     */
    UUID getIdentifier();

    /**
     * @return namespace that this KrakenProject.
     */
    String getNamespace();

    /**
     * @return name of {@link ContextDefinition} which is a root node in ContextDefinition tree
     */
    String getRootContextName();

    /**
     * @return External context bound to this Kraken project.
     */
    ExternalContext getExternalContext();

    /**
     * @return all instances of {@link ContextDefinition} applicable for this KrakenProject
     */
    Map<String, ContextDefinition> getContextDefinitions();

    /**
     * @return all instances of {@link ExternalContextDefinition} applicable for this KrakenProject
     */
    Map<String, ExternalContextDefinition> getExternalContextDefinitions();

    /**
     * @return all instances of {@link EntryPoint} applicable for this KrakenProject
     */
    List<EntryPoint> getEntryPoints();

    /**
     * @return all instances of {@link Rule} applicable for this KrakenProject
     */
    List<Rule> getRules();

    /**
     * @return all instances of {@link EntryPoint} grouped by name where key is {@link EntryPoint#getName()}
     *     and value is all implementations with same name applicable for this KrakenProject.
     */
    Map<String, List<EntryPoint>> getEntryPointVersions();

    /**
     * @return all instances of {@link Rule} grouped by name where key is {@link Rule#getName()}
     *     and value is all implementations with same name applicable for this KrakenProject.
     */
    Map<String, List<Rule>> getRuleVersions();

    /**
     * @return a projected view of {@link ContextDefinition} with processed inheritance by merging
     *     {@link ContextDefinition#getChildren()},
     *     {@link ContextDefinition#getContextFields()},
     *     and {@link ContextDefinition#getParentDefinitions()}
     *     from inherited contexts.
     */
    ContextDefinition getContextProjection(String contextName);

    /**
     * @return Returns namespace tree representing namespaces and their inter-dependencies.
     */
    NamespaceTree getNamespaceTree();

    /**
     * @return a list of available functions to be used in rules defined in this KrakenProject.
     * A function implementation must exist at runtime for each function signature defined.
     */
    List<FunctionSignature> getFunctionSignatures();

    /**
     *
     * @return a list of function implementations to be used in rules defined in this KrakenProject
     */
    List<Function> getFunctions();

    /**
     * Connected context definitions, are connected via
     * direct parent -> child connection ({@link ContextDefinition#getChildren()}),
     * and parent -> child on inherited contexts.
     * Context definitions {@link KrakenProject#getContextDefinitions()},
     * can contain not connected entries. Connection is calculated from @{@link KrakenProject#getRootContextName()}.
     *
     * @return collection of {@link ContextDefinition#getName()}
     * @since 1.0.28
     */
    Collection<String> getConnectedContextDefinitions();

}
