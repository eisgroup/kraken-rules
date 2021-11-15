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

import { Layout } from "antd";
import * as React from "react";
import { fetch, BuildInfo } from "./rule-engine/api";

import "./components/AutoPolicySummary/Policy.less";

import { AutoPolicySummaryContainer } from "./components/AutoPolicySummary/Policy.container";
import { ErrorBoundary } from "./ErrorBoundary";

const { Header, Footer, Content } = Layout;

const styles = {
    header: {
        fontSize: "24px",
        padding: "10px",
        background: "white",
        color: "dark-grey"
    },
    content: {
        padding: "10px",
        background: "url(\"https://www.toptal.com/designers/subtlepatterns/patterns/bright_squares.png\")"
    },
    footer: {
        padding: "10px",
        background: "#070f13d1",
        color: "white"
    }
};

export const App = () => {
    const [info, setInfo] = React.useState<undefined | BuildInfo>(undefined);
    React.useEffect(() => {
        fetch.buildInfo().then(bi => setInfo(bi));
    }, []);
    return (
        <Layout>
            <Header style={styles.header}><b>Auto Policy</b></Header>
            <ErrorBoundary>
                <Content style={styles.content}>
                    <AutoPolicySummaryContainer />
                </Content>
                <Footer style={styles.footer}>
                    {info && info.date && `🐙 Date: ${info.date}`}
                    {info && info.date && `🐙 Build number: ${info.number}`}
                    {info && info.date && `🐙 Revision: ${info.revision}`}
                    {info && info.date && `🐙 Start time: ${info.startTime}`}
                </Footer>
            </ErrorBoundary>
        </Layout>
    );
};
