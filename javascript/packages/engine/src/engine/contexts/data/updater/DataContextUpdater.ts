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

import { DataContext } from "../DataContext";

export type DataContextDependency = {
    contextName: string
};

/**
 * Updates {@link DataContext#externalReferences}. It uses paths from
 * EntryPointBundle to extract references. All References are extracted as an
 * array. Same data context references can be updated more than once, if rules
 * are defined on different context definitions, that are parent or ancestors.
 * External references in that case are being merged.
 */
export interface DataContextUpdater {
    update(dataContext: DataContext, dependency: DataContextDependency): void;
}
