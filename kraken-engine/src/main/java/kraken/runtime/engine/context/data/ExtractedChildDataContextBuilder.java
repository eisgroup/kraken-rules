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
package kraken.runtime.engine.context.data;

import kraken.runtime.model.context.ContextNavigation;
import kraken.runtime.engine.context.extraction.ContextChildExtractionInfo;
import kraken.runtime.engine.context.extraction.instance.ContextExtractionResultBuilder;
import kraken.runtime.expressions.KrakenExpressionEvaluator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author psurinin
 */
public class ExtractedChildDataContextBuilder {

    private final DataContextBuilder dataContextBuilder;

    private final ContextExtractionResultBuilder childBuilder;

    private final KrakenExpressionEvaluator evaluator;

    public ExtractedChildDataContextBuilder(
            DataContextBuilder dataContextBuilder,
            ContextExtractionResultBuilder childBuilder,
            KrakenExpressionEvaluator evaluator
    ) {
        this.dataContextBuilder = dataContextBuilder;
        this.childBuilder = childBuilder;
        this.evaluator = evaluator;
    }

    public List<DataContext> resolveImmediateChildren(ContextChildExtractionInfo info) {
        return Optional
                .ofNullable(resolveInheritedChildren(info))
                .orElseGet(() -> resolveExtractedChildren(info));
    }

    private List<DataContext> resolveExtractedChildren(ContextChildExtractionInfo info) {
        ContextNavigation contextNavigation = info.getFrom()
                .getChildren()
                .get(info.getChildContextName());

        Object extractedChild = evaluator.evaluateNavigationExpression(contextNavigation, info.getParentDataContext().getDataObject());

        return childBuilder.buildFrom(extractedChild).stream()
                .map(instance -> dataContextBuilder.buildFromExtractedObject(instance, info))
            .collect(Collectors.toList());
}

    private List<DataContext> resolveInheritedChildren(ContextChildExtractionInfo info) {
        // if its a step up on inheritance tree, just create data context with same object and different name
        final Collection<String> inheritedContexts = info.getFrom().getInheritedContexts();
        final boolean hasParents = Objects.nonNull(inheritedContexts) && !inheritedContexts.isEmpty();
        // if any of inherited contexts equals next in chain, create copy with its name
        final boolean parentsContainsChild =
                hasParents && inheritedContexts.contains(info.getChildContextName());
        if (hasParents && parentsContainsChild) {
            final DataContext dataContext = dataContextBuilder.buildForAncestorObject(
                    info.getParentDataContext().getDataObject(),
                    info.getChildContextName(),
                    info.getParentDataContext());
            return Collections.singletonList(dataContext);
        }
        return null;
    }
}
