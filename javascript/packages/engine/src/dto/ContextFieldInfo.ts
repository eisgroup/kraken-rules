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

import { Contexts } from "kraken-model";
import { DataContext } from "../engine/contexts/data/DataContext";
import { ErrorCode, KrakenRuntimeError } from "../error/KrakenRuntimeError";

export class ContextFieldInfo {
    readonly contextId: string;
    readonly contextName: string;
    readonly fieldName: string;
    readonly fieldPath: string;
    readonly fieldType: string;
    constructor(dataContext: DataContext, fieldName: string) {
        let projection: Contexts.ContextField;
        if (dataContext.definitionProjection && dataContext.definitionProjection[fieldName]) {
            projection = dataContext.definitionProjection[fieldName];
        } else {
            throw new KrakenRuntimeError(
                ErrorCode.INCORRECT_MODEL_TREE,
                `Fields projection for a DataContext instance lacks of field '${fieldName}'`
            );
        }
        this.contextId = dataContext.contextId;
        this.contextName = dataContext.contextName;
        this.fieldName = fieldName;
        this.fieldPath = projection.fieldPath;
        this.fieldType = projection.fieldType;
    }
}
