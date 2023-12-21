/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.el.functionregistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kraken.annotations.API;
import kraken.el.FunctionContextHolder;

/**
 * Annotated Java Method will be available as Expression Function in expressions evaluated by Kraken Expression Language.
 * Java Method must be static method and must be in implementation of {@link FunctionLibrary}.
 * <p/>
 * Expression Function can be invoked in expression by function name.
 * Expression Function can have any number of parameters.
 * <p/>
 * Return type of Expression Function is determined by Class name of Java Method return type.
 * Additionally, return type can be overridden by {@link ReturnType}.
 * <p/>
 * Type of Expression Function parameter is determined by Class name of Java Parameter.
 * Alternatively, parameter type can be overridden by {@link ParameterType}.
 * <p/>
 * If {@link ReturnType} or {@link ParameterType} is not defined,
 * then the actual parameter is determined by Class name as follows:
 * <ul>
 *     <li>
 *         If type is non parameterized raw Java Class, then the type is resolved from the Class as follows:
 *         <ul>
 *             <li>{@link Number} or implementation of {@link Number} - is resolved to Number</li>
 *             <li>{@link String} - is resolved to String</li>
 *             <li>{@link javax.money.MonetaryAmount} - is resolved to Money</li>
 *             <li>{@link Boolean} - is resolved to Boolean</li>
 *             <li>{@link java.time.LocalDate} - is resolved to Date</li>
 *             <li>{@link java.time.LocalDateTime} - is resolved to DateTime</li>
 *             <li>{@link java.util.Collection} - is resolved to Any</li>
 *             <li>{@link java.util.Map} - is resolved to Any</li>
 *             <li>{@link Object} - is resolved to Any</li>
 *             <li>for any other Class it is resolved to {@link Class#getSimpleName()}</li>
 *         </ul>
 *     </li>
 *     <li>
 *         If type is a parameterized {@link java.util.Collection}, then type is an array of the type parameter.
 *         For example, for {@code Collection<String>} type will be resolved to {@code String[]}.
 *     </li>
 *     <li>
 *         If type is an unbounded generic then type will be resolved to generic type {@code <T>}.
 *     </li>
 *     <li>
 *         If type is a bounded generic then type will be resolved to type of the bound.
 *         For example, for {@code <T extends Collection<Coverage>>} type will be resolved to {@code Coverage[]}.
 *     </li>
 * </ul>
 * <p/>
 * To indicate a business error you can throw {@link kraken.el.ExpressionEvaluationException}
 * and engine will handle this appropriately. For example, Kraken engine would ignore the rule.
 * Any other type of exception is propagated outside of the engine and would terminate the engine.
 * <p/>
 * Every defined Expression Function must be unique in the system
 * where uniqueness is determined by name of Expression Function and by parameter count.
 *<p/>
 * Custom function implementation can access {@link kraken.el.FunctionContextHolder.FunctionContext} by
 * {@link FunctionContextHolder#getFunctionContext()} to access current evaluation session specific metadata,
 * such as a current evaluation time zone.
 *
 * @author mulevicius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@API
public @interface ExpressionFunction {

    /**
     * @return name of Expression Function; if not provided then {@link java.lang.reflect.Method#getName()} will be used.
     */
    String value() default "";

    /**
     * @return true if function modifies function parameters (state). By default, Expression Function cannot modify state.
     * <p/>
     * Setting this to true will greatly limit environments where this Expression Function can be used.
     */
    boolean modifiesState() default false;

    /**
     *
     * @return a list of information about generic types used in parameter type and return type of this function.
     */
    GenericType[] genericTypes() default {};
}
