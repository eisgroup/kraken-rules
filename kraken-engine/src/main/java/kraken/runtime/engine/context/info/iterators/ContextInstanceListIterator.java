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

import java.util.List;

/**
 * Implementation of {@link ContextInstanceIterator} for {@link List}, used {@link Integer}
 * as index type
 *
 * @author rimas
 * @since 1.0
 */
public class ContextInstanceListIterator<K> implements ContextInstanceIterator<Integer> {

    private final List<K> list;

    private int index;

    public ContextInstanceListIterator(List<K> list) {
        this.list = list;
        this.index = -1;
    }

    @Override
    public boolean hasNext() {
        return list.size() > index + 1;
    }

    @Override
    public K next() {
        return list.get(++index);
    }

    @Override
    public Integer key() {
        if (index >= 0) {
            return index;
        } else {
            throw new IllegalStateException("Use next() first");
        }
    }

    @Override
    public Object getValue(Object index) {
        return list.get(((Integer) index));
    }
}
