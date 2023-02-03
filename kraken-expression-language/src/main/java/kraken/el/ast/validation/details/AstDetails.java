/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
 */
package kraken.el.ast.validation.details;

/**
 * Template for AST details.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public abstract class AstDetails {

    private final AstDetailsType type;

    public AstDetails(AstDetailsType type) {
        this.type = type;
    }

    public AstDetailsType getType() {
        return type;
    }

}
