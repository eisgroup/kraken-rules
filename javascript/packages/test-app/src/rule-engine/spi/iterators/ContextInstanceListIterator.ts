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

/**
 * Iterator are used in Kraken java implementation. For now there is no use cases of using iterator
 * so this code in this folder is not included in any engine  process.
 * It is kept in case some use cases of iterator will appear.
 * Also this folder is excluded to collect coverage from in jest config.
 */
import { ContextInstanceIterator } from './ContextInstanceIterator'

export class ContextInstanceListIterator<K = unknown> implements ContextInstanceIterator<number, K> {
    private list: K[]
    private idx: number
    private lastIndex: number

    constructor(list: K[]) {
        this.list = [...list]
        this.idx = 0
        this.lastIndex = -1
    }

    hasNext: () => boolean = () => this.idx < this.list.length

    next: () => K = () => {
        const val = this.list[this.idx]
        this.lastIndex = this.idx
        this.idx = this.idx + 1
        return val
    }

    index: () => number = () => {
        if (this.lastIndex >= 0) {
            return this.lastIndex
        }
        throw new Error('Use next() first')
    }
}
