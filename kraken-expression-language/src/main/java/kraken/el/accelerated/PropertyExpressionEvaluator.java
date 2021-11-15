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
package kraken.el.accelerated;

import kraken.el.ExpressionEvaluationException;

/**
 * Evaluates bean property expression by trying to use registered {@link AcceleratedPropertyHandler} in the system.
 * If such handler does not exist then a reflection based evaluator is used.
 *
 * @author mulevicius
 */
public class PropertyExpressionEvaluator {

    private ReflectivePropertyExpressionEvaluator reflectivePropertyExpressionEvaluator;

    public PropertyExpressionEvaluator() {
        this.reflectivePropertyExpressionEvaluator = new ReflectivePropertyExpressionEvaluator();
    }

    public Object evaluate(String property, Object dataObject) throws ExpressionEvaluationException {
        AcceleratedPropertyHandler propertyHandler = AcceleratedPropertyHandlerProvider.findPropertyHandlerFor(dataObject);
        if(propertyHandler != null) {
            return propertyHandler.get(property, propertyHandler.getType().cast(dataObject));
        }
        return reflectivePropertyExpressionEvaluator.get(property, dataObject);
    }
}
