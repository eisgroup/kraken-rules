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

// tslint:disable:no-any
import * as React from "react";
import { FieldProps, FieldComponent } from "./FieldComponent";
import { Icon, Tag } from "antd";
import "antd/lib/icon/style";
import "antd/lib/tag/style";
import { isMoment } from "moment";
import { trans } from "../DateUtils";
import moment = require("moment");
import { tagStyle } from "./FieldModelInfo";

interface State {
    numberOfRecords: number;
    values: any[];
}

function modelValue(value: any): any {
    if (typeof value === "boolean" || typeof value === "number" || typeof value === "string") {
        return value;
    }
    // tslint:disable-next-line:triple-equals
    if (value.target && value.target.value != undefined) {
        return value.target.value;
    }
    if (isMoment(value)) {
        return trans.toDate(value);
    }
}

function componentValue(value: any): any {
    if (value instanceof Date) {
        return moment(value);
    }
    return value;
}

export class MultipleField extends React.Component<FieldProps, State> {

    constructor(props: FieldProps) {
        super(props);
        this.state = {
            numberOfRecords: Array.isArray(props.value) && props.value.length ? props.value.length : 1,
            values: Array.isArray(props.value) ? props.value.map(componentValue) : []
        };
    }

    componentWillReceiveProps(props: Readonly<FieldProps>): void {
        this.setState({
            values: Array.isArray(props.value) ? props.value.map(componentValue) : [],
            numberOfRecords: Array.isArray(props.value) && props.value.length ? props.value.length : 1
        });
    }

    addValue = (index: number) => (e: any) => {
        const arr = this.state.values.slice();
        arr[index] = componentValue(modelValue(e));
        this.setState({ values: arr });
        this.props.onChange(arr.map(modelValue));
    }

    inputs(): JSX.Element[] {
        const els = [];
        for (let index = 0; index < this.state.numberOfRecords; index++) {
            const element = (<span key={index}>
                {this.props.renderInput({ value: this.state.values[index], onChange: this.addValue(index) })}
            </span>);
            els.push(element);
        }
        return els;
    }

    renderInputs = () => <span>{this.inputs()}</span>;

    addInput = (): void => {
        this.setState({ numberOfRecords: this.state.numberOfRecords + 1 });
    }

    removeInput = (): void => {
        this.setState(
            { numberOfRecords: this.state.numberOfRecords - 1 },
            () => this.props.onChange(this.state.values.slice(0, this.state.values.length - 1).map(modelValue))
        );

    }

    render(): JSX.Element {
        return (
            <span>
                <FieldComponent {...this.props} renderInput={this.renderInputs} />
                <span onClick={this.addInput}>
                    <Tag color="green" style={tagStyle}>
                        <Icon type="plus-circle" />
                    </Tag>
                </span>
                <span onClick={this.removeInput}>
                    <Tag color="red" style={tagStyle}>
                        <Icon type="minus-circle" />
                    </Tag>
                </span>
            </span>);
    }
}
