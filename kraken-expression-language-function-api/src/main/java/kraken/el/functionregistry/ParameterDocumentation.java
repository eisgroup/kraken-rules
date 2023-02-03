/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
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
