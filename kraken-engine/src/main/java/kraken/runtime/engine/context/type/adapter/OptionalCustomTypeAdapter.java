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

import kraken.runtime.engine.context.type.ContextTypeAdapter;

import java.util.Optional;

/**
 * Default implementation for extracted object resolution with type {@link Optional}
 *
 * @author psurinin
 * @since 1.0
 */
public class OptionalCustomTypeAdapter implements ContextTypeAdapter {

    @Override
    public Object getValue(Object object) {
        return ((Optional) object).orElse(null);
    }

    @Override
    public boolean isApplicable(Object object) {
        return object instanceof Optional;
    }
}
