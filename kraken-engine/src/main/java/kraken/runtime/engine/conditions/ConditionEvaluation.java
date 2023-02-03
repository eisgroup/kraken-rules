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
package kraken.runtime.engine.conditions;

import kraken.annotations.API;
import kraken.model.Rule;

/**
 * Enum that indicates evaluation of {@link Rule#getCondition()}.
 * If error occurred during evaluation in {@link RuleApplicabilityEvaluator} and
 * {@link ConditionEvaluation#ERROR} will be applied {@link Rule}
 * will not be executed.
 *
 * @author psurinin
 */
@API
public enum ConditionEvaluation {

    APPLICABLE("Applicable"),
    NOT_APPLICABLE("Not Applicable"),
    ERROR("Error");

    private final String name;

    ConditionEvaluation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
