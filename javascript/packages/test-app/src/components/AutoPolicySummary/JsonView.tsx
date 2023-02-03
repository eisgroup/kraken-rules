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
import ReactJson from 'react-json-view'

export interface JsonViewProps {
    data: object
}

export const JsonView: React.SFC<JsonViewProps> = (props: JsonViewProps) => {
    return <ReactJson shouldCollapse={field => (field.name === null ? false : true)} name={null} src={props.data} />
}
