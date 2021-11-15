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
 * API for resolving cross context paths.
 *
 * @author Tomas Dapkunas
 * @since 1.1.1
 */
public interface CrossContextPathsResolver {

    /**
     * Resolves all navigable cross context paths between from given path to given target
     * context definition.
     *
     * @param fromPath          Context Path to resolve paths from.
     * @param targetContextName Name of target context to resolve paths to.
     * @return A list of cross context paths resolved from given path to target context.
     */
    List<CrossContextPath> resolvePaths(ContextPath fromPath, String targetContextName);

}
