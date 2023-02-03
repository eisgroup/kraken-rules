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
package kraken.model.context.external;

import java.io.Serializable;
import java.util.Objects;

/**
 * Default implementation of {@code ExternalContextDefinitionReference}.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public final class ExternalContextDefinitionReferenceImpl implements ExternalContextDefinitionReference, Serializable {

    private static final long serialVersionUID = 7870488749447470845L;

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExternalContextDefinitionReferenceImpl that = (ExternalContextDefinitionReferenceImpl) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

}
