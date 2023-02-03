/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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

/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 * or incorporated into any other media without EIS Group prior written consent.
 */

import { Contexts } from 'kraken-model'
import { DataContext } from './DataContext'

export namespace DataContextTypes {
    export type ReferenceDataContextObject = Record<string, DataContext[]>
    export type ExternalReferences = Record<Contexts.Cardinality, ReferenceDataContextObject>
    export type ExternalObjectReferences = Record<string, object | object[] | undefined>
}
