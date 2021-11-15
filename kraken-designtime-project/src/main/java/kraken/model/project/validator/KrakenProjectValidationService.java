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
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import kraken.model.Rule;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.exception.IllegalKrakenProjectStateException;
import kraken.model.project.validator.context.ContextDefinitionValidator;
import kraken.model.project.validator.context.ExternalContextValidator;
import kraken.model.project.validator.entrypoint.EntryPointDefinitionValidator;
import kraken.model.project.validator.function.FunctionSignatureValidator;
import kraken.model.project.validator.rule.RuleDefinitionValidator;

/**
 * Executes all registered {@link KrakenProjectValidator} in tiered sequence
 *
 * @author mulevicius
 */
public final class KrakenProjectValidationService {

    private final List<DynamicRuleValidator> additionalDynamicRuleValidators;
    private final List<KrakenProjectValidator> additionalValidators;

    /**
     * Creates an instance of validation service using all native validators and all additional external validators
     * registered as a ServiceLoader
     */
    public KrakenProjectValidationService() {
        this(
            ServiceLoader.load(KrakenProjectValidator.class)
                .stream()
                .map(Provider::get)
                .collect(Collectors.toList())
        );
    }

    /**
     * Creates an instance of validation service using all native validators and all additional validators
     * provided as a parameter. Does not include external validators registered as a ServiceLoader.
     * Such validators must be loaded externally and provided as a parameter if needed.
     *
     * @param additionalValidators
     */
    public KrakenProjectValidationService(List<KrakenProjectValidator> additionalValidators) {
        this.additionalValidators = additionalValidators;
        this.additionalDynamicRuleValidators = additionalValidators.stream()
            .filter(v -> v instanceof DynamicRuleValidator)
            .map(v -> (DynamicRuleValidator) v)
            .collect(Collectors.toList());
    }

    /**
     * Validates full contents of KrakenProject, including rules and entrypoints.
     *
     * @param krakenProject KrakenProject to validate.
     * @return Result of validation.
     * @throws IllegalKrakenProjectStateException if KrakenProject is incorrectly initialized.
     */
    public ValidationResult validate(KrakenProject krakenProject) {
        ValidationSession validationSession = new ValidationSession();

        ensureKrakenProjectConsistency(krakenProject);

        ContextDefinitionValidator contextDefinitionValidator = new ContextDefinitionValidator(krakenProject);
        ExternalContextValidator externalContextValidator = new ExternalContextValidator(krakenProject);

        contextDefinitionValidator.validate(validationSession);
        externalContextValidator.validate(validationSession);
        if(validationSession.hasContextDefinitionError() || validationSession.hasExternalContextError()) {
            return validationSession.result();
        }

        FunctionSignatureValidator functionSignatureValidator = new FunctionSignatureValidator(krakenProject);
        functionSignatureValidator.validate(validationSession);
        if(validationSession.hasFunctionSignatureError()) {
            return validationSession.result();
        }

        ValidationResult result = validateRulesAndEntryPoints(krakenProject);
        validationSession.addAll(result.getValidationMessages());

        return validationSession.result();
    }

    /**
     * Validates only rules and entrypoints of KrakenProject and assumes that other contents are already valid.
     *
     * @param krakenProject KrakenProject to validate.
     * @return Result of rules and entrypoints validation.
     */
    public ValidationResult validateRulesAndEntryPoints(KrakenProject krakenProject) {
        ValidationSession validationSession = new ValidationSession();

        EntryPointDefinitionValidator entryPointDefinitionValidator = new EntryPointDefinitionValidator(krakenProject);
        for(EntryPoint entryPoint : krakenProject.getEntryPoints()) {
            entryPointDefinitionValidator.validate(entryPoint, validationSession);
        }

        RuleDefinitionValidator ruleDefinitionValidator = new RuleDefinitionValidator(krakenProject);
        for(Rule rule : krakenProject.getRules()) {
            ruleDefinitionValidator.validate(rule, validationSession);
        }

        additionalValidators.forEach(validator ->
            validationSession.addAll(validator.validate(krakenProject)));

        return validationSession.result();
    }

    /**
     * Validates dynamically loaded rule in scope of KrakenProject. Assumes that dynamic rule is not in KrakenProject.
     *
     * @param dynamicRule Rule to validate.
     * @param krakenProject KrakenProject determines validation context, such as contexts and functions.
     * @return Result of single dynamic rule validation.
     */
    public ValidationResult validateDynamicRule(Rule dynamicRule, KrakenProject krakenProject) {
        ValidationSession validationSession = new ValidationSession();

        RuleDefinitionValidator ruleDefinitionValidator = new RuleDefinitionValidator(krakenProject);
        ruleDefinitionValidator.validateDynamicRule(dynamicRule, validationSession);
        if(validationSession.hasRuleError()) {
            return validationSession.result();
        }

        additionalDynamicRuleValidators.forEach(validator ->
            validationSession.addAll(validator.validate(dynamicRule, krakenProject)));

        return validationSession.result();
    }

    private static void ensureKrakenProjectConsistency(KrakenProject krakenProject) {
        if(krakenProject.getNamespace() == null) {
            throw new IllegalKrakenProjectStateException("Namespace in KrakenProject must not be null");
        }
        if(krakenProject.getRootContextName() == null) {
            throw new IllegalKrakenProjectStateException("RootContextName in KrakenProject must not be null");
        }
        if(krakenProject.getRules() == null) {
            throw new IllegalKrakenProjectStateException("Rules in KrakenProject must not be null");
        }
        if(krakenProject.getContextDefinitions() == null) {
            throw new IllegalKrakenProjectStateException("ContextDefinitions in KrakenProject must not be null");
        }
        if(krakenProject.getEntryPoints() == null) {
            throw new IllegalKrakenProjectStateException("EntryPoints in KrakenProject must not be null");
        }
        if(krakenProject.getExternalContextDefinitions() == null) {
            throw new IllegalKrakenProjectStateException("ExternalContextDefinitions in KrakenProject must not be null");
        }
        if(krakenProject.getFunctionSignatures() == null) {
            throw new IllegalKrakenProjectStateException("FunctionSignatures in KrakenProject must not be null");
        }
        if(!krakenProject.getContextDefinitions().containsKey(krakenProject.getRootContextName())) {
            throw new IllegalKrakenProjectStateException("Root Context is " + krakenProject.getRootContextName() +
                    " but such ContextDefinition does not exist in KrakenProject: " + krakenProject.getNamespace());
        }
    }

}
