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
package kraken.runtime.engine.evaluation.loop;

import kraken.model.entrypoint.EntryPoint;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.context.ContextDataProvider;
import kraken.runtime.engine.core.EntryPointEvaluation;

/**
 * Part of core rule engine which performs evaluation of the rules from provided {@link EntryPoint}
 * Different implementations may use different strategies to order, select and evaluate appropriate
 * rules
 *
 * @author rimas
 * @since 1.0
 */
public interface EvaluationLoop {

    /**
     * Main evaluation method, evaluates all rule logic in provided entry point in context of provided
     * data context object and returns result tree
     *
     * @param entryPointEvaluation            entry point data from bundle
     * @param contextDataProvider             context provider for root object to be evaluated
     * @param session
     * @return                                rule results
     */
    EntryPointResult evaluate(
            EntryPointEvaluation entryPointEvaluation,
            ContextDataProvider contextDataProvider,
            EvaluationSession session
    );
}
