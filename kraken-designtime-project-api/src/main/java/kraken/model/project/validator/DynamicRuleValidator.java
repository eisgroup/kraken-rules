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
package kraken.model.project.validator;

import java.util.List;

import kraken.annotations.SPI;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;

/**
 * A type of {@link KrakenProjectValidator} which can also validate {@link Rule} resolved from DynamicRuleRepository at runtime.
 * If any {@link ValidationMessage} has {@link Severity#ERROR} then error will be thrown at runtime.
 * <p/>
 * Note, that instance of this must also implement {@link KrakenProjectValidator}
 * and be registered as a {@link KrakenProjectValidator}.
 *
 * @author mulevicius
 * @deprecated Custom dynamic Rule validation is no longer supported, because such validators cannot be run in
 * tooling environment.
 */
@SPI
@Deprecated(since = "1.22.0")
public interface DynamicRuleValidator {

    /**
     *
     * @param dynamicRule that was provided by DynamicRuleRepository
     * @param krakenProject scope of validation for the dynamically loaded rule
     * @return a list of validation messages
     */
    List<ValidationMessage> validate(Rule dynamicRule, KrakenProject krakenProject);
}
