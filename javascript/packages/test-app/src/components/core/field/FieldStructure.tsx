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

import { Col } from 'antd'
import 'antd/lib/col/style'

export interface FieldStructureProps {
    isVisible: boolean
}

export const FieldStructure = (props: React.Props<FieldStructureProps> & FieldStructureProps) =>
    props.isVisible ? (
        <Col xs={12} sm={12} md={8} lg={6} xl={4}>
            {props.children}
        </Col>
    ) : null
