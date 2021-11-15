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

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * An utility class containing functions for working with {@link Optional}.
 *
 * @author gvisokinskas
 */
public final class OptionalUtils {
    /**
     * Private constructor for utility class.
     */
    private OptionalUtils() {

    }

    /**
     * Chains the provided optionals in an OR chain. Lazily evaluates every one of them
     * until the first non empty optional is found. Returns the first found optional.
     *
     * @param optionals the optionals to OR.
     * @param <T> the type of the {@link Optional}.
     * @return first non empty {@link Optional}, {@link Optional#empty()} if not found.
     */
    @SafeVarargs
    public static <T> Optional<T> or(Supplier<Optional<T>>... optionals) {
        return Arrays.stream(optionals)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(Optional::empty);
    }
}
