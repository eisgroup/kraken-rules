/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
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
