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
package kraken.model;

import kraken.annotations.API;

import java.util.Map;

/**
 * Metadata for the {@link Rule}. Metadata models information about the rule which
 * does not participate directly in rule evaluation logic, but can be used by engine
 * and/or infrastructure to change behavior
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface Metadata {

    /**
     * @see Metadata#asMap()
     * @since 1.0.41
     * @deprecated use {@link Metadata#asMap()} from {@link Rule#getMetadata()} to check if rule has dimensions added
     */
    @Deprecated(since = "1.16.0", forRemoval = true)
    String DIMENSIONAL_LABEL = "__dimensional__";

    /**
     * @see Metadata#asMap()
     * @since 1.0.41
     * @deprecated use {@link Metadata#asMap()} from {@link Rule#getMetadata()} to check if rule has dimensions added
     */
    @Deprecated(since = "1.16.0", forRemoval = true)
    String DIMENSIONAL_VALUE_ENABLED = "true";

    /**
     * Returns type of Payload, because several interfaces implementing this interface. According to other languages
     * limitations, this type will help to define what type of object this payload is.
     * Example: ChildInterface extends ParentInterface, so implementing ChildInterface class in
     * this getter must return ChildInterface#class#getSimpleName()
     * @return  interface name
     */
    String getType();

    /**
     * Gets the value of a property. If property does not exist then null will be returned.
     *
     * @param propertyName the name of a property
     * @return the value of a property
     */
    Object getProperty(String propertyName);

    /**
     * Sets the value of a property. If property already exists then it will be overwritten with new value.
     *
     * @param propertyName the name of a property
     * @param propertyValue the value of a property
     */
    void setProperty(String propertyName, Object propertyValue);

    /**
     * Indicates if property with such name exists
     *
     * @param propertyName
     * @return
     */
    boolean hasProperty(String propertyName);

    /**
     * Contains metadata for the element. Metadata can be dimension filter inputs.
     *
     * @return all metadata as immutable map
     */
    Map<String, Object> asMap();
}
