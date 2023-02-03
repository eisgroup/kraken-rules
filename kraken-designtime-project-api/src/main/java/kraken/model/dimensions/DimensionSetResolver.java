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
package kraken.model.dimensions;

import kraken.annotations.SPI;
import kraken.dimensions.DimensionSet;
import kraken.model.MetadataAware;

/**
 * SPI to handle mapping between parameters of Dimension annotations of rules and entry points
 * and names of actual dimensions, by which rule or entry point variations will change and will
 * be cached accordingly. Used to enable more effective caching of dimensional rules on Kraken
 * UI engine.
 *
 * @author kjuraityte
 * @since 1.40.0
 */
@SPI
public interface DimensionSetResolver {

    /**
     * Provides relationship between first parameter in the @Dimension annotation and dimension name
     *
     * @param namespace
     * @param metadataAwareKrakenItem
     * @return a set of dimension names
     */
    DimensionSet resolve(String namespace, MetadataAware metadataAwareKrakenItem);

}
