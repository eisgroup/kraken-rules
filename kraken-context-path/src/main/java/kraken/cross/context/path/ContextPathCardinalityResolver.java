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
package kraken.cross.context.path;

import kraken.context.path.ContextPath;
import kraken.model.context.Cardinality;

/**
 * API for resolving context path cardinality.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public final class ContextPathCardinalityResolver {

    private final ContextCardinalityResolver contextCardinalityResolver;

    private ContextPathCardinalityResolver(ContextCardinalityResolver contextCardinalityResolver) {
        this.contextCardinalityResolver = contextCardinalityResolver;
    }

    public static ContextPathCardinalityResolver create(ContextCardinalityResolver contextCardinalityResolver) {
        return new ContextPathCardinalityResolver(contextCardinalityResolver);
    }

    /**
     * Resolves cardinality of given path based on relationship cardinality between path nodes.
     * If cardinality of at least one relationship is multiple, then resolved cardinality will
     * be multiple, otherwise cardinality is resolved as single.
     *
     * @param contextPath Context path to resolve cardinality for.
     * @return Cardinality for context path.
     */
    public Cardinality resolve(ContextPath contextPath) {
        if (contextPath.getPath().size() == 1) {
            return Cardinality.SINGLE;
        }

        for (int i = 0; i < contextPath.getPath().size() - 1; i++) {
            Cardinality parentChildCardinality = resolveParentChildCardinality(
                    contextPath.getPath().get(i),
                    contextPath.getPath().get(i + 1)
            );

            if (parentChildCardinality == Cardinality.MULTIPLE) {
                return Cardinality.MULTIPLE;
            }
        }

        return Cardinality.SINGLE;
    }

    private Cardinality resolveParentChildCardinality(String parent, String child) {
        return contextCardinalityResolver.getCardinality(parent, child);
    }

}
