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
import kraken.model.project.KrakenProject;

/**
 * Validates {@link KrakenProject} contents.
 * Validator may be invoked on application startup or by Kraken Validation Maven plugin at compile time.
 * To validate Rule definitions provided by DynamicRuleRepository at runtime also implement {@link DynamicRuleValidator}
 * <p/>
 * Implementation of {@link KrakenProjectValidator} must be registered in the system by following {@link java.util.ServiceLoader}
 * <p/>
 * Note, that this is NOT an API.
 *
 * @author mulevicius
 */
public interface KrakenProjectValidator {

    /**
     * @param krakenProject to be validated
     * @return a list of validation messages
     */
    List<ValidationMessage> validate(KrakenProject krakenProject);

}
