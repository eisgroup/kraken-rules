/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
import { requireDefinedValue } from "../../../../utils/Utils";
import { ContextDataExtractor } from "./ContextDataExtractor.types";
import { logger } from "../../../../utils/DevelopmentLogger";

/**
 * Resolves context data instance for rule in given root context
 */
export interface ContextDataProvider {
    /**
     * Extracts context instances by provided context name and adds dependencies to already
     * extracted context instance. Dependencies are also context instances. If extracted
     * context instance already has dependencies, it will be updated.
     *
     * @param {string} contextName          context definition name to extract
     * @param {Dependency[]} dependencies   dependencies, that will be extracted and added to context instance
     * @returns {DataContext[]}             context definition instances, that were extracted
     */
    resolveContextData: (contextName: string) => DataContext[];
}

export class ContextDataProviderImpl implements ContextDataProvider {
    constructor(
        private readonly root: DataContext,
        private readonly dataExtractor: ContextDataExtractor,
        private readonly restriction?: DataContext
    ) {
        requireDefinedValue(root, "Root dataContext must be defined");
        requireDefinedValue(dataExtractor, "ContextDataExtractor must be defined");
    }

    /**
     * @override
     */
    public resolveContextData(contextName: string): DataContext[] {
        const extractedDataContexts = this.dataExtractor.extractByName(contextName, this.root, this.restriction);
        logger.debug(`Extracted '${extractedDataContexts.length}' context definition '${contextName}' instances`);
        return extractedDataContexts;
    }
}
