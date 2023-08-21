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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_BODY_EXPRESSION_SYNTAX_ERROR;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_BODY_EXPRESSION_SYNTAX_INFO;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_BODY_EXPRESSION_SYNTAX_WARNING;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_BODY_IS_LOGICALLY_EMPTY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.FUNCTION_BODY_NOT_PARSEABLE;

import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.ast.validation.AstMessage;
import kraken.el.scope.Scope;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.type.Type;
import kraken.model.Function;
import kraken.model.project.KrakenProject;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
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
            var m = ValidationMessageBuilder.create(FUNCTION_BODY_NOT_PARSEABLE, function).build();
            session.add(m);
            return;
        }

        ast.getValidationMessages().stream()
            .map(m -> toValidationMessage(m, function))
            .forEach(session::add);

        if(ast.getValidationMessages().isEmpty()) {
            FunctionSymbol symbol = scopeBuilder.buildFunctionSymbol(function);
            Type returnType = symbol.getType();
            if (!returnType.isAssignableFrom(ast.getExpression().getEvaluationType())) {
                var m = ValidationMessageBuilder.create(Message.FUNCTION_BODY_WRONG_RETURN_TYPE, function)
                    .parameters(ast.getExpression().getEvaluationType(), function.getReturnType())
                    .build();
                session.add(m);
            }
        }

        if(ast.getExpression().isEmpty()) {
            var m = ValidationMessageBuilder.create(FUNCTION_BODY_IS_LOGICALLY_EMPTY, function).build();
            session.add(m);
        }
    }

    private ValidationMessage toValidationMessage(AstMessage astMessage, Function function) {
        var message = astMessage.getMessage();
        switch (astMessage.getSeverity()) {
            case ERROR:
                return ValidationMessageBuilder.create(FUNCTION_BODY_EXPRESSION_SYNTAX_ERROR, function)
                    .parameters(astMessage.getNode().getToken(), message)
                    .build();
            case WARNING:
                return ValidationMessageBuilder.create(FUNCTION_BODY_EXPRESSION_SYNTAX_WARNING, function)
                    .parameters(astMessage.getNode().getToken(), message)
                    .build();
            case INFO:
                return ValidationMessageBuilder.create(FUNCTION_BODY_EXPRESSION_SYNTAX_INFO, function)
                    .parameters(astMessage.getNode().getToken(), message)
                    .build();
            default:
                throw new IllegalArgumentException("Unknown AstSeverity encountered: " + astMessage.getSeverity());
        }
    }

    private Ast parseFunctionBody(Function function) {
        String bodyExpression = function.getBody().getExpressionString();
        Scope scope = scopeBuilder.buildFunctionScope(function);
        return AstBuilder.from(bodyExpression, scope);
    }

}
