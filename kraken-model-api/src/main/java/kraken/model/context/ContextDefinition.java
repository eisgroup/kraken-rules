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
package kraken.model.context;

import kraken.annotations.API;
import kraken.model.KrakenModelItem;

import java.util.Collection;
import java.util.Map;

/**
 * Models data context object on which rules are defined and executed.
 * Each context definition is identified by unique name
 *
 * @author rimas
 * @since 1.0
 */
@API
@SuppressWarnings("WeakerAccess")
public interface ContextDefinition extends KrakenModelItem {
    /**
     * If set to true, indicates that context definition is strict, meaning all field attributes
     * in context must be defined in {{getContextFields}} map
     *
     * @return
     */
    boolean isStrict();

    void setStrict(boolean strict);

    /**
     * If set to {@code true}, indicates that context definition is marked as system type.
     * Such context definitions have the following restrictions:
     *
     * <ul>
     *     <li>Can be only used as a field in other context definitions.</li>
     *     <li>Cannot have child contexts.</li>
     *     <li>Cannot be used as target for a rule.</li>
     * </ul>
     */
    boolean isSystem();

    void setSystem(boolean system);

    void setChildren(Map<String, ContextNavigation> children);

    /**
     * Navigation expressions to resolve child contexts, mapped to child context name
     */
    Map<String, ContextNavigation> getChildren();

    void setContextFields(Map<String, ContextField> fields);

    /**
     * Collection of context field attributes, empty and not used is isStrict == false
     *
     * @return
     */
    Map<String, ContextField> getContextFields();

    /**
     * If not null, defines name from which this context is derived
     */
    void setParentDefinitions(Collection<String> parentDefinitions);

    Collection<String> getParentDefinitions();

    /**
     * Identifies that context definition is a root context.
     * Root context definition can be one per namespace.
     * @return
     */
    boolean isRoot();

    boolean setRoot(boolean isRoot);
}
