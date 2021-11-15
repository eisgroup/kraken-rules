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
package kraken.runtime.engine.context.type;

/**
 * Parent interface of type adapters.
 *
 * @see ContextTypeAdapter
 * @see IterableContextTypeAdapter
 * @author psurinin
 * @since 1.0
 */
public interface TypeAdapter {
    /**
     * Check is adapter can be used for this object
     * @param object    extracted object
     * @return          is this type adapter can be used for that object
     */
    boolean isApplicable(Object object);
}
