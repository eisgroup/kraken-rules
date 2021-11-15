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
package kraken.runtime.engine.context.type.adapter;

import kraken.runtime.engine.context.info.iterators.ContextInstanceIterator;
import kraken.runtime.engine.context.info.iterators.ContextInstanceMapIterator;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;

import java.util.Map;

/**
 * Default adapter that provides iterator for any instance of {@link Map}
 *
 * @author psurinin
 * @since 1.0
 */
public class DefaultMapTypeAdapter implements IterableContextTypeAdapter {

    @Override
    public ContextInstanceIterator createIterator(Object object) {
        return new ContextInstanceMapIterator(((Map) object));
    }

    @Override
    public boolean isApplicable(Object object) {
        return object instanceof Map;
    }

}
