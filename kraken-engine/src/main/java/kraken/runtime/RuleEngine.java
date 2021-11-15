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
package kraken.runtime;

import kraken.annotations.API;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.result.reducers.EntryPointResultReducer;

/**
 * Main interface to core rule engine implementation - provides functionality to evaluate rules
 * from rules model on specified data object instance.<br>
 *
 * Rules to be evaluated are specified using entryPointName - corresponding entry point will be
 * loaded up, and rules to be evaluated resolved from rule names specified in entry point.
 *
 * First parameter data specifies and object to use as root context instance for evaluation.
 * Engine will try to match it to one of the context definitions in the rules model, and then
 * use relationships between context definitions to navigate data structure.
 *
 * Rule results are returned in raw as detailed result tree, represented by {@link EntryPointResult}.
 * It contrains result object for each field and rule evaluated.
 *
 * To process these raw rule results, one of {@link EntryPointResultReducer}s should be used.
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface RuleEngine {

    /**
     * Evaluate rules from entry point with entryPointName on context instance represented by data object
     *
     * @param data              root context instance data object
     * @param entryPointName    entry point name
     * @return                  raw rule results
     */
    EntryPointResult evaluate(Object data, String entryPointName);

    /**
     * Evaluate rules from entry point with entryPointName on subtree of context instance represented by node
     *
     * @param data              root context instance data object
     * @param node              node that indicates instance subtree to validate; only this instance and descendants will be validated
     * @param entryPointName    entry point name
     * @return                  raw rule results
     */
    EntryPointResult evaluateSubtree(Object data, Object node, String entryPointName);

    /**
     * Evaluate rules from entry point with entryPointName on context instance represented by data object.
     *
     * @param data              root context instance data object
     * @param entryPointName    entry point name
     * @param evaluationConfig  contains configuration of evaluation
     * @return                  raw rule results
     */
    EntryPointResult evaluate(Object data, String entryPointName, EvaluationConfig evaluationConfig);


    /**
     * Evaluate rules from entry point with entryPointName on subtree of context instance represented by node.
     *
     * @param data              root context instance data object
     * @param node              node that indicates instance subtree to validate; only this instance and descendants will be validated
     * @param entryPointName    entry point name
     * @param evaluationConfig  contains configuration of evaluation
     * @return                  raw rule results
     */
    EntryPointResult evaluateSubtree(Object data, Object node, String entryPointName, EvaluationConfig evaluationConfig);
}
