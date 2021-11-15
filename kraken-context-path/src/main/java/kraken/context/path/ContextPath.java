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
package kraken.context.path;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Class containing a path to context definition node in a model tree.
 *
 * @author psurinin
 * @since 1.0
 */
public class ContextPath implements Serializable {

    public static final String PATH_SEPARATOR = ".";

    private static final long serialVersionUID = -5455930846432886645L;
    private final List<String> path;

    private ContextPath(List<String> path) {
        this.path = path;
    }

    public static class ContextPathBuilder {

        private final List<String> path;

        public ContextPathBuilder() {
            path = new ArrayList<>();
        }

        public ContextPathBuilder(String contextName) {
            path = new ArrayList<>();
            path.add(contextName);
        }

        /**
         * Appends given path element at the end of existing path element
         * collection.
         *
         * @param pathElement Path element to add.
         * @return Builder.
         */
        public ContextPathBuilder addPathElement(String pathElement) {
            this.path.add(pathElement);
            return this;
        }

        /**
         * Appends given path element at the beginning of existing path element
         * collection.
         *
         * @param pathElement Path element to add.
         * @return Builder.
         */
        public ContextPathBuilder addFirstPathElement(String pathElement) {
            this.path.add(0, pathElement);
            return this;
        }

        /**
         * Appends given path elements at the start of existing path element
         * collection.
         *
         * @param pathElements Path elements to add.
         * @return Builder.
         */
        public ContextPathBuilder addPathElements(List<String> pathElements) {
            this.path.addAll(0, pathElements);
            return this;
        }

        public ContextPath build() {
            return new ContextPath(path);
        }
    }

    /**
     * Returns a path to context definition node in order from root node
     * to context definition node.
     *
     * @return A collection of path elements to context definition.
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * Returns a sub-path of this context path starting from index location of
     * given path element (exclusive).
     *
     * @param pathElement Path element.
     * @return Sub-path starting form given path element.
     */
    public List<String> getPathFrom(String pathElement) {
        return path.subList(path.indexOf(pathElement) + 1, path.size());
    }

    /**
     * Returns a sub-path of this context path starting from index location of
     * given path element (inclusive).
     *
     * @param pathElement Path element.
     * @return Sub-path starting form given path element.
     */
    public List<String> getPathFromInclusive(String pathElement) {
        final ArrayList<String> list = new ArrayList<>(getPathFrom(pathElement));
        list.add(0, pathElement);

        return list;
    }

    /**
     * Returns a root path element of this context path. Root element is always
     * a first element in a collection of path elements.
     *
     * @return Root path element.
     */
    public String getRootElement() {
        return path.get(0);
    }

    /**
     * Returns a last path element of this context path.
     *
     * @return Last path element.
     */
    public String getLastElement() {
        return path.get(path.size() - 1);
    }

    /**
     * Returns length of context path.
     *
     * @return Path length.
     */
    public int getPathLength() {
        return path.size();
    }

    /**
     * Performs a check whether this context path contains given path element.
     *
     * @param pathElement Path element to test presence of.
     * @return {@code true} if context path contains the path element.
     */
    public boolean contains(String pathElement) {
        return getPath().contains(pathElement);
    }

    /**
     * Returns path as string separated by dot from root to target context.
     *
     * @return Path as string.
     */
    public String getPathAsString() {
        return String.join(PATH_SEPARATOR, path);
    }

    @Override
    public String toString() {
        return getPathAsString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContextPath that = (ContextPath) o;

        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

}
