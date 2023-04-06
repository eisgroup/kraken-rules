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
package kraken.model.project.builder;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;

/**
 * @author mulevicius
 */
public class ContextDefinitionEquality {

    /**
     * Checks if {@link ContextDefinition}s are equal.
     *
     * @param contextDefinition1 the first {@link ContextDefinition} used for comparison
     * @param contextDefinition2 the second {@link ContextDefinition} used for comparison
     * @return true if {@link ContextDefinition}s are equal, false otherwise
     */
    static boolean areEqual(ContextDefinition contextDefinition1, ContextDefinition contextDefinition2) {
        return Objects.equals(contextDefinition1.getName(), contextDefinition2.getName())
            && Objects.equals(contextDefinition1.isSystem(), contextDefinition2.isSystem())
            && Objects.equals(contextDefinition1.getPhysicalNamespace(), contextDefinition2.getPhysicalNamespace())
            && Objects.equals(contextDefinition1.getParentDefinitions(), contextDefinition2.getParentDefinitions())
            && Objects.equals(contextDefinition1.isRoot(), contextDefinition2.isRoot())
            && Objects.equals(contextDefinition1.isStrict(), contextDefinition2.isStrict())
            && contextDefinitionsChildrenAreEqual(contextDefinition1, contextDefinition2)
            && contextDefinitionsContextFieldsAreEqual(contextDefinition1, contextDefinition2);
    }

    /**
     * Checks if {@link ContextDefinition}s children are equal by navigation expressions and target names.
     *
     * @param contextDefinition1 the first {@link ContextDefinition} used for comparison
     * @param contextDefinition2 the second {@link ContextDefinition} used for comparison
     * @return true if {@link ContextDefinition}s children are equal, false otherwise
     */
    private static boolean contextDefinitionsChildrenAreEqual(ContextDefinition contextDefinition1, ContextDefinition contextDefinition2){
        Map<String, ContextNavigation> contextChildren1 = contextDefinition1.getChildren();
        Map<String, ContextNavigation> contextChildren2 = contextDefinition2.getChildren();
        return contextChildren2.keySet().containsAll(contextChildren1.keySet())
                && contextChildren1.keySet().containsAll(contextChildren2.keySet())
                && contextChildren2.keySet().stream()
                .filter(contextNavigationExpressionsAreEqual(contextChildren1, contextChildren2))
                .filter(contextNavigationTargetNamesAreEqual(contextChildren1, contextChildren2))
                .filter(contextNavigationCardinalitiesAreEqual(contextChildren1, contextChildren2))
                .filter(contextNavigationAreEqualByForbidReference(contextChildren1, contextChildren2))
                .count() == contextChildren2.keySet().size();
    }

    /**
     * Checks if {@link ContextDefinition}s context fields are equal by cardinality, field path, field type, and name.
     *
     * @param contextDefinition1 the first {@link ContextDefinition} used for comparison
     * @param contextDefinition2 the second {@link ContextDefinition} used for comparison
     * @return true if {@link ContextDefinition}s children are equal, false otherwise
     */
    private static boolean contextDefinitionsContextFieldsAreEqual(ContextDefinition contextDefinition1, ContextDefinition contextDefinition2){
        Map<String, ContextField> contextFields1 = contextDefinition1.getContextFields();
        Map<String, ContextField> contextFields2 = contextDefinition2.getContextFields();
        return contextFields2.keySet().containsAll(contextFields1.keySet())
            && contextFields1.keySet().containsAll(contextFields2.keySet())
            && contextFields2.keySet().stream()
            .filter(contextFieldsAreEqualByCardinality(contextFields1, contextFields2))
            .filter(contextFieldsAreEqualByFieldPath(contextFields1, contextFields2))
            .filter(contextFieldsAreEqualByFieldType(contextFields1, contextFields2))
            .filter(contextFieldsAreEqualByName(contextFields1, contextFields2))
            .filter(contextFieldsAreEqualByForbidTarget(contextFields1, contextFields2))
            .filter(contextFieldsAreEqualByForbidReference(contextFields1, contextFields2))
            .count() == contextFields2.keySet().size();
    }

    private static Predicate<String> contextNavigationTargetNamesAreEqual(Map<String, ContextNavigation> contextChildren1,
                                                                   Map<String, ContextNavigation> contextChildren2){
        return key -> Objects.equals(contextChildren1.get(key).getTargetName(), contextChildren2.get(key).getTargetName());
    }

    private static Predicate<String> contextNavigationCardinalitiesAreEqual(Map<String, ContextNavigation> contextChildren1,
                                                                            Map<String, ContextNavigation> contextChildren2){
        return key -> Objects.equals(contextChildren1.get(key).getCardinality(), contextChildren2.get(key).getCardinality());
    }

    private static Predicate<String> contextNavigationExpressionsAreEqual(Map<String, ContextNavigation> contextChildren1,
                                                                   Map<String, ContextNavigation> contextChildren2){
        return key -> Objects.equals(contextChildren1.get(key).getNavigationExpression(), contextChildren2.get(key).getNavigationExpression());
    }

    private static Predicate<String> contextNavigationAreEqualByForbidReference(
        Map<String, ContextNavigation> contextChildren1,
        Map<String, ContextNavigation> contextChildren2
    ) {
        return key -> Objects.equals(
            contextChildren1.get(key).getForbidReference(),
            contextChildren2.get(key).getForbidReference()
        );
    }

    private static Predicate<String> contextFieldsAreEqualByCardinality(Map<String, ContextField> contextFields1,
                                                                 Map<String, ContextField> contextFields2){
        return key -> Objects.equals(contextFields1.get(key).getCardinality(), contextFields2.get(key).getCardinality());
    }

    private static Predicate<String> contextFieldsAreEqualByFieldPath(Map<String, ContextField> contextFields1,
                                                               Map<String, ContextField> contextFields2){
        return key -> Objects.equals(contextFields1.get(key).getFieldPath(), contextFields2.get(key).getFieldPath());
    }

    private static Predicate<String> contextFieldsAreEqualByFieldType(Map<String, ContextField> contextFields1,
                                                               Map<String, ContextField> contextFields2){
        return key -> Objects.equals(contextFields1.get(key).getFieldType(), contextFields2.get(key).getFieldType());
    }

    private static Predicate<String> contextFieldsAreEqualByName(Map<String, ContextField> contextFields1,
                                                          Map<String, ContextField> contextFields2){
        return key -> Objects.equals(contextFields1.get(key).getName(), contextFields2.get(key).getName());
    }

    private static Predicate<String> contextFieldsAreEqualByForbidTarget(Map<String, ContextField> contextFields1,
                                                                         Map<String, ContextField> contextFields2){
        return key ->
            Objects.equals(contextFields1.get(key).getForbidTarget(), contextFields2.get(key).getForbidTarget());
    }

    private static Predicate<String> contextFieldsAreEqualByForbidReference(Map<String, ContextField> contextFields1,
                                                                            Map<String, ContextField> contextFields2){
        return key ->
            Objects.equals(contextFields1.get(key).getForbidReference(), contextFields2.get(key).getForbidReference());
    }
}
