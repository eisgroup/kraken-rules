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
package kraken.runtime.repository.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import kraken.annotations.SPI;
import kraken.runtime.model.MetadataContainer;

/**
 * Filters list of Kraken Model items against one or more dimension values in a context.
 * Used by Kraken repositories to plug-in dimensional filtering capability at runtime.
 * Multiple instances can be combined to narrow down applicable rule instances to single one.
 *
 * @author mulevicius
 */
@SPI
public interface DimensionFilter {

    /**
     * @param items to filter; collection of items is immutable and cannot be modified
     * @param context that shall contain dimensions and other additional context data as a key value pairs
     * @return filtered collection that contains only items that match the filter;
     *         if there is nothing to filter then collection instance can be the same as provided in parameter,
     *         otherwise it should be a new collection instance
     * @deprecated use {@link #filter(String, Collection, Map)} instead
     */
    @Deprecated(since = "1.38.0", forRemoval = true)
    default <T extends MetadataContainer> Collection<T> filter(Collection<T> items, Map<String, Object> context) {
        throw new UnsupportedOperationException("DimensionFilter#filter(Collection, Map) is not supported");
    };

    /**
     * @param items to filter; collection of items is immutable and cannot be modified
     * @param context that shall contain dimensions and other additional context data as a key value pairs
     * @param namespace namespace of the kraken project of this evaluation
     * @return filtered collection that contains only items that match the filter;
     *         if there is nothing to filter then collection instance can be the same as provided in parameter,
     *         otherwise it should be a new collection instance
     */
    default <T extends MetadataContainer> Collection<T> filter(String namespace,
                                                               Collection<T> items,
                                                               Map<String, Object> context) {
        return filter(items, context);
    };
}
