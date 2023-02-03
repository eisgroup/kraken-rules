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

/**
 * Describes how to use {@link ExpressionFunction} in {@link FunctionLibrary}.
 * It must be used on a method annotated as {@link ExpressionFunction}.
 * Function parameters can be documented with @{link ParameterDocumentation}
 *
 * @author psurinin
 * @see ParameterDocumentation
 * @see ExpressionFunction
 * @see FunctionLibrary
 * @since 1.24.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@API
public @interface FunctionDocumentation {

    /**
     * accepts markdown notations
     */
    String description();

    Example[] example() default {};

    /**
     * If value is not provided, @{link LibraryDocumentation#since()} value will be used.
     *
     * @return version when function was added
     */
    String since() default "";

    /**
     * Documentation will be more readable if it will start from an "if ...".
     */
    String throwsError() default "";
}
