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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import kraken.context.path.ContextPath;
import kraken.model.context.ContextDefinition;

import static java.util.Objects.nonNull;

/**
 * @author psurinin
 */
public class DataContexts {

    /**
     * Finds root of {@link DataContext} in parent chain if it is present
     */
    public static List<DataContext> parents(DataContext dataContext) {
        final List<DataContext> elements = new ArrayList<>();
        DataContext currentDataContext = dataContext;
        while (nonNull(currentDataContext)) {
            elements.add(currentDataContext);
            currentDataContext = currentDataContext.getParentDataContext();
        }
        return Lists.reverse(elements);
    }

    /**
     * Returns path to {@link DataContext} as a string ({@link ContextDefinition#getName()})
     * collecting it from root, with parents chain
     */
    public static String getPathAsStringTo(DataContext dataContext) {
        final List<String> elements = new ArrayList<>();
        DataContext currentDataContext = dataContext;
        while (nonNull(currentDataContext)) {
            elements.add(currentDataContext.getContextName());
            currentDataContext = currentDataContext.getParentDataContext();
        }
        return Lists.reverse(elements).stream().collect(Collectors.joining("."));
    }

    /**
     * Returns a calculated context path to {@code DataContext}.
     *
     * @param dataContext Data context.
     * @return Calculated context path.
     */
    public static ContextPath getAsContextPath(DataContext dataContext) {
        final ContextPath.ContextPathBuilder builder = new ContextPath.ContextPathBuilder();
        DataContext currentDataContext = dataContext;

        while (nonNull(currentDataContext)) {
            builder.addFirstPathElement(currentDataContext.getContextName());
            currentDataContext = currentDataContext.getParentDataContext();
        }

        return builder.build();
    }

}
