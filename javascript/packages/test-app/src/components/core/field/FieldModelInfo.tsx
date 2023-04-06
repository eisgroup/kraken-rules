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

import { CopyToClipboard } from 'react-copy-to-clipboard'
import { Tag, Icon, Popover, Tooltip } from 'antd'
import 'antd/lib/popover/style'
import 'antd/lib/tooltip/style'
import 'antd/lib/tag/style'
import 'antd/lib/icon/style'
import { ContextField } from '../../../rule-engine/api'
import { RuleCreator } from './RuleCreator'

export interface FieldModelInfoProps {
    fieldInfo: ContextField
    contextName: string
    fieldName: string
    id: string
}

function typeColor(props: FieldModelInfoProps): string {
    return props.fieldInfo.fieldType === 'not found' ? 'red' : 'blue'
}

export const tagStyle = { margin: '2px' }
export class FieldModelInfo extends React.PureComponent<FieldModelInfoProps> {
    render(): JSX.Element {
        return (
            <div>
                <Tag color='blue' style={tagStyle}>
                    {contextReference(this.props)}
                </Tag>
                <Tag className='copyccr' color='blue' style={tagStyle}>
                    <CopyToClipboard text={contextReference(this.props)}>
                        <Tooltip placement='bottom' title={'Copy CCR reference'}>
                            <Icon type='link' style={{ fontSize: '14px', color: '#1890ffe8', cursor: 'pointer' }} />
                        </Tooltip>
                    </CopyToClipboard>
                </Tag>
                <Tag className='createrule' color='blue' style={tagStyle}>
                    <Popover
                        content={
                            <RuleCreator
                                id={this.props.id}
                                fieldName={this.props.fieldName}
                                contextName={this.props.contextName}
                            />
                        }
                        trigger='click'
                        title='Create rule'
                    >
                        <Tooltip placement='bottom' title={'Create rule for this field'}>
                            <Icon type='login' style={{ fontSize: '14px', color: '#1890ffe8', cursor: 'pointer' }} />
                        </Tooltip>
                    </Popover>
                </Tag>

                <br />
                <Tag color={typeColor(this.props)} style={tagStyle}>
                    {this.props.fieldInfo.fieldType}
                </Tag>
                <Tag color={typeColor(this.props)} style={tagStyle}>
                    {this.props.fieldInfo.cardinality}
                </Tag>

                {this.props.fieldInfo.forbidTarget && (
                    <Tag color='blue' style={tagStyle}>
                        {'FORBID_TARGET'}
                    </Tag>
                )}

                {this.props.fieldInfo.forbidReference && (
                    <Tag color='blue' style={tagStyle}>
                        {'FORBID_REFERENCE'}
                    </Tag>
                )}
            </div>
        )
    }
}

function contextReference(props: FieldModelInfoProps): string {
    return `${props.contextName}.${props.fieldName}`
}
