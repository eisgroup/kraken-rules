/* eslint-disable @typescript-eslint/no-non-null-assertion */

import { Reducer } from 'declarative-js'
import {
    AbstractParseTreeVisitor,
    AccessByIndexContext,
    BooleanContext,
    CastContext,
    ConjunctionContext,
    ConjunctionPredicateContext,
    DateContext,
    DateTimeContext,
    DecimalContext,
    DisjunctionContext,
    DisjunctionPredicateContext,
    EqualityComparisonContext,
    EqualityComparisonPredicateContext,
    ExponentContext,
    ExpressionContext,
    FilterContext,
    ForEachContext,
    ForEveryContext,
    ForEveryPredicateContext,
    ForSomeContext,
    ForSomePredicateContext,
    FunctionContext,
    IdentifierContext,
    IdentifierReferenceContext,
    IfValueContext,
    InContext,
    InlineArrayContext,
    InlineMapContext,
    KelVisitor,
    MatchesRegExpContext,
    MatchesRegExpPredicateContext,
    MultiplicationOrDivisionContext,
    MultiplicationOrDivisionValueContext,
    NegationContext,
    NegationPredicateContext,
    NegativeContext,
    NegativeValueContext,
    NullContext,
    NumericalComparisonContext,
    NumericalComparisonPredicateContext,
    ParserRuleContext,
    PathContext,
    PrecedenceContext,
    PrecedencePredicateContext,
    ReferencePrecedenceContext,
    ReferenceValueContext,
    StringContext,
    SubtractionOrAdditionContext,
    SubtractionOrAdditionValueContext,
    ThisValueContext,
    TypeComparisonContext,
    TypeComparisonPredicateContext,
    ValueContext,
    ErrorNode,
    PrecedenceValueContext,
    ReferenceContext,
    IncompletePathContext,
    IndicesContext,
    PredicateContext,
    PathSeparatorContext,
    VariableContext,
    ValueBlockContext,
    ValueWithVariablesContext,
} from 'kraken-expression-language-visitor'

import { Scope } from '../scope/Scope'
import { SymbolTable } from '../symbol/SymbolTable'
import { VariableSymbol } from '../symbol/VariableSymbol'
import { Deque } from './Deque'
import { NodeType, TypeFact, Node, isValidKelNode, ValidKelNode } from './Node'
import { cursorAtTheBeginningOf, cursorAtTheEndOf, emptyRangeAt, getTokenText, Range, Token } from './NodeUtils'
import { Type, ArrayType, UnionType } from '../type/Types'
import { ScopeType } from '../scope/ScopeType'

let counter = 0

export class AstGeneratingVisitor extends AbstractParseTreeVisitor<Node> implements KelVisitor<Node> {
    queue: ParserRuleContext[] = []
    scopes: Deque<Scope>
    typeGuardContexts: Deque<Record<string, TypeFact>>

    constructor(scope: Scope) {
        super()
        this.scopes = new Deque([scope])
        this.typeGuardContexts = new Deque<Record<string, TypeFact>>([{}])
    }

    visit(ctx: ParserRuleContext): Node {
        this.queue.push(ctx)

        const result = super.visit(ctx)

        this.queue.pop()

        return result
    }

    visitErrorNode(errorNode: ErrorNode): Node {
        return {
            scope: this.scope(),
            evaluationType: Type.UNKNOWN,
            nodeType: 'ERROR',
            errorNode,
            children: [],
        }
    }

    protected defaultResult(): Node {
        return {
            scope: this.scope(),
            evaluationType: Type.ANY,
            nodeType: 'EMPTY',
            children: [],
        }
    }

    protected aggregateResult(_p: Node, next: Node): Node {
        return next
    }

    visitPrecedencePredicate(context: PrecedencePredicateContext): Node {
        return this.visit(context.valuePredicate())
    }
    visitConjunction(context: ConjunctionContext): Node {
        return this._visitConjunction(context)
    }
    visitConjunctionPredicate(context: ConjunctionPredicateContext): Node {
        return this._visitConjunction(context)
    }
    visitDisjunction(context: DisjunctionContext): Node {
        return this._visitTwoChildren(context, 'OR', Type.BOOLEAN)
    }
    visitDisjunctionPredicate(context: DisjunctionPredicateContext): Node {
        return this._visitTwoChildren(context, 'OR', Type.BOOLEAN)
    }
    visitEqualityComparison(context: EqualityComparisonContext): Node {
        return this._visitTwoChildren(context, this.getEqComparisonType(context), Type.BOOLEAN)
    }
    visitEqualityComparisonPredicate(context: EqualityComparisonPredicateContext): Node {
        return this.visitEqualityComparison(context)
    }
    visitTypeComparison(context: TypeComparisonContext): Node {
        const expressionNode = this.visit(context.value())
        const scope = this.scope()

        const typeNode: ValidKelNode = {
            nodeType: 'TYPE',
            context: context.identifier(),
            evaluationType: Type.TYPE,
            scope,
            children: [],
        }

        const deducedTypeFacts: Record<string, TypeFact> = {}
        if (isValidKelNode(expressionNode)) {
            const token = getTokenText(expressionNode.context)
            const castedType = scope.resolveTypeOf(getTokenText(typeNode.context))
            if (castedType.isKnown()) {
                deducedTypeFacts[token] = {
                    expression: expressionNode,
                    type: scope.resolveTypeOf(getTokenText(typeNode.context)),
                }
            }
        }

        return {
            children: [expressionNode, typeNode],
            context,
            nodeType: this.getTypeComparisonType(context),
            evaluationType: Type.BOOLEAN,
            deducedTypeFacts,
            scope,
        }
    }
    visitTypeComparisonPredicate(context: TypeComparisonPredicateContext): Node {
        return this.visitTypeComparison(context)
    }
    visitNumericalComparison(context: NumericalComparisonContext): Node {
        return this._visitTwoChildren(context, this.getNumericalComparisonType(context), Type.BOOLEAN)
    }
    visitNumericalComparisonPredicate(context: NumericalComparisonPredicateContext): Node {
        return this.visitNumericalComparison(context)
    }
    visitMatchesRegExp(context: MatchesRegExpContext): Node {
        return {
            children: [this.visit(context.value())],
            context,
            nodeType: 'MATCHES_REG_EXP',
            evaluationType: Type.BOOLEAN,
            scope: this.scope(),
        }
    }
    visitMatchesRegExpPredicate(context: MatchesRegExpPredicateContext): Node {
        return this.visitMatchesRegExp(context)
    }
    visitNegation(context: NegationContext): Node {
        return {
            children: [this.visit(context.value())],
            context,
            nodeType: 'NEGATION',
            evaluationType: Type.BOOLEAN,
            scope: this.scope(),
        }
    }
    visitNegationPredicate(context: NegationPredicateContext): Node {
        return this.visit(context.value())
    }
    visitReferencePrecedence(context: ReferencePrecedenceContext): Node {
        return this.visit(context.reference())
    }
    visitCast(context: CastContext): Node {
        const scope = this.scope()

        const ref = this.visit(context.reference())

        const typeNode: ValidKelNode = {
            nodeType: 'TYPE',
            context: context.type(),
            evaluationType: Type.TYPE,
            scope,
            children: [],
        }

        return {
            children: [typeNode, ref],
            context,
            nodeType: 'CAST',
            evaluationType: scope.resolveTypeOf(typeNode.context.text),
            scope,
        }
    }
    visitIdentifierReference(context: IdentifierReferenceContext): Node {
        return this.visit(context.identifier())
    }
    visitFunction(context: FunctionContext): Node {
        const functionCallContext = context.functionCall()
        const scope = this.scope()

        const parameters = functionCallContext._arguments
            ? functionCallContext._arguments.value().map(v => this.visit(v))
            : []
        const functionName = functionCallContext._functionName.text

        let evaluationType = this.findTypeOf(context)
        if (!evaluationType) {
            const functionSymbol = scope.resolveFunctionSymbol(functionName, parameters.length)
            if (functionSymbol) {
                const rewrites = functionSymbol.resolveGenericRewrites(parameters.map(p => p.evaluationType))
                evaluationType = functionSymbol.type.rewriteGenericTypes(rewrites)
            } else {
                evaluationType = Type.UNKNOWN
            }
        }

        return { context, nodeType: 'FUNCTION', scope, evaluationType, children: parameters }
    }
    visitAccessByIndex(context: AccessByIndexContext): Node {
        const { scopes } = this
        function unwrapParentScopeOfPath(): Deque<Scope> {
            const ps = new Deque<Scope>([])
            while (scopes.peek().scopeType === 'PATH') {
                ps.push(scopes.pop()!)
            }
            return ps
        }
        function wrapPathScopes(ps: Scope[]): void {
            for (const scope of ps) {
                scopes.push(scope)
            }
        }

        const collection = this.visit(context._collection)

        const pathScopes = unwrapParentScopeOfPath()
        const index = this._createIndices(context.indices(), context)

        this.pushScope(
            'FILTER',
            // merged current scope object from object that is for access by index with object that is for filter
            // so that when 'this.' is used, then autocomplete provides properties from both
            UnionType.createUnion(collection.evaluationType.unwrapArrayType(), this.scope().type),
        )
        const indexMergedWithPredicate = this._createIndices(context.indices(), context)
        this.popScope()
        wrapPathScopes(pathScopes.toArray())

        const evaluationType = this.findTypeOf(context) ?? collection.evaluationType.unwrapArrayType()

        return {
            children: [collection, index],
            maybeFilterPredicate: indexMergedWithPredicate,
            context,
            nodeType: 'ACCESS_BY_INDEX',
            scope: this.scope(),
            evaluationType,
        }
    }
    _createIndices(indicesContext: IndicesContext, context: AccessByIndexContext): Node {
        const indexValue = indicesContext.indexValue()
        if (indexValue) {
            return this.visit(indexValue)
        } else {
            const endOfLeftSquareBracket = cursorAtTheEndOf(context.indices().L_SQUARE_BRACKETS().symbol)
            return this.emptyNodeWithin(emptyRangeAt(endOfLeftSquareBracket))
        }
    }
    visitPath(context: PathContext): Node {
        return this._createPath(context._object, context._property, context, context.pathSeparator())
    }
    visitIncompletePath(context: IncompletePathContext): Node {
        return this._createPath(context._object, undefined, context, context.pathSeparator())
    }
    _createPath(
        objectContext: ReferenceContext,
        propertyContext: ReferenceContext | undefined,
        context: PathContext | IncompletePathContext,
        pathSeparatorContext: PathSeparatorContext,
    ): Node {
        const object = this.visit(objectContext)

        const objectEvaluationType = object.evaluationType.unwrapArrayType()
        const pathSeparatorToken = (pathSeparatorContext.DOT() ?? pathSeparatorContext.QDOT())!.symbol

        this.pushScopeWithParent('PATH', undefined, objectEvaluationType)
        const prop = propertyContext
            ? this.visit(propertyContext)
            : this.emptyNodeWithin(emptyRangeAt(cursorAtTheEndOf(pathSeparatorToken)))
        this.popScope()

        const evaluationType =
            this.findTypeOf(context) ?? object.evaluationType.isAssignableToArray()
                ? object.evaluationType.mapTo(prop.evaluationType.unwrapArrayType())
                : prop.evaluationType

        return {
            children: [object, prop],
            context,
            nodeType: 'PATH',
            scope: this.scope(),
            evaluationType,
        }
    }
    visitFilter(context: FilterContext): Node {
        const collection = this.visit(context._filterCollection)
        const predicateContext = context.predicate()

        const children = []
        children.push(collection)

        let collectionEvaluationType = collection.evaluationType
        if (predicateContext) {
            this.pushScope('FILTER', collection.evaluationType.unwrapArrayType())
            const predicate = this._createPredicate(predicateContext)
            children.push(predicate)
            this.popScope()

            if (isValidKelNode(predicate)) {
                if (predicate.deducedTypeFacts && predicate.deducedTypeFacts['this']) {
                    collectionEvaluationType = ArrayType.createArray(predicate.deducedTypeFacts['this'].type)
                }
            }
        }

        const evaluationType = this.findTypeOf(context) ?? collectionEvaluationType

        return {
            children,
            context,
            nodeType: 'COLLECTION_FILTER',
            scope: this.scope(),
            evaluationType,
        }
    }
    _createPredicate(predicateContext: PredicateContext): Node {
        const valuePredicate = predicateContext.valuePredicate()
        const value = predicateContext.value()
        if (valuePredicate) {
            return this.visit(valuePredicate)
        } else if (value) {
            return this.visit(value)
        } else {
            const endOfLeftSquareBracket = cursorAtTheEndOf(predicateContext.L_SQUARE_BRACKETS().symbol)
            return this.emptyNodeWithin(emptyRangeAt(endOfLeftSquareBracket))
        }
    }
    visitPrecedence(context: PrecedenceContext): Node {
        return this.visit(context.value())
    }
    visitPrecedencePrecedence(context: PrecedenceContext): Node {
        return this.visit(context.value())
    }
    visitPrecedenceValue(context: PrecedenceValueContext): Node {
        return this.visit(context.indexValue())
    }
    visitExponent(context: ExponentContext): Node {
        return this._visitTwoChildren(context, 'EXPONENT', Type.NUMBER)
    }
    visitExponentValue(context: ExponentContext): Node {
        return this._visitTwoChildren(context, 'EXPONENT', Type.NUMBER)
    }
    visitNegative(context: NegativeContext): Node {
        return {
            context,
            nodeType: 'NEGATIVE',
            evaluationType: Type.NUMBER,
            scope: this.scope(),
            children: [this.visit(context.value())],
        }
    }
    visitIn(context: InContext): Node {
        return this._visitTwoChildren(context, 'IN', Type.BOOLEAN)
    }
    visitInPredicate(context: InContext): Node {
        return this._visitTwoChildren(context, 'IN', Type.BOOLEAN)
    }
    visitSubtractionOrAddition(context: SubtractionOrAdditionContext): Node {
        return this._visitTwoChildren(context, this.getSubtractionAdditionType(context), Type.NUMBER)
    }
    visitSubtractionOrAdditionValue(context: SubtractionOrAdditionValueContext): Node {
        return this.visitSubtractionOrAddition(context)
    }
    visitMultiplicationOrDivisionValue(context: MultiplicationOrDivisionValueContext): Node {
        return this._visitTwoChildren(context, this.getMultiplicationOrDivisionType(context), Type.NUMBER)
    }
    visitMultiplicationOrDivision(context: MultiplicationOrDivisionContext): Node {
        return this._visitTwoChildren(context, this.getMultiplicationOrDivisionType(context), Type.NUMBER)
    }
    visitInlineArray(context: InlineArrayContext): Node {
        function determineInlineArrayItemType(types: Type[]): Type {
            if (!types.length) {
                return Type.ANY
            }
            let result = types[0]
            for (const type of types) {
                result = result.resolveCommonTypeOf(type) ?? Type.ANY
            }
            return result
        }
        const valueList = context.valueList()
        const items = valueList ? valueList.value().map(v => this.visit(v)) : []

        const inlineArrayType = determineInlineArrayItemType(items.map(item => item.evaluationType))

        return {
            children: items,
            context,
            nodeType: 'INLINE_ARRAY',
            evaluationType: ArrayType.createArray(inlineArrayType),
            scope: this.scope(),
        }
    }
    visitInlineMap(context: InlineMapContext): Node {
        // eslint-disable-next-line @typescript-eslint/no-this-alias
        const thiz = this
        function buildMapType(): Type {
            const pairs = context
                .keyValuePairs()
                .keyValuePair()
                .map(kv => {
                    const { text } = kv._key
                    return {
                        key: text!,
                        value: thiz.visit(kv.value()),
                    }
                })
            return Type.createType({
                name: `InlineMap_${counter++}`,
                extendedTypes: [],
                properties: SymbolTable.create({
                    functions: [],
                    references: pairs.reduce(
                        Reducer.toObject(
                            p => p.key,
                            p =>
                                VariableSymbol.create({
                                    name: p.key,
                                    type: p.value.evaluationType,
                                }),
                        ),
                        {},
                    ),
                }),
            })
        }
        return {
            children: context
                .keyValuePairs()
                .keyValuePair()
                .map(p => this.visit(p)),
            context,
            nodeType: 'INLINE_MAP',
            scope: this.scope(),
            evaluationType: buildMapType(),
        }
    }
    visitIfValue(context: IfValueContext): Node {
        const children: Node[] = []

        const condition = this.visit(context._condition)
        const conditionDeducedTypeFacts = isValidKelNode(condition) ? condition.deducedTypeFacts ?? {} : {}

        children.push(condition)

        this.pushTypeFacts(conditionDeducedTypeFacts)
        const then = this.visit(context._thenExpression)
        this.popTypeFacts()
        children.push(then)

        let evaluationType = then.evaluationType

        if (context._elseExpression) {
            const elze = this.visit(context._elseExpression)
            children.push(elze)

            evaluationType = then.evaluationType.resolveCommonTypeOf(elze.evaluationType) ?? Type.UNKNOWN
        }

        return {
            children,
            context,
            nodeType: 'IF',
            evaluationType,
            scope: this.scope(),
        }
    }
    visitThisValue(context: ThisValueContext): Node {
        const scope = this.scope()
        const evaluationType = this.findTypeOf(context) ?? scope.resolveClosestReferencableScope().type
        return {
            context,
            nodeType: 'THIS',
            evaluationType,
            scope,
            children: [],
        }
    }
    visitReferenceValue(context: ReferenceValueContext): Node {
        return this._createReferenceValue(context)
    }
    visitReferenceValueValue(context: ReferenceValueContext): Node {
        return this._createReferenceValue(context)
    }
    _createReferenceValue(context: ReferenceValueContext): Node {
        const reference = this.visit(context.reference())
        return {
            context,
            children: [reference],
            nodeType: 'REFERENCE',
            scope: this.scope(),
            evaluationType: reference.evaluationType,
        }
    }
    visitForEach(context: ForEachContext): Node {
        const collection = this._createCollection(context._collection, context._opIn)
        const variable = this._createIterationVar(context._var, collection, context._opStart)
        if (!collection) {
            return {
                context,
                nodeType: 'FOR',
                evaluationType: Type.ANY,
                scope: this.scope(),
                children: [variable],
            }
        }
        const returnNode = this._createReturn(context._var, collection, context._returnExpression, context._opReturn)
        if (!returnNode) {
            return {
                context,
                nodeType: 'FOR',
                evaluationType: Type.ANY,
                scope: this.scope(),
                children: [variable, collection],
            }
        }
        return {
            context,
            nodeType: 'FOR',
            evaluationType: returnNode.evaluationType.wrapArrayType(),
            scope: this.scope(),
            children: [variable, collection, returnNode],
        }
    }
    visitForEvery(context: ForEveryContext): Node {
        return this._createSomeOrEvery(context, 'EVERY')
    }
    visitForEveryPredicate(context: ForEveryPredicateContext): Node {
        return this._createSomeOrEvery(context, 'EVERY')
    }
    visitForSome(context: ForSomeContext): Node {
        return this._createSomeOrEvery(context, 'SOME')
    }
    visitForSomePredicate(context: ForSomePredicateContext): Node {
        return this._createSomeOrEvery(context, 'SOME')
    }
    _createSomeOrEvery(context: ForEveryContext | ForSomeContext, nodeType: NodeType): Node {
        const collection = this._createCollection(context._collection, context._opIn)
        const variable = this._createIterationVar(context._var, collection, context._opStart)
        if (!collection) {
            return {
                context,
                nodeType: 'EVERY',
                evaluationType: Type.BOOLEAN,
                scope: this.scope(),
                children: [variable],
            }
        }
        const returnNode = this._createReturn(context._var, collection, context._returnExpression, context._opReturn)
        if (!returnNode) {
            return {
                context,
                nodeType: 'EVERY',
                evaluationType: Type.BOOLEAN,
                scope: this.scope(),
                children: [variable, collection],
            }
        }
        return {
            context,
            nodeType,
            evaluationType: Type.BOOLEAN,
            scope: this.scope(),
            children: [variable, collection, returnNode],
        }
    }
    _createVar(varContext: IdentifierContext | undefined, valueType: Type | undefined, varStart: Token): Node {
        // create empty scope because variable does not refer to anything in scope
        // use PATH scope type so that no keywords are suggested either
        const scope = Scope.createEmptyScope()
        if (varContext && !varContext.exception) {
            return {
                children: [],
                nodeType: 'VARIABLE_NAME',
                evaluationType: valueType ?? Type.ANY,
                context: varContext,
                scope,
            }
        }
        return this.emptyNodeWithinWithScope(emptyRangeAt(cursorAtTheEndOf(varStart)), scope)
    }
    _createIterationVar(
        varContext: IdentifierContext | undefined,
        collection: Node | undefined,
        forStart: Token,
    ): Node {
        return this._createVar(varContext, collection?.evaluationType?.unwrapArrayType(), forStart)
    }
    _createReturn(
        varContext: IdentifierContext | undefined,
        collectionNode: Node,
        returnContext: ValueBlockContext | undefined,
        opReturn: Token | undefined,
    ): Node | undefined {
        const iterationType = varContext
            ? this.buildTypeWithVariable(varContext.text, collectionNode.evaluationType.unwrapArrayType())
            : Type.ANY
        this.pushScope('VARIABLES_MAP', iterationType)
        let returnNode
        if (returnContext && !returnContext.exception) {
            returnNode = this.visit(returnContext)
        } else if (opReturn) {
            returnNode = this.emptyNodeWithin(emptyRangeAt(cursorAtTheEndOf(opReturn)))
        }
        this.popScope()
        return returnNode
    }
    _createCollection(collectionContext: ValueContext | undefined, opIn: Token | undefined): Node | undefined {
        if (collectionContext && !collectionContext.exception) {
            return this.visit(collectionContext)
        } else if (opIn) {
            return this.emptyNodeWithin(emptyRangeAt(cursorAtTheEndOf(opIn)))
        }
        return undefined
    }
    visitNegativeValue(context: NegativeValueContext): Node {
        const value = this.visit(context.value())
        return {
            children: [value],
            context,
            nodeType: 'NEGATIVE',
            evaluationType: value.evaluationType,
            scope: this.scope(),
        }
    }
    visitExpression(context: ExpressionContext): Node {
        const value = context.valueBlock()
        if (value) {
            return this.visit(value)
        }
        return this.emptyNodeWithin(emptyRangeAt(cursorAtTheBeginningOf(context.start)))
    }
    visitValueBlock(context: ValueBlockContext): Node {
        const valueWithVariablesContext = context.valueWithVariables()
        if (valueWithVariablesContext) {
            return this.visit(valueWithVariablesContext)
        }
        return this.visit(context.value()!)
    }
    visitValueWithVariables(context: ValueWithVariablesContext): Node {
        const valueCtx = context.value()
        const variableNodes = []
        for (const variableCtx of context.variable()) {
            const variableNode = this.visit(variableCtx)
            variableNodes.push(variableNode)
            const variableName = variableCtx.identifier().text
            const variableType = this.buildTypeWithVariable(variableName, variableNode.evaluationType)
            this.pushScope('VARIABLES_MAP', variableType)
        }

        const valueNode = valueCtx
            ? this.visit(valueCtx)
            : this.emptyNodeWithin(
                  emptyRangeAt(cursorAtTheEndOf(context.RETURN()?.symbol ?? context.stop ?? context.start)),
              )

        variableNodes.forEach(() => this.popScope())

        return {
            children: [...variableNodes, valueNode],
            context,
            nodeType: 'VALUE_BLOCK',
            evaluationType: valueNode.evaluationType,
            scope: this.scope(),
        }
    }
    private buildTypeWithVariable(variableName: string, variableType: Type): Type {
        return Type.createType({
            name: 'VARIABLES_MAP_' + variableName,
            properties: SymbolTable.create({
                functions: [],
                references: {
                    [variableName]: VariableSymbol.create({ name: variableName, type: variableType }),
                },
            }),
            extendedTypes: [],
        })
    }
    visitVariable(context: VariableContext): Node {
        const valueCtx = context.value()
        const value = valueCtx
            ? this.visit(valueCtx)
            : this.emptyNodeWithin(emptyRangeAt(cursorAtTheEndOf(context.SET().symbol)))

        const variable = this._createVar(context.identifier(), value.evaluationType, context.SET().symbol)

        return {
            children: [variable, value],
            context,
            nodeType: 'VARIABLE',
            evaluationType: value.evaluationType,
            scope: this.scope(),
        }
    }
    visitDate(context: DateContext): Node {
        return { context, nodeType: 'DATE', evaluationType: Type.DATE, scope: this.scope(), children: [] }
    }
    visitDateTime(context: DateTimeContext): Node {
        const nodeType = 'DATETIME'
        return { context, nodeType, evaluationType: Type.DATETIME, scope: this.scope(), children: [] }
    }
    visitBoolean(context: BooleanContext): Node {
        return { context, nodeType: 'BOOLEAN', evaluationType: Type.BOOLEAN, scope: this.scope(), children: [] }
    }
    visitDecimal(context: DecimalContext): Node {
        return { context, nodeType: 'DECIMAL', evaluationType: Type.NUMBER, scope: this.scope(), children: [] }
    }
    visitString(context: StringContext): Node {
        return { context, nodeType: 'STRING', evaluationType: Type.STRING, scope: this.scope(), children: [] }
    }
    visitNull(context: NullContext): Node {
        return { context, nodeType: 'NULL', evaluationType: Type.ANY, scope: this.scope(), children: [] }
    }
    visitIdentifier(context: IdentifierContext): Node {
        const scope = this.scope()
        const evaluationType =
            this.findTypeOf(context) ?? scope.resolveReferenceSymbol(context.text)?.type ?? Type.UNKNOWN

        return {
            context,
            nodeType: 'IDENTIFIER',
            evaluationType,
            scope,
            children: [],
        }
    }
    private _visitConjunction(context: ConjunctionContext): Node {
        const left = this.visit(context.value(0))
        const leftDeducedTypeFacts = isValidKelNode(left) ? left.deducedTypeFacts ?? {} : {}

        this.pushTypeFacts(leftDeducedTypeFacts)
        const right = this.visit(context.value(1))
        const rightDeducedTypeFacts = isValidKelNode(right) ? right.deducedTypeFacts ?? {} : {}
        this.popTypeFacts()

        return {
            children: [left, right],
            context,
            nodeType: 'AND',
            evaluationType: Type.BOOLEAN,
            scope: this.scope(),
            deducedTypeFacts: { ...leftDeducedTypeFacts, ...rightDeducedTypeFacts },
        }
    }
    private _visitTwoChildren<
        CONTEXT extends {
            value(i: number): ValueContext
        } & ValueContext,
    >(context: CONTEXT, nodeType: NodeType, evaluationType: Type): Node {
        return {
            children: [this.visit(context.value(0)), this.visit(context.value(1))],
            context,
            nodeType,
            evaluationType,
            scope: this.scope(),
        }
    }

    private getNumericalComparisonType(context: NumericalComparisonContext): NodeType {
        let nodeType: NodeType
        if (context.OP_LESS()) {
            nodeType = 'LESS_THAN'
        } else if (context.OP_LESS_EQUALS()) {
            nodeType = 'LESS_THAN_OR_EQUALS'
        } else if (context.OP_MORE()) {
            nodeType = 'MORE_THAN'
        } else if (context.OP_MORE_EQUALS()) {
            nodeType = 'MORE_THAN_OR_EQUALS'
        } else {
            throw new Error('Failed to determine node type: in context' + JSON.stringify(context))
        }
        return nodeType
    }
    private getMultiplicationOrDivisionType(context: MultiplicationOrDivisionContext): NodeType {
        let nodeType: NodeType
        if (context.OP_MULT()) {
            nodeType = 'MULTIPLICATION'
        } else if (context.OP_DIV()) {
            nodeType = 'DIVISION'
        } else if (context.OP_MOD()) {
            nodeType = 'MODULUS'
        } else {
            throw new Error('Failed to determine node type: in context' + JSON.stringify(context))
        }
        return nodeType
    }
    private getTypeComparisonType(context: TypeComparisonContext): NodeType {
        let nodeType: NodeType
        if (context.OP_INSTANCEOF()) {
            nodeType = 'INSTANCEOF'
        } else if (context.OP_TYPEOF()) {
            nodeType = 'TYPEOF'
        } else {
            throw new Error('Failed to determine node type: in context' + JSON.stringify(context))
        }
        return nodeType
    }
    private getSubtractionAdditionType(context: SubtractionOrAdditionContext): NodeType {
        let nodeType: NodeType
        if (context.OP_MINUS()) {
            nodeType = 'SUBTRACTION'
        } else if (context.OP_ADD()) {
            nodeType = 'ADDITION'
        } else {
            throw new Error('Failed to determine node type: in context' + JSON.stringify(context))
        }
        return nodeType
    }
    private getEqComparisonType(context: EqualityComparisonPredicateContext): NodeType {
        return context.OP_EQUALS() ? 'EQUALS' : 'NOT_EQUALS'
    }
    private emptyNodeWithin(r: Range): Node {
        return this.emptyNodeWithinWithScope(r, this.scope())
    }
    private emptyNodeWithinWithScope(r: Range, scope: Scope): Node {
        return {
            range: r,
            scope: scope,
            nodeType: 'EMPTY',
            children: [],
            evaluationType: Type.ANY,
        }
    }
    private popTypeFacts(): void {
        this.typeGuardContexts.pop()
    }
    private pushTypeFacts(facts: Record<string, TypeFact>): void {
        const unionOfFacts = {
            ...this.typeGuardContexts.peek(),
            ...facts,
        }
        this.typeGuardContexts.push(unionOfFacts)
    }
    private findTypeOf(context: ParserRuleContext): Type | undefined {
        const token = getTokenText(context)
        if (token && this.typeGuardContexts.peek()[token]) {
            return this.typeGuardContexts.peek()[token].type
        }
        return undefined
    }
    private scope(): Scope {
        return this.scopes.peek()
    }
    private popScope(): Scope {
        this.popTypeFacts()
        return this.scopes.pop()
    }
    private pushScope(scopeType: ScopeType, evaluationType: Type): void {
        this.pushScopeWithParent(scopeType, this.scope(), evaluationType)
    }
    private pushScopeWithParent(scopeType: ScopeType, parent: Scope | undefined, evaluationType: Type): void {
        this.scopes.push(Scope.createScope(scopeType, parent, evaluationType))
        this.typeGuardContexts.push({})
    }
}
