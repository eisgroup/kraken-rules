import * as React from "react";
import { useState } from "react";

import { Col, Input, List, Radio, Row, Select, Slider } from "antd";
import {
    Completion, CompletionItem, createParser, KelService, KelTraversingVisitor,
    Node, Scope, ScopeDeserializer, token, Validation
} from "kraken-expression-language";

import ParentSize from "@visx/responsive/lib/components/ParentSize";

import { fetch } from "../../rule-engine/api";
import { ExpressionTreeView, TreeNodeData } from "./ExpressionTreeView";

export const ExpressionTree: React.FC<{ scopes: string[] }> = ({ scopes }) => {
    const [data, setData] = useState<TreeNodeData>({ name: "root", typeName: "noop", token: "", failing: false });
    const [expression, setExpression] = useState<string>(
        "for i in riskItems return for c in i.anubisCoverages return c.code"
    );
    const [scope, setScope] = useState<Scope>();
    const [cd, setCd] = useState<string>(scopes[0]);
    const [colors, setColors] = useState<any>(colorSchema);
    const [completion, setCompletion] = useState<Completion>();
    const [validation, setValidation] = useState<Validation>({ semantic: [], syntax: [] });
    const [completionEndIndexSubtraction, setCompletionEndIndexSubtraction] = useState<number>(() => 0);

    const [deserializer, setDeserializer] = useState<ScopeDeserializer>();
    const [kelService, setKelService] = useState<KelService>();

    const [serviceTime, setServiceTime] = useState<number>();

    React.useEffect(() => {
        fetch.scope.typeRegistry().then(tr => {
            const d = new ScopeDeserializer(tr);
            setDeserializer(d);
        });
    }, []);

    React.useEffect(() => {
        if (deserializer) {
            fetch.scope.scope(cd)
                .then(json => deserializer.provideScope(json))
                .then(s => {
                    setScope(s);
                    return new KelService(s);
                })
                .then(setKelService);
        }
    }, [deserializer, cd]);

    React.useEffect(() => {
        if (kelService) {
            try {
                const cursor = {
                    line: 1,
                    column: expression.length - 1 - completionEndIndexSubtraction
                };
                const start = performance.now();
                const v = kelService.provideValidation(expression);
                const c = kelService.provideCompletion(expression, cursor);
                const end = performance.now();
                setServiceTime(end - start);

                setValidation(v);
                setCompletion(c);

                function toTreeNode(n: Node): TreeNodeData {
                    const error = v.semantic.find(err => token(err.node) === token(n));
                    return {
                        token: token(n),
                        name: n.nodeType,
                        typeName: n.evaluationType.stringify(),
                        children: n.children.map(toTreeNode),
                        failing: Boolean(error),
                        error: error?.message
                    };
                }
                setData(
                    toTreeNode(
                        new KelTraversingVisitor(scope).visit(
                            createParser(
                                expression
                            ).expression()
                        )
                    )
                );

            } catch (error) {
                console.log(error);
            }
        }
    }, [expression, completionEndIndexSubtraction, scope, kelService]);

    const inputStyle: React.CSSProperties = { display: "inline-block", marginBottom: 8 };
    const labelStyle: React.CSSProperties = { fontWeight: "bold" };

    const complIndex = expression.length - 1 - completionEndIndexSubtraction;

    return (
        <div style={{ marginTop: 16 }}>
            <div style={{ ...inputStyle, width: "105", marginRight: 8 }}>
                <div style={labelStyle}>Color Theme</div>
                <Radio.Group onChange={e => {
                    if (e.target.value === "light") {
                        setColors(colorSchema);
                    } else {
                        setColors(undefined);
                    }
                }} defaultValue="light">
                    <Radio.Button value="light">🌕</Radio.Button>
                    <Radio.Button value="dark">🌑</Radio.Button>
                </Radio.Group>
            </div>
            <div style={{ ...inputStyle, width: "25%", marginRight: 8 }}>
                <div style={labelStyle}>Scope</div>
                <Select
                    style={{ width: "100%" }}
                    placeholder="Select a person"
                    defaultValue={scopes[0]}
                    onSelect={(e: string) => setCd(e.toString())}
                >
                    {scopes.map(s => <Select.Option value={s}>{s}</Select.Option>)}
                </Select>
            </div>
            <div style={{ ...inputStyle, width: "calc(100% - 25% - 8px - 105px - 8px)" }}>
                <div style={labelStyle}>Expression</div>
                <Input value={expression} onChange={e => {
                    return setExpression(e.target.value);
                }} />
            </div>
            <ParentSize>
                {({ width }) => <ExpressionTreeView
                    data={data}
                    height={500}
                    width={width}
                    colorSchema={colors}
                />}
            </ParentSize>
            <small>
                KEL service evaluation time {` ${Math.round(serviceTime)} ms`}
            </small>
            <Row style={{ marginTop: 4 }}>
                <Col span={12}>
                    <List
                        size="small"
                        header={
                            <div style={labelStyle}>
                                Expression Errors ({validation.semantic.length + validation.semantic.length})
                            </div>
                        }
                        bordered
                        dataSource={
                            validation.semantic.map(e => ({ emoji: "🐛", type: "semantic", message: e.message }))
                                .concat(
                                    validation.syntax.map(se => ({
                                        emoji: "🚫",
                                        type: "syntax",
                                        message: se.error
                                    }))
                                )
                        }
                        renderItem={(item: { emoji: string, type: string, message: string }) => <List.Item>
                            <List.Item.Meta title={`${item.emoji} ${item.type} error`} />
                            {item.message}
                        </List.Item>}
                    />
                </Col>
                <Col span={12}>
                    <List
                        style={{ marginBottom: 8, marginLeft: 8 }}
                        size="small"
                        header={
                            <div>
                                <span style={labelStyle}>
                                    Completion Items ({completion?.completions.length ?? 0})
                                </span>
                                <hr style={{ borderTop: "0px solid #d9d9d9" }} />
                                <Row>
                                    <Col span={12}>
                                        Slider to emulate cursor position
                                        <pre>1:{expression.length - 1 - completionEndIndexSubtraction}</pre>
                                    </Col>
                                    <Col span={12}>
                                        <Slider
                                            reverse
                                            min={0}
                                            max={expression.length - 1}
                                            onChange={e => setCompletionEndIndexSubtraction(Number(e))}
                                            value={completionEndIndexSubtraction}
                                            tipFormatter={value => expression.length - 1 - value}
                                        />
                                    </Col>
                                </Row>
                                <span style={{ fontFamily: "monospace", backgroundColor: "#F0F0F0", padding: 4 }}>
                                    {expression.slice(0, complIndex)}
                                    <span
                                        style={{ color: "white", backgroundColor: "#282828" }}
                                    >
                                        {
                                            expression[complIndex] === " "
                                                ? "█"
                                                : expression[complIndex]
                                        }
                                    </span>
                                    {expression.slice(complIndex + 1, expression.length)}
                                </span>
                            </div>
                        }
                        bordered
                        dataSource={(completion?.completions ?? [])}
                        renderItem={(item: CompletionItem) => <List.Item>
                            {
                                item.type === "reference" ? "☝️"
                                    : item.type === "function" ? "🤸🏻‍♂️"
                                        : item.type === "keyword" ? "🔑"
                                            : "?"} {item.text} <i>{item.info}</i>
                        </List.Item>}
                    />

                </Col>
            </Row>
        </div>
    );
};

const fill = "white";
const text = "#616771";
const colorSchema = {
    background: "#E9EBEE",
    errorNode: { fill, stroke: "red", text: "#616771" },
    leaf: { fill, stroke: "none", text: "#37474f" },
    node: { fill, text, stroke: "none" },
    link: "#B4B4B4",
    root: { fill, stroke: "none", text }
};
