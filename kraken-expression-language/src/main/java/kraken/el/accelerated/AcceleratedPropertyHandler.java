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
package kraken.el.accelerated;

/**
 * Simple property handler. Will be invoked for properties
 * This handler will be use for both simple expression handling and also registered for complex expressions
 *
 * @author mulevicius
 */
public interface AcceleratedPropertyHandler<T> {

    /**
     * @param property of object
     * @param object to extract value from
     * @return value extracted by property from object
     */
    Object get(String property, T object);

    /**
     * @param property of object
     * @param object to set value
     * @param value that will be set to object
     * @return value that was set
     */
    Object set(String property, T object, Object value);

    /**
     * @return type that this handler is applicable to; this handler will also be called for subtypes as well
     */
    Class<T> getType();
}

