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
package kraken.model.project.validator.entrypoint;

import static kraken.model.project.validator.Severity.ERROR;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_NAME_IS_NULL;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.namespaced.NamespacedValidator;

/**
 * @author mulevicius
 */
public class EntryPointDefinitionValidator {

    private final EntryPointIncludesValidator entryPointIncludesValidator;
    private final EntryPointRuleExistenceValidator entryPointRuleExistenceValidator;
    private final EntryPointServerSideOnlyValidator entryPointServerSideOnlyValidator;
    private final EntryPointDimensionsValidator entryPointDimensionsValidator;

    public EntryPointDefinitionValidator(KrakenProject krakenProject) {
        this.entryPointIncludesValidator = new EntryPointIncludesValidator(krakenProject);
        this.entryPointRuleExistenceValidator = new EntryPointRuleExistenceValidator(krakenProject);
        this.entryPointServerSideOnlyValidator = new EntryPointServerSideOnlyValidator(krakenProject);
        this.entryPointDimensionsValidator = new EntryPointDimensionsValidator(krakenProject);
    }

    public void validate(EntryPoint entryPoint, ValidationSession session) {
        ValidationSession epValidationSession = new ValidationSession();
        if(entryPoint.getName() == null) {
            var m = ValidationMessageBuilder.create(ENTRYPOINT_NAME_IS_NULL, entryPoint).build();
            epValidationSession.add(m);
        }
        epValidationSession.addAll(NamespacedValidator.validate(entryPoint));
        if(!epValidationSession.hasEntryPointError()) {
            entryPointIncludesValidator.validate(entryPoint, epValidationSession);
            entryPointRuleExistenceValidator.validate(entryPoint, epValidationSession);
            entryPointServerSideOnlyValidator.validate(entryPoint, epValidationSession);
            entryPointDimensionsValidator.validate(entryPoint, epValidationSession);
        }

        session.addAll(epValidationSession.getValidationMessages());
    }

}
