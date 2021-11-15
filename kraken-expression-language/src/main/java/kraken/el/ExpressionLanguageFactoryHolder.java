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

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

import kraken.el.InvocationContextHolder.InvocationContext;
import kraken.el.ast.Ast;
import kraken.el.ast.Function;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.functionregistry.FunctionDefinition;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.FunctionRegistry;
import org.apache.commons.lang3.StringUtils;

/**
 * Holds all available Expression Languages in classpath.
 * Allows to retrieve Expression Language for Target Environment.
 *
 * @author mulevicius
 */
public class ExpressionLanguageFactoryHolder {

    private ExpressionLanguageFactoryHolder() {
    }

    private static final Map<TargetEnvironment, ExpressionLanguageEnvironment> expressionLanguageFactories =
            ServiceLoader.load(ExpressionLanguageFactory.class).stream()
                    .map(ServiceLoader.Provider::get)
                    .map(ExpressionLanguageFactoryHolder::decorateForEnvironment)
                    .collect(Collectors.groupingBy(
                            ExpressionLanguageFactory::getTargetEnvironment,
                            Collectors.collectingAndThen(Collectors.toList(), ExpressionLanguageEnvironment::new)
                    ));

    public static ExpressionLanguageFactory getExpressionLanguageFactory(TargetEnvironment environment) {
        Objects.requireNonNull(environment);
        if(!expressionLanguageFactories.containsKey(environment)) {
            String template = "Cannot resolve Expression Language for environment '%s', " +
                    "because there are no Expression Languages registered for this environment in the system. " +
                    "This indicates an error in application deployment.";
            String message = String.format(template, environment.toString().toLowerCase());
            throw new IllegalStateException(message);
        }
        return expressionLanguageFactories.get(environment).getTargetEvaluator();
    }

    static class ExpressionLanguageEnvironment {

        private TargetEnvironment environment;

        private ExpressionLanguageFactory primaryEvaluator;

        private Map<String, ExpressionLanguageFactory> evaluators;

        ExpressionLanguageEnvironment(Collection<ExpressionLanguageFactory> evaluators) {
            this.environment = evaluators.iterator().next().getTargetEnvironment();
            this.evaluators = evaluators.stream()
                    .collect(Collectors.toMap(ExpressionLanguageFactory::getName, e -> e));

            List<ExpressionLanguageFactory> primaryEvaluators = evaluators.stream()
                    .filter(ExpressionLanguageFactory::isPrimary)
                    .collect(Collectors.toList());

            if(primaryEvaluators.size() > 1) {
                String template = "Error while initializing Kraken Expression Language module for environment '%s', " +
                        "because there are more than one primary evaluator registered in the system: %s. " +
                        "This indicates an error in application deployment.";
                String primaryString = primaryEvaluators.stream().map(ExpressionLanguageFactory::getName).collect(Collectors.joining(", "));
                String message = String.format(template, environment.toString().toLowerCase(), primaryString);
                throw new IllegalStateException(message);
            }
            if(!primaryEvaluators.isEmpty()) {
                this.primaryEvaluator = primaryEvaluators.get(0);
            }
        }

        private ExpressionLanguageFactory getTargetEvaluator() {
            String environmentName = environment.toString().toLowerCase();
            String property = String.format("kraken.el.evaluator.%s", environmentName);
            String name = System.getProperty(property);
            if(name == null) {
                if(primaryEvaluator == null && evaluators.size() > 1) {
                    String template = "Cannot resolve Expression Language for environment '%s', " +
                            "because there more than one candidate exists with the same priority. " +
                            "This indicates an error in application deployment.";
                    String message = String.format(template, environmentName);
                    throw new IllegalStateException(message);
                }
                return primaryEvaluator != null
                        ? primaryEvaluator
                        : evaluators.values().iterator().next();
            } else {
                if(!evaluators.containsKey(name)) {
                    String template = "Cannot resolve Expression Language for environment '%s', " +
                            "by name '%s', because there is no Expression Language registered with this name in the system. " +
                            "This indicates an error in application deployment.";
                    String message = String.format(template, environmentName, name);
                    throw new IllegalStateException(message);
                }
                return evaluators.get(name);
            }
        }
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
                    new ValidatingExpressionLanguage(
                            expressionLanguageFactory.createExpressionLanguage(configuration),
                            configuration
                    )
            );
        }

        @Override
        public String getName() {
            return expressionLanguageFactory.getName();
        }

        @Override
        public boolean isPrimary() {
            return expressionLanguageFactory.isPrimary();
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
            return new ValidatingExpressionLanguage(
                    expressionLanguageFactory.createExpressionLanguage(configuration),
                    configuration
            );
        }

        @Override
        public String getName() {
            return expressionLanguageFactory.getName();
        }

        @Override
        public boolean isPrimary() {
            return expressionLanguageFactory.isPrimary();
        }
    }

    static class ValidatingExpressionLanguage implements ExpressionLanguage {

        private static final String FUNCTION_DOES_NOT_EXIST = "Cannot invoke function {0} because it does not exist.";
        private static final String ILLEGAL_FUNCTION_TARGET = "Function {0} is invoked in illegal target environment. " +
                "Expression target environment is {1} but function is only allowed for {2} environments.";

        private final ExpressionLanguage expressionLanguage;

        private final ExpressionLanguageConfiguration configuration;

        public ValidatingExpressionLanguage(ExpressionLanguage expressionLanguage, ExpressionLanguageConfiguration configuration) {
            this.expressionLanguage = expressionLanguage;
            this.configuration = configuration;
        }

        @Override
        public String translate(Ast ast) {
            validateFunctions(ast);

            return expressionLanguage.translate(ast);
        }

        @Override
        public Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException {
            return expressionLanguage.evaluate(expression, dataObject, vars);
        }

        @Override
        public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
            expressionLanguage.evaluateSetExpression(valueToSet, path, dataObject);
        }

        private void validateFunctions(Ast ast) {
            for(Function f : ast.getFunctions().values()) {
                FunctionDefinition function = FunctionRegistry.getFunctions().get(new FunctionHeader(f.getFunctionName(), f.getParameters().size()));
                if(function == null) {
                    String message = MessageFormat.format(FUNCTION_DOES_NOT_EXIST, f.getFunctionSignatureString());
                    throw new IllegalFunctionInvocationException(message);
                }
                if(configuration.getExpressionTarget() != null
                        && !function.getExpressionTargets().isEmpty()
                        && !function.getExpressionTargets().contains(configuration.getExpressionTarget())) {
                    String message = MessageFormat.format(ILLEGAL_FUNCTION_TARGET,
                            f.getFunctionSignatureString(),
                            configuration.getExpressionTarget(),
                            function.getExpressionTargets()
                    );
                    throw new IllegalFunctionInvocationException(message);
                }
            }
        }
    }

    static class IllegalFunctionInvocationException extends AstBuildingException {
        public IllegalFunctionInvocationException(String message) {
            super(message);
        }
    }

    static class InvocationContextAwareExpressionLanguage implements ExpressionLanguage {

        private final ExpressionLanguage expressionLanguage;

        public InvocationContextAwareExpressionLanguage(ExpressionLanguage expressionLanguage) {
            this.expressionLanguage = expressionLanguage;
        }

        @Override
        public String translate(Ast ast) {
            return expressionLanguage.translate(ast);
        }

        @Override
        public Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException {
            if(StringUtils.isEmpty(expression)) {
                return null;
            }
            InvocationContext previous = InvocationContextHolder.getInvocationContext();
            InvocationContextHolder.setInvocationContext(new InvocationContext(dataObject, vars));
            try {
                return expressionLanguage.evaluate(expression, dataObject, vars);
            } finally {
                InvocationContextHolder.setInvocationContext(previous);
            }
        }

        @Override
        public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
            InvocationContext previous = InvocationContextHolder.getInvocationContext();
            InvocationContextHolder.setInvocationContext(new InvocationContext(dataObject, Map.of()));
            try {
                expressionLanguage.evaluateSetExpression(valueToSet, path, dataObject);
            } finally {
                InvocationContextHolder.setInvocationContext(previous);
            }
        }
    }
}
