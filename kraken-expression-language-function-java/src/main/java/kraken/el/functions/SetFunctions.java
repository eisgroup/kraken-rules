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
package kraken.el.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Native;

/**
 * @author mulevicius
 * @since 1.0.30
 */
@Native
public class SetFunctions implements FunctionLibrary {

    /**
     *
     * @param a
     * @param b
     * @param <T>
     * @return a mathematical union of two sets; null parameter is treated as empty set
     */
    @ExpressionFunction("Union")
    public static <T> Collection<T> union(Collection<T> a, Collection<T> b) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.addAll(nullToEmpty(b));
        return new ArrayList<>(firstSet);
    }

    /**
     *
     * @param a
     * @param b
     * @param <T>
     * @return a mathematical intersection of two sets; null parameter is treated as empty set
     */
    @ExpressionFunction("Intersection")
    public static <T> Collection<T> intersection(Collection<T> a, Collection<T> b) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.retainAll(nullToEmpty(b));
        return new ArrayList<>(firstSet);
    }

    /**
     *
     * @param a
     * @param b
     * @param <T>
     * @return a mathematical difference between first set and second set; null parameter is treated as empty set
     */
    @ExpressionFunction("Difference")
    public static <T> Collection<T> difference(Collection<T> a, Collection<T> b) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.removeAll(nullToEmpty(b));
        return new ArrayList<>(firstSet);
    }

    /**
     *
     * @param a
     * @param b
     * @param <T>
     * @return a mathematical symmetric different of two sets; null parameter is treated as empty set
     */
    @ExpressionFunction("SymmetricDifference")
    public static <T> Collection<T> symmetricDifference(Collection<T> a, Collection<T> b) {
        Set<T> firstSet = new HashSet<>(nullToEmpty(a));
        firstSet.removeAll(nullToEmpty(b));
        Set<T> secondSet = new HashSet<>(nullToEmpty(b));
        secondSet.removeAll(nullToEmpty(a));
        firstSet.addAll(secondSet);
        return new ArrayList<>(firstSet);
    }

    /**
     *
     * @param a
     * @param <T>
     * @return a set of values with duplicates removed; null parameter is treated as empty set
     */
    @ExpressionFunction("Distinct")
    public static <T> Collection<T> distinct(Collection<T> a) {
        return new ArrayList<>(new HashSet<>(nullToEmpty(a)));
    }

    private static <T> Collection<T> nullToEmpty(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }
}
