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
package kraken.runtime.expressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import kraken.el.TypeProvider;
import kraken.runtime.engine.context.info.ContextInstanceInfoResolver;
import kraken.runtime.repository.RuntimeContextRepository;

/**
 * Provides Kraken specific types for Kraken Expression Language.
 * Types and inheritance is based on Kraken {@link kraken.model.context.ContextDefinition} domain
 *
 * @author mulevicius
 */
public class KrakenTypeProvider implements TypeProvider {

    private ContextInstanceInfoResolver contextInstanceInfoResolver;

    private RuntimeContextRepository contextRepository;

    public KrakenTypeProvider(ContextInstanceInfoResolver contextInstanceInfoResolver, RuntimeContextRepository contextRepository) {
        this.contextRepository = contextRepository;
        this.contextInstanceInfoResolver = contextInstanceInfoResolver;
    }

    @Override
    public String getTypeOf(Object object) {
        if(!contextInstanceInfoResolver.validateContextDataObject(object).isEmpty()) {
            return null;
        }
        return contextInstanceInfoResolver.resolveContextNameForObject(object);
    }

    @Override
    public Collection<String> getInheritedTypesOf(Object object) {
        if(!contextInstanceInfoResolver.validateContextDataObject(object).isEmpty()) {
            return Collections.emptyList();
        }
        String name = contextInstanceInfoResolver.resolveContextNameForObject(object);
        Collection<String> subtypes = contextRepository.getContextDefinition(name).getInheritedContexts();
        Collection<String> inheritedTypes = new ArrayList<>();
        inheritedTypes.addAll(subtypes);
        inheritedTypes.add(name);
        return inheritedTypes;
    }

}
