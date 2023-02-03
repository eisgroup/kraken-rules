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

import axios from 'axios'
import { optional } from 'declarative-js'
import { memoize } from 'lodash'
import { message } from 'antd'
import { messageFrom, BackendError } from './BackendErrorMessage'

const post = memoize(axios.post, (p1, p2) => JSON.stringify({ p1, p2 }))

async function resolveBackendUrl(): Promise<string> {
    let backend
    try {
        backend = await axios
            .get(
                window.location.protocol +
                    '//' +
                    // host with port (example: localhost:3000)
                    window.location.host +
                    '/path',
            )
            .then(r => {
                const response = r.data
                return response
            })
    } catch (error) {
        backend = 'http://localhost:8888'
    }
    return backend
}

const url = resolveBackendUrl()

export interface BuildInfo {
    date: string
    startTime: string
    number: string
    revision: string
}

export interface ContextDefinitionInfo {
    fieldType: string
    cardinality: string
}

export interface ContextDefinition {
    contextFields: {
        [keyof: string]: ContextDefinitionInfo
    }
    parentDefinitions: string[]
}

export const fetch = {
    scope: {
        typeRegistry: () => url.then(host => axios.get(`${host}/scope/type-registry`)).then(response => response.data),
        scope: (contextDefinitionName: string) =>
            url.then(host => axios.get(`${host}/scope/${contextDefinitionName}`)).then(response => response.data),
    },
    rule: {
        qa: (entryPointName: string) =>
            url.then(host => axios.get(`${host}/rule/${entryPointName}`)).then(response => response.data),
        namesJson: (entryPointName: string) =>
            url
                .then(host => axios.post(`${host}/entrypoint`, { name: entryPointName }))
                .then(response => response.data.ruleNames),
        byName: (ruleName: string) =>
            url.then(host => axios.post(`${host}/rule`, { name: ruleName })).then(response => response.data),
        json: (entryPointName: string) =>
            fetch.rule.namesJson(entryPointName).then(ruleNames => ruleNames.map(fetch.rule.byName)),
    },

    raw: (data: object, entryPointName: string) =>
        url.then(host => axios.post(`${host}/evaluation/${entryPointName}/raw`, data)).then(response => response.data),
    validations: (data: object, entryPointName: string) =>
        url
            .then(host => axios.post(`${host}/evaluation/${entryPointName}/validation`, data))
            .then(response => response.data),

    bundle: (name: string, isDelta: boolean, dimensions?: object) =>
        url
            .then(host => axios.post(`${host}/bundle/${name}`, { dimensions }, { params: { isDelta } }))
            .then(response => response.data)
            .catch((err: BackendError) => message.error(messageFrom(err))),
    buildInfo: (): Promise<BuildInfo> =>
        url.then(host => axios.get(`${host}/build/properties`)).then(response => response.data),
    context: (contextName: string) =>
        url
            .then()
            .then(host => post(`${host}/context`, { name: contextName }))
            .then(response => response.data),
    contextFieldType: (contextName: string, fieldName: string): Promise<ContextDefinitionInfo> =>
        url
            .then(host => post(`${host}/context`, { name: contextName }))
            .then(response => response.data as ContextDefinition)
            .then(cd =>
                optional(cd.contextFields[fieldName])
                    .map(x =>
                        Promise.resolve({
                            fieldType: x.fieldType.toLowerCase(),
                            cardinality: x.cardinality.toLowerCase(),
                        }),
                    )
                    .orElseGet(() => fetch.contextFieldType(cd.parentDefinitions[0], fieldName)),
            ),

    createRule: function createRule(rules: unknown): Promise<boolean> {
        return url
            .then(host =>
                axios.post(`${host}/dynamic/rule/dsl/UI`, rules, { headers: { 'Content-Type': 'text/plain' } }),
            )
            .then(() => true)
    },
    resetEntryPoint: function resetEntryPoint(name: string): Promise<void> {
        return url.then(host => axios.post(`${host}/dynamic/entrypoint/${name}/reset`)).then(() => void 0)
    },
}
