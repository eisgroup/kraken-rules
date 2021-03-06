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
package kraken.runtime.engine.result.reducers.validation;

import java.util.HashMap;
import java.util.Map;

import kraken.model.context.Cardinality;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.ExternalDataReference;
import kraken.runtime.engine.dto.OverrideDependency;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.context.ContextField;
import kraken.runtime.model.rule.Dependency;
import kraken.runtime.model.rule.RuntimeRule;
import org.apache.commons.lang3.StringUtils;

import static kraken.model.context.PrimitiveFieldDataType.isPrimitiveType;

/**
 * Utility for extracting Rule Override Dependencies for Rule.
 *
 * @author mulevicius
 */
public class OverrideDependencyExtractor {

    private KrakenExpressionEvaluator krakenExpressionEvaluator;

    public OverrideDependencyExtractor(KrakenExpressionEvaluator krakenExpressionEvaluator) {
        this.krakenExpressionEvaluator = krakenExpressionEvaluator;
    }

    public Map<String, OverrideDependency> extractOverrideDependencies(RuntimeRule rule, DataContext dataContext) {
        Map<String, OverrideDependency> overrideDependencies = new HashMap<>();

        if(rule.getDependencies() == null || dataContext.getContextDefinition() == null) {
            return overrideDependencies;
        }

        for(Dependency dependency : rule.getDependencies()) {
            if(isCrossContextFieldDependency(dependency)) {
                ExternalDataReference reference = dataContext.getExternalReferences().get(dependency.getContextName());
                if(isCrossContextReferenceSingular(reference)) {
                    ContextField contextField = reference.getDataContext().getContextDefinition().getFields().get(dependency.getTargetPath());
                    if (isFieldSimplePrimitive(contextField)) {
                        Object value = krakenExpressionEvaluator.evaluateGetProperty(
                                contextField.getFieldPath(),
                                reference.getDataContext().getDataObject()
                        );
                        String name = reference.getDataContext().getContextName() + "." + contextField.getName();
                        PrimitiveFieldDataType type = PrimitiveFieldDataType.valueOf(contextField.getFieldType());
                        overrideDependencies.put(name, new OverrideDependency(name, value, type));
                    }
                }
            }
        }

        return overrideDependencies;
    }

    private boolean isCrossContextFieldDependency(Dependency dependency) {
        return dependency.isContextDependency() && StringUtils.isNotEmpty(dependency.getTargetPath());
    }

    private boolean isCrossContextReferenceSingular(ExternalDataReference reference) {
        return reference != null && reference.getCardinality() == Cardinality.SINGLE;
    }

    private boolean isFieldSimplePrimitive(ContextField contextField) {
        return contextField != null
                && contextField.getCardinality() == Cardinality.SINGLE
                && isPrimitiveType(contextField.getFieldType());
    }

}
