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

package kraken.el.functionregistry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import kraken.annotations.API;

/**
 * Describes  {@link ExpressionFunction} parameters.
 * Parameter documentation must have parameter name {@link ParameterDocumentation#name()} and
 * description @{link ParameterDocumentation#description()}
 *
 * @author psurinin
 * @see ExpressionFunction
 * @since 1.24.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER })
@API
public @interface ParameterDocumentation {

    /**
     * Starting you variable with underscore is discouraged. While it's technically legal to begin
     * your variable's name with "_", this practice is discouraged.
     * White space is not permitted.
     * Starting variable from number is not permitted.
     *
     * @return
     */
    String name();

    String description() default "";
}
