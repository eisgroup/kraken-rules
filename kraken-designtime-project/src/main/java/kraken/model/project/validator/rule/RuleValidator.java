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
package kraken.model.project.validator.rule;

import kraken.model.Rule;
import kraken.model.project.validator.ValidationSession;

/**
 * Validates Kraken Rule.
 * Can control if validation should be invoked.
 * Useful when validator depends on some fields of the Rule existing.
 *
 * @author mulevicius
 */
public interface RuleValidator {

    /**
     * Validates Rule and pushes validation messages to session
     *
     * @param rule
     * @param session
     */
    void validate(Rule rule, ValidationSession session);

    /**
     *
     * @param rule
     * @return true if {@link #validate(Rule, ValidationSession)} should be invoked for this Rule
     */
    boolean canValidate(Rule rule);
}
