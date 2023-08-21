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
package kraken.runtime.expressions;

import static kraken.message.SystemMessageBuilder.Message.EXPRESSION_CANNOT_EVALUATE_SET;
import static kraken.message.SystemMessageBuilder.Message.EXPRESSION_CANNOT_EVALUATE_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.el.EvaluationContext;
import kraken.el.Expression;
import kraken.el.ExpressionEvaluationException;
import kraken.el.ExpressionLanguage;
import kraken.el.ExpressionLanguageConfiguration;
import kraken.el.KrakenKel;
import kraken.el.TargetEnvironment;
import kraken.el.accelerated.PropertyExpressionEvaluator;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.functionregistry.FunctionInvoker;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;
import kraken.el.scope.Scope;
import kraken.message.SystemMessageBuilder;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.context.ContextNavigation;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;
import kraken.runtime.utils.TargetPathUtils;
import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.utils.Assertions;

/**
 * Central point for evaluating Kraken Expressions in Kraken Model.
 * Expressions must be translated for the appropriate Target Environment.
 *
 * @author mulevicius
 */
public class KrakenExpressionEvaluator {

    private final ExpressionLanguage expressionLanguage;

    private final PropertyExpressionEvaluator propertyExpressionEvaluator;

    private final InterpretingExpressionEvaluator functionEvaluator;

    public KrakenExpressionEvaluator() {
        this.expressionLanguage = KrakenKel.create(TargetEnvironment.JAVA);
        this.propertyExpressionEvaluator = new PropertyExpressionEvaluator();
        this.functionEvaluator = new InterpretingExpressionEvaluator(new ExpressionLanguageConfiguration(false, true));
    }

    public Object evaluate(CompiledExpression expression, DataContext dataContext, EvaluationSession session) {
        Assertions.assertNotNull(expression, "Expression");
        Assertions.assertNotEmpty(expression.getExpressionString(), "Expression");
        Assertions.assertNotNull(dataContext.getDataObject(), "Data");

        if (expression.getExpressionType() == ExpressionType.LITERAL) {
            return expression.getCompiledLiteralValue();
        }
        if (expression.getExpressionType() == ExpressionType.PATH) {
            return evaluateGetProperty(
                expression.getExpressionString(),
                expression.getAst(),
                dataContext.getDataObject()
            );
        }

        KrakenTypeProvider typeProvider = session.getKrakenTypeProvider();
        EvaluationContext evaluationContext = new EvaluationContext(
            dataContext.getDataObject(),
            createExpressionVars(session, dataContext),
            typeProvider,
            new FunctionInvoker(session.getFunctions(), functionEvaluator, typeProvider, KrakenKel.EXPRESSION_TARGET)
        );
        return evaluate(expression.getExpressionString(), expression.getAst(), evaluationContext);
    }

    public Object evaluateSetProperty(Object valueToSet, String path, Object dataObject) {
        Assertions.assertNotEmpty(path, "Path");
        Assertions.assertNotNull(dataObject, "Data");

        try {
            expressionLanguage.evaluateSetExpression(valueToSet, path, dataObject);
            return evaluateGetProperty(path, dataObject);
        } catch (ExpressionEvaluationException ex) {
            var m = SystemMessageBuilder.create(EXPRESSION_CANNOT_EVALUATE_SET)
                .parameters(path)
                .build();
            throw new KrakenExpressionEvaluationException(m, ex);
        }
    }

    public Object evaluateNavigationExpression(ContextNavigation contextNavigation, DataContext dataContext, EvaluationSession session) {
        if(contextNavigation.getNavigationExpression().getExpressionType() == ExpressionType.PATH) {
            return evaluateGetProperty(
                contextNavigation.getNavigationExpression().getExpressionString(),
                contextNavigation.getNavigationExpression().getAst(),
                dataContext.getDataObject()
            );
        }
        KrakenTypeProvider typeProvider = session.getKrakenTypeProvider();
        EvaluationContext evaluationContext = new EvaluationContext(
            dataContext.getDataObject(),
            Map.of(),
            typeProvider,
            new FunctionInvoker(session.getFunctions(), functionEvaluator, typeProvider, KrakenKel.EXPRESSION_TARGET)
        );

        return evaluate(
            contextNavigation.getNavigationExpression().getExpressionString(),
            contextNavigation.getNavigationExpression().getAst(),
            evaluationContext
        );
    }

    public Object evaluateGetProperty(String path, Object dataObject) {
        Ast ast = AstBuilder.from(path, Scope.dynamic());
        return evaluateGetProperty(path, ast, dataObject);
    }

    public Object evaluateTargetField(String targetPath, DataContext dataContext) {
        String path = TargetPathUtils.resolveTargetPath(targetPath, dataContext);
        return this.evaluateGetProperty(path, dataContext.getDataObject());
    }

    private Object evaluateGetProperty(String path, Ast ast, Object dataObject) {
        try {
            if(path.indexOf('.') < 0) {
                return propertyExpressionEvaluator.evaluate(path, dataObject);
            }
            return expressionLanguage.evaluate(new Expression(path, ast), new EvaluationContext(dataObject));
        } catch (ExpressionEvaluationException ex) {
            var m = SystemMessageBuilder.create(EXPRESSION_CANNOT_EVALUATE_VALUE)
                .parameters(path)
                .build();
            throw new KrakenExpressionEvaluationException(m, ex);
        }
    }

    public List<String> evaluateTemplateVariables(ErrorMessage message, DataContext context, EvaluationSession session) {
        if(message == null) {
            return List.of();
        }
        return message.getTemplateExpressions().stream()
            .map(e -> {
                try {
                    return evaluate(e, context, session);
                } catch (KrakenExpressionEvaluationException ex) {
                    return null;
                }
            })
            .map(TemplateParameterRenderer::render)
            .collect(Collectors.toList());
    }

    private Object evaluate(String expression, Ast ast, EvaluationContext evaluationContext) {
        Assertions.assertNotEmpty(expression, "Expression");

        try {
            return expressionLanguage.evaluate(new Expression(expression, ast), evaluationContext);
        } catch (ExpressionEvaluationException ex) {
            var m = SystemMessageBuilder.create(EXPRESSION_CANNOT_EVALUATE_VALUE)
                .parameters(expression)
                .build();
            throw new KrakenExpressionEvaluationException(m, ex);
        }
    }

    private static Map<String, Object> createExpressionVars(EvaluationSession session, DataContext dataContext) {
        var vars = new HashMap<>(dataContext.getObjectReferences());
        vars.put("context", session.getExpressionContext());
        return vars;
    }

}

