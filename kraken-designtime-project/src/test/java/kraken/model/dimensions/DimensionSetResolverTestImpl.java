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

import kraken.dimensions.DimensionSet;
import kraken.model.MetadataAware;

import java.util.Collections;
import java.util.Set;


/**
 * Implementation for SPI loader testing
 *
 * @author kjuraityte
 * @since 1.40.0
 */
public class DimensionSetResolverTestImpl implements DimensionSetResolver {

    @Override
    public DimensionSet resolve(String namespace, MetadataAware metadataAwareKrakenItem) {
        Set<String> keys = metadataAwareKrakenItem.getMetadata().asMap().keySet();
        return DimensionSet.createForDimensions(Collections.unmodifiableSet(keys));
    }
}
