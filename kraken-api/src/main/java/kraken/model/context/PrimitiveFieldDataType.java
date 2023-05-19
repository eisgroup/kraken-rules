/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.model.context;

import kraken.annotations.API;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Kraken primitive data type
 *
 * @since 1.1.0
 */
@API
public enum PrimitiveFieldDataType {

    INTEGER, DECIMAL, STRING, BOOLEAN, DATE, DATETIME, MONEY;

    private static final Set<String> primitiveFieldDataTypes = Arrays.stream(PrimitiveFieldDataType.values())
            .map(PrimitiveFieldDataType::toString)
            .collect(Collectors.toSet());

    public static boolean isPrimitiveType(String type) {
        return primitiveFieldDataTypes.contains(type);
    }

}
