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
package kraken.namespace;

import kraken.annotations.API;

import java.util.Optional;

/**
 * Defines the name identifiers for items in Kraken model.
 * Implies that items implementing this interface can be projected based on the namespace name.
 * For example, the same model item can be defined in multiple namespaces and based on the context
 * the {@link #getPhysicalNamespace()} would return different namespace names.
 * <p>
 * The namespace resolving logic does not leave the repository layer
 *
 * @author gvisokinskas
 */
@API
public interface Namespaced {
    /**
     * The separator which is used to split the current item's name projection.
     */
    String SEPARATOR = ":";

    /**
     * The name for the global namespace.
     * This namespace should be set on entities that do not have an explicit namespace defined.
     */
    String GLOBAL = "";

    /**
     * @return the name of this model item, can be duplicated across different namespace projections.
     */
    String getName();

    /**
     * Sets the name of this model part.
     *
     * @param name the name to set.
     */
    void setName(String name);

    /**
     * @return the name of the <b>physical</b> namespace.
     */
    String getPhysicalNamespace();

    /**
     * Sets the name of the current namespace projection.
     *
     * @param name the name to set.
     */
    void setPhysicalNamespace(String name);

    /**
     * @return the full (physical) name of this item with the namespace name added.
     */
    default String getFullName() {
        return Optional.ofNullable(getPhysicalNamespace())
                .filter(ns -> !Namespaced.GLOBAL.equals(ns))
                .map(ns -> ns + SEPARATOR + getName())
                .orElse(getName());
    }
}
