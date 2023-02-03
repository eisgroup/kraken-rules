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

import * as React from 'react'

import { Tag, Tooltip } from 'antd'
import { KRAKEN_MODEL_TREE_POLICY } from 'kraken-test-product-model-tree'
import { ContextModelTree } from 'kraken-typescript-engine'
const modelTree = KRAKEN_MODEL_TREE_POLICY as ContextModelTree.ContextModelTree

const tagStyle: React.CSSProperties = { margin: '2px' }

const NULL = null
export const ContextDefinitionInfo = React.memo((props: { contextName: string }) => {
    const projection = modelTree.contexts[props.contextName].fields
    const inheritance = modelTree.contexts[props.contextName].inheritedContexts
    const children = modelTree.contexts[props.contextName].children
    function resolveFieldNames(contextName: string): string[] {
        const contextDefinition = modelTree.contexts[contextName]
        return Object.keys(contextDefinition.fields) || []
    }

    return (
        <div className='ContextDefinitionInfo'>
            <h2>{props.contextName}</h2>
            {Object.keys(children).length ? (
                <React.Fragment>
                    <h4>Children</h4>
                    <div>
                        {Object.keys(children).map(p => (
                            <Tag key={p} style={tagStyle}>
                                {p}
                            </Tag>
                        ))}
                    </div>
                </React.Fragment>
            ) : (
                NULL
            )}
            {Object.keys(projection).length ? (
                <React.Fragment>
                    <h4>Projection Fields</h4>
                    <div>
                        {Object.keys(projection).map(p => {
                            const isChildAndField = Object.keys(children).includes(projection[p].fieldType)
                            const isField = Object.keys(projection).includes(projection[p].name)
                            const tag = (
                                <Tag color={isChildAndField ? 'orange' : isField ? 'blue' : undefined} style={tagStyle}>
                                    {p}
                                </Tag>
                            )
                            return (
                                <Tooltip
                                    key={p}
                                    placement='bottom'
                                    title={
                                        isField
                                            ? isChildAndField
                                                ? 'Context child and field: ' + projection[p].fieldType
                                                : 'Context field'
                                            : 'Inherited field'
                                    }
                                >
                                    {tag}
                                </Tooltip>
                            )
                        })}
                    </div>
                </React.Fragment>
            ) : (
                NULL
            )}
            {inheritance.length ? (
                <React.Fragment>
                    <h4>Parent Definitions</h4>
                    <div>
                        {inheritance.map(p => (
                            <Tooltip key={p} placement='bottom' title={'Fields: ' + resolveFieldNames(p).join(' | ')}>
                                <Tag style={tagStyle}>{p}</Tag>
                            </Tooltip>
                        ))}
                    </div>
                </React.Fragment>
            ) : (
                NULL
            )}
        </div>
    )
})
