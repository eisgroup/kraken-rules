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

import java.util.Collection;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and holds instance of {@link DimensionSetResolver} if registered in the application.
 * <p/>
 * If no {@link DimensionSetResolver} is registered, then uses {@link DefaultDimensionSetResolver}.
 * <p/>
 * Uses ServiceLoader to load instance of {@link DimensionSetResolver}.
 * Throws error if more than one implementation is registered.
 *
 * @author kjuraityte
 * @since 1.40.0
 */
public class DimensionSetResolverHolder {

    private static final Logger logger = LoggerFactory.getLogger(DimensionSetResolverHolder.class);

    private static final DimensionSetResolver RESOLVER;

    static {
        Collection<DimensionSetResolver> resolvers = ServiceLoader.load(DimensionSetResolver.class).stream()
            .map(Provider::get).collect(Collectors.toList());

        if (resolvers.size() > 1) {
            String template = "Error - more than one implementation of %s is available in the application: %s";
            String implementationsString = resolvers.stream()
                .map(r -> r.getClass().getName())
                .collect(Collectors.joining(", "));

            String message = String.format(template, DimensionSetResolver.class.getSimpleName(), implementationsString);
            throw new IllegalStateException(message);
        }

        RESOLVER = resolvers.size() >= 1 ? resolvers.iterator().next() : new DefaultDimensionSetResolver();

        logger.trace("Loaded instance of DimensionSetResolver: " + RESOLVER.getClass().getName());
    }

    /**
     *
     * @return instance of {@link DimensionSetResolver} registered in the application or default implementation
     */
    public static DimensionSetResolver getInstance() {
        return RESOLVER;
    }
}
