import {
    AccessByIndexContext,
    FilterContext,
    FunctionContext,
    ValueWithVariablesContext,
    VariableContext,
} from 'kraken-expression-language-visitor'
import { Deque } from '../Deque'
import { AstMessage, AstMessageSeverity } from './AstMessage'
import { BaseNodeVisitor, PartialNodeVisitor } from '../BaseNodeVisitor'
import { getRange, getText } from '../NodeUtils'
import { NodeType, isEmptyKelNode, isValidKelNode, Node } from '../Node'
import leven from 'fast-levenshtein'
import { Scope } from '../../scope/Scope'
import { VariableSymbol } from '../../symbol/VariableSymbol'
import { Type } from '../../type/Types'

export class ValidatingNodeVisitor extends BaseNodeVisitor {
    private messages: AstMessage[] = []
    private currentReferenceValue: Deque<Node> = new Deque([])
    private referenceValuesWithIdentifierErrors = new Set<Node>()

    getMessages(): AstMessage[] {
        return this.messages
    }

    protected getNodeVisitor(): PartialNodeVisitor {
        const nodeQueue = this.queue
        const visitChildren = (node: Node) => super.visitChildren(node)
        const { currentReferenceValue, referenceValuesWithIdentifierErrors } = this

        const addMessage = (message: AstMessage) => this.messages.push(message)
        const addMessages = (messages: AstMessage[]) => messages.forEach(addMessage)
        return {
            // math
            visit_addition(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateArithmeticOperation(node))
            },
            visit_subtraction(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateArithmeticOperation(node))
            },
            visit_division(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateArithmeticOperation(node))
            },
            visit_multiplication(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateArithmeticOperation(node))
            },
            visit_exponent(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateArithmeticOperation(node))
            },
            visit_modulus(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateArithmeticOperation(node))
            },

            // logical
            visit_and(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateBinaryLogicalOperation(node))
            },
            visit_or(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateBinaryLogicalOperation(node))
            },

            // equality
            visit_equals(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateTypeCompatibility(node))
            },
            visit_not_equals(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateTypeCompatibility(node))
            },

            // comparison
            visit_more_than(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateBinaryComparison(node))
            },
            visit_more_than_or_equals(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateBinaryComparison(node))
            },
            visit_less_than(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateBinaryComparison(node))
            },
            visit_less_than_or_equals(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateBinaryComparison(node))
            },

            visit_in(node: Node): void {
                visitChildren(node)

                const [value, collection] = node.children
                const { evaluationType: arrayType } = collection
                if (!arrayType.isAssignableToArray()) {
                    const message = `Right side of '${readable(
                        node.nodeType,
                    )}' operation must be array but was '${collection.evaluationType.stringify()}'`
                    addMessage(createError(message, node))
                } else {
                    addMessages(
                        validator.validateBinarySide({
                            side: 'Left',
                            parent: node,
                            expectedType: arrayType.unwrapArrayType(),
                            node: value,
                        }),
                    )
                }
            },
            visit_matches_reg_exp(node: Node): void {
                visitChildren(node)

                const [text] = node.children
                const errors = validator.validateToBeNotNull({
                    node: text,
                    parent: node,
                })
                if (!Type.STRING.isAssignableFrom(text.evaluationType)) {
                    const message = `Regular expression must be of type 'STRING' but was '${text.evaluationType.stringify()}'`
                    addMessage(createError(message, node))
                }
                addMessages(errors)
            },

            // negation, negative
            visit_negation(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateUnary(node, Type.BOOLEAN))
            },
            visit_negative(node: Node): void {
                visitChildren(node)
                addMessages(validator.validateUnary(node, Type.NUMBER))
            },

            visit_reference(node: Node): void {
                currentReferenceValue.push(node)
                visitChildren(node)
                currentReferenceValue.pop()
            },
            visit_identifier(node: Node): void {
                if (!isValidKelNode(node)) {
                    return
                }
                visitChildren(node)

                if (!node.scope.resolveReferenceSymbol(node.context.text)) {
                    if (!referenceValuesWithIdentifierErrors.has(currentReferenceValue.peek())) {
                        const tokenText = getText(node)

                        const references = getReferences(node.scope)
                        let diff = 1000
                        let guessRef
                        for (const reference of references) {
                            const d = leven.get(reference.name, tokenText)

                            if (diff > d) {
                                guessRef = reference.name
                                diff = d
                            }
                        }
                        let notFound = ''
                        if (node.scope.scopeType === 'PATH') {
                            notFound = `Attribute '${tokenText}' not found in ${node.scope.type.name}`
                        } else {
                            notFound = `Reference '${tokenText}' not found`
                        }
                        const didYouMean = guessRef ? ` Did you mean '${guessRef}'?` : ''

                        const message = `${notFound}.${didYouMean}`
                        addMessage(createError(message, node))

                        referenceValuesWithIdentifierErrors.add(currentReferenceValue.peek())
                    }
                }
            },
            visit_function(node: Node): void {
                if (!isValidKelNode(node)) {
                    return
                }
                visitChildren(node)

                if (!(node.context instanceof FunctionContext)) throw new Error()
                const parameters = node.children

                const functionName = node.context.functionCall()._functionName.text
                const functionSymbol = node.scope.resolveFunctionSymbol(functionName, parameters.length)
                if (functionSymbol) {
                    for (let index = 0; index < parameters.length; index++) {
                        const argumentValue = parameters[index]
                        const argumentValueType = parameters[index].evaluationType
                        const parameter = functionSymbol.parameters[index]
                        if (parameter.type.isGeneric()) {
                            const argumentTypeBoundsOnly = parameter.type.rewriteGenericBounds()
                            if (!argumentTypeBoundsOnly.isAssignableFrom(argumentValueType)) {
                                const message = `Incompatible type '${argumentValueType.stringify()}' of function parameter at index ${index} when invoking function ${functionName}. Type must be assignable to '${argumentTypeBoundsOnly.stringify()}'`
                                addMessage(createError(message, argumentValue))
                            }
                        } else if (!parameter.type.isAssignableFrom(argumentValueType)) {
                            const message = `Incompatible type '${argumentValueType.stringify()}' of function parameter at index ${index} when invoking function ${functionName}. Expected type is '${parameter.type.stringify()}'`
                            addMessage(createError(message, argumentValue))
                        }
                    }
                } else {
                    const message = `Function '${functionName}' with ${parameters.length} parameter(s) does not exist.`
                    addMessage(createError(message, node))
                }
            },
            visit_if(node: Node): void {
                visitChildren(node)

                const [condition, then] = node.children
                addMessages(validator.validateToBeNotNull({ node: condition, parent: node }))
                addMessages(validator.validateToBeNotNull({ node: then, parent: node }))

                if (!Type.BOOLEAN.isAssignableFrom(condition.evaluationType)) {
                    const message = `Condition in '${readable(
                        node.nodeType,
                    )}' operation must be of type '${Type.BOOLEAN.stringify()}' but is '${condition.evaluationType.stringify()}'`
                    addMessage(createError(message, condition))
                }
            },
            visit_path(node: Node): void {
                visitChildren(node)

                const [_object, property] = node.children
                if (isEmptyKelNode(property)) {
                    const message = `Path is incomplete, property is missing`
                    addMessage(createError(message, node))
                } else if (property.nodeType !== 'IDENTIFIER' && property.nodeType !== 'ACCESS_BY_INDEX') {
                    const message = `Unsupported path expression '${getText(
                        node,
                    )}'. Property expression shall be identifier or access by index, but found: '${readable(
                        property.nodeType,
                    )}'`
                    addMessage(createError(message, node))
                }
            },
            visit_access_by_index(node: Node): void {
                visitChildren(node)
                const [collection, index] = node.children

                if (isValidKelNode(node) && node.context instanceof AccessByIndexContext) {
                    if (!node.context.indices().R_SQUARE_BRACKETS()) {
                        const message = `Access by index is missing closing square brackets ']'`
                        addMessage(createError(message, node))
                    } else if (!node.context.indices().indexValue()) {
                        const message = `Access by index is empty. There must be an expression between square brackets '[]'`
                        addMessage(createError(message, node))
                    }
                }

                addMessages(validator.validateCollectionOperation({ node: collection, parent: node }))
                if (!Type.NUMBER.isAssignableFrom(index.evaluationType)) {
                    const message = `Index of '${readable(
                        node.nodeType,
                    )}' operation must be of type '${Type.NUMBER.stringify()}' but is '${index.evaluationType.stringify()}'`
                    addMessage(createError(message, index))
                }
                const childOfNegativeNode = index.children[0]
                if (index.nodeType === 'NEGATIVE' && childOfNegativeNode.nodeType === 'DECIMAL') {
                    const message = `Index of '${readable(
                        node.nodeType,
                    )}' operation must be positive number, but it is negative`
                    addMessage(createError(message, index))
                }
            },
            visit_collection_filter(node: Node): void {
                visitChildren(node)
                const [collection, predicate] = node.children

                if (isValidKelNode(node) && node.context instanceof FilterContext && node.context.predicate()) {
                    // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
                    const predicateContext = node.context.predicate()!
                    if (!predicateContext.R_SQUARE_BRACKETS()) {
                        const message = `Filter operation is missing closing square brackets ']'`
                        addMessage(createError(message, predicate))
                    } else if (!predicateContext.value() && !predicateContext.valuePredicate()) {
                        const message = `Filter is empty. There must be an expression between square brackets '[]'`
                        addMessage(createError(message, predicate))
                    }
                }

                addMessages(validator.validateCollectionOperation({ node: collection, parent: node }))
                if (predicate && !Type.BOOLEAN.isAssignableFrom(predicate.evaluationType)) {
                    const message = `Predicate of '${readable(
                        node.nodeType,
                    )}' operation must be of type '${Type.BOOLEAN.stringify()}' but is '${predicate.evaluationType.stringify()}'`
                    addMessage(createError(message, predicate))
                }
                addMessages(validator.validateCyclomaticComplexity(node, nodeQueue))
            },
            visit_for(node: Node): void {
                visitChildren(node)
                const [variableName, collection] = node.children
                addMessages(validator.validateUniqueVariableNameInScope(variableName, node.scope))
                addMessages(validator.validateCollectionOperation({ node: collection, parent: node }))
                addMessages(validator.validateCyclomaticComplexity(node, nodeQueue))
            },
            visit_some(node: Node): void {
                visitChildren(node)
                const [variableName, collection] = node.children
                addMessages(validator.validateUniqueVariableNameInScope(variableName, node.scope))
                addMessages(validator.validateCollectionOperation({ node: collection, parent: node }))
                addMessages(validator.validateBooleanReturnType(node))
                addMessages(validator.validateCyclomaticComplexity(node, nodeQueue))
            },
            visit_every(node: Node): void {
                visitChildren(node)
                const [variableName, collection] = node.children
                addMessages(validator.validateUniqueVariableNameInScope(variableName, node.scope))
                addMessages(validator.validateCollectionOperation({ node: collection, parent: node }))
                addMessages(validator.validateBooleanReturnType(node))
                addMessages(validator.validateCyclomaticComplexity(node, nodeQueue))
            },
            visit_variable(node: Node): void {
                visitChildren(node)

                const [variableName, value] = node.children

                addMessages(validator.validateUniqueVariableNameInScope(variableName, node.scope))

                if (!isValidKelNode(node) || !(node.context instanceof VariableContext)) {
                    return
                }

                if (!node.context.TO()) {
                    const message = `Variable assignment is missing keyword 'to'. Variable name must be followed by keyword 'to' and then a value statement.`
                    addMessage(createError(message, node))
                } else if (node.context.TO() && isEmptyKelNode(value)) {
                    const message = `Variable assignment is incomplete because value statement is missing. Variable name must be followed by keyword 'to' and then a value statement.`
                    addMessage(createError(message, node))
                }
            },
            visit_value_block(node: Node): void {
                visitChildren(node)

                const valueNode = node.children[node.children.length - 1]

                if (!isValidKelNode(node) || !(node.context instanceof ValueWithVariablesContext)) {
                    return
                }

                if (node.context.variable().length > 0) {
                    if (!node.context.RETURN()) {
                        const message = `Missing keyword 'return'. Variable assignments must be followed by keyword 'return' and then a value statement.`
                        addMessage(createError(message, node))
                    } else if (node.context.RETURN() && isEmptyKelNode(valueNode)) {
                        const message = `Expression is incomplete because value statement is missing. Variable assignments must be followed by keyword 'return' and then a value statement.`
                        addMessage(createError(message, node))
                    }
                } else if (node.context.RETURN()) {
                    const message = `Keyword 'return' is redundant. Value statement can be specified without keyword 'return'.`
                    addMessage(createError(message, node))
                }
            },
            visit_cast(node: Node): void {
                visitChildren(node)

                const [typeNode, reference] = node.children
                const referenceType = reference.evaluationType

                if (!isValidKelNode(typeNode) || !referenceType.isKnown() || referenceType.isDynamic()) {
                    return
                }
                const castTypeToken = typeNode.context.text
                const castType = node.scope.resolveTypeOf(castTypeToken)

                if (!castType.isKnown()) {
                    const message = `Unknown type: '${castTypeToken}'`
                    addMessage(createError(message, typeNode))
                } else if (castType.isGeneric()) {
                    const message = `Casting to generic type is not allowed`
                    addMessage(createError(message, typeNode))
                } else if (castType.isUnion()) {
                    const message = `Casting to union type is not allowed`
                    addMessage(createError(message, typeNode))
                } else if (castType.equals(referenceType)) {
                    const message = `Cast is redundant because type of object is already '${referenceType.stringify()}'`
                    addMessage(createInfo(message, typeNode))
                } else if (castType.isAssignableFrom(referenceType)) {
                    const message = `Cast to '${castType.stringify()}' is redundant because object type is '${referenceType.stringify()}' and it extends '${castType.stringify()}'`
                    addMessage(createInfo(message, typeNode))
                } else if (!referenceType.isAssignableFrom(castType)) {
                    const message = `Cast to '${castType.stringify()}' could be an error because object type is '${referenceType.stringify()}' and it is not a supertype of '${castType.stringify()}'`
                    addMessage(createWarning(message, typeNode))
                }
            },
        }
    }
}

const validator = {
    validateCyclomaticComplexity(node: Node, queue: Node[]): AstMessage[] {
        const types = new Set<NodeType>(['FOR', 'EVERY', 'SOME'])
        const maxLevel = 3
        const nestedLoops = queue.filter(
            n => types.has(n.nodeType) || (n.nodeType === 'COLLECTION_FILTER' && Boolean(n.children[1])),
        )
        if (nestedLoops.length > maxLevel) {
            const message = `Cyclomatic complexity level is too high for expression. Maximum allowed cyclomatic complexity is ${maxLevel}`
            return [createError(message, node)]
        }
        return []
    },
    validateBooleanReturnType(node: Node): AstMessage[] {
        const [_var, _collection, returnExpression] = node.children
        if (!returnExpression) {
            return []
        }
        if (!Type.BOOLEAN.isAssignableFrom(returnExpression.evaluationType)) {
            const message = `Return type of '${readable(node.nodeType)}' operation must be of type '${
                Type.BOOLEAN.name
            }' but is '${returnExpression.evaluationType.name}'`
            return [createError(message, node)]
        }
        return []
    },
    validateUniqueVariableNameInScope(variableNode: Node, scope: Scope): AstMessage[] {
        if (!isValidKelNode(variableNode)) {
            return []
        }
        const variable = variableNode.context.text
        if (scope.isReferenceStrictlyInScope(variable)) {
            const message = `Variable '${variable}' is already defined`
            return [createError(message, variableNode)]
        }
        return []
    },
    validateCollectionOperation(p: WithNode & WithParent): AstMessage[] {
        if (!p.node) {
            return []
        }
        const evalType = p.node.evaluationType
        if (!evalType.isAssignableToArray()) {
            const message = `Operation '${readable(
                p.parent.nodeType,
            )}' can only be performed on array, but was performed on type '${evalType.name}'`
            return [createError(message, p.parent)]
        }
        return []
    },
    validateBinaryComparison(node: Node): AstMessage[] {
        let errors: AstMessage[] = []
        const [left, right] = node.children

        errors = errors
            .concat(validator.validateToBeNotNull({ node: left, parent: node }))
            .concat(validator.validateToBeNotNull({ node: right, parent: node }))

        if (!left.evaluationType.isComparableWith(right.evaluationType)) {
            const message = `Operation '${readable(
                node.nodeType,
            )}' can only be performed on comparable types, but was performed on '${left.evaluationType.stringify()}' and '${right.evaluationType.stringify()}'`
            errors.push(createError(message, node))
        }
        return errors
    },
    validateArithmeticOperation(node: Node): AstMessage[] {
        return validator.validateBinary(node, Type.NUMBER, Type.NUMBER)
    },
    validateBinaryLogicalOperation(node: Node): AstMessage[] {
        return validator.validateBinary(node, Type.BOOLEAN, Type.BOOLEAN)
    },
    validateTypeCompatibility(node: Node): AstMessage[] {
        const [left, right] = node.children
        if (!areVersusAssignable(left.evaluationType, right.evaluationType)) {
            const message = `Both sides of operator '${readable(
                node.nodeType,
            )}' must have same type, but left side was of type '${left.evaluationType.stringify()}' and right side was of type '${right.evaluationType.stringify()}'`
            return [createError(message, node)]
        }
        return []
    },
    validateUnary(node: Node, expectedType: Type): AstMessage[] {
        const [child] = node.children
        const errors = validator.validateToBeNotNull({ node: child, parent: node })
        if (!expectedType.isAssignableFrom(child.evaluationType)) {
            const message = `Operation '${readable(node.nodeType)}' can only be performed on type '${
                expectedType.name
            }' but was performed on '${child.evaluationType.stringify()}'`
            errors.push(createError(message, child))
        }
        return errors
    },
    validateBinary(node: Node, expectedLeftType: Type, expectedRightType: Type): AstMessage[] {
        const [left, right] = node.children
        return [
            ...this.validateBinarySide({ parent: node, expectedType: expectedLeftType, node: left, side: 'Left' }),
            ...this.validateBinarySide({ parent: node, expectedType: expectedRightType, node: right, side: 'Right' }),
        ]
    },
    validateBinarySide(p: { side: 'Left' | 'Right'; expectedType: Type } & WithNode & WithParent): AstMessage[] {
        let errors: AstMessage[] = []
        errors = errors.concat(validator.validateToBeNotNull(p))
        if (!p.expectedType.isAssignableFrom(p.node.evaluationType)) {
            const message = `${p.side} side of '${getText(p.parent)}' operation must be of type '${
                p.expectedType.name
            }' but was '${p.node.evaluationType.stringify()}'`
            errors.push(createError(message, p.node))
        }
        return errors
    },
    validateToBeNotNull(p: WithParent & WithNode): AstMessage[] {
        if (p.node.nodeType === 'NULL') {
            const message = `Operand cannot be 'null' literal for operation: '${readable(p.parent.nodeType)}'`
            return [createError(message, p.node)]
        }
        return []
    },
}

function areVersusAssignable(type1: Type, type2: Type): boolean {
    return type1.isAssignableFrom(type2) || type2.isAssignableFrom(type1)
}

function readable(nodeType: NodeType): string {
    return nodeType.toLowerCase().split('_').join(' ')
}

function createError(message: string, node: Node): AstMessage {
    return createAstMessage(message, node, 'ERROR')
}

function createWarning(message: string, node: Node): AstMessage {
    return createAstMessage(message, node, 'WARNING')
}

function createInfo(message: string, node: Node): AstMessage {
    return createAstMessage(message, node, 'INFO')
}

function createAstMessage(message: string, node: Node, severity: AstMessageSeverity): AstMessage {
    return {
        message,
        node,
        range: getRange(node),
        severity,
    }
}

function getReferences(scope?: Scope): VariableSymbol[] {
    if (!scope) {
        return []
    }
    const refs = [...Object.values(scope.type.properties.references), ...getReferences(scope.parentScope)]
    return refs
}

export type WithParent = { parent: Node }
export type WithNode = { node: Node }
