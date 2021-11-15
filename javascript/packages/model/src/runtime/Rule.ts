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

import { Dependency } from "./Dependency";
import { Metadata } from "./Metadata";
import { Payloads } from "./Payloads";
import { Condition } from "./Condition";

export interface Rule {
    name: string;
    context: string;
    targetPath: string;
    condition?: Condition;
    payload: Payloads.Payload;
    description?: string;
    dependencies?: Dependency[];
    metadata?: Metadata;
    /**
     * Rule is considered dimensional if it is marked as dimensional in {@link Metadata#asMap()}
     * Or rule has two or more versions.
     * Default is *false*.
     *
     * @return is rule dimensional
     * @see {@link Metadata}
     * @since 1.0.41
     */
    dimensional: boolean;
}
