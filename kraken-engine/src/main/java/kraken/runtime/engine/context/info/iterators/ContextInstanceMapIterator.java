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
package kraken.runtime.engine.context.info.iterators;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Implementation of {@link ContextInstanceIterator} for {@link Map}
 *
 * @author rimas
 * @since 1.0
 */
public class ContextInstanceMapIterator<T> implements ContextInstanceIterator<T> {

    private final Iterator<Entry<T, Object>> entryIterator;
    private final Map<T, Object> map;

    private T lastKey;

    public ContextInstanceMapIterator(Map<T, Object> map) {
        this.entryIterator = map.entrySet().iterator();
        this.map = map;
    }

    @Override
    public boolean hasNext() {
        return entryIterator.hasNext();
    }

    @Override
    public Object next() {
        Entry<T, Object> entry = entryIterator.next();
        lastKey = entry.getKey();
        return entry.getValue();
    }

    @Override
    public T key() {
        if (lastKey != null) {
            return lastKey;
        } else {
            throw new IllegalStateException("Use next() first");
        }
    }

    @Override
    public Object getValue(Object index) {
        return map.get(index);
    }
}
