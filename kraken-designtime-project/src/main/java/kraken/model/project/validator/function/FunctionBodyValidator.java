/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.function;

import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.AstMessageSeverity;
import kraken.el.scope.Scope;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;
import kraken.model.Function;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * Validates {@link Function} body expressions. It is a separate validator from {@link FunctionValidator} because we can
 * validate {@link Function} body expression only when definition of every {@link Function} is checked to be valid first.
 *
 * @author mulevicius
 */
public class FunctionBodyValidator {

    private final KrakenProject krakenProject;
    private final ScopeBuilder scopeBuilder;

    public FunctionBodyValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
    }

    public void validate(ValidationSession session) {
        for(Function function : krakenProject.getFunctions()) {
            validate(function, session);
        }
    }

    private void validate(Function function, ValidationSession session) {
        Ast ast;
        try {
            ast = parseFunctionBody(function);
        } catch (AstBuildingException e) {
            // stop validation if function body expression cannot be parsed
            session.add(functionBodyIsNotParseableError(function));
            return;
        }

        ast.getValidationMessages().stream()
            .map(m -> toValidationMessage(m, function))
            .forEach(session::add);

        if(ast.getValidationMessages().isEmpty()) {
            FunctionSymbol symbol = scopeBuilder.buildFunctionSymbol(function);
            Type returnType = symbol.getType();
            if (!returnType.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                session.add(new ValidationMessage(
                    function,
                    String.format(
                        "Evaluation type of function body expression is '%s' and it is not assignable to "
                            + "function return type '%s'",
                        ast.getExpression().getEvaluationType(),
                        function.getReturnType()
                    ),
                    Severity.ERROR
                ));
            }
        }

        if(ast.getExpression().isEmpty()) {
            session.add(functionBodyIsLogicallyEmptyError(function));
        }
    }

    private Ast parseFunctionBody(Function function) {
        String bodyExpression = function.getBody().getExpressionString();
        Scope scope = scopeBuilder.buildFunctionScope(function);
        return AstBuilder.from(bodyExpression, scope);
    }

    private ValidationMessage functionBodyIsNotParseableError(Function function) {
        return new ValidationMessage(
            function,
            "Function body expression cannot be parsed, because there is an error in expression syntax",
            Severity.ERROR
        );
    }

    private ValidationMessage functionBodyIsLogicallyEmptyError(Function function) {
        return new ValidationMessage(
            function,
            "Function body expression is logically empty. "
                + "Please check if there are unintentional spaces, new lines or comments remaining.",
            Severity.ERROR
        );
    }

    private ValidationMessage toValidationMessage(AstMessage message, Function function) {
        return new ValidationMessage(function, message.getMessage(), toSeverity(message.getSeverity()));
    }

    private Severity toSeverity(AstMessageSeverity severity) {
        switch (severity) {
            case ERROR:
                return Severity.ERROR;
            case WARNING:
                return Severity.WARNING;
            case INFO:
                return Severity.INFO;
            default:
                throw new IllegalArgumentException("Unknown AstMessageSeverity encountered: " + severity);
        }
    }

}
