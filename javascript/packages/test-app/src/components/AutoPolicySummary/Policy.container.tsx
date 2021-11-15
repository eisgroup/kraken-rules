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
import { message } from "antd";
import "antd/lib/message/style";
import { saveAs } from "file-saver/FileSaver";
import { KRAKEN_MODEL_TREE_POLICY as modelTree } from "kraken-test-product-model-tree";
import { TestProduct } from "kraken-test-product";
import domain = TestProduct.kraken.testproduct.domain;
import {
    RepoClientCache,
    SyncEngine,
    ContextModelTree,
    FieldMetadataReducer,
    FieldEvaluationResult,
    RuleEvaluationResults
} from "kraken-typescript-engine";

import { ValidationStatus } from "../../rule-engine/ValidationStatusReducer";
import { infoResolver } from "../../rule-engine/spi";
import { reduceResult } from "../../rule-engine/ResultReducer";
import { fetch, BuildInfo } from "../../rule-engine/api";
import { mapValidationResults } from "../../rule-engine/resultMappers";
import {
    NavigationStepsContextInstanceInfoResolver
} from "../../rule-engine/spi/navpath/NavigationStepsContextInstanceInfoResolver";

import { AppContextInterface } from "../core/ContextHOC";
import { ValidationMetadata } from "../core/field/SingleField";
import { initialModel } from "../../store/model/InitialInstance";
import { entryPointNames } from "../../common/constants";
import { Policy } from "./Policy";

const getDimensions = (data: domain.Policy) => data.state ? ({ state: data.state }) : {};

const evaluationConfig = (data: domain.Policy) => (
    { currencyCd: "USD", context: { dimensions: getDimensions(data) } }
);

export interface EvaluationResponse {
    fieldResults?: { [keyof: string]: FieldEvaluationResult }[];
    allRuleResults?: RuleEvaluationResults.RuleEvaluationResult[];
    validationStatus?: ValidationStatus;
    modelBeforeEvaluation?: domain.Policy;
    modelAfterEvaluation?: domain.Policy;
    importableModelState?: string;
    backendRawResult?: object;
    backendDefaultResult?: object;
    backendValidationResult?: object;
}

export interface AutoPolicyViewState {
    isLoading: boolean;
    validationEntryPointName: string;
    isUIResultModalVisible: boolean;
    isBackendResultModalVisible: boolean;
    isModelModalVisible: boolean;
    isImportModalVisible: boolean;
    _metadata: { [key: string]: ValidationMetadata };
    autoPolicy: domain.Policy;
    modelCurrentState: domain.Policy;
    buildInfo?: BuildInfo;
    isBackendFailed: boolean;
    isExpressionModalVisible: boolean;
}

export const AutoPolicyContext = React.createContext<AppContextInterface>({ metadata: {} });

export class AutoPolicySummaryContainer extends React.Component<{}, AutoPolicyViewState & EvaluationResponse> {
    engine: SyncEngine;
    cache = new RepoClientCache(true);
    /**
     * registry to decide either load delta rules by entrypoint name either all rules.
     * key is entrypoint name, value if true, then static rules already cached and only delta is required
     */
    constructor(props: {}) {
        super(props);
        this.state = {
            isExpressionModalVisible: false,
            isLoading: true,
            validationEntryPointName: entryPointNames.UI,
            isUIResultModalVisible: false,
            isBackendResultModalVisible: false,
            isModelModalVisible: false,
            isImportModalVisible: false,
            _metadata: {},
            autoPolicy: initialModel(),
            modelCurrentState: initialModel(),
            isBackendFailed: false
        };
    }
    async componentDidMount(): Promise<void> {
        this.setState({ isLoading: true });

        message.config({
            top: 75
        });
        this.setBuildInfo();
        const addBundle = this.cache.addBundleForDimension(getDimensions(this.state.autoPolicy));
        const bundlePromises = Object.values(entryPointNames)
            .map(name => this.resolveEntryPointName(name))
            .map(name => this.fetchBundle(name)
                .then(entryPointBundle => addBundle({
                    entryPointName: name,
                    entryPointBundle
                })));
        const contextInstanceInfoResolver = new NavigationStepsContextInstanceInfoResolver(infoResolver);
        const promises = await Promise.all(bundlePromises);
        this.engine = new SyncEngine({
            cache: this.cache,
            dataInfoResolver: infoResolver,
            contextInstanceInfoResolver,
            modelTree: modelTree as unknown as ContextModelTree.ContextModelTree
        });
        this.setState({ isLoading: false });
    }
    fetchBundle = (entryPointName: string) => {
        return fetch.bundle(
            entryPointName,
            this.cache.areCachedStaticRules(entryPointName),
            getDimensions(this.state.autoPolicy)
        );
    }
    fetchNewRulesForEntryPoint = async (entryPointName: string) => {
        this.cache.clearCache();
        return this.fetchBundle(entryPointName)
            .then(entryPointBundle => this.cache.addBundleForDimension(getDimensions(this.state.autoPolicy))({
                entryPointName,
                entryPointBundle
            }));
    }
    setBuildInfo = () => fetch.buildInfo().then(info => this.setState({ buildInfo: info }));

    validate = async () => {
        this.setState({ isLoading: true });
        const entryPointName = this.resolveEntryPointName(this.state.validationEntryPointName);
        await this.fetchNewRulesForEntryPoint(entryPointName);
        const { _metadata } = this.state;
        const modelBeforeEvaluation = JSON.parse(JSON.stringify(this.state.autoPolicy));
        const entryPointResult = this.engine.evaluate(
            this.state.autoPolicy, entryPointName, evaluationConfig(this.state.autoPolicy)
        );
        const {
            allRuleResults,
            fieldResults,
            validationStatus
        } = reduceResult(entryPointResult);
        const fieldMetadata = new FieldMetadataReducer().reduce(entryPointResult);
        const metadata = mapValidationResults(_metadata)(fieldMetadata);
        let rawResult = {};
        let defaultResult = {};
        let validationResult = {};
        let isBackendFailed = false;
        try {
            rawResult = await fetch.raw(
                this.deepCopy(modelBeforeEvaluation),
                entryPointName
            );
            defaultResult = {};
            validationResult = await fetch.validations(
                this.deepCopy(modelBeforeEvaluation),
                entryPointName
            );
            const errors = [...(validationResult as any).validationStatus.errorResults];
            if (!errors.length) {
                message.success("Backend engine evaluation complete without validation errors");
            } else {
                message.info(`Backend engine evaluation complete with '${errors.length}' validation errors`);
            }
        } catch (error) {
            message.error(`Error evaluating rules on backend. For more details check "Backend Result" modal.`);
            rawResult = { error: error.response.data };
            defaultResult = { error: error.response.data };
            validationResult = { error: error.response.data };
            isBackendFailed = true;
        }
        this.setState({
            allRuleResults: allRuleResults,
            fieldResults: fieldResults,
            validationStatus: validationStatus,
            backendRawResult: rawResult,
            backendDefaultResult: defaultResult,
            backendValidationResult: validationResult,
            modelBeforeEvaluation: modelBeforeEvaluation,
            modelAfterEvaluation: this.deepCopy(this.state.autoPolicy),
            _metadata: metadata,
            isLoading: false,
            isBackendFailed
        });
    }
    deepCopy = (objectToCopy: any) => JSON.parse(JSON.stringify(objectToCopy));
    onClickResetUI = () => {
        this.setState({
            backendRawResult: {},
            backendDefaultResult: {},
            backendValidationResult: {},
            autoPolicy: initialModel(),
            modelAfterEvaluation: {},
            modelBeforeEvaluation: {},
            modelCurrentState: initialModel(),
            allRuleResults: [],
            fieldResults: [],
            validationStatus: { critical: [], warning: [], info: [] },
            _metadata: {},
            isBackendFailed: false
        });
    }
    resolveEntryPointName = (entryPointName: string) => {
        return entryPointName;
    }
    onClickExportCurrentState = () => {
        this.fetchRules(this.state.validationEntryPointName)
            .then(rules => ({
                "EntryPointRules": rules,
                "AllRuleResults": this.state.allRuleResults,
                "FieldResults": this.state.fieldResults,
                "ModelBeforeEvaluation": this.state.modelBeforeEvaluation,
                "ModelAfterEvaluation": this.state.modelAfterEvaluation
            }))
            .then(data => new Blob([this.stringifyObject(data)], { type: "application/json" }))
            .then(blob => saveAs(blob, "KrakenTestingAppUiState" + Date.now() + ".json"));
    }
    fetchRules = (entryPointName: string) => {
        const resolvedEntryPointName = this.resolveEntryPointName(entryPointName);
        return fetch.bundle(resolvedEntryPointName, false, {});
    }
    stringifyObject = (object: Object) => {
        return JSON.stringify(object, undefined, 4);
    }
    onValidationEntryPointChange = (validationEntryPointName: string) => {
        this.setState({ validationEntryPointName: entryPointNames[validationEntryPointName] });
    }
    onStateChange = (state: string) => {
        const autoPolicy = { ...this.state.autoPolicy, state };
        this.setState({ autoPolicy });
    }
    onPolicyChange = (e: React.FormEvent<HTMLInputElement>) => {
        const autoPolicy = { ...this.state.autoPolicy, policyNumber: e.currentTarget.value };
        this.setState({ autoPolicy });
    }
    onCreatedFromPolicyRevChange = (e: React.ReactText) => {
        const autoPolicy = { ...this.state.autoPolicy, createdFromPolicyRev: e as number };
        this.setState({ autoPolicy });
    }
    onTransDetailsChange = (det: domain.TransactionDetails) => {
        const autoPolicy = { ...this.state.autoPolicy, transactionDetails: det };
        this.setState({ autoPolicy });
    }
    onPolicyDetailsChange = (det: domain.PolicyDetail) => {
        const autoPolicy = { ...this.state.autoPolicy, policyDetail: det };
        this.setState({ autoPolicy });
    }
    onTermDetailsChange = (det: domain.TermDetails) => {
        const autoPolicy = { ...this.state.autoPolicy, termDetails: det };
        this.setState({ autoPolicy });
    }
    onAccessTrackInfoChange = (accessTrackInfo: domain.AccessTrackInfo) => {
        const autoPolicy = { ...this.state.autoPolicy, accessTrackInfo: accessTrackInfo };
        this.setState({ autoPolicy });
    }
    onCreditCardInfoChange = (creditCardInfo: domain.CreditCardInfo) => {
        const autoPolicy = {
            ...this.state.autoPolicy, billingInfo: { ...this.state.autoPolicy.billingInfo, creditCardInfo }
        };
        this.setState({ autoPolicy });
    }

    onInsuredChange = (insured: domain.Insured) => {
        const autoPolicy = { ...this.state.autoPolicy, insured: insured };
        this.setState({ autoPolicy });
    }

    onVehicleChange = (changedVehicleIndex: number) => (vehicle: domain.Vehicle) => {
        const autoPolicy = {
            ...this.state.autoPolicy, riskItems: this.state.autoPolicy.riskItems.map(
                (riskItem, index) => (index === changedVehicleIndex ? { ...vehicle } : riskItem)
            )
        };
        this.setState({ autoPolicy });
    }
    onPartyChange = (party: domain.Party) => {
        const autoPolicy = { ...this.state.autoPolicy, parties: [{ ...party }] };
        this.setState({ autoPolicy });
    }

    onClickShowUIResultModal = () => {
        this.setState({
            isUIResultModalVisible: !this.state.isUIResultModalVisible
        });
    }
    onClickShowBackendResultModal = () => {
        this.setState({
            isBackendResultModalVisible: !this.state.isBackendResultModalVisible
        });
    }
    onExpressionModalShow = () => {
        this.setState({
            isExpressionModalVisible: !this.state.isExpressionModalVisible
        });
    }
    onClickShowImportModal = () => {
        this.setState({
            isImportModalVisible: !this.state.isImportModalVisible
        });
    }
    onClickShowModelModal = () => {
        this.setState({
            isModelModalVisible: !this.state.isModelModalVisible,
            modelCurrentState: this.state.autoPolicy
        });
    }
    onImportableModelChange = (e: React.FormEvent<HTMLTextAreaElement>) => {
        this.setState({ importableModelState: e.currentTarget.value });
    }
    onImportableModelSubmit = () => {
        this.setState({
            autoPolicy: JSON.parse(this.state.importableModelState),
            isImportModalVisible: !this.state.isImportModalVisible,
            importableModelState: ""
        });
    }
    onPoliciesChange = (e: string[]) => {
        const autoPolicy = { ...this.state.autoPolicy, policies: e };
        this.setState({ autoPolicy });
    }
    onClickResetEntryPoint = () => {
        fetch.resetEntryPoint(this.state.validationEntryPointName);
    }

    onRefToCustomerChange = () => {
        message.warn("This field is readonly!");
    }

    onRefererChange = (referer: domain.Referer) => {
        const autoPolicy = { ...this.state.autoPolicy, referer };
        this.setState({ autoPolicy });
    }

    render(): JSX.Element {
        return (
            <AutoPolicyContext.Provider value={{ metadata: this.state._metadata }}>
                <Policy
                    onClickShowExpressionModal={this.onExpressionModalShow}
                    onRefererChange={this.onRefererChange}
                    onRefToCustomerChange={this.onRefToCustomerChange}
                    autoPolicyViewState={this.state}
                    onPoliciesChange={this.onPoliciesChange}
                    onClickResetEntryPoint={this.onClickResetEntryPoint}
                    onClickShowUIResultModal={this.onClickShowUIResultModal}
                    onClickShowBackendResultModal={this.onClickShowBackendResultModal}
                    onClickShowModelModal={this.onClickShowModelModal}
                    onClickShowImportModal={this.onClickShowImportModal}
                    onImportableModelSubmit={this.onImportableModelSubmit}
                    onImportableModelChange={this.onImportableModelChange}
                    onValidationEntryPointChange={this.onValidationEntryPointChange}
                    validate={this.validate}
                    onClickExportCurrentState={this.onClickExportCurrentState}
                    onClickResetUI={this.onClickResetUI}
                    onStateChange={this.onStateChange}
                    onPolicyChange={this.onPolicyChange}
                    onCreatedFromPolicyRevChange={this.onCreatedFromPolicyRevChange}
                    onTransDetailsChange={this.onTransDetailsChange}
                    onPolicyDetailsChange={this.onPolicyDetailsChange}
                    onTermDetailsChange={this.onTermDetailsChange}
                    onAccessTrackInfoChange={this.onAccessTrackInfoChange}
                    onCreditCardInfoChange={this.onCreditCardInfoChange}
                    onInsuredChange={this.onInsuredChange}
                    onVehicleChange={this.onVehicleChange}
                    onPartyChange={this.onPartyChange}
                />
            </AutoPolicyContext.Provider>
        );
    }
}
