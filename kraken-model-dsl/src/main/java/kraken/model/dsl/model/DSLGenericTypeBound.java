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
package kraken.model.dsl.model;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents generic type bounds parsed from Kraken DSL
 *
 * @author mulevicius
 */
public class DSLGenericTypeBound {

    private final String generic;
    private final String bound;

    public DSLGenericTypeBound(String generic, String bound) {
        this.generic = Objects.requireNonNull(generic);
        this.bound = Objects.requireNonNull(bound);
    }

    public String getGeneric() {
        return generic;
    }

    public String getBound() {
        return bound;
    }
}
