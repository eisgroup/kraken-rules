/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import { DataContext } from "../DataContext";

/**
 * Extracts ContextDefinition instances from  provided DataContext.
 */
export interface ContextDataExtractor {
    /**
     * Founds {@link ContextDefinition} instances by provided name in
     * parameters. If restriction node in parameters is defined, then path to
     * instances is restricted by tree mode.
     * If model tree is: (* - multiple cardinality)
     * <pre>
     * A
     *      B
     *          C
     *          D
     *      E*
     *          D*
     * </pre>
     * {@link ContextDefinition} name is D, restriction node is instance of E,
     * then extractor will search instances in
     * <pre>
     * A -> E -> D
     * </pre>
     * Filter by instance of E from parameters to be in path
     * and flatten it in on {@link List}
     * @param childContextName   {@link ContextDefinition} to extract by this name
     * @param root              root {@link DataContext} to extract from
     * @param restriction       {@link DataContext} to be part in the path
     *                          (e.g. Paths: Policy - Insured1 - RiskItem1, Policy - Insured2 - RiskItem2)
     *                          if Restriction is Insured2, and childContextName RiskItem,
     *                          this function will return only
     *                          RiskItem2 {@link DataContext}
     * @returns {DataContext[]} {@link DataContext}s by provided parameters.
     */
    extractByPath(root: DataContext, path: string[]): DataContext[];

    /**
     * Founds ContextDefinition instances by provided path in parameters.
     * If tree is A -> B* -> C* (* - multiple cardinality)
     * Root is instance of A, path is [A, B, C],  then all
     * ContextDefinition instances of C will be found and flattened to
     * one list.
     *
     * @param root  DataContext that is root of the tree
     * @param path  Path to ContextDefinition instance, represented by strings
     * @return      DataContexts, that are found by path.
     */
    extractByName(childContextName: string, root: DataContext, restriction?: DataContext): DataContext[];
}
