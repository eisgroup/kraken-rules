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
 * Describes and describe purpose of {@link FunctionLibrary} in KEL expressions.
 * It must be used on a class implementing @{link FunctionLibrary}.
 *
 * @author psurinin
 * @see ExpressionFunction
 * @see FunctionLibrary
 * @since 1.24.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@API
public @interface LibraryDocumentation {

    String name();

    /**
     * accepts markdown notations
     */
    String description() default "";

    String since() default "";
}
