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

import "antd/lib/radio/style";
import "antd/lib/input/style";
import "antd/lib/input-number/style";
import "antd/lib/date-picker/style";
import "antd/lib/button/style";
import "antd/lib/spin/style";
import "antd/lib/select/style";
import "antd/lib/collapse/style";
import "antd/lib/row/style";
import "antd/lib/list/style";
import "antd/lib/notification/style";
import "antd/lib/slider/style";

import { Button, Spin, Row, Select, Collapse, Popover, Radio } from "antd";

import { SingleField, ManyFields } from "../core/field/SingleField";
import { renderers } from "../core/RenderInputFunctions";
import { entryPointNames } from "../../common/constants";

import { AutoPolicyViewState, EvaluationResponse } from "./Policy.container";
import { TransactionDetails } from "./TransactionDetails";
import { PolicyDetails } from "./PolicyDetails";
import { TermDetails } from "./TermDetails";
import { AccessTrackInfo } from "./AccessTrackInfo";
import { CreditCardInfo } from "./CreditCardInfo";
import { Insured } from "./Insured";
import { Vehicle } from "./Vehicle";
import { Party } from "./Party";
import { UIResultModal } from "./modals/UIResultModal";
import { BackendResultModal } from "./modals/BackendResultModal";
import { ModelModal } from "./modals/ModelModal";
import { ImportModal } from "./modals/ImportModal";

import { TestProduct } from "kraken-test-product";
import domain = TestProduct.kraken.testproduct.domain;
import { ErrorBoundary } from "../../ErrorBoundary";
import { ContextDefinitionInfo } from "../core/field/ContextDefinitionInfo";
import { EntityBox } from "../core/EntityBox";
import { Referer } from "./Referer";
import { ExpressionTreeModal } from "./modals/ExpressionTreeModal";

export interface AutoPolicyCallbackFunctionProps {
    onClickShowExpressionModal: () => void;
    onClickShowUIResultModal: () => void;
    onClickShowBackendResultModal: () => void;
    onClickShowModelModal: () => void;
    onClickShowImportModal: () => void;
    onImportableModelSubmit: () => void;
    onImportableModelChange: (e: React.FormEvent<HTMLTextAreaElement>) => void;
    onValidationEntryPointChange: (validationEntryPointName: string) => void;
    validate: () => void;
    onClickExportCurrentState: () => void;
    onClickResetUI: () => void;
    onStateChange: (state: string) => void;
    onPolicyChange: (e: React.FormEvent<HTMLInputElement>) => void;
    onCreatedFromPolicyRevChange: (e: React.ReactText) => void;
    onPoliciesChange: (e: string[]) => void;
    onTransDetailsChange: (det: domain.TransactionDetails) => void;
    onPolicyDetailsChange: (det: domain.PolicyDetail) => void;
    onTermDetailsChange: (det: domain.TermDetails) => void;
    onAccessTrackInfoChange: (accessTrackInfo: domain.AccessTrackInfo) => void;
    onCreditCardInfoChange: (creditCardInfo: domain.CreditCardInfo) => void;
    onInsuredChange: (insured: domain.Insured) => void;
    onVehicleChange: (changedVehicleIndex: Number) => (det: domain.Vehicle) => void;
    onPartyChange: (party: domain.Party) => void;
    onRefToCustomerChange: (e: React.FormEvent<HTMLInputElement>) => void;
    onClickResetEntryPoint: () => void;
    onRefererChange: (r: domain.Referer) => void;
}

export interface AutoPolicyProps extends AutoPolicyCallbackFunctionProps {
    autoPolicyViewState: AutoPolicyViewState & EvaluationResponse;
}

export class Policy extends React.Component<AutoPolicyProps> {
    render(): JSX.Element {
        const { autoPolicyViewState } = this.props;
        return (
            <Spin spinning={false} size="large">
                {/* <Spin spinning={autoPolicyViewState.isLoading} size="large"> */}
                <div>
                    <b>{autoPolicyViewState.buildInfo && autoPolicyViewState.buildInfo.date}</b>
                    <UIResultModal
                        onClickShowModal={this.props.onClickShowUIResultModal}
                        modalVisibility={autoPolicyViewState.isUIResultModalVisible}
                        fieldResults={autoPolicyViewState.fieldResults}
                        allRuleResults={autoPolicyViewState.allRuleResults}
                        validationStatus={autoPolicyViewState.validationStatus}
                    />
                    <BackendResultModal
                        onClickShowModal={this.props.onClickShowBackendResultModal}
                        modalVisibility={autoPolicyViewState.isBackendResultModalVisible}
                        backendRawResult={autoPolicyViewState.backendRawResult}
                        backendDefaultResult={autoPolicyViewState.backendDefaultResult}
                        backendValidationResult={autoPolicyViewState.backendValidationResult}
                    />
                    <ModelModal
                        onClickShowModal={this.props.onClickShowModelModal}
                        modalVisibility={autoPolicyViewState.isModelModalVisible}
                        modelBeforeEvaluation={autoPolicyViewState.modelBeforeEvaluation}
                        modelAfterEvaluation={autoPolicyViewState.modelAfterEvaluation}
                        modelCurrentState={autoPolicyViewState.modelCurrentState}
                    />
                    <ExpressionTreeModal
                        modalVisibility={autoPolicyViewState.isExpressionModalVisible}
                        onClickShowModal={this.props.onClickShowExpressionModal}
                    />
                    <ImportModal
                        modalVisibility={autoPolicyViewState.isImportModalVisible}
                        onClickShowModal={this.props.onClickShowImportModal}
                        onImportableModelSubmit={this.props.onImportableModelSubmit}
                        onImportableModelChange={this.props.onImportableModelChange}
                        importableModelState={autoPolicyViewState.importableModelState}
                    />
                    <div id="buttonPanel">
                        <Select
                            className="entry-point-name-select"
                            defaultValue={entryPointNames.UI}
                            onChange={this.props.onValidationEntryPointChange}
                            style={{ width: 200 }}
                        >
                            {Object.keys(entryPointNames).map((entryPointName: string) => (
                                <Select.Option key={entryPointName} value={entryPointName}>
                                    {entryPointName}
                                </Select.Option>
                            ))}
                        </Select>
                        <Button
                            loading={this.props.autoPolicyViewState.isLoading}
                            type="dashed"
                            icon="code-o"
                            onClick={this.props.validate}
                        >
                            Validate
                        </Button>
                        <Button type="dashed" icon="eye-o" onClick={this.props.onClickShowUIResultModal}>
                            UI result
                        </Button>
                        <Button
                            type={this.props.autoPolicyViewState.isBackendFailed ? "danger" : "dashed"}
                            icon="eye-o"
                            onClick={this.props.onClickShowBackendResultModal}
                        >
                            Backend result
                        </Button>
                        <Button type="dashed" icon="eye-o" onClick={this.props.onClickShowModelModal}>
                            Model
                        </Button>
                        <Button type="dashed" icon="experiment" onClick={this.props.onClickShowExpressionModal}>
                            Expression
                        </Button>
                        <Button type="dashed" icon="upload" onClick={this.props.onClickShowImportModal}>
                            Import model
                        </Button>
                        <Button type="dashed" icon="download" onClick={this.props.onClickExportCurrentState}>
                            Export state
                        </Button>
                        <Button type="danger" icon="delete" onClick={this.props.onClickResetUI}>
                            Reset form
                        </Button>
                        <Button type="danger" icon="reload" onClick={this.props.onClickResetEntryPoint}>
                            Reset EntryPoint
                        </Button>
                        <br />
                        <span>
                            Status <b id="status">{autoPolicyViewState.isLoading ? "Loading" : "OK"}</b>&nbsp;
                        </span>
                        <br />
                    </div>
                    <ErrorBoundary>
                        <EntityBox title="Policy" >
                            <ContextDefinitionInfo contextName="Policy" />
                            <Row>
                                <SingleField
                                    id={autoPolicyViewState.autoPolicy.id}
                                    value={autoPolicyViewState.autoPolicy.state}
                                    contextName="Policy"
                                    modelFieldName="state"
                                    onChange={this.props.onStateChange}
                                    // tslint:disable-next-line: max-line-length
                                    info="dimension: state, use 'Dimensional' entrypoint to get rules based on that dimension"
                                    renderInput={renderers.select}
                                    selections={["CA", "AZ"]}

                                />
                                <SingleField
                                    id={autoPolicyViewState.autoPolicy.id}
                                    value={autoPolicyViewState.autoPolicy.policyNumber}
                                    contextName="Policy"
                                    modelFieldName="policyNumber"
                                    onChange={this.props.onPolicyChange}
                                    renderInput={renderers.input}
                                />
                                <SingleField
                                    id={autoPolicyViewState.autoPolicy.id}
                                    value={autoPolicyViewState.autoPolicy.createdFromPolicyRev}
                                    contextName="Policy"
                                    modelFieldName="createdFromPolicyRev"
                                    onChange={this.props.onCreatedFromPolicyRevChange}
                                    renderInput={renderers.inputNumber}
                                />
                                <SingleField
                                    id={autoPolicyViewState.autoPolicy.id}
                                    value={autoPolicyViewState.autoPolicy.refToCustomer}
                                    contextName="Policy"
                                    modelFieldName="refToCustomer"
                                    onChange={this.props.onRefToCustomerChange}
                                    renderInput={renderers.inputDisabled}
                                    info="This field is readonly"
                                />
                                <ManyFields
                                    id={autoPolicyViewState.autoPolicy.id}
                                    value={autoPolicyViewState.autoPolicy.policies}
                                    contextName="Policy"
                                    modelFieldName="policies"
                                    onChange={this.props.onPoliciesChange}
                                    renderInput={renderers.input}
                                />
                            </Row>
                            <TransactionDetails
                                id={autoPolicyViewState.autoPolicy.id}
                                value={autoPolicyViewState.autoPolicy.transactionDetails}
                                onChange={this.props.onTransDetailsChange}
                            />
                            <PolicyDetails
                                id={autoPolicyViewState.autoPolicy.id}
                                value={autoPolicyViewState.autoPolicy.policyDetail}
                                onChange={this.props.onPolicyDetailsChange}
                            />
                            <TermDetails
                                id={autoPolicyViewState.autoPolicy.id}
                                value={autoPolicyViewState.autoPolicy.termDetails}
                                onChange={this.props.onTermDetailsChange}
                            />
                            <AccessTrackInfo
                                id={autoPolicyViewState.autoPolicy.id}
                                value={autoPolicyViewState.autoPolicy.accessTrackInfo}
                                onChange={this.props.onAccessTrackInfoChange}
                            />
                        </EntityBox>
                        <EntityBox title="Referer" >
                            <ContextDefinitionInfo contextName="Referer" />
                            <Referer
                                id={autoPolicyViewState.autoPolicy.referer.id}
                                value={autoPolicyViewState.autoPolicy.referer}
                                onChange={this.props.onRefererChange}
                            />
                        </EntityBox>
                        <EntityBox title="CreditCardInfo" >
                            <ContextDefinitionInfo contextName="CreditCardInfo" />
                            <CreditCardInfo
                                id={autoPolicyViewState.autoPolicy.billingInfo.creditCardInfo.id}
                                value={autoPolicyViewState.autoPolicy.billingInfo.creditCardInfo}
                                onChange={this.props.onCreditCardInfoChange}
                            />
                        </EntityBox>
                        <EntityBox title="Insured" >
                            <ContextDefinitionInfo contextName="Insured" />
                            <Insured
                                id={autoPolicyViewState.autoPolicy.insured.id}
                                value={autoPolicyViewState.autoPolicy.insured}
                                onChange={this.props.onInsuredChange}
                            />
                        </EntityBox>
                        <EntityBox title="Risk Items" >
                            <Collapse>
                                {autoPolicyViewState.autoPolicy.riskItems.map((riskItem, index) => (
                                    <Collapse.Panel header={"Vehicle " + index} key={riskItem.id}>
                                        <Vehicle
                                            id={autoPolicyViewState.autoPolicy.riskItems[index].id}
                                            value={autoPolicyViewState.autoPolicy.riskItems[index]}
                                            onChange={this.props.onVehicleChange(index)}
                                        />
                                    </Collapse.Panel>
                                ))}
                            </Collapse>
                        </EntityBox>
                        <EntityBox title="Party" >
                            <Party
                                id={autoPolicyViewState.autoPolicy.parties[0].id}
                                value={autoPolicyViewState.autoPolicy.parties[0]}
                                onChange={this.props.onPartyChange}
                            />
                        </EntityBox>
                    </ErrorBoundary>
                </div>
            </Spin >
        );
    }
}
