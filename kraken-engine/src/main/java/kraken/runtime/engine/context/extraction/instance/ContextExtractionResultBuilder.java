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
package kraken.runtime.engine.context.extraction.instance;

import kraken.runtime.engine.context.info.iterators.ContextInstanceIterator;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.runtime.engine.context.type.registry.TypeRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Builds {@link ContextExtractionResult}s from extracted object
 * Object can be transformed with {@link ContextTypeAdapter} or
 * be iterated with {@link ContextInstanceIterator} from
 * {@link IterableContextTypeAdapter}
 *  <br/>
 *  <br/>
 * <img src="doc-files/ContextChildExtraction.png">
 * @author psurinin
 * @since 1.0
 */
public class ContextExtractionResultBuilder {

    private final TypeRegistry registry;

    public ContextExtractionResultBuilder(TypeRegistry registry) {
        this.registry = registry;
    }

    public List<ContextExtractionResult> buildFrom(Object child) {
        return Optional.ofNullable(child)
                .map(this::buildFromNonNull)
                .orElseGet(Collections::emptyList);
    }

    private List<ContextExtractionResult> buildFromNonNull(Object child) {
        final ContextExtractionResult value = getCustomValue(child);
        if(value.getValue() == null) {
            return Collections.emptyList();
        }
        if(registry.isIterableType(value.getValue())) {
            return buildFromObjectToIterate(value);
        }
        return List.of(value);
    }

    private List<ContextExtractionResult> buildFromObjectToIterate(ContextExtractionResult objectToIterate) {
        final IterableContextTypeAdapter iterableTypeAdapter =
                registry.getIterableTypeAdapter(objectToIterate.getValue());
        final ContextInstanceIterator iterator =
                iterableTypeAdapter.createIterator(objectToIterate.getValue());
        final List<ContextExtractionResult> children = new ArrayList<>();
        while (iterator.hasNext()) {
            final Object next = iterator.next();
            if(next == null) {
                continue;
            }
            if(registry.isIterableType(next)) {
                children.addAll(buildFromObjectToIterate(getCustomValue(next)));
            } else {
                ContextExtractionResult contextDataInstance = getCustomValue(next);
                if (contextDataInstance.getValue() != null) {
                    ContextExtractionResult instance = new ContextExtractionResult(
                            contextDataInstance.getValue()
                    );
                    children.add(instance);
                }
            }
        }
        return children;
    }

    private ContextExtractionResult getCustomValue(Object o) {
        if (registry.isCustomType(o)) {
            return new ContextExtractionResult(
                    registry.getCustomTypeAdapter(o).getValue(o)
            );
        }

        return new ContextExtractionResult(o);
    }
}
