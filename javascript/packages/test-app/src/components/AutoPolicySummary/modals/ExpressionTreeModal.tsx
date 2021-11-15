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

import { Modal } from "antd";
import "antd/lib/modal/style";
import "antd/lib/tabs/style";
import { ExpressionTree } from "../../../expression/tree/ExpressionTree";
import { KRAKEN_MODEL_TREE_POLICY } from "kraken-test-product-model-tree";

export interface ImportModalProps {
    modalVisibility: boolean;
    onClickShowModal: () => void;
}

export class ExpressionTreeModal extends React.Component<ImportModalProps> {
    render(): JSX.Element {
        return (
            <div>
                <Modal
                    title={"Expression Analysis"}
                    visible={this.props.modalVisibility}
                    onCancel={this.props.onClickShowModal}
                    width={"95%"}
                >
                    <ExpressionTree scopes={Object.keys(KRAKEN_MODEL_TREE_POLICY.contexts)} />
                </Modal>
            </div >
        );
    }
}
