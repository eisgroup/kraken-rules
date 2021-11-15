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
package kraken.el.mvel.evaluator;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.money.MonetaryAmount;

import kraken.el.ExpressionEvaluationException;
import kraken.el.accelerated.AcceleratedPropertyHandler;
import kraken.el.accelerated.AcceleratedPropertyHandlerProvider;
import kraken.el.function.FunctionInvoker;
import org.mvel2.*;
import org.mvel2.integration.PropertyHandlerFactory;
import org.mvel2.integration.VariableResolverFactory;
import org.mvel2.optimizers.OptimizerFactory;

import static org.mvel2.optimizers.OptimizerFactory.SAFE_REFLECTIVE;

/**
 * @author mulevicius
 */
public class MvelExpressionEvaluator {

    public static final String FILTER_FUNCTION_NAME = "Filter";
    public static final String FLATMAP_FUNCTION_NAME = "FlatMap";
    public static final String FOREACH_FUNCTION_NAME = "ForEach";
    public static final String FORSOME_FUNCTION_NAME = "ForSome";
    public static final String FOREVERY_FUNCTION_NAME = "ForEvery";
    public static final String FROMMONEY_FUNCTION_NAME = "FromMoney";
    public static final String GET_ELEMENT_FUNCTION_NAME = "GetElement";

    public static final String INVOKE_FUNCTION_NAME = "Invoke";
    public static final String INVOKE_WITH_ITERATION_FUNCTION_NAME = "InvokeWithIteration";

    private static final Map<String, Object> imports;

    static {
        OptimizerFactory.setDefaultOptimizer(SAFE_REFLECTIVE);

        DataConversion.addConversionHandler(LocalDateTime.class, DateConversionHandler.getDatetimeToDate());
        DataConversion.addConversionHandler(LocalDate.class, DateConversionHandler.getDateToDatetime());

        AcceleratedPropertyHandlerProvider.getPropertyHandlers().stream()
                .forEach(handler -> PropertyHandlerFactory.registerPropertyHandler(handler.getType(), new MvelPropertyHandlerAdapter(handler)));

        imports = new HashMap<>();
        try {
            imports.put(FILTER_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("filter", Collection.class, String.class));
            imports.put(FLATMAP_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("flatMap", Collection.class, String.class));
            imports.put(FOREACH_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("forEach", String.class, Collection.class, String.class));
            imports.put(FORSOME_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("forSome", String.class, Collection.class, String.class));
            imports.put(FOREVERY_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("forEvery", String.class, Collection.class, String.class));
            imports.put(FROMMONEY_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("fromMoney", MonetaryAmount.class));
            imports.put(GET_ELEMENT_FUNCTION_NAME, MvelNativeFunctions.class.getMethod("getElement", Collection.class, Object.class));

            imports.put("_n", MvelNativeFunctions.class.getMethod("_n", Object.class));
            imports.put("_nd", MvelNativeFunctions.class.getMethod("_nd", Object.class));
            imports.put("_s", MvelNativeFunctions.class.getMethod("_s", Object.class));
            imports.put("_b", MvelNativeFunctions.class.getMethod("_b", Object.class));
            imports.put("_eq", MvelNativeFunctions.class.getMethod("_eq", Object.class, Object.class));
            imports.put("_neq", MvelNativeFunctions.class.getMethod("_neq", Object.class, Object.class));
            imports.put("_in", MvelNativeFunctions.class.getMethod("_in", Object.class, Object.class));
            imports.put("_i", MvelNativeFunctions.class.getMethod("_i", Object.class, String.class));
            imports.put("_t", MvelNativeFunctions.class.getMethod("_t", Object.class, String.class));

            imports.put("_mod", MvelNativeFunctions.class.getMethod("_mod", Object.class, Object.class));
            imports.put("_sub", MvelNativeFunctions.class.getMethod("_sub", Object.class, Object.class));
            imports.put("_mult", MvelNativeFunctions.class.getMethod("_mult", Object.class, Object.class));
            imports.put("_pow", MvelNativeFunctions.class.getMethod("_pow", Object.class, Object.class));
            imports.put("_div", MvelNativeFunctions.class.getMethod("_div", Object.class, Object.class));
            imports.put("_add", MvelNativeFunctions.class.getMethod("_add", Object.class, Object.class));

            imports.put(INVOKE_FUNCTION_NAME, FunctionInvoker.class.getMethod("invoke", String.class, Object[].class));
            imports.put(INVOKE_WITH_ITERATION_FUNCTION_NAME, FunctionInvoker.class.getMethod("invokeWithIteration", String.class, Object[].class));

        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot initialize Kraken Expression Language.", e);
        }
    }

    private final ParserConfiguration parserConfiguration;

    private final Map<String, Serializable> compiledExpressionCache;

    public MvelExpressionEvaluator() {
        this.parserConfiguration = new ParserConfiguration(imports, Collections.emptyMap());
        this.compiledExpressionCache = new ConcurrentHashMap<>();
    }

    public Object evaluate(String expression, Object dataObject, Map<String, Object> vars) throws ExpressionEvaluationException {
        try {
            return MVEL.executeExpression(compiled(expression), dataObject, vars);
        } catch (RuntimeException ex) {
            RuntimeException internalException = findInternalCause(ex);
            if(internalException != null) {
                throw internalException;
            }
            throw new ExpressionEvaluationException("Error while evaluating expression: " + expression, ex);
        }
    }

    public void evaluateSetExpression(Object valueToSet, String path, Object dataObject) throws ExpressionEvaluationException {
        try {
            Serializable compiledExpression = MVEL.compileSetExpression(path, new ParserContext(parserConfiguration));
            MVEL.executeSetExpression(compiledExpression, dataObject, valueToSet);
        } catch (RuntimeException ex) {
            RuntimeException internalException = findInternalCause(ex);
            if(internalException != null) {
                throw internalException;
            }
            throw new ExpressionEvaluationException("Error while evaluating path expression: " + path, ex);
        }
    }

    private Serializable compiled(String expression) {
        Serializable compiledExpression = compiledExpressionCache.get(expression);
        if(compiledExpression == null) {
            compiledExpression = MVEL.compileExpression(expression, new ParserContext(parserConfiguration));
            compiledExpressionCache.putIfAbsent(expression, compiledExpression);
        }
        return compiledExpression;
    }

    private RuntimeException findInternalCause(RuntimeException e) {
        if(e.getCause() instanceof InvocationTargetException) {
            if(e.getCause().getCause() instanceof RuntimeException) {
                return (RuntimeException) e.getCause().getCause();
            }
        }
        return null;
    }

    private static class MvelPropertyHandlerAdapter implements org.mvel2.integration.PropertyHandler {

        private final AcceleratedPropertyHandler acceleratedPropertyHandler;

        MvelPropertyHandlerAdapter(AcceleratedPropertyHandler acceleratedPropertyHandler) {
            this.acceleratedPropertyHandler = acceleratedPropertyHandler;
        }

        @Override
        public Object getProperty(String name, Object contextObj, VariableResolverFactory variableFactory) {
            return acceleratedPropertyHandler.get(name, acceleratedPropertyHandler.getType().cast(contextObj));
        }

        @Override
        public Object setProperty(String name, Object contextObj, VariableResolverFactory variableFactory, Object value) {
            return acceleratedPropertyHandler.set(name, acceleratedPropertyHandler.getType().cast(contextObj), value);
        }
    }

}
