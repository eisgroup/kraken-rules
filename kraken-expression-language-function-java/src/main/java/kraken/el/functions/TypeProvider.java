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
package kraken.el.functions;

import java.util.Collection;

/**
 * Provides types of objects for Kraken Expression Language.
 *
 * @author mulevicius
 */
public interface TypeProvider {

    /**
     * Key in Kraken Expression Language variable context that will be used to dynamically resolve TypeProvider
     */
    String TYPE_PROVIDER_PROPERTY = "_internalTypeProvider";

    /**
     *
     * @param object that can never be null
     * @return type of object or null if such object is not represented by a type in system
     */
    String getTypeOf(Object object);

    /**
     *
     * @param object that can never be null
     * @return all types of object that it inherits including exact type that it implements,
     * or empty collection if such object is not represented by a type in system
     */
    Collection<String> getInheritedTypesOf(Object object);

}
