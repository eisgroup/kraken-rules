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
package kraken.runtime.engine.context;

import java.util.Collection;
import java.util.List;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.rule.Dependency;

/**
 * Resolves context data instance for rule in given root context
 *
 * @author rimas
 * @since 1.0
 */
public interface ContextDataProvider {

    List<DataContext> resolveContextData(String contextName, Collection<Dependency> fieldDependencies);

}
