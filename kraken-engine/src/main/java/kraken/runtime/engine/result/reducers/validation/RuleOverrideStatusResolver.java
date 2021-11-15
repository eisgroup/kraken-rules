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
package kraken.runtime.engine.result.reducers.validation;

import kraken.annotations.SPI;
import kraken.runtime.engine.dto.OverridableRuleContextInfo;
import kraken.runtime.engine.dto.RuleInfo;

/**
 * SPI that should know what rules are overridden on specific context and effectiveness date
 */
@SPI
@FunctionalInterface
public interface RuleOverrideStatusResolver {

    /**
     * Method supplied to {@link kraken.runtime.engine.result.reducers.validation.ValidationStatusReducer}
     * to filter out overridden rule
     */
    boolean isRuleOverridden(RuleInfo ruleInfo, OverridableRuleContextInfo ruleContextInfo);
}
