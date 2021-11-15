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
import { TestProduct } from "kraken-test-product";

import { InnerInputsComponentProps, SingleField } from "../core/field/SingleField";
import { renderers } from "../core/RenderInputFunctions";

import domain = TestProduct.kraken.testproduct.domain;
import { withMetadata } from "../core/ContextHOC";

class Component extends React.Component<InnerInputsComponentProps<domain.PolicyDetail>> {
    onCurrentQuoteIndChange = (e: boolean) => {
        this.props.onChange(Object.assign({}, this.props.value, { currentQuoteInd: e }));
    }

    onVersionDescriptionChange = (e: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { versionDescription: e.currentTarget.value }));
    }

    onOosProcessingStageChange = (e: React.FormEvent<HTMLInputElement>) => {
        this.props.onChange(Object.assign({}, this.props.value, { oosProcessingStage: e.currentTarget.value }));
    }

    render(): JSX.Element {
        const { id, value } = this.props;
        return (
            <Row>
                <SingleField
                    id={id}
                    value={value.currentQuoteInd}
                    contextName="Policy"
                    modelFieldName="currentQuoteInd"
                    onChange={this.onCurrentQuoteIndChange}

                    renderInput={renderers.boolean}
                />
                <SingleField
                    id={id}
                    value={value.versionDescription}
                    contextName="Policy"
                    modelFieldName="versionDescription"
                    onChange={this.onVersionDescriptionChange}

                    renderInput={renderers.input}
                />
                <SingleField
                    id={id}
                    value={value.oosProcessingStage}
                    contextName="Policy"
                    modelFieldName="oosProcessingStage"
                    onChange={this.onOosProcessingStageChange}

                    renderInput={renderers.input}
                />
            </Row>
        );
    }
}
export const PolicyDetails = withMetadata(Component);
