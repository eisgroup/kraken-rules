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

import java.net.URI;
import java.util.Map;

/**
 * Metadata for the {@link MetadataAware} Kraken model item . Metadata models information about Kraken model
 * item which does not participate directly logic, but can be used by engine and/or infrastructure
 * to change behavior.
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface Metadata {

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
     * Sets the URI of resource from which the {@code KrakenModelItem} was parsed.
     *
     * @param uri Kraken model item resource URI.
     */
    void setUri(URI uri);

    /**
     * Returns the URI of resource from which the {@code KrakenModelItem} was parsed.
     *
     * @return Kraken model item resource URI.
     */
    URI getUri();

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
