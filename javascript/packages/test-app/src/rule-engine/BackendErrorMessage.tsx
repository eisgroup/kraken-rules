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
import { upperFirst } from 'lodash'

export interface BackendError {
    error: Record<string, unknown>
    response: {
        data: {
            status: number
            error: string
            exception: string
            message: string
            path: string
        }
    }
}

const style = {
    listItem: { borderBottom: '1px solid grey', textAlign: 'left' } as React.CSSProperties,
    info: { fontSize: '11px' } as React.CSSProperties,
}

/**
 * React Component used to display error message from backend
 */
export const BackendErrorMessage = ({ error }: { error: BackendError }) => {
    return (
        <div style={{ width: 500 }}>
            {Object.keys(error.response.data).map(key => (
                <p key={key} style={style.listItem}>
                    <b>{upperFirst(key)}:</b> {error.response.data[key]}
                </p>
            ))}
        </div>
    )
}

export const messageFrom = (error: BackendError) => <BackendErrorMessage error={error} />
