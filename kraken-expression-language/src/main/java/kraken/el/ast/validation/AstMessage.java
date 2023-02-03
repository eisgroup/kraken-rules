/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el.ast.validation;

import javax.annotation.Nullable;

import kraken.el.ast.Expression;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.validation.details.AstDetails;

/**
 * Represents a single error in Abstract Syntax Tree parsed from expression
 *
 * @author mulevicius
 */
public class AstMessage {

    private ReferenceValue referenceValue;

    private String message;

    private Expression node;

    private AstMessageSeverity severity;
    
    private AstDetails astDetails;

    public AstMessage(String message, ReferenceValue referenceValue, Expression node, AstMessageSeverity severity) {
        this(message, referenceValue, node, severity, null);
    }

    public AstMessage(String message,
                      ReferenceValue referenceValue,
                      Expression node,
                      AstMessageSeverity severity,
                      AstDetails astDetails) {
        this.referenceValue = referenceValue;
        this.message = message;
        this.node = node;
        this.severity = severity;
        this.astDetails = astDetails;
    }

    public String getMessage() {
        return message;
    }

    public ReferenceValue getReferenceValue() {
        return referenceValue;
    }

    public Expression getNode() {
        return node;
    }

    public AstMessageSeverity getSeverity() {
        return severity;
    }
    
    @Nullable
    public AstDetails getDetails() {
        return astDetails;
    }

    @Override
    public String toString() {
        return message;
    }
}
