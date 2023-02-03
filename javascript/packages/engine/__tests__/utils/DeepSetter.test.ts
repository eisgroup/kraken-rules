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

import { DeepSetter } from '../../src/utils/DeepSetter'

interface Person {
    name?: string
    surname?: string
    children?: Person[]
}

const person = (): Person => ({
    name: 'John',
    children: [
        {
            name: 'Coca',
        },
        {
            name: 'Cola',
            children: [
                {
                    name: 'Audio',
                },
                {
                    name: 'Technika',
                },
            ],
        },
    ],
})

describe('DeepSetter', () => {
    it('should set value to exsisting property', () => {
        const newPerson: Person = DeepSetter.on(person()).set('Albert', 'name').get()
        expect(newPerson.name).toBe('Albert')
        expect(person().name).toBe(person().name)
    })
    it('should set value to exsisting property deep in array', () => {
        const newPerson: Person = DeepSetter.on(person()).set('Video', 'children.1.children.0.name').get()
        expect(newPerson.children![1].children![0].name).toBe('Video')
        expect(person().children![1].children![0].name).toBe('Audio')
    })
    it('should set value to non-exsisting property', () => {
        const newPerson: Person = DeepSetter.on(person()).set('Einstein', 'surname').get()
        expect(newPerson.surname).toBe('Einstein')
        expect(person().surname).not.toBeDefined()
    })
    it('should set value to non-exsisting property deep in array', () => {
        const newPerson: Person = DeepSetter.on(person()).set('Video', 'children.0.children.0.surname').get()
        expect(newPerson.children![0].children![0].surname).toBe('Video')
    })
    it('should set value to non-exsisting property on empty object', () => {
        const newPerson: Person = DeepSetter.on({} as Person)
            .set('Video', 'children.0.children.0.surname')
            .get()
        expect(newPerson.children![0].children![0].surname).toBe('Video')
    })
})
