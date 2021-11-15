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

import kraken.runtime.engine.context.extraction.instance.ContextExtractionResultBuilder;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Registry that can provide {@link kraken.runtime.engine.context.type.TypeAdapter}
 * and check, does extracted object has {@link kraken.runtime.engine.context.type.TypeAdapter}.
 * Is used in {@link ContextExtractionResultBuilder}
 * To avoid invalid behavour can be built only with {@link TypeRegistry#builder()}
 *
 * @author psurinin
 * @since 1.0
 */
public final class TypeRegistry {
    private final ArrayList<ContextTypeAdapter> customTypeAdapters;
    private final ArrayList<IterableContextTypeAdapter> iterableTypeAdapters;

    public static TypeRegistryBuilder builder() {
        return new TypeRegistryBuilder();
    }

    TypeRegistry(
            List<ContextTypeAdapter> customTypeAdapters,
            List<IterableContextTypeAdapter> iterableTypeAdapters) {
        this.customTypeAdapters = new ArrayList<>(customTypeAdapters);
        this.iterableTypeAdapters = new ArrayList<>(iterableTypeAdapters);
    }

    public boolean isCustomType(Object object) {
        return customTypeAdapters.stream().anyMatch(a -> a.isApplicable(object));
    }

    public boolean isIterableType(Object object) {
        return iterableTypeAdapters.stream().anyMatch(a -> a.isApplicable(object));
    }

    public ContextTypeAdapter getCustomTypeAdapter(Object object) {
        return customTypeAdapters.stream()
                .filter(adapter -> adapter.isApplicable(object))
                .findFirst()
                .orElseThrow(() -> new TypeAdapterNotFoundException(object.getClass()));
    }

    public ContextTypeAdapter getCustomTypeAdapter(Class<? extends ContextTypeAdapter> clazz) {
        return customTypeAdapters.stream()
                .filter(adapter -> adapter.getClass().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new TypeAdapterNotFoundException(clazz));
    }

    public IterableContextTypeAdapter getIterableTypeAdapter(Object object) {
        return iterableTypeAdapters.stream()
                .filter(adapter -> adapter.isApplicable(object))
                .findFirst()
                .orElseThrow(() -> new TypeAdapterNotFoundException(object.getClass()));
    }


    public IterableContextTypeAdapter getIterableTypeAdapter(Class<? extends IterableContextTypeAdapter> clazz) {
        return iterableTypeAdapters.stream()
                .filter(adapter -> adapter.getClass().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new TypeAdapterNotFoundException(clazz));
    }

}
