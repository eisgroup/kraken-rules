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

import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_FIELD_IS_NULL;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_PROJECT_ROOT_CONTEXT_DEFINITION_UNKNOWN;

import java.util.List;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import kraken.message.SystemMessageBuilder;
import kraken.model.Rule;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.exception.IllegalKrakenProjectStateException;
import kraken.model.project.validator.context.ContextDefinitionValidator;
import kraken.model.project.validator.context.ExternalContextValidator;
import kraken.model.project.validator.entrypoint.EntryPointDefinitionValidator;
import kraken.model.project.validator.function.FunctionBodyValidator;
import kraken.model.project.validator.function.FunctionDocumentationValidator;
import kraken.model.project.validator.function.FunctionSignatureValidator;
import kraken.model.project.validator.function.FunctionValidator;
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
        FunctionValidator functionValidator = new FunctionValidator(krakenProject);
        functionValidator.validate(validationSession);
        if(validationSession.hasFunctionSignatureError() || validationSession.hasFunctionError()) {
            return validationSession.result();
        }

        FunctionBodyValidator functionBodyValidator = new FunctionBodyValidator(krakenProject);
        functionBodyValidator.validate(validationSession);
        FunctionDocumentationValidator documentationValidator = new FunctionDocumentationValidator(krakenProject);
        documentationValidator.validate(validationSession);
        if(validationSession.hasFunctionError()) {
            return validationSession.result();
        }

        ValidationResult result = validateRulesAndEntryPoints(krakenProject);
        validationSession.addAll(result.getAllMessages());

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
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Namespace", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getRootContextName() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Root context name", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getRules() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Rules", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getContextDefinitions() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Context definitions", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getEntryPoints() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Entry points", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getExternalContextDefinitions() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("External context definitions", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getFunctionSignatures() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Function signatures", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getFunctions() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Functions", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(krakenProject.getDimensions() == null) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_FIELD_IS_NULL)
                .parameters("Dimensions", krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
        if(!krakenProject.getContextDefinitions().containsKey(krakenProject.getRootContextName())) {
            var m = SystemMessageBuilder.create(KRAKEN_PROJECT_ROOT_CONTEXT_DEFINITION_UNKNOWN)
                .parameters(krakenProject.getRootContextName(), krakenProject.getNamespace())
                .build();
            throw new IllegalKrakenProjectStateException(m);
        }
    }

}
