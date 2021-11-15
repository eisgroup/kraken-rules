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
package kraken.utils.dto;

import java.util.Objects;

/**
 * @author psurinin
 */
public class Triplet<L, C, R> {
    private final L left;
    private final C center;
    private final R right;

    public Triplet(L left, C center, R right) {
        this.left = left;
        this.center = center;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public C getCenter() {
        return center;
    }

    public R getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Triplet{" +
                "left=" + left +
                ", center=" + center +
                ", right=" + right +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Triplet<?, ?, ?> triplet = (Triplet<?, ?, ?>) o;
        return Objects.equals(left, triplet.left) &&
                Objects.equals(center, triplet.center) &&
                Objects.equals(right, triplet.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, center, right);
    }
}
