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
package kraken.cross.context.path;

import kraken.model.context.Cardinality;

import java.util.List;
import java.util.Objects;

/**
 * @author psurinin
 */
public class CrossContextPath {

    private final List<String> path;
    private final Cardinality cardinality;

    public CrossContextPath(List<String> path, Cardinality cardinality) {
        this.path = path;
        this.cardinality = cardinality;
    }

    public List<String> getPath() {
        return path;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CrossContextPath that = (CrossContextPath) o;

        return Objects.equals(path, that.path) &&
                cardinality == that.cardinality;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, cardinality);
    }

    @Override
    public String toString() {
        return String.join(".", path);
    }

}
