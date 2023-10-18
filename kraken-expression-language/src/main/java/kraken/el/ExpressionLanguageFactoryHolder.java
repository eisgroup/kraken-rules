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
package kraken.el;

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import kraken.el.InvocationContextHolder.InvocationContext;
import kraken.el.ast.Ast;
import kraken.el.FunctionContextHolder.FunctionContext;

/**
 * Holds all available Expression Languages in classpath.
 * Allows to retrieve Expression Language for Target Environment.
 *
 * @author mulevicius
 */
public class ExpressionLanguageFactoryHolder {

    private ExpressionLanguageFactoryHolder() {
    }

    private static final Map<TargetEnvironment, ExpressionLanguageFactory> expressionLanguageFactories =
            ServiceLoader.load(ExpressionLanguageFactory.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .map(ExpressionLanguageFactoryHolder::decorateForEnvironment)
                    .collect(Collectors.toMap(ExpressionLanguageFactory::getTargetEnvironment, e -> e));

    public static ExpressionLanguageFactory getExpressionLanguageFactory(TargetEnvironment environment) {
        Objects.requireNonNull(environment);
        if(!expressionLanguageFactories.containsKey(environment)) {
            String template = "Cannot resolve Expression Language for environment '%s', " +
                    "because there are no Expression Languages registered for this environment in the system. " +
                    "This indicates an error in application deployment.";
            String message = String.format(template, environment.toString().toLowerCase());
            throw new IllegalStateException(message);
        }
        return expressionLanguageFactories.get(environment);
    }

    private static ExpressionLanguageFactory decorateForEnvironment(ExpressionLanguageFactory expressionLanguage) {
        if(expressionLanguage.getTargetEnvironment() == TargetEnvironment.JAVA) {
            return new JavaExpressionLanguageFactory(expressionLanguage);
        }
        if(expressionLanguage.getTargetEnvironment() == TargetEnvironment.JAVASCRIPT) {
            return new JavascriptExpressionLanguage(expressionLanguage);
        }
        return expressionLanguage;
    }

    static class JavaExpressionLanguageFactory implements ExpressionLanguageFactory {

        private final ExpressionLanguageFactory expressionLanguageFactory;

        public JavaExpressionLanguageFactory(ExpressionLanguageFactory expressionLanguageFactory) {
            this.expressionLanguageFactory = expressionLanguageFactory;
        }

        @Override
        public TargetEnvironment getTargetEnvironment() {
            return expressionLanguageFactory.getTargetEnvironment();
        }

        @Override
        public ExpressionLanguage createExpressionLanguage(ExpressionLanguageConfiguration configuration) {
            return new InvocationContextAwareExpressionLanguage(
                expressionLanguageFactory.createExpressionLanguage(configuration)
            );
        }

        @Override
        public String getName() {
            return expressionLanguageFactory.getName();
        }

    }

    static class JavascriptExpressionLanguage implements ExpressionLanguageFactory {

        private final ExpressionLanguageFactory expressionLanguageFactory;

        public JavascriptExpressionLanguage(ExpressionLanguageFactory expressionLanguageFactory) {
            this.expressionLanguageFactory = expressionLanguageFactory;
        }

        @Override
        public TargetEnvironment getTargetEnvironment() {
            return expressionLanguageFactory.getTargetEnvironment();
        }

        @Override
        public ExpressionLanguage createExpressionLanguage(ExpressionLanguageConfiguration configuration) {
            return expressionLanguageFactory.createExpressionLanguage(configuration);
        }

        @Override
        public String getName() {
            return expressionLanguageFactory.getName();
        }

    }

    static class InvocationContextAwareExpressionLanguage implements ExpressionLanguage {

        private final ExpressionLanguage expressionLanguage;

        public InvocationContextAwareExpressionLanguage(ExpressionLanguage expressionLanguage) {
            this.expressionLanguage = expressionLanguage;
        }

        @Override
        public Expression translate(Ast ast) {
            return expressionLanguage.translate(ast);
        }

        @Override
        public Object evaluate(Expression expression, EvaluationContext evaluationContext) throws ExpressionEvaluationException {
            if(StringUtils.isEmpty(expression.getExpression())) {
                return null;
            }
            var previousInvocationContext = InvocationContextHolder.getInvocationContext();
            var previousFunctionContext = FunctionContextHolder.getFunctionContext();
            try {
                InvocationContextHolder.setInvocationContext(new InvocationContext(evaluationContext));
                FunctionContextHolder.setFunctionContext(new FunctionContext(evaluationContext.getZoneId()));
                return expressionLanguage.evaluate(expression, evaluationContext);
            } finally {
                FunctionContextHolder.setFunctionContext(previousFunctionContext);
                InvocationContextHolder.setInvocationContext(previousInvocationContext);
            }
        }

        @Override
        public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
            expressionLanguage.evaluateSetExpression(valueToSet, path, dataObject);
        }
    }
}
