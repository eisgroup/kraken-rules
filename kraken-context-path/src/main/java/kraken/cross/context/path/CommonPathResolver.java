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

import java.util.List;

/**
 * Utility class for resolving common path elements.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public final class CommonPathResolver {

    private CommonPathResolver() {

    }

    /**
     * Resolves common context path between source and target paths.
     *
     * @param source Source context path.
     * @param target Target context path.
     * @return Common context path between source and target.
     */
    public static ContextPath getCommonPath(ContextPath source, ContextPath target) {
        return getCommonPath(source.getPath(), target.getPath());
    }

    private static ContextPath getCommonPath(List<String> source, List<String> target) {
        ContextPath.ContextPathBuilder commonPathBuilder = new ContextPath.ContextPathBuilder();

        for (int i = 0; i < Math.min(source.size(), target.size()); i++) {
            if (source.get(i).equals(target.get(i))) {
                commonPathBuilder.addPathElement(source.get(i));
            } else {
                break;
            }
        }

        return commonPathBuilder.build();
    }

}
