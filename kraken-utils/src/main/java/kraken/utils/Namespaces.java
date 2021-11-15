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
package kraken.utils;

import kraken.namespace.Namespaced;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utility class for working with {@link Namespaced} entities.
 *
 * @author gvisokinskas
 */
public final class Namespaces {
    /**
     * Private constructor for utility class.
     */
    private Namespaces() {

    }

    /**
     * Resolves the namespace name from the provided simple or full name.
     * Assumes that the name is separated using {@link Namespaced#SEPARATOR}.
     *
     * @param name the name from which the namespace should be resolved.
     * @return the resolved namespace name or {@link Namespaced#GLOBAL} if the namespace name is not found.
     */
    public static String toNamespaceName(String name) {
        if (name != null && name.contains(Namespaced.SEPARATOR)) {
            return name.substring(0, name.lastIndexOf(Namespaced.SEPARATOR));
        }
        return Namespaced.GLOBAL;
    }

    /**
     * Concatenates the provided simple name with the namespace name.
     *
     * @param namespaceName the namespace name to use.
     * @param name          the simple name of the object.
     * @return a fully qualified name.
     */
    public static String toFullName(String namespaceName, String name) {
        if (namespaceName != null && isGlobal().negate().test(namespaceName)) {
            return namespaceName + Namespaced.SEPARATOR + name;
        }
        return name;
    }

    /**
     * @return an instance of {@link Predicate} to check if the provided namespace name is global
     */
    public static Predicate<String> isGlobal() {
        return Namespaced.GLOBAL::equals;
    }

    /**
     * If parameter contains namespace separated by {@link Namespaced#SEPARATOR},
     * then namespace with separator will be stripped. Otherwise name form parameter
     * will be returned.
     * @param name  name to be resolved
     * @return simple name of provided name in parameter.
     */
    public static String toSimpleName(String name) {
        return Optional.of(name)
                .map(Namespaces::toNamespaceName)
                .filter(isGlobal().negate())
                .map(n -> name.substring(name.lastIndexOf(Namespaced.SEPARATOR) + 1))
                .orElse(name);
    }
}
