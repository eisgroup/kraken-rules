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

package kraken.testproduct.dimension.filter;

import kraken.runtime.model.MetadataContainer;
import kraken.runtime.repository.filter.DimensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class StateDimensionFilter implements DimensionFilter {

    private static final String STATE = "state";
    private Logger LOGGER = LoggerFactory.getLogger(StateDimensionFilter.class);

    @Override
    public <T extends MetadataContainer> Collection<T> filter(Collection<T> items, Map<String, Object> context) {
        return items.stream()
                .filter(rule -> {
                    if (
                            rule.getMetadata() != null && context != null && context.containsKey(STATE) && rule
                            .getMetadata()
                            .getProperties()
                            .get(STATE) != null
                    ) {
                        final boolean hasMatched =
                                rule
                                        .getMetadata()
                                        .getProperties()
                                        .getOrDefault(STATE, new Object())
                                        .equals(context.get(STATE));
                        if (hasMatched) {
                            LOGGER.debug(
                                    "Item '{}' has matched state dimension '{}'",
                                    rule,
                                    context.get(STATE)
                            );
                        }
                        return hasMatched;
                    }
                    return items.size() == 1;
                }).collect(Collectors.toList());
    }
}