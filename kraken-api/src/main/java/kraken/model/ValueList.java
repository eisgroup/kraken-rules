/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import kraken.annotations.API;
import kraken.el.math.Numbers;
import kraken.model.context.PrimitiveFieldDataType;

/**
 * Represents a collection of values of a known type. Refer to {@link DataType} for
 * enumeration of types supported in {@code ValueList}.
 *
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
@API
public final class ValueList {

    private final DataType valueType;
    private final List<?> values;

    private ValueList(DataType valueType, List<?> values) {
        this.valueType = valueType;
        this.values = values;
    }

    public static ValueList fromNumber(List<Number> values) {
        return new ValueList(DataType.DECIMAL, Collections.unmodifiableList(values));
    }

    public static ValueList fromString(List<String> values) {
        return new ValueList(DataType.STRING, Collections.unmodifiableList(values));
    }

    public DataType getValueType() {
        return valueType;
    }

    public List<?> getValues() {
        return values;
    }

    public boolean has(@Nonnull Number value) {
        if (valueType != DataType.DECIMAL) {
            return false;
        }
        return values.stream().anyMatch(v -> Numbers.areEqual((Number) v, value));
    }

    public boolean has(@Nonnull String value) {
        if (valueType != DataType.STRING) {
            return false;
        }
        return values.stream().anyMatch(v -> v.equals(value));
    }

    public String valuesAsString() {
        return values.stream()
            .map(Objects::toString)
            .collect(Collectors.joining(", "));
    }

    /**
     * Enumerates all supported types for {@link ValueList} container.
     */
    @API
    public enum DataType {

        DECIMAL(
            Set.of(
                PrimitiveFieldDataType.INTEGER,
                PrimitiveFieldDataType.DECIMAL,
                PrimitiveFieldDataType.MONEY
            )
        ),

        STRING(
            Set.of(
                PrimitiveFieldDataType.STRING
            )
        );

        private static final Set<String> supportedFieldDataTypes = Arrays.stream(values())
            .flatMap(dataType -> dataType.getFieldTypes().stream())
            .map(PrimitiveFieldDataType::toString)
            .collect(Collectors.toSet());

        private final Set<PrimitiveFieldDataType> fieldTypes;

        DataType(Set<PrimitiveFieldDataType> fieldTypes) {
            this.fieldTypes = fieldTypes;
        }

        public Set<PrimitiveFieldDataType> getFieldTypes() {
            return fieldTypes;
        }

        public static boolean isSupportedFieldDataType(String fieldDataType) {
            return supportedFieldDataTypes.contains(fieldDataType);
        }

    }

}
