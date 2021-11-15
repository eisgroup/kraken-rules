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

import { AutoPolicyContext } from "../AutoPolicySummary/Policy.container";
import { ValidationMetadata, InnerInputsComponentProps } from "./field/SingleField";

type Omit<T, K extends keyof T> = Pick<T, Exclude<keyof T, K>>;

export interface AppContextInterface {
    metadata: { [key: string]: ValidationMetadata };
}

export type ReactComponent<P> = React.ComponentClass<P> | React.StatelessComponent<P>;

export function withMetadata<T, P extends InnerInputsComponentProps<T>, R = Omit<P, "metadata">>(
    Component: ReactComponent<P>): React.FunctionComponent<R> {
    // tslint:disable-next-line:no-any
    return function BoundComponent(props: any): JSX.Element {
        const idProp = props.contextName ? { id: props.contextName + "." + props.modelFieldName + "." + props.id } : {};
        return (
            <div {...idProp}>
                <AutoPolicyContext.Consumer>
                    {value => <Component {...props} metadata={value.metadata} />}
                </AutoPolicyContext.Consumer>
            </div>
        );
    };
}
