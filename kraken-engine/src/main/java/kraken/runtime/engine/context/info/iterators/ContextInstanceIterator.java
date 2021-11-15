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
package kraken.runtime.engine.context.info.iterators;

import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;

/**
 * Part of {@link ContextInstanceInfoResolver} SPI, models iteration through collection
 * like data structures, when these are returned by navigation expression.
 * Provides ability to iterate through collection, as well as create implementation specific
 * index value of type T, and create value by provided key.
 *
 * @author rimas
 * @since 1.0
 */
public interface ContextInstanceIterator<T> {

    boolean hasNext();

    Object next();

    T key();

    Object getValue(Object index);
}
