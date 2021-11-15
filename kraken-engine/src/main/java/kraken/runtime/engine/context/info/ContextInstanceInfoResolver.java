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
package kraken.runtime.engine.context.info;

import kraken.runtime.model.context.RuntimeContextDefinition;

import java.util.Collection;

/**
 * Provides SPI for context instance information resolution. This interface is
 * called by Kraken rule engine during context extraction for each data context instance
 * extracted or constructed. Resolved metadata information is returned as instance of
 * {@link ContextInstanceInfo}<br><br>
 *
 * Provides three different info resolution methods, which are called depending on how
 * particular context instance data object was extracted.<br><br>
 *
 * Also provides method to process instance info stored in {@link ContextInstanceInfo}
 * to application-specific format, which can be used in rule result reducers by invoking
 * application
 *
 * @author rimas
 * @since 1.0
 */
public interface ContextInstanceInfoResolver<T> {

    /**
     * Is invoked when dataObject is passed as root and root data context
     * is created. Resolves {@link ContextInstanceInfo} for root context.
     *
     * @param dataObject    root context data object instance
     * @return              context instance info metadata for root
     */
    ContextInstanceInfo resolveRootInfo(Object dataObject);

    /**
     * Is invoked when target context instance is extracted from source context using
     * context navigation expressions.
     *
     * @param dataObject        extracted data object instance by {@link kraken.model.Expression}
     * @param target            context definition of extracted context
     * @param source            context definition of source (container) context
     * @param parentInfo        context instance info metadata for source context instance
     *
     * @return                  context instance info metadata for extracted data object
     */
    ContextInstanceInfo resolveExtractedInfo(Object dataObject,
                                             RuntimeContextDefinition target,
                                             RuntimeContextDefinition source,
                                             ContextInstanceInfo parentInfo);

    /**
     * Is invoked when child context is generalized and returned as partent context
     * to support inheritance of logic.
     *
     * @param dataObject    context instance
     * @param ancestor      parent context instance definition
     * @param child         child context instance definition
     * @param childInfo     child context instance information
     * @return              context instance info metadata for parent data object
     */
    ContextInstanceInfo resolveAncestorInfo(Object dataObject,
                                            RuntimeContextDefinition ancestor,
                                            RuntimeContextDefinition child,
                                            ContextInstanceInfo childInfo);

    /**
     * Called to transform {@link ContextInstanceInfo} to SPI implementation specific form - T.
     * This can be further interpreted by invoking client application
     *
     * @param contextInstanceInfo       context instance info for particular context
     * @param dataObject                data object instance for this context
     * @return                          implementation specific result
     */
    T processContextInstanceInfo(ContextInstanceInfo contextInstanceInfo, Object dataObject);

    /**
     * Validates if type of passed context data object is supported by this SPI implementation.
     *
     * @param contextDataObject data object instance for extracted context
     * @return                  collection of error messages
     */
    Collection<DataErrorDefinition> validateContextDataObject(Object contextDataObject);

    /**
     * @param data
     * @return ContextDefinition name of object
     */
    String resolveContextNameForObject(Object data);
}
