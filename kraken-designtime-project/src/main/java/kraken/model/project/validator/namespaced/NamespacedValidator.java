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
package kraken.model.project.validator.namespaced;

import static kraken.model.project.validator.ValidationMessageBuilder.Message.NAMESPACED_NAME_HAS_FORBIDDEN_SYMBOLS;

import java.util.ArrayList;
import java.util.List;

import kraken.model.KrakenModelItem;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.namespace.Namespaced;

/**
 * @author mulevicius
 */
public class NamespacedValidator {

    public static List<ValidationMessage> validate(KrakenModelItem namespaced) {
        List<ValidationMessage> validationMessages = new ArrayList<>();
        if(namespaced.getName() != null && namespaced.getName().contains(Namespaced.SEPARATOR)) {
            var m = ValidationMessageBuilder.create(NAMESPACED_NAME_HAS_FORBIDDEN_SYMBOLS, namespaced).build();
            validationMessages.add(m);
        }
        return validationMessages;
    }
}
