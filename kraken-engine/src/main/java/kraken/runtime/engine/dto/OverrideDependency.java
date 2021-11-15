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
package kraken.runtime.engine.dto;

import kraken.annotations.API;
import kraken.model.context.PrimitiveFieldDataType;

/**
 * Represents a value that the rule override depends on
 *
 * @author mulevicius
 */
@API
public class OverrideDependency {

    /**
     * name of the variable; in case this is a Cross Context Reference then name will follow a pattern: {contextName}.{fieldName}
     */
    private String name;

    /**
     * value of the variable; can only be a primitive value represented by {@link PrimitiveFieldDataType}
     */
    private Object value;

    /**
     * type of variable
     */
    private PrimitiveFieldDataType type;

    public OverrideDependency(String name, Object value, PrimitiveFieldDataType type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public PrimitiveFieldDataType getType() {
        return type;
    }
}
