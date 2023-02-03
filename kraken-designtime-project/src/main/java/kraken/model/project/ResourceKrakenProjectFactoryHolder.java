/*
 *  Copyright 2021 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project;

import java.util.Collection;

import kraken.annotations.API;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.resource.Resource;

/**
 * API for retrieving {@code KrakenProjectFactory} instances.
 *
 * @author Tomas Dapkunas
 * @since 1.6.0
 */
@API
public final class ResourceKrakenProjectFactoryHolder {

    private static class ResourceKrakenProjectFactoryHolderSingleton {
        private static final ResourceKrakenProjectFactoryHolder INSTANCE = new ResourceKrakenProjectFactoryHolder();
    }

    public static ResourceKrakenProjectFactoryHolder getInstance() {
        return ResourceKrakenProjectFactoryHolderSingleton.INSTANCE;
    }

    private ResourceKrakenProjectFactoryHolder() {
    }

    /**
     * Creates and returns an instance of {@code KrakenProjectFactory} which has all the resources
     * available in classpath. Note that calling this method results in resources being read and
     * parsed each time.
     *
     * @return An instance of {@code KrakenProjectFactory}.
     */
    public KrakenProjectFactory createKrakenProjectFactory() {
        return createKrakenProjectFactory(new DSLReader().read(""));
    }

    /**
     * Creates and returns an instance of {@code KrakenProjectFactory} with given resources.
     *
     * @param resources A collection of resources.
     * @return An instance of {@code KrakenProjectFactory}.
     */
    public KrakenProjectFactory createKrakenProjectFactory(Collection<Resource> resources) {
        return new DefaultKrakenProjectFactory(
            new ResourceKrakenProjectBuilder(resources),
            new KrakenProjectValidationService()
        );
    }

}
