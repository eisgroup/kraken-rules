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
package kraken.model.project.validator.entrypoint;

import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_UNKNOWN_RULE;

import java.util.Set;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class EntryPointRuleExistenceValidator {

    private final KrakenProject krakenProject;

    public EntryPointRuleExistenceValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(EntryPoint entryPoint, ValidationSession session) {
        Set<String> availableRules = krakenProject.getRuleVersions().keySet();
        for(String includedRuleName : entryPoint.getRuleNames()) {
            if(!availableRules.contains(includedRuleName)) {
                var m = ValidationMessageBuilder.create(ENTRYPOINT_UNKNOWN_RULE, entryPoint)
                    .parameters(includedRuleName)
                    .build();
                session.add(m);
            }
        }
    }

}
