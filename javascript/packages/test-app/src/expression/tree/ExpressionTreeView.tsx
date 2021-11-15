import * as React from "react";

import { notification } from "antd";

import { LinearGradient } from "@visx/gradient";
import { Group } from "@visx/group";
import { hierarchy, Tree } from "@visx/hierarchy";
import { LinkVertical } from "@visx/shape";

import { useForceUpdate } from "./useForceUpdate";
import { HierarchyPointNode } from "d3-hierarchy";

export interface TreeNodeData {
    name: string;
    typeName: string;
    token: string;
    isExpanded?: boolean;
    children?: TreeNodeData[];
    failing: boolean;
    error?: string;
}

const defaultMargin = { top: 30, left: 30, right: 30, bottom: 70 };

export type LinkTypesProps = {
    width: number;
    height: number;
    margin?: { top: number; right: number; bottom: number; left: number };
    data: TreeNodeData,
    colorSchema?: ColorSchema
};

type ElementColor = {
    stroke: string;
    fill: string;
    text: string;
};

interface ColorSchema {
    link: string;
    background: string;
    root: ElementColor;
    errorNode: ElementColor;
    leaf: ElementColor;
    node: ElementColor;
}

const defaultColors: ColorSchema = {
    link: "rgb(254,110,158,0.6)",
    background: "#272b4d",
    root: {
        fill: "#4e2770",
        stroke: "#5e2771",
        text: "white"
    },
    errorNode: {
        fill: "#272b4d",
        stroke: "red",
        text: "white"
    },
    leaf: {
        fill: "#272b4d",
        stroke: "#26deb0",
        text: "#26deb0"
    },
    node: {
        fill: "#272b4d",
        stroke: "#03c0dc",
        text: "white"
    }
};

export function ExpressionTreeView({
    data,
    width: totalWidth,
    height: totalHeight,
    margin = defaultMargin,
    colorSchema = defaultColors
}: LinkTypesProps): JSX.Element {
    const forceUpdate = useForceUpdate();

    const innerWidth = totalWidth - margin.left - margin.right;
    const innerHeight = totalHeight - margin.top - margin.bottom;

    const origin = { x: 0, y: 0 };
    const sizeWidth = innerWidth;
    const sizeHeight = innerHeight;

    return totalWidth < 10 ? null : (
        <div>
            <svg width={totalWidth} height={totalHeight}>
                <LinearGradient id="links-gradient" from="#fd9b93" to="#fe6e9e" />
                <rect width={totalWidth} height={totalHeight} rx={4} fill={colorSchema.background} />
                <Group top={margin.top} left={margin.left}>
                    <Tree
                        root={hierarchy(data, d => (d.isExpanded ? null : d.children))}
                        size={[sizeWidth, sizeHeight]}
                        separation={(a, b) => (a.parent === b.parent ? 1 : 0.5) / a.depth}
                    >
                        {tree => (
                            <Group top={origin.y} left={origin.x}>
                                {tree.links().map((link, i) => (
                                    <LinkVertical
                                        key={i}
                                        data={link}
                                        stroke={colorSchema.link}
                                        strokeWidth="1"
                                        fill="none"
                                    />
                                ))}

                                {tree.descendants().map((node, key) => {
                                    const width = 120;
                                    const height = 40;

                                    const top = node.y;
                                    const left = node.x;

                                    return (
                                        <Group top={top} left={left} key={key}>
                                            {node.depth === 0 && <RootNode
                                                colors={colorSchema}
                                                node={node}
                                            />}
                                            {node.depth !== 0 && <Node
                                                colors={colorSchema}
                                                node={node}
                                                width={width}
                                                height={height}
                                            />}
                                        </Group>
                                    );
                                })}
                            </Group>
                        )}
                    </Tree>
                </Group>
            </svg>
        </div>
    );
}

const RootNode: React.FC<{ node: HierarchyPointNode<TreeNodeData>, colors: ColorSchema }> = ({ node, colors }) => (
    <g>
        <circle
            r={25}
            strokeWidth={node.data.failing ? 2 : 1}
            stroke={node.data.failing ? colors.errorNode.stroke : colors.root.stroke}
            fill={node.data.failing ? colors.errorNode.fill : colors.root.fill}
            onClick={() => {
                if (node.data.failing) {
                    notification.error({
                        message: "Validation Error",
                        description: node.data.error
                    });
                }
            }}
        />
        <NodeText colors={colors} node={node} />
    </g>
);

const Node: React.FC<{
    node: HierarchyPointNode<TreeNodeData>,
    height: number,
    width: number,
    colors: ColorSchema
}> = ({ node, height, width, colors }) => {
    const isLeaf = !Boolean(node.children);
    let { fill, stroke } = colors.node;
    if (isLeaf && !node.data.failing) {
        fill = colors.leaf.fill;
        stroke = colors.leaf.stroke;
    } else if (node.data.failing) {
        fill = colors.errorNode.fill;
        stroke = colors.errorNode.stroke;
    }
    return (
        <g>
            <rect
                height={height}
                width={width}
                y={-height / 2}
                x={-width / 2}
                fill={fill}
                strokeLinecap={"round"}
                stroke={stroke}
                strokeWidth={node.data.failing ? 3 : 1}
                strokeDasharray={node.data.children ? "0" : "2,2"}
                strokeOpacity={node.data.children ? 1 : 0.6}
                rx={2}
                onClick={() => {
                    if (node.data.failing) {
                        notification.error({
                            message: "Validation Error",
                            description: node.data.error
                        });
                    }
                }} />
            <NodeText colors={colors} node={node} />
        </g>
    );
};

const NodeText: React.FC<{ node: HierarchyPointNode<TreeNodeData>, colors: ColorSchema }> = ({ node, colors }) => {
    const isLeaf = !Boolean(node.children);
    let { text } = colors.node;
    if (isLeaf && !node.data.failing) {
        text = colors.leaf.text;
    } else if (node.data.failing) {
        text = colors.errorNode.text;
    }
    return (
        <g>
            <text
                dy="-.5em"
                fontSize={12}
                fontFamily="Arial"
                textAnchor="middle"
                style={{ pointerEvents: "none" }}
                fill={text}
            >
                {node.data.token} ({node.data.typeName})
            </text>
            <text
                dy="1.3em"
                fontSize={12}
                fontFamily="Arial"
                textAnchor="middle"
                style={{ pointerEvents: "none" }}
                fill={text}
            >
                {node.data.name}
            </text>
        </g>
    );
};
