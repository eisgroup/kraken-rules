/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import { Contexts } from "kraken-model";
import ContextDefinition = Contexts.ContextDefinition;

import { ContextInstanceInfo } from "./ContextInstanceInfo";
import { DataErrorDefinition } from "./DataObjectInfoResolver";

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
 * to application-specific format, which can be used in rule payloadResult reducers by invoking
 * application
 */
export interface ContextInstanceInfoResolver<T> {

    /**
     * Is invoked when dataObject is passed as root and root data context
     * is created. Resolves {@link ContextInstanceInfo} for root context.
     *
     * @param dataObject    root context data object instance
     * @return              context instance info metadata for root
     */
    resolveRootInfo: (dataObject: object) => ContextInstanceInfo;

    /**
     * Is invoked when target context instance is extracted from source context using
     * context navigation expressions.
     *
     * @param dataObject    extracted data object instance
     * @param target        context definition of extracted context
     * @param source        context definition of source (container) context
     * @param parentInfo    context instance info metadata for source context instance
     * @param index         returned if extraction yields collection like structure, null for fields with
     *                      single cardinality
     * @return              context instance info metadata for extracted data object
     */
    resolveExtractedInfo: (
        dataObject: object,
        target: ContextDefinition,
        source: ContextDefinition,
        parentInfo: ContextInstanceInfo,
        index?: number
    ) => ContextInstanceInfo;

    /**
     * Is invoked when child context is generalized and returned as parent context
     * to support inheritance of logic.
     *
     * @param dataObject    context instance
     * @param ancestor      parent context instance definition
     * @param child         child context instance definition
     * @param childInfo     child context instance information
     * @return              context instance info metadata for parent data object
     */
    resolveAncestorInfo: (
        dataObject: object,
        ancestor: ContextDefinition,
        child: ContextDefinition,
        childInfo: ContextInstanceInfo
    ) => ContextInstanceInfo;

    /**
     * Called to transform {@link ContextInstanceInfo} to SPI implementation specific form - T.
     * This can be further interpreted by invoking client application
     *
     * @param contextInstanceInfo       context instance info for particular context
     * @param dataObject                data object instance for this context
     * @return                          implementation specific payloadResult
     */
    processContextInstanceInfo: (contextInstanceInfo: ContextInstanceInfo, dataObject: object) => T;

    /**
     * Validates if type of passed context data object is supported by this SPI implementation.
     *
     * @param contextDataObject data object instance for extracted context
     * @return                  an array of errors
     */
    validateContextDataObject: (contextDataObject: object) => DataErrorDefinition[];

}
