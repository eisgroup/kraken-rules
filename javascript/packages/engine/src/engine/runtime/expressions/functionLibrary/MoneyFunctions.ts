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
import { Moneys } from '../math/Moneys'

function FromMoney(obj?: unknown): number | undefined | null {
    if (obj && Moneys.isMoney(obj)) {
        return obj.amount
    }
    if (typeof obj === 'number' || obj == null) {
        return obj as number | null | undefined
    }
    throw new Error(`Type '${typeof obj}' of parameter is not supported.`)
}

export const moneyFunctions = { FromMoney }
