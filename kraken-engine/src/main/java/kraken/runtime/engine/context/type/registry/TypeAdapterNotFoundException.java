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

/**
 * Exception to indicate that {@link kraken.runtime.engine.context.type.TypeAdapter} was not found
 *
 * @author psurinin
 * @since 1.0
 */
class TypeAdapterNotFoundException extends RuntimeException {
    TypeAdapterNotFoundException(Class clazz) {
        super(String.format("TypeAdapter not found for class: %s", clazz.getName()));
    }
}
