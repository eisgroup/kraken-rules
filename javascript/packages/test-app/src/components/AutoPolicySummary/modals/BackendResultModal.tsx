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

import { Modal, Tabs } from "antd";
import "antd/lib/modal/style";
import "antd/lib/tabs/style";
import { JsonView } from "../JsonView";

const TabPane = Tabs.TabPane;

export interface ResultModalProps {
    modalVisibility: boolean;
    onClickShowModal: () => void;
    backendRawResult: object;
    backendDefaultResult: object;
    backendValidationResult: object;
}

export class BackendResultModal extends React.Component<ResultModalProps> {
    render(): JSX.Element {
        return (
            <div>
                <Modal
                    visible={this.props.modalVisibility}
                    onCancel={this.props.onClickShowModal}
                    // tslint:disable-next-line
                    footer={null}
                    width={1000}
                >
                    <Tabs defaultActiveKey="1" size={"large"}>
                        <TabPane tab="All rules result" key="1">
                            <JsonView data={this.props.backendRawResult} />
                        </TabPane>
                        <TabPane tab="Default result" key="2">
                            <JsonView data={this.props.backendDefaultResult} />
                        </TabPane>
                        <TabPane tab="Validation result" key="3">
                            <JsonView data={this.props.backendValidationResult} />
                        </TabPane>
                    </Tabs>
                </Modal>
            </div>
        );
    }
}
