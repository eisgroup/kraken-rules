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
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import kraken.annotations.API;

/**
 * Defines all data types supported by Kraken Dimension.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
@API
public enum DimensionDataType {

    STRING("String"),
    BOOLEAN("Boolean"),
    INTEGER("Integer"),
    DECIMAL("Decimal"),
    DATE("Date"),
    DATETIME("Datetime");

    private static final Map<String, DimensionDataType> dimensionDataTypes = Arrays.stream(values())
        .collect(Collectors.toMap(Enum::name, type -> type));

    private final String renderedRepresentation;

    DimensionDataType(String renderedRepresentation) {
        this.renderedRepresentation = renderedRepresentation;
    }

    public static boolean isDimensionType(String type) {
        return dimensionDataTypes.containsKey(type);
    }

    @Nullable
    public static DimensionDataType getDataType(String type) {
        return dimensionDataTypes.get(type);
    }

    public String getRenderedRepresentation() {
        return renderedRepresentation;
    }

    @Override
    public String toString() {
        return renderedRepresentation;
    }

}
