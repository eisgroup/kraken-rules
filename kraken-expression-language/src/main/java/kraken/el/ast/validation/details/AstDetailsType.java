/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
 */
package kraken.el.ast.validation.details;

/**
 * Enumerates types used to identify {@code AstDetails}.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public enum AstDetailsType {

    /**
     * Indicates a mismatch of {@code Expression} types in {@code ComparisonOperation}.
     */
    COMPARISON_TYPE_ERROR,

    /**
     * Indicates a mismatch of {@code Expression} type and function parameter type.
     */
    FUNCTION_TYPE_ERROR,

}
