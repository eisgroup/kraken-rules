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

import { DataContextTypes } from "./DataContext.types";
import { DataContext } from "./DataContext";

export class ExternalReferences {

    readonly references: DataContextTypes.ExternalObjectReferences = {};
    readonly singleDataContexts: Record<string, DataContext> = {};

    addSingle(contextName: string, dc: DataContext | undefined): void {
        // tslint:disable-next-line: triple-equals
        if (dc && this.references[contextName] == null) {
            this.references[contextName] = dc.dataObject;
            this.singleDataContexts[contextName] = dc;
        }
    }

    addMultiple(contextName: string, dcs: DataContext[] | undefined): void {
        // tslint:disable-next-line: triple-equals
        if (dcs && dcs.length && this.references[contextName] == null) {
            this.references[contextName] = dcs.map(d => d.dataObject);
        }
    }
}
