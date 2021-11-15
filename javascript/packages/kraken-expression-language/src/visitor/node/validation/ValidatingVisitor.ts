// tslint:disable: max-file-line-count
import { FunctionCallContext, IdentifierContext } from "kraken-expression-language-visitor";
import { ArrayType } from "../../../type/ArrayType";
import { Type } from "../../../type/Type";
import { Deque } from "../../Deque";
import { isValidKelNode, Node } from "../../KelTraversingVisitor";
import { AstError } from "./AstError";
import { BaseNodeVisitor, PartialNodeVisitor } from "../BaseNodeVisitor";
import { Objects } from "../../../factory/Objects";
import { getRange, token } from "../../NodeUtils";
import { NodeType } from "../../NodeType";

export class ValidatingNodeVisitor extends BaseNodeVisitor {
    private errors: AstError[] = [];
    private currentReferenceValue: Deque<Node> = new Deque([]);
    private referenceValuesWithIdentifierErrors = new Set<Node>();

    getErrors(): AstError[] { return this.errors; }

    protected getNodeVisitor(): PartialNodeVisitor {
        const nodeQueue = this.queue;
        const visitChildren = (node: Node) => super.visitChildren(node);
        const { currentReferenceValue, referenceValuesWithIdentifierErrors } = this;
        const parentNode = currentReferenceValue.peek() ?? this.queue[this.queue.length - 1];
        // tslint:disable-next-line: max-line-length
        const toMessage = (m: string) => {
            return `error in '${token(parentNode)}' with message: ${m}`;
        };
        const addError = (error: AstError) => this.errors.push({ ...error, message: toMessage(error.message) });
        const addErrors = (errors: AstError[]) => errors.forEach(addError);
        return {

            // math
            visit_addition(node: Node): void {
                addErrors(checker.validateArithmeticOperation(node));
                visitChildren(node);
            },
            visit_subtraction(node: Node): void {
                addErrors(checker.validateArithmeticOperation(node));
                visitChildren(node);
            },
            visit_division(node: Node): void {
                addErrors(checker.validateArithmeticOperation(node));
                visitChildren(node);
            },
            visit_multiplication(node: Node): void {
                addErrors(checker.validateArithmeticOperation(node));
                visitChildren(node);
            },
            visit_exponent(node: Node): void {
                addErrors(checker.validateArithmeticOperation(node));
                visitChildren(node);
            },
            visit_modulus(node: Node): void {
                addErrors(checker.validateArithmeticOperation(node));
                visitChildren(node);
            },

            // logical
            visit_and(node: Node): void {
                addErrors(checker.validateBinaryLogicalOperation(node));
                visitChildren(node);
            },
            visit_or(node: Node): void {
                addErrors(checker.validateBinaryLogicalOperation(node));
                visitChildren(node);
            },

            // equality
            visit_equals(node: Node): void {
                addErrors(checker.validateTypeCompatibility(node));
                visitChildren(node);
            },
            visit_not_equals(node: Node): void {
                addErrors(checker.validateTypeCompatibility(node));
                visitChildren(node);
            },

            // comparison
            visit_more_than(node: Node): void {
                addErrors(checker.validateBinaryComparison(node));
                visitChildren(node);
            },
            visit_more_than_or_equals(node: Node): void {
                addErrors(checker.validateBinaryComparison(node));
                visitChildren(node);
            },
            visit_less_than(node: Node): void {
                addErrors(checker.validateBinaryComparison(node));
                visitChildren(node);
            },
            visit_less_than_or_equals(node: Node): void {
                addErrors(checker.validateBinaryComparison(node));
                visitChildren(node);
            },

            visit_in(node: Node): void {
                let errors: AstError[] = [];
                const [value, collection] = node.children;
                const { evaluationType: arrayType } = collection;
                if (!arrayType.equals(Type.ANY) && !(arrayType instanceof ArrayType)) {
                    // tslint:disable-next-line: max-line-length
                    const message = `Right side of '${readable(node.nodeType)}' operation must be array but was '${collection.evaluationType.stringify()}'`;
                    errors.push({ node, message, range: getRange(node) });
                }
                if (arrayType instanceof ArrayType) {
                    errors = errors.concat(checker.validateBinarySide({
                        side: "Left",
                        parent: node,
                        expectedType: arrayType.elementType,
                        node: value
                    }));
                }
                addErrors(errors);
                visitChildren(node);
            },
            visit_matches_reg_exp(node: Node): void {
                const [text] = node.children;
                const errors = checker.validateToBeNotNull({
                    node: text,
                    parent: node
                });
                if (!Type.STRING.isAssignableFrom(text.evaluationType)) {
                    errors.push({
                        node: text,
                        message: `Regexp value must be of type 'STRING' but was '${text.evaluationType.stringify()}'`,
                        range: getRange(node)
                    });
                }
                addErrors(errors);
                visitChildren(node);
            },

            // negation, negative
            visit_negation(node: Node): void {
                addErrors(checker.validateUnary(node, Type.BOOLEAN));
                visitChildren(node);
            },
            visit_negative(node: Node): void {
                addErrors(checker.validateUnary(node, Type.NUMBER));
                visitChildren(node);
            },

            visit_reference(node: Node): void {
                currentReferenceValue.push(node);
                visitChildren(node);
                currentReferenceValue.pop();
            },
            visit_identifier(node: Node): void {
                if (!isValidKelNode(node)) {
                    return;
                }
                if (!node.scope.resolveReferenceSymbol(node.context.text)) {
                    if (!referenceValuesWithIdentifierErrors.has(currentReferenceValue.peek())) {
                        addError({
                            message: `Symbol '${token(node)}' cannot be resolved in scope: ${node.scope.name}`,
                            node,
                            range: getRange(node)
                        });
                        referenceValuesWithIdentifierErrors.add(currentReferenceValue.peek());
                    }
                }
                visitChildren(node);
            },
            visit_function(node: Node): void {
                if (!isValidKelNode(node)) {
                    return;
                }
                if (!(node.context instanceof FunctionCallContext)) throw new Error();
                const parameters = node.children;

                const functionName = node.context._functionName.text;
                const functionSymbol = node.scope.resolveFunctionSymbol(
                    functionName,
                    parameters.length
                );
                if (functionSymbol) {
                    for (let index = 0; index < parameters.length; index++) {
                        const param = parameters[index];
                        const paramType = functionSymbol.parameters[index].type;
                        if (!paramType.isAssignableFrom(param.evaluationType)) {
                            addError({
                                // tslint:disable-next-line: max-line-length
                                message: `Incompatible type '${param.evaluationType.stringify()}' of function parameter at index ${index} when invoking function ${functionName}. Expected type is '${paramType.name}'`,
                                node: param,
                                range: getRange(param)
                            });
                        }

                    }
                } else {
                    const paramsString = parameters.map(p => p.evaluationType.stringify()).join(", ");
                    addError({
                        // tslint:disable-next-line: max-line-length
                        message: `Function '${functionName}(${paramsString})' cannot be found in the scope: ${node.scope.name}`,
                        node,
                        range: getRange(node)
                    });
                }
                visitChildren(node);
            },
            visit_if(node: Node): void {
                const [condition, then, elze] = node.children;
                addErrors(checker.validateToBeNotNull({ node: condition, parent: node }));
                addErrors(checker.validateToBeNotNull({ node: then, parent: node }));

                if (elze && !areVersusAssignable(elze.evaluationType, then.evaluationType)) {
                    addError({
                        node,
                        // tslint:disable-next-line: max-line-length
                        message: `"Return types must be the same between THEN and ELSE blocks in IF expression, but return type of THEN is '${then.evaluationType.stringify()}' while return type of ELSE is '${elze.evaluationType.stringify()}'"`,
                        range: getRange(node)
                    });
                }
                if (!Type.BOOLEAN.isAssignableFrom(condition.evaluationType)) {
                    addError({
                        node: condition,
                        // tslint:disable-next-line: max-line-length
                        message: `Condition in '${readable(node.nodeType)}' operation must be of type '${Type.BOOLEAN.stringify()}' but is '${condition.evaluationType.stringify()}'`,
                        range: getRange(condition)
                    });
                }
                visitChildren(node);
            },
            visit_path(node: Node): void {
                const [_object, property] = node.children;
                if (property.nodeType !== "IDENTIFIER" && property.nodeType !== "ACCESS_BY_INDEX") {
                    addError({
                        node,
                        // tslint:disable-next-line: max-line-length
                        message: `Unsupported path expression '${token(node)}'. Property expression shall be identifier or access by index, but found: '${readable(property.nodeType)}'`,
                        range: getRange(node)
                    });
                }
                visitChildren(node);
            },
            visit_access_by_index(node: Node): void {
                const [collection, index] = node.children;
                addErrors(checker.validateCollectionOperation({ node: collection, parent: node }));
                if (!Type.NUMBER.isAssignableFrom(index.evaluationType)) {
                    addError({
                        node: index,
                        // tslint:disable-next-line: max-line-length
                        message: `Index of '${readable(node.nodeType)}' operation must be of type '${Type.NUMBER.stringify()}' but is '${index.evaluationType.stringify()}'`,
                        range: getRange(index)
                    });
                }
                const childOfNegativeNode = index.children[0];
                if (index.nodeType === "NEGATIVE" && childOfNegativeNode.nodeType === "DECIMAL") {
                    addError({
                        // tslint:disable-next-line: max-line-length
                        message: `Index of '${readable(node.nodeType)}' operation must be positive number, but it is negative`,
                        node: index,
                        range: getRange(index)
                    });
                }
                visitChildren(node);
            },
            visit_collection_filter(node: Node): void {
                const [collection, predicate] = node.children;
                addErrors(checker.validateCyclomaticComplexity(node, nodeQueue));
                addErrors(checker.validateCollectionOperation({ node: collection, parent: node }));
                if (predicate && !Type.BOOLEAN.isAssignableFrom(predicate.evaluationType)) {
                    addError({
                        node: predicate,
                        // tslint:disable-next-line: max-line-length
                        message: `Predicate of '${readable(node.nodeType)}' operation must be of type '${Type.BOOLEAN.stringify()}' but is '${predicate.evaluationType.stringify()}'`,
                        range: getRange(predicate)
                    });
                }
                visitChildren(node);
            },
            visit_for(node: Node): void {
                const [collection] = node.children;
                addErrors(checker.validateCollectionOperation({ node: collection, parent: node }));
                addErrors(checker.validateUniqueVariableNameInScope(node));
                addErrors(checker.validateCyclomaticComplexity(node, nodeQueue));
                visitChildren(node);
            },
            visit_some(node: Node): void {
                const [collection] = node.children;
                addErrors(checker.validateCollectionOperation({ node: collection, parent: node }));
                addErrors(checker.validateUniqueVariableNameInScope(node));
                addErrors(checker.validateBooleanReturnType(node));
                addErrors(checker.validateCyclomaticComplexity(node, nodeQueue));
                visitChildren(node);
            },
            visit_every(node: Node): void {
                const [collection] = node.children;
                addErrors(checker.validateCollectionOperation({ node: collection, parent: node }));
                addErrors(checker.validateUniqueVariableNameInScope(node));
                addErrors(checker.validateBooleanReturnType(node));
                addErrors(checker.validateCyclomaticComplexity(node, nodeQueue));
                visitChildren(node);
            }
        };
    }

}

const checker = {
    validateCyclomaticComplexity(node: Node, queue: Node[]): AstError[] {
        const types = new Set<NodeType>(["FOR", "EVERY", "SOME"]);
        const maxLevel = 3;
        const nestedLoops = queue
            .filter(n => types.has(n.nodeType)
                || (n.nodeType === "COLLECTION_FILTER" && Boolean(n.children[1]))
            );
        if (nestedLoops.length > maxLevel) {
            return [{
                // tslint:disable-next-line: max-line-length
                message: `Cyclomatic complexity level is too high for expression. Maximum allowed cyclomatic complexity is ${maxLevel}`,
                node,
                range: getRange(node)
            }];
        }
        return [];
    },
    validateBooleanReturnType(node: Node): AstError[] {
        const [_c, returnExpression] = node.children;
        if (!returnExpression) {
            return [];
        }
        if (!Type.BOOLEAN.isAssignableFrom(returnExpression.evaluationType)) {
            return [{
                node,
                // tslint:disable-next-line: max-line-length
                message: `Return type of '${readable(node.nodeType)}' operation must be of type '${Type.BOOLEAN.name}' but is '${returnExpression.evaluationType.name}'`,
                range: getRange(node)
            }];
        }
        return [];

    },
    validateUniqueVariableNameInScope(node: Node): AstError[] {
        if (!isValidKelNode(node)) {
            return [];
        }
        if (!(Objects.propertyExists(node.context, "_var") && node.context._var instanceof IdentifierContext)) {
            throw new Error(`Node '${readable(node.nodeType)}' must have '_var' in 'node.context'`);
        }
        const variable = node.context._var.text;

        if (node.scope.isReferenceInCurrentScope(variable)) {
            return [{
                // tslint:disable-next-line: max-line-length
                message: `Variable '${variable}' defined in '${readable(node.nodeType)}' operation is already defined in scope: '${node.scope.name}'`,
                node,
                range: getRange(node)
            }];
        }
        return [];
    },
    validateCollectionOperation(p: WithNode & WithParent): AstError[] {
        if (!p.node) {
            return [];
        }
        const evalType = p.node.evaluationType;
        if (!evalType.equals(Type.ANY) && !(evalType instanceof ArrayType)) {
            return [{
                // tslint:disable-next-line: max-line-length
                message: `Operation '${readable(p.parent.nodeType)}' can only be performed on array, but was performed on type '${evalType.name}'`,
                node: p.parent,
                range: getRange(p.parent)
            }];
        }
        return [];
    },
    validateBinaryComparison(node: Node): AstError[] {
        let errors: AstError[] = [];
        const [left, right] = node.children;

        errors = errors
            .concat(checker.validateToBeNotNull({ node: left, parent: node }))
            .concat(checker.validateToBeNotNull({ node: right, parent: node }));

        if (!left.evaluationType.isComparableWith(right.evaluationType)) {
            errors.push({
                node: node,
                // tslint:disable-next-line: max-line-length
                message: `Operation '${readable(node.nodeType)}' can only be performed on comparable types, but was performed on '${left.evaluationType.stringify()}' and '${right.evaluationType.stringify()}'`,
                range: getRange(node)
            });
        }
        return errors;
    },
    validateArithmeticOperation(node: Node): AstError[] {
        return checker.validateBinary(node, Type.NUMBER, Type.NUMBER);
    },
    validateBinaryLogicalOperation(node: Node): AstError[] {
        return checker.validateBinary(node, Type.BOOLEAN, Type.BOOLEAN);
    },
    validateTypeCompatibility(node: Node): AstError[] {
        const [left, right] = node.children;
        if (!areVersusAssignable(left.evaluationType, right.evaluationType)) {
            // tslint:disable-next-line: max-line-length
            const message = `Both sides of operator '${readable(node.nodeType)}' must have same type, but left side was of type '${left.evaluationType.stringify()}' and right side was of type '${right.evaluationType.stringify()}'`;
            return [{ message, node, range: getRange(node) }];
        }
        return [];
    },
    validateUnary(node: Node, expectedType: Type): AstError[] {
        const [child] = node.children;
        const errors = checker.validateToBeNotNull({ node: child, parent: node });
        if (!expectedType.isAssignableFrom(child.evaluationType)) {
            // tslint:disable-next-line: max-line-length
            const message = `Operation '${readable(node.nodeType)}' can only be performed on type '${expectedType.name}' but was performed on '${child.evaluationType.stringify()}'`;
            errors.push({
                message, node: child, range: getRange(child)
            });
        }
        return errors;
    },
    validateBinary(node: Node, expectedLeftType: Type, expectedRightType: Type): AstError[] {
        const [left, right] = node.children;
        return [
            ...this.validateBinarySide({ parent: node, expectedType: expectedLeftType, node: left, side: "Left" }),
            ...this.validateBinarySide({ parent: node, expectedType: expectedRightType, node: right, side: "Right" })
        ];
    },
    validateBinarySide(p: { side: "Left" | "Right", expectedType: Type } & WithNode & WithParent): AstError[] {
        let errors: AstError[] = [];
        errors = errors.concat(checker.validateToBeNotNull(p));
        if (!p.expectedType.isAssignableFrom(p.node.evaluationType)) {
            // tslint:disable-next-line: max-line-length
            const message = `${p.side} side of '${token(p.parent)}' operation must be of type '${p.expectedType.name}' but was '${p.node.evaluationType.stringify()}'`;
            errors.push({
                node: p.node,
                message: message,
                range: getRange(p.node)
            });
        }
        return errors;
    },
    validateToBeNotNull(p: WithParent & WithNode): AstError[] {
        if (p.node.nodeType === "NULL") {
            const message = `Operand cannot be 'null' literal for operation: '${readable(p.parent.nodeType)}'`;
            return [{ message, node: p.node, range: getRange(p.node) }];
        }
        return [];
    }
};

function areVersusAssignable(type1: Type, type2: Type): boolean {
    return type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1);
}

function readable(nodeType: NodeType): string {
    return nodeType.toLowerCase().split("_").join(" ");
}

export type WithParent = { parent: Node };
export type WithNode = { node: Node };
