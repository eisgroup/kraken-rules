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
package kraken.runtime.engine.context.type.registry;

import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.runtime.engine.context.type.adapter.DefaultMapTypeAdapter;
import kraken.runtime.engine.context.type.adapter.DefaultListTypeAdapter;
import kraken.runtime.engine.context.type.adapter.OptionalCustomTypeAdapter;

import java.util.ArrayList;

/**
 * Class that is builder for {@link TypeRegistry}. Will add in order {@link IterableContextTypeAdapter}s
 * and at the end will add predifined
 * @see DefaultListTypeAdapter
 * @see DefaultMapTypeAdapter
 * Will add {@link OptionalCustomTypeAdapter} as predifined to {@link ContextTypeAdapter}s
 *
 * @author psurinin
 * @since 1.0
 */
public final class TypeRegistryBuilder {
    private final ArrayList<ContextTypeAdapter> customTypeAdapters;
    private final ArrayList<IterableContextTypeAdapter> iterableTypeAdapters;

    TypeRegistryBuilder() {
        this.customTypeAdapters = new ArrayList<>();
        this.iterableTypeAdapters = new ArrayList<>();
    }

    public TypeRegistryBuilder addIterableTypeAdapter(IterableContextTypeAdapter adapter) {
        iterableTypeAdapters.add(adapter);
        return this;
    }

    public TypeRegistryBuilder addCustomTypeAdapter(ContextTypeAdapter adapter) {
        customTypeAdapters.add(adapter);
        return this;
    }

    public TypeRegistry build() {
        this.addCustomTypeAdapter(new OptionalCustomTypeAdapter())
                .addIterableTypeAdapter(new DefaultListTypeAdapter())
                .addIterableTypeAdapter(new DefaultMapTypeAdapter());
        return new TypeRegistry(customTypeAdapters, iterableTypeAdapters);
    }
}
