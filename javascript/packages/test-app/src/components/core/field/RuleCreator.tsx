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
import { Row, Col, message } from "antd";
import "antd/lib/popover/style";
import "antd/lib/button/style";
import "antd/lib/icon/style";

import { fetch } from "../../../rule-engine/api";
import { v1 } from "uuid";
import AceEditor from "react-ace";
import { Input } from "antd";

interface RuleCreatorProps {
    fieldName: string;
    id: string;
    contextName: string;
}

interface State {
    assertExpression: string;
    assertLoading: boolean;
    assertError?: string;
    defaultExpression: string;
    defaultError?: string;
    defaultLoading: boolean;
    fullRule: string;
    fullError?: string;
    fullLoading: boolean;
}

function createDefault(fieldName: string, contextName: string, expression: string): string {
    const ruleName = `[From UI] reset ${contextName}.${fieldName} v.${v1().slice(0, 7)}`;
    return `
Rule "${ruleName}" On ${contextName}.${fieldName} {
    Reset To ${expression}
}`;
}

function createAssertion(fieldName: string, contextName: string, expression: string): string {
    const ruleName = `[From UI] assert ${contextName}.${fieldName} v.${v1().slice(0, 7)}`;
    return `
Rule "${ruleName}" On ${contextName}.${fieldName} {
    Assert ${expression}
    Error "code":"Assertion expression '${expression}' failed"
}`;
}

export class RuleCreator extends React.PureComponent<RuleCreatorProps, State> {
    constructor(props: RuleCreatorProps) {
        super(props);
        this.state = {
            assertExpression: "",
            assertLoading: false,
            defaultExpression: "",
            defaultLoading: false,
            fullRule: "",
            fullLoading: false
        };
        this.onAssertChange = this.onAssertChange.bind(this);
        this.createAssert = this.createAssert.bind(this);
        this.onDefaultChange = this.onDefaultChange.bind(this);
        this.createDefault = this.createDefault.bind(this);
        this.onFullChange = this.onFullChange.bind(this);
        this.createFullRule = this.createFullRule.bind(this);
    }

    // tslint:disable-next-line:no-any
    onAssertChange(e: any): void {
        this.setState({ assertExpression: e.target.value });
    }

    // tslint:disable-next-line:no-any
    onDefaultChange(e: any): void {
        this.setState({ defaultExpression: e.target.value });
    }

    onFullChange(e: any): void {
        this.setState({ fullRule: e.target.value });
    }

    createFullRule(): void {
        this.setState({ fullLoading: true });
        fetch.createRule(this.state.fullRule)
            .then(() => {
                message.success(`Rule created successfully`);
                return this.setState({ assertExpression: "", fullLoading: false, assertError: undefined });
            })
            .catch(error => {
                const fullError = error.response.data.message || error.response.data;
                message.error(`Rule creation failed: ${fullError}`);
                this.setState({ fullLoading: false, fullError });
            });
    }

    createAssert(): void {
        this.setState({ assertLoading: true });
        const rule = createAssertion(this.props.fieldName, this.props.contextName, this.state.assertExpression);
        fetch.createRule(rule)
            .then(() => {
                message.success(`Rule created successfully`);
                return this.setState({ assertExpression: "", assertLoading: false, assertError: undefined });
            })
            .catch(error => {
                const assertError = error.response.data.message || error.response.data;
                message.error(`Rule creation failed: ${assertError}`);
                this.setState({ assertLoading: false, assertError });
            });
    }

    createDefault(): void {
        this.setState({ defaultLoading: true });
        const rule = createDefault(this.props.fieldName, this.props.contextName, this.state.defaultExpression);
        fetch.createRule(rule)
            .then(() => {
                message.success(`Rule created successfully`);
                return this.setState({ defaultExpression: "", defaultLoading: false, defaultError: undefined });
            })
            .catch(error => {
                const defaultError = error.response.data.message || error.response.data;
                message.error(`Rule creation failed: ${defaultError}`);
                this.setState({ defaultLoading: false, defaultError });
            });
    }
    render(): JSX.Element {
        const id = `${this.props.contextName}.${this.props.fieldName}.${this.props.id}`;
        return (
            <div id={`createrule-${id}`}>
                <Row>
                    <Col lg={20} md={20}>
                        <h5>
                            Create 'Assert' rule
                            {!this.state.assertLoading
                                ? <a onClick={this.createAssert} >&nbsp;(submit)</a>
                                : " (submitting...)"
                            }
                        </h5>
                        <Input.TextArea
                            style={{
                                height: "100px",
                                marginTop: "5px",
                                marginBottom: "5px",
                                borderRadius: "3px",
                                width: "100%",
                                fontSize: "12px",
                                fontFamily: "monospace",
                                border: "1px solid #d9d9d9"
                            }}
                            value={this.state.assertExpression}
                            onChange={this.onAssertChange}
                            defaultValue=""
                        />
                        {this.state.assertError ? <h5 style={{ color: "red" }}>{this.state.assertError}</h5> : ""}
                        <h5>
                            Create 'Reset' rule
                            {!this.state.defaultLoading
                                ? <a onClick={this.createDefault} >&nbsp;(submit)
                                </a>
                                : " (submitting...)"
                            }
                        </h5>
                        <Input.TextArea
                            style={{
                                height: "100px",
                                marginTop: "5px",
                                marginBottom: "5px",
                                borderRadius: "3px",
                                width: "100%",
                                fontSize: "12px",
                                fontFamily: "monospace",
                                border: "1px solid #d9d9d9"
                            }}
                            value={this.state.defaultExpression}
                            onChange={this.onDefaultChange}
                            defaultValue=""
                        />
                        {this.state.defaultError ? <h5 style={{ color: "red" }}>{this.state.defaultError}</h5> : ""}

                        <h5>
                            Create Rule
                            {!this.state.fullLoading
                                ? <a
                                    id={`createrule-full-submit-${id}`}
                                    onClick={this.createFullRule}
                                >&nbsp;(submit)</a>
                                : " (submitting...)"
                            }
                        </h5>
                        <Input.TextArea
                            // tslint:disable-next-line: max-line-length
                            id={`createrule-full-textarea-${id}`}
                            style={{
                                height: "100px",
                                marginTop: "5px",
                                marginBottom: "5px",
                                borderRadius: "3px",
                                width: "100%",
                                fontSize: "12px",
                                fontFamily: "monospace",
                                border: "1px solid #d9d9d9"
                            }}
                            value={this.state.fullRule}
                            onChange={this.onFullChange}
                            defaultValue=""
                        />
                        {this.state.fullError ? <h5 style={{ color: "red" }}>{this.state.fullError}</h5> : ""}
                    </Col>
                </Row>
            </div>
        );
    }
}
