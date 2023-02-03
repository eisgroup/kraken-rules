/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
 */

package kraken.el.functionregistry;

import kraken.annotations.API;

/**
 * Defines an example of how to use a function. {@link FunctionDocumentation} can have more than one example defined.
 * If needed, example can also show how NOT to use a function by setting {@link Example#validCall()} to false,
 * which is true by default.
 *
 * @author psurinin
 * @see ExpressionFunction
 * @see FunctionDocumentation
 * @since 1.24.0
 */
@API
public @interface Example {
    String value();

    String result() default "";

    boolean validCall() default true;
}
