/* eslint-disable @typescript-eslint/no-non-null-assertion */
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

import { DataObjectInfoResolver } from 'kraken-typescript-engine'

import { TestProduct } from 'kraken-test-product'
import Identifiable = TestProduct.kraken.testproduct.domain.meta.Identifiable

export const infoResolver: DataObjectInfoResolver = {
    /**
     * @override
     */
    validate: (data: Identifiable) => {
        const validationErrors = []
        if (!Object.keys(data).length) {
            validationErrors.push({ message: 'Validation data object cannot be empty' })
        }

        if (data['id'] == undefined) {
            validationErrors.push({ message: "Validation data object field 'id' is required" })
        }

        if (data['cd'] == undefined) {
            validationErrors.push({ message: "Validation data object field 'cd' is required" })
        }
        return validationErrors
    },
    /**
     * @override
     */
    resolveId: data => (data as Identifiable).id!,
    /**
     * @override
     */
    resolveName: data => (data as Identifiable).cd!,
}
