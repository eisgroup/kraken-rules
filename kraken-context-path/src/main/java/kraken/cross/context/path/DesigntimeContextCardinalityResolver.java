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

import kraken.context.path.node.ContextPathNode;
import kraken.context.path.node.ContextPathNodeRepository;
import kraken.model.context.Cardinality;

/**
 *
 * Implementation of {@code ContextCardinalityResolver} which is backed by {@code ContextPathNodeRepository}.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public class DesigntimeContextCardinalityResolver implements ContextCardinalityResolver {

    private final ContextPathNodeRepository contextPathNodeRepository;

    private DesigntimeContextCardinalityResolver(ContextPathNodeRepository contextPathNodeRepository) {
        this.contextPathNodeRepository = contextPathNodeRepository;
    }

    public static ContextCardinalityResolver create(ContextPathNodeRepository contextPathNodeRepository) {
        return new DesigntimeContextCardinalityResolver(contextPathNodeRepository);

    }

    @Override
    public Cardinality getCardinality(String parent, String child) {
        ContextPathNode.ChildNode childNode = contextPathNodeRepository.get(parent)
                .getChildren().get(child);

        if (childNode == null) {
            throw new CrossContextNavigationException(String.format(
                    "Couldn't find navigation from '%s' to '%s'", parent, child));
        }

        return childNode.getCardinality();
    }

}
