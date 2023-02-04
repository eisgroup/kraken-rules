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

import org.apache.commons.lang3.StringUtils;

import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.details.AstDetailsType;
import kraken.el.ast.validation.details.ComparisonTypeDetails;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;

/**
 * Implementation which decorates original error message with additional details when expressions of
 * comparison operation are of the same type, but resolves to different cardinality.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public final class ComparisonTypeDecorator extends AbstractTypeMessageDecorator<ComparisonTypeDetails> {

    public ComparisonTypeDecorator(KrakenProject krakenProject,
                                   CrossContextService crossContextService) {
        super(krakenProject, crossContextService);
    }

    @Override
    public String decorate(AstMessage message, Rule rule) {
        ComparisonTypeDetails details = getDetails(message);

        if (!isSameScalarType(details) || !isDifferentCardinality(details)) {
            return message.getMessage();
        }

        String leftCcrMessage = describeIfExpressionIsSimplePathAndCcr(details.getLeft(), rule);
        String rightCcrMessage = describeIfExpressionIsSimplePathAndCcr(details.getRight(), rule);

        String ccrMessage = leftCcrMessage + rightCcrMessage;

        return StringUtils.isBlank(ccrMessage)
            ? message.getMessage()
            : message.getMessage() + ccrMessage;
    }

    private boolean isDifferentCardinality(ComparisonTypeDetails details) {
        return isSimplePathAndCcrIsArray(details.getLeft()) ^ isSimplePathAndCcrIsArray(details.getRight());
    }

    private boolean isSameScalarType(ComparisonTypeDetails details) {
        return details.getLeft().getEvaluationType().unwrapArrayType()
            .equals(details.getRight().getEvaluationType().unwrapArrayType());
    }

    @Override
    public AstDetailsType getSupportedType() {
        return AstDetailsType.COMPARISON_TYPE_ERROR;
    }

}
