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

import java.io.Serializable;

/**
 * Implementation of {@link ContextField}
 *
 * @author rimas
 * @since 1.0
 */
public class ContextFieldImpl implements ContextField, Serializable {

    private String name;

    private String fieldPath;

    private Cardinality cardinality;

    private String fieldType;

    private boolean external;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Cardinality getCardinality() {
        return cardinality;
    }

    @Override
    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public String getFieldType() {
        return fieldType;
    }

    @Override
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    @Override
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public void setFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
    }

    @Override
    public String toString() {
        return getName() + " : " + getFieldType().toLowerCase() + (getCardinality().equals(Cardinality.MULTIPLE) ? "[]" : "");
    }

    @Override
    public boolean isExternal() {
        return external;
    }

    @Override
    public void setExternal(boolean external) {
        this.external = external;
    }
}
