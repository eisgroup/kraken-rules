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
package kraken.context.model.tree.repository;

import kraken.context.model.tree.ContextModelTree;
import kraken.el.TargetEnvironment;

/**
 * Repository, that contains {@link ContextModelTree}s.
 *
 @author psurinin
 */
public interface ContextModelTreeRepository {

    ContextModelTree get(String namespace, TargetEnvironment targetEnvironment);
}
