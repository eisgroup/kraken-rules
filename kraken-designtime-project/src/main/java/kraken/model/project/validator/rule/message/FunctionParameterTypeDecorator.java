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
import kraken.el.ast.validation.details.AstDetailsType;
import kraken.el.ast.validation.details.FunctionParameterTypeDetails;
import kraken.el.scope.type.ArrayType;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;

/**
 * Implementation which decorates original error message with additional details
 * when parameter value type is a CCR and cardinality mismatches function signature parameter type
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public final class FunctionParameterTypeDecorator
    extends AbstractTypeMessageDecorator<FunctionParameterTypeDetails> {

    public FunctionParameterTypeDecorator(KrakenProject krakenProject,
                                          CrossContextService crossContextService) {
        super(krakenProject, crossContextService);
    }

    @Override
    public String decorate(AstMessage message, Rule rule) {
        FunctionParameterTypeDetails details = getDetails(message);

        if (!isSameScalarType(details) || !isDifferentCardinality(details)) {
            return message.getMessage();
        }

        String ccrPathMessage = describeIfExpressionIsSimplePathAndCcr(details.getFunctionExpression(), rule);

        return ccrPathMessage.isEmpty()
            ? message.getMessage()
            : message.getMessage() + ccrPathMessage;
    }

    private boolean isSameScalarType(FunctionParameterTypeDetails details) {
        return details.getFunctionExpression().getEvaluationType().unwrapArrayType()
            .equals(details.getFunctionParameterType().unwrapArrayType());
    }

    private boolean isDifferentCardinality(FunctionParameterTypeDetails details) {
        return isSimplePathAndCcrIsArray(details.getFunctionExpression())
            ^ details.getFunctionParameterType() instanceof ArrayType;
    }

    @Override
    public AstDetailsType getSupportedType() {
        return AstDetailsType.FUNCTION_TYPE_ERROR;
    }

}
