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
package kraken.runtime.engine.context.data;

import java.util.Collection;
import java.util.Objects;

import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;

/**
 * Represents a reference from {@link DataContext} to other {@link DataContext} by name.
 * This could an external data context or a self data context.
 *
 * @author mulevicius
 */
public class DataReference {

    private String name;

    private Collection<DataContext> dataContexts;

    private Cardinality cardinality;

    public DataReference(String name, Collection<DataContext> dataContexts, Cardinality cardinality) {
        this.name = Objects.requireNonNull(name);
        this.dataContexts = Objects.requireNonNull(dataContexts);
        this.cardinality = Objects.requireNonNull(cardinality);
    }

    /**
     *
     * @return name of reference, should match {@link ContextDefinition#getName()} as written in rule expression
     */
    public String getName() {
        return name;
    }

    /**
     * @return a collection of referred instances;
     * if cardinality of reference is {@link Cardinality#SINGLE} then collection will contain single element;
     * if there is no references then collection wil be empty
     */
    public Collection<DataContext> getDataContexts() {
        return dataContexts;
    }

    /**
     * @return returns single element or null;
     * useful when it is known that {@link #getCardinality()} is {@link Cardinality#SINGLE}
     */
    public DataContext getDataContext() {
        if(dataContexts.size() > 1) {
            throw new IllegalStateException("Multiple DataContext references exists but single reference was expected!");
        }
        if(dataContexts.size() == 1) {
            return dataContexts.iterator().next();
        }
        return null;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
}
