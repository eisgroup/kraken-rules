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

export type PrimitiveType = boolean | number | string;
export type PrimitiveArrayType = boolean[] | number[] | string[];

/**
 * Utility class to help set value to object by provided path.
 * @example
 *  const person = {
 *      name: "John"
 * };
 *  const newPerson = DeepSetter
 *    .on(person)
 *    .set("Deep", "surname")
 *    .set("Brad", "children.0.name")
 *    .set("Put", "children.0.surname")
 *    .get();
 * )
 */
export class DeepSetter {

    constructor(private data: object) {
    }

    /**
     * Creates {@link DeepSetter} instance with deep copy of data.
     * @param {Object} data     Object to set new values in
     * @returns {DeepSetter}    {@link DeepSetter} instance
     */
    static on(data: object): DeepSetter {
        return new DeepSetter(Object.assign({}, data));
    }

    /**
     *
     * @param {PrimitiveType | PrimitiveArrayType} value    value to set
     * @param {string} path                                 path must be separated only by {@code .}
     *                                                      (e.g. "a.b.0.c.4", access equivalent would be a.b[0].c[4])
     * @returns {DeepSetter}                                return instance with changed data
     */
    set(value: PrimitiveType | PrimitiveArrayType, path: string): DeepSetter {
        const pathArray = path.split(".");
        let obj: any = this.data;
        let i = 0;
        for (i; i < pathArray.length - 1; i++) {
            if (pathArray[i] in obj) {
                obj = obj[pathArray[i]];
            } else {
                obj[pathArray[i]] = isNaN(parseInt(pathArray[i + 1], 0)) ? {} : [];
                obj = obj[pathArray[i]];
            }
        }
        obj[pathArray[i]] = value;
        return this;
    }

    /**
     * Returns data object with new values
     */
    get(): object {
        return this.data;
    }
}
