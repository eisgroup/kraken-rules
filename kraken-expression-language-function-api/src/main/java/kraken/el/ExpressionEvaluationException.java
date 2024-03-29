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

import kraken.annotations.API;

/**
 * Indicates that some kind of business logic error occurred during expression evaluation due to missing data
 * or for some other business logic related reason.
 * When this exception is thrown then kraken rule will be ignored, but the evaluation will continue.
 *
 * @author mulevicius
 */
@API
public class ExpressionEvaluationException extends RuntimeException {

    private static final long serialVersionUID = 3634578616663834012L;

    public ExpressionEvaluationException(String message) {
        super(message);
    }

    public ExpressionEvaluationException(String message, Throwable cause) {
        super(message, cause);
    }
}
