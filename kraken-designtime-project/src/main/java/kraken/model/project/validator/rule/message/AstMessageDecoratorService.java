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

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.details.AstDetails;
import kraken.el.ast.validation.details.AstDetailsType;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;

/**
 * Decorates {@link AstMessage}. Delegates to specific configured {@link AstMessageDecorator} by details type.
 * If no {@link AstMessageDecorator} is configured for {@link AstDetailsType} or
 * {@link AstMessage} has no details then returns original undecorated message.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public final class AstMessageDecoratorService {

    private final Map<AstDetailsType, AstMessageDecorator<?>> decorators;

    public AstMessageDecoratorService(KrakenProject krakenProject, CrossContextService crossContextService) {
        this.decorators = Stream.of(
            new ComparisonTypeDecorator(krakenProject, crossContextService),
            new FunctionParameterTypeDecorator(krakenProject, crossContextService)
        ).collect(Collectors.toUnmodifiableMap(AstMessageDecorator::getSupportedType, provider -> provider));
    }

    public String decorate(AstMessage message, Rule rule) {
        AstDetails astDetails = message.getDetails();
        if (astDetails != null && decorators.get(astDetails.getType()) != null) {
            return decorators.get(astDetails.getType()).decorate(message, rule);
        }
        return message.getMessage();
    }

}
