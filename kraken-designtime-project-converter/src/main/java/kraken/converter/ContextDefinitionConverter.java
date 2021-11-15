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
package kraken.converter;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.converter.translation.KrakenExpressionTranslator;
import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.context.ContextNavigation;

/**
 * @author mulevicius
 */
public class ContextDefinitionConverter {

    private KrakenExpressionTranslator krakenExpressionTranslator;

    private KrakenProject krakenProject;

    public ContextDefinitionConverter(KrakenProject krakenProject, KrakenExpressionTranslator krakenExpressionTranslator) {
        this.krakenProject = krakenProject;
        this.krakenExpressionTranslator = krakenExpressionTranslator;
    }

    public Map<String, RuntimeContextDefinition> convert(Map<String, kraken.model.context.ContextDefinition> contextDefinitions) {
        return contextDefinitions.values().stream()
                .map(c -> convert(c))
                .collect(Collectors.toMap(c -> c.getName(), c -> c));
    }

    private RuntimeContextDefinition convert(kraken.model.context.ContextDefinition contextDefinition) {
        ContextDefinition contextProjection = krakenProject.getContextProjection(contextDefinition.getName());

        Map<String, ContextField> fields = contextProjection.getContextFields().values().stream()
                .map(f -> convert(f))
                .collect(Collectors.toMap(f -> f.getName(), f -> f));

        Map<String, ContextNavigation> children = contextProjection.getChildren().values().stream()
                .map(navigation -> convert(navigation))
                .collect(Collectors.toMap(f -> f.getTargetName(), f -> f));

        return new RuntimeContextDefinition(
                contextProjection.getName(),
                children,
                fields,
                new ArrayList<>(contextProjection.getParentDefinitions())
        );
    }

    private ContextField convert(kraken.model.context.ContextField contextField) {
        return new ContextField(
                contextField.getName(),
                contextField.getFieldType(),
                contextField.getFieldPath(),
                contextField.getCardinality()
        );
    }

    private ContextNavigation convert(kraken.model.context.ContextNavigation contextNavigation) {
        return new ContextNavigation(
                contextNavigation.getTargetName(),
                krakenExpressionTranslator.translateContextNavigationExpression(contextNavigation),
                contextNavigation.getCardinality()
        );
    }

}
