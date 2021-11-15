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
import { Moment } from "moment";

import { Switch, InputNumber, Input, DatePicker, Select } from "antd";
import "antd/lib/switch/style";
import "antd/lib/input/style";
import "antd/lib/input-number/style";
import "antd/lib/date-picker/style";
import { SelectValue } from "antd/lib/select";

export interface InputRendererProps<T> {
    disabled: boolean;
    style: React.CSSProperties;
    value: T;
    onChange: (e: React.FormEvent<HTMLInputElement> | Moment | React.ReactText | boolean | SelectValue) => void;
    className: string;
    selections?: string[];
}

export const renderers = {
    select: (config: InputRendererProps<string>) => {
        return <Select
            value={config.value}
            onChange={(e: SelectValue) => {
                config.onChange(e.toString());
            }} {...config}
        >
            {config.selections && config.selections.map(key => <Select.Option key={key} value={key}>{key}
            </Select.Option>)}
        </Select>;
    },
    input: (config: InputRendererProps<string>) => <Input onChange={(e: any) => config.onChange(e)} {...config} />,
    inputDisabled: (config: InputRendererProps<string>) =>
        <Input {...config} disabled onChange={(e: any) => config.onChange(e)} />,
    inputNumber: (config: InputRendererProps<number>) => <InputNumber {...config} />,
    inputCurrency: (config: InputRendererProps<number>) => (
        <InputNumber
            formatter={value => `$ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ",")}
            {...config}
        />
    ),
    date: (config: InputRendererProps<Moment>) => <DatePicker {...config} style={{ marginBottom: "5px" }} />,
    dateTime: (config: InputRendererProps<Moment>) =>
        <DatePicker showTime={{ format: "HH:mm:ss" }} format="YYYY-MM-DD HH:mm:ss" {...config} />,
    boolean: (config: InputRendererProps<boolean>) => (
        <span style={config.style}>
            <Switch
                disabled={config.disabled}
                defaultChecked={false}
                checked={config.value}
                onChange={config.onChange}
            />
        </span>
    )
};
