/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import java.util.Collection;
import java.util.Objects;

/**
 * Represents single external context definition.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class DSLExternalContextDefinition {

    private final String name;
    private final Collection<DSLExternalContextDefinitionField> fields;

    public DSLExternalContextDefinition(String name, Collection<DSLExternalContextDefinitionField> fields) {
        this.name = Objects.requireNonNull(name);
        this.fields = Objects.requireNonNull(fields);
    }

    public String getName() {
        return name;
    }

    public Collection<DSLExternalContextDefinitionField> getFields() {
        return fields;
    }

}
