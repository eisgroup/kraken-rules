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

package kraken.runtime.model.rule.payload.validation;

import java.util.List;
import java.util.Objects;

import kraken.runtime.model.expression.CompiledExpression;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ErrorMessage {

    private final String errorCode;
    private final List<String> templateParts;
    private final List<CompiledExpression> templateExpressions;

    public ErrorMessage(String errorCode, List<String> templateParts, List<CompiledExpression> templateExpressions) {
        this.errorCode = errorCode;
        this.templateParts = Objects.requireNonNull(templateParts);
        this.templateExpressions = Objects.requireNonNull(templateExpressions);
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     *
     * @return  a list of message parts separated by expressions.
     *          If message is not specified in DSL then this is empty list.
     *          If message is specified in DSL then this always contains at least an empty string.
     */
    public List<String> getTemplateParts() {
        return templateParts;
    }

    public List<CompiledExpression> getTemplateExpressions() {
        return templateExpressions;
    }
}