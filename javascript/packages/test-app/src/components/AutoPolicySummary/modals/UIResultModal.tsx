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
import { FieldEvaluationResult, RuleEvaluationResults } from "kraken-typescript-engine";
import { JsonView } from "../JsonView";
import { ValidationStatus } from "../../../rule-engine/ValidationStatusReducer";

const TabPane = Tabs.TabPane;

export interface ResultModalProps {
    modalVisibility: boolean;
    onClickShowModal: () => void;
    fieldResults?: { [keyof: string]: FieldEvaluationResult }[];
    allRuleResults?: RuleEvaluationResults.RuleEvaluationResult[];
    validationStatus?: ValidationStatus;
}

export class UIResultModal extends React.Component<ResultModalProps> {
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
                        <TabPane tab="All rule result" key="1">
                            <JsonView data={this.props.allRuleResults} />
                        </TabPane>
                        <TabPane tab="Field result" key="2">
                            <JsonView data={this.props.fieldResults} />
                        </TabPane>
                        <TabPane tab="Validation reducer result" key="5">
                            <JsonView data={this.props.validationStatus} />
                        </TabPane>
                    </Tabs>
                </Modal>
            </div>
        );
    }
}
