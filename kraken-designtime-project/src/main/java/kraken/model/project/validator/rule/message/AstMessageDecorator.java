/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule.message;

import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.details.AstDetails;
import kraken.el.ast.validation.details.AstDetailsType;
import kraken.model.Rule;

/**
 * A provider used to decorate original {@link AstMessage} message based on {@link AstDetails}.
 *
 * @param <T> Type of details.
 * @author Tomas Dapkunas
 * @see AstMessageDecoratorService
 * @since 1.29.0
 */
public interface AstMessageDecorator<T extends AstDetails> {

    /**
     * Returns a decorated error message for given {@link AstMessage}.
     *
     * @param message AST Error for a Rule.
     * @param rule  A Rule with invalid expression.
     * @return Decorated error message.
     */
    String decorate(AstMessage message, Rule rule);

    /**
     * {@link AstDetailsType} supported by this decorator. It is assumed that there will
     * be a single implementation per {@link AstDetailsType}.
     *
     * @return Supported type.
     */
    AstDetailsType getSupportedType();

    /**
     * Resolves {@link AstDetails} from {@link AstMessage}.
     *
     * @param message AST Error to resolve details from.
     * @return AST Error details.
     */
    @SuppressWarnings("unchecked")
    default T getDetails(AstMessage message) {
        return (T) message.getDetails();
    }

}
