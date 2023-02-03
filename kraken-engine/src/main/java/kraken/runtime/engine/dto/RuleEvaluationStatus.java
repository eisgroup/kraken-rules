/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.dto;

import kraken.annotations.API;

/**
 * Indicates a status of rule evaluation on a particular context instance.
 * Statuses are mutually exclusive and every rule evaluation will have a status.
 * <p/>
 * This partitions each rule evaluation into distinct and well-defined category and allows investigating
 * how each rule was evaluated.
 *
 * @author mulevicius
 * @since 1.40.0
 */
@API
public enum RuleEvaluationStatus {

    /**
     * Indicates that the rule was not used during this evaluation.
     *
     * <p/>
     * This could happen when target context instance does not exist so there is no target to apply the rule on,
     * or when the rule with higher priority was applied.
     */
    UNUSED,

    /**
     * Indicates that the rule was skipped.
     * Rule is skipped when rule condition is not satisfied.
     * If rule is skipped then rule payload is not applied.
     *
     * <p/>
     * The difference between skipped and ignored statuses is in the evaluation of the rule condition.
     * If the rule condition can be evaluated without errors, but it is not satisfied then the rule is skipped.
     * However, if rule condition is evaluated with errors then the rule is ignored.
     */
    SKIPPED,

    /**
     * Indicates that the rule was ignored.
     * Rule is ignored if any expression evaluates with an error.
     * If rule is ignored then rule payload is not applied.
     *
     * <p/>
     * The difference between skipped and ignored statuses is in the evaluation of the rule condition.
     * If the rule condition can be evaluated without errors, but it is not satisfied then the rule is skipped.
     * However, if rule condition is evaluated with errors then the rule is ignored.
     */
    IGNORED,

    /**
     * Indicates that the rule was successfully and fully evaluated without errors and rule payload was applied.
     */
    APPLIED
}
