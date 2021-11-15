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

import * as React from "react";
import { Row } from "antd";

import { InnerInputsComponentProps, SingleField } from "../core/field/SingleField";
import { renderers } from "../core/RenderInputFunctions";

import { TestProduct } from "kraken-test-product";
import domain = TestProduct.kraken.testproduct.domain;
import { withMetadata } from "../core/ContextHOC";

export class Component extends React.Component<InnerInputsComponentProps<domain.PartyRole>> {
    onRoleChange = (event: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { role: event.currentTarget.value }));
    }
    onLimitChange = (limit: React.ReactText) => {
        this.props.onChange(Object.assign({}, this.props.value, { limit }));
    }
    render(): JSX.Element {
        return (
            <Row>
                <SingleField
                    id={this.props.id}
                    value={this.props.value.role}
                    contextName="PartyRole"
                    modelFieldName="role"
                    onChange={this.onRoleChange}
                    renderInput={renderers.input}
                />
                <SingleField
                    id={this.props.id}
                    value={this.props.value.limit}
                    contextName="PartyRole"
                    modelFieldName="limit"
                    onChange={this.onLimitChange}
                    renderInput={renderers.inputNumber}
                />
            </Row>
        );
    }
}

export const PartyRole = withMetadata(Component);
