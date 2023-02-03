import * as React from 'react'
import { useState } from 'react'

import { Col, Input, List, Radio, Row, Select, Slider } from 'antd'
import {
    Completion,
    CompletionItem,
    KelService,
    AstGeneratingVisitor,
    Node,
    Scope,
    ScopeDeserializer,
    getText,
    getRange,
    Validation,
    KelParser,
    LocationInfo,
    Cursor,
} from 'kraken-expression-language'

import ParentSize from '@visx/responsive/lib/components/ParentSize'

import { fetch } from '../../rule-engine/api'
import { ExpressionTreeView, TreeNodeData } from './ExpressionTreeView'

export const ExpressionTree: React.FC<{ scopes: string[] }> = ({ scopes }) => {
    const [data, setData] = useState<TreeNodeData>({ name: 'root', typeName: 'noop', text: '', failing: false })
    const [expression, setExpression] = useState<string>(
        'for i in riskItems return for c in i.anubisCoverages return c.code',
    )
    const [scope, setScope] = useState<Scope>()
    const [cd, setCd] = useState<string>(scopes[0])
    const [colors, setColors] = useState(colorSchema)
    const [locationInfo, setLocationInfo] = useState<LocationInfo>()
    const [completion, setCompletion] = useState<Completion>()
    const [validation, setValidation] = useState<Validation>({ messages: [] })
    const [cursor, setCursor] = useState<Cursor>({ line: 1, column: 0 })

    const [deserializer, setDeserializer] = useState<ScopeDeserializer>()
    const [kelService, setKelService] = useState<KelService>()

    const [serviceTime, setServiceTime] = useState<number>()

    React.useEffect(() => {
        fetch.scope.typeRegistry().then(tr => {
            const d = new ScopeDeserializer(tr)
            setDeserializer(d)
        })
    }, [])

    React.useEffect(() => {
        if (deserializer) {
            fetch.scope
                .scope(cd)
                .then(json => deserializer.provideScope(json))
                .then(s => {
                    setScope(s)
                    return new KelService(s)
                })
                .then(setKelService)
        }
    }, [deserializer, cd])

    React.useEffect(() => {
        if (kelService) {
            try {
                const start = performance.now()
                const v = kelService.provideValidation(expression)
                const c = kelService.provideCompletion(expression, cursor)
                const i = kelService.provideInfoAtLocation(expression, cursor)
                const end = performance.now()
                setServiceTime(end - start)

                setLocationInfo(i)
                setValidation(v)
                setCompletion(c)

                // eslint-disable-next-line no-inner-declarations
                function toTreeNode(n: Node): TreeNodeData {
                    const nodeRange = JSON.stringify(getRange(n))
                    const validationMessage = v.messages
                        .filter(message => message.range)
                        .find(message => JSON.stringify(message.range) === nodeRange)

                    return {
                        text: getText(n),
                        name: n.nodeType,
                        typeName: n.evaluationType.stringify(),
                        children: n.children.map(toTreeNode),
                        failing: Boolean(validationMessage),
                        error: validationMessage?.message,
                    }
                }
                setData(toTreeNode(new AstGeneratingVisitor(scope).visit(new KelParser(expression).parseExpression())))
            } catch (error) {
                console.log(error)
            }
        }
    }, [expression, cursor, scope, kelService])

    const inputStyle: React.CSSProperties = { display: 'inline-block', marginBottom: 8 }
    const labelStyle: React.CSSProperties = { fontWeight: 'bold' }

    let locationInfoText = '🤔'
    const locationTokenRange = { start: -1, end: -1 }
    if (locationInfo) {
        switch (locationInfo.type) {
            case 'function':
                locationInfoText = `${locationInfo.functionName}`
                locationTokenRange.start = locationInfo.range.start.column
                locationTokenRange.end = locationInfo.range.end.column
                break
            case 'type':
                locationInfoText = `${locationInfo.evaluationType}`
                locationTokenRange.start = locationInfo.range.start.column
                locationTokenRange.end = locationInfo.range.end.column
                break
            default:
                break
        }
    }

    return (
        <div style={{ marginTop: 16 }}>
            <div style={{ ...inputStyle, width: '105', marginRight: 8 }}>
                <div style={labelStyle}>Color Theme</div>
                <Radio.Group
                    onChange={e => {
                        if (e.target.value === 'light') {
                            setColors(colorSchema)
                        } else {
                            setColors(undefined)
                        }
                    }}
                    defaultValue='light'
                >
                    <Radio.Button value='light'>🌕</Radio.Button>
                    <Radio.Button value='dark'>🌑</Radio.Button>
                </Radio.Group>
            </div>
            <div style={{ ...inputStyle, width: '25%', marginRight: 8 }}>
                <div style={labelStyle}>Scope</div>
                <Select
                    style={{ width: '100%' }}
                    placeholder='Select a person'
                    defaultValue={scopes[0]}
                    onSelect={(e: string) => setCd(e.toString())}
                >
                    {scopes.map(s => (
                        <Select.Option value={s}>{s}</Select.Option>
                    ))}
                </Select>
            </div>
            <div style={{ ...inputStyle, width: 'calc(100% - 25% - 8px - 105px - 8px)' }}>
                <div style={labelStyle}>Expression</div>
                <Input
                    value={expression}
                    onChange={e => {
                        return setExpression(e.target.value)
                    }}
                />
            </div>
            <ParentSize>
                {({ width }) => <ExpressionTreeView data={data} height={500} width={width} colorSchema={colors} />}
            </ParentSize>
            <small>KEL service evaluation time {` ${Math.round(serviceTime)} ms`}</small>
            <Row style={{ marginTop: 4 }}>
                <Col span={12}>
                    <List
                        size='small'
                        header={<div style={labelStyle}>Validation Messages ({validation.messages.length})</div>}
                        bordered
                        dataSource={validation.messages}
                        renderItem={validationMessage => (
                            <List.Item>
                                {validationMessage.severity === 'ERROR' ? '🛇' : '🛈'} {validationMessage.message}
                            </List.Item>
                        )}
                    />
                </Col>
                <Col span={12}>
                    <List
                        style={{ marginBottom: 8, marginLeft: 8 }}
                        size='small'
                        header={
                            <div>
                                <span style={labelStyle}>Completion Items ({completion?.completions.length ?? 0})</span>
                                <hr style={{ borderTop: '0px solid #d9d9d9' }} />
                                <Row>
                                    <Col span={12}>
                                        Slider to emulate cursor position
                                        <pre>
                                            {cursor.line}:{cursor.column}
                                        </pre>
                                    </Col>
                                    <Col span={12}>
                                        <Slider
                                            min={0}
                                            max={expression.length - 1}
                                            onChange={e => setCursor({ line: cursor.line, column: Number(e) })}
                                            value={cursor.column}
                                            tipFormatter={value => expression.length - value}
                                        />
                                    </Col>
                                </Row>
                                <div style={{ padding: 4, fontFamily: 'monospace', backgroundColor: '#F0F0F0' }}>
                                    {expression.split('').map((s, idx) => (
                                        <span
                                            style={{
                                                color:
                                                    idx >= locationTokenRange.start && idx < locationTokenRange.end
                                                        ? 'red'
                                                        : 'inherit',
                                                fontFamily: 'monospace',
                                                backgroundColor:
                                                    idx === cursor.column ? 'rgb(40 40 40 / 28%)' : '#F0F0F0',
                                                whiteSpace: 'pre',
                                            }}
                                        >
                                            {s}
                                        </span>
                                    ))}
                                </div>
                                <div style={{ lineHeight: 2 }}>Location info: {locationInfoText}</div>
                            </div>
                        }
                        bordered
                        dataSource={completion?.completions ?? []}
                        renderItem={(item: CompletionItem) => (
                            <List.Item>
                                {item.type === 'reference'
                                    ? '☝️'
                                    : item.type === 'function'
                                    ? '🤸🏻‍♂️'
                                    : item.type === 'keyword'
                                    ? '🔑'
                                    : '?'}{' '}
                                {item.text} <i>{item.type}</i>
                            </List.Item>
                        )}
                    />
                </Col>
            </Row>
        </div>
    )
}

const fill = 'white'
const text = '#616771'
const colorSchema = {
    background: '#E9EBEE',
    errorNode: { fill, stroke: 'red', text: '#616771' },
    leaf: { fill, stroke: 'none', text: '#37474f' },
    node: { fill, text, stroke: 'none' },
    link: '#B4B4B4',
    root: { fill, stroke: 'none', text },
}
