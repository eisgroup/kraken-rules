// tslint:disable: max-file-line-count
import { Reducer } from "declarative-js";
import {
    AbstractParseTreeVisitor, AccessByIndexContext, BooleanContext, CastContext,
    ConjunctionContext, ConjunctionPredicateContext, DateContext, DateTimeContext,
    DecimalContext, DisjunctionContext, DisjunctionPredicateContext,
    EqualityComparisonContext, EqualityComparisonPredicateContext,
    ExponentContext, ExpressionContext, FilterContext, ForEachContext,
    ForEveryContext, ForEveryPredicateContext, ForSomeContext,
    ForSomePredicateContext, FunctionCallContext, FunctionContext,
    IdentifierContext, IdentifierReferenceContext, IfValueContext, InContext,
    InlineArrayContext, InlineMapContext, KelVisitor, MatchesRegExpContext,
    MatchesRegExpPredicateContext, MultiplicationOrDivisionContext,
    MultiplicationOrDivisionValueContext, NegationContext,
    NegationPredicateContext, NegativeContext, NegativeValueContext, NullContext,
    NumericalComparisonContext, NumericalComparisonPredicateContext,
    ParserRuleContext, PathContext, PrecedenceContext, PrecedencePredicateContext,
    ReferencePrecedenceContext, ReferenceValueContext,
    StringContext, SubtractionOrAdditionContext,
    SubtractionOrAdditionValueContext, ThisContext, ThisValueContext,
    TypeComparisonContext, TypeComparisonPredicateContext, ValueContext, ErrorNode
} from "kraken-expression-language-visitor";

import { Scope } from "../scope/Scope";
import { ScopeType } from "../scope/ScopeType";
import { FunctionSymbol } from "../symbol/FunctionSymbol";
import { SymbolTable } from "../symbol/SymbolTable";
import { VariableSymbol } from "../symbol/VariableSymbol";
import { ArrayType } from "../type/ArrayType";
import { GenericType } from "../type/GenericType";
import { Type } from "../type/Type";
import { Deque } from "./Deque";
import { NodeType } from "./NodeType";

let counter = 0;

export type Node = ValidKelNode | ErrorKelNode | PartialKelNode;

export interface PartialKelNode {
    nodeType: "PARTIAL";
    /** `Type.UNKNOWN`
     * @see {@link Type}
     */
    evaluationType: Type;
    scope: Scope;
    /** no children */
    children: Node[];
}

export interface ErrorKelNode {
    errorNode: ErrorNode;
    nodeType: "PARTIAL";
    /** `Type.UNKNOWN`
     * @see {@link Type}
     */
    evaluationType: Type;
    scope: Scope;
    /** no children */
    children: Node[];
}
export interface ValidKelNode {
    context: ParserRuleContext;
    nodeType: NodeType;
    evaluationType: Type;
    scope: Scope;
    /**
     * nodes in order, in which they occur in expression.
     * If expression is ` if true then 1 else 2 `, then child nodes will be `[true, 1, 2]` nodes
     * If expression is ` if true then 1 `, then child nodes will be `[true, 1]` nodes
     */
    children: Node[];
}

export function isValidKelNode(node: Node): node is ValidKelNode {
    return node.nodeType !== "PARTIAL";
}
export function isErrorKelNode(node: Node): node is ErrorKelNode {
    return node.nodeType === "PARTIAL" && Boolean((node as ErrorKelNode).errorNode);
}
export function isPartialKelNode(node: Node): node is PartialKelNode {
    return !isErrorKelNode(node) && !isValidKelNode(node);
}

export class KelTraversingVisitor
    extends AbstractParseTreeVisitor<Node>
    implements KelVisitor<Node> {

    queue: ParserRuleContext[] = [];
    scopes: Deque<Scope>;

    constructor(scope: Scope) {
        super();
        this.scopes = new Deque([scope]);
    }

    visit(ctx: ParserRuleContext): Node {
        this.queue.push(ctx);

        const result = super.visit(ctx);

        this.queue.pop();

        return result;
    }

    visitErrorNode(errorNode: ErrorNode): Node {
        return {
            scope: this.scopes.peek(),
            evaluationType: Type.UNKNOWN,
            nodeType: "PARTIAL",
            errorNode,
            children: []
        };
    }

    protected defaultResult(): Node {
        return {
            scope: this.scopes.peek(),
            evaluationType: Type.ANY,
            nodeType: "PARTIAL",
            children: []
        };
    }

    protected aggregateResult(_p: Node, next: Node): Node {
        return next;
    }

    visitPrecedencePredicate(context: PrecedencePredicateContext): Node {
        return this.visit(context.valuePredicate());
    }
    visitConjunction(context: ConjunctionContext): Node {
        return this._visitTwoChildren(context, "AND", Type.BOOLEAN);
    }
    visitDisjunction(context: DisjunctionContext): Node {
        return this._visitTwoChildren(context, "OR", Type.BOOLEAN);
    }
    visitDisjunctionPredicate(context: DisjunctionPredicateContext): Node {
        return this.visitDisjunction(context);
    }
    visitConjunctionPredicate(context: ConjunctionPredicateContext): Node {
        return this.visitConjunction(context);
    }
    visitEqualityComparison(context: EqualityComparisonContext): Node {
        return this._visitTwoChildren(context, this.getEqComparisonType(context), Type.BOOLEAN);
    }
    visitEqualityComparisonPredicate(context: EqualityComparisonPredicateContext): Node {
        return this.visitEqualityComparison(context);
    }
    visitTypeComparison(context: TypeComparisonContext): Node {
        return {
            children: [
                this.visit(context.value()),
                this.visit(context.identifier())
            ],
            context,
            nodeType: this.getTypeComparisonType(context),
            evaluationType: Type.BOOLEAN,
            scope: this.scopes.peek()
        };
    }
    visitTypeComparisonPredicate(context: TypeComparisonPredicateContext): Node {
        return this.visitTypeComparison(context);
    }
    visitNumericalComparison(context: NumericalComparisonContext): Node {
        return this._visitTwoChildren(context, this.getNumericalComparisonType(context), Type.BOOLEAN);
    }
    visitNumericalComparisonPredicate(context: NumericalComparisonPredicateContext): Node {
        return this.visitNumericalComparison(context);
    }
    visitMatchesRegExp(context: MatchesRegExpContext): Node {
        return {
            children: [this.visit(context.value())],
            context,
            nodeType: "MATCHES_REG_EXP",
            evaluationType: Type.BOOLEAN,
            scope: this.scopes.peek()
        };
    }
    visitMatchesRegExpPredicate(context: MatchesRegExpPredicateContext): Node {
        return this.visitMatchesRegExp(context);
    }
    visitNegation(context: NegationContext): Node {
        return {
            children: [this.visit(context.value())],
            context,
            nodeType: "NEGATION",
            evaluationType: Type.BOOLEAN,
            scope: this.scopes.peek()
        };
    }
    visitNegationPredicate(context: NegationPredicateContext): Node {
        return this.visit(context.value());
    }
    visitReferencePrecedence(context: ReferencePrecedenceContext): Node {
        return this.visit(context.reference());
    }
    visitCast(context: CastContext): Node {
        const ref = this.visit(context.reference());
        const type = context.type().text;
        const scope = this.scopes.peek();
        return {
            children: [ref],
            context,
            nodeType: "CAST",
            evaluationType: scope.resolveTypeOf(type),
            scope
        };
    }
    visitIdentifierReference(context: IdentifierReferenceContext): Node {
        return this.visit(context.identifier());
    }
    visitFunction(context: FunctionContext): Node {
        return this.visit(context.functionCall());
    }
    visitAccessByIndex(context: AccessByIndexContext): Node {
        const { scopes } = this;
        function unwrapParentScopeOfPath(): Deque<Scope> {
            const ps = new Deque<Scope>([]);
            while (scopes.peek().scopeType === "PATH") {
                ps.push(scopes.pop()!);
            }
            return ps;
        }
        function wrapPathScopes(ps: Scope[]): void {
            for (const scope of ps) {
                scopes.push(scope);
            }
        }

        const collection = this.visit(context._collection);
        const pathScopes = unwrapParentScopeOfPath();
        const index = this.visit(context.indices().indexValue());
        wrapPathScopes(pathScopes.toArray());

        return {
            children: [collection, index],
            context,
            nodeType: "ACCESS_BY_INDEX",
            scope: scopes.peek(),
            evaluationType: this.unwrapArrayType(collection.evaluationType)
        };
    }
    visitPath(context: PathContext): Node {
        const object = this.visit(context._object);

        let evaluationType: Type = object.evaluationType;
        if (evaluationType instanceof ArrayType) {
            evaluationType = evaluationType.elementType;
        }

        this.scopes.push(this.createScope("PATH", undefined, evaluationType));
        const prop = this.visit(context._property);
        this.scopes.pop();
        return {
            children: [object, prop],
            context,
            nodeType: "PATH",
            scope: this.scopes.peek(),
            evaluationType: object.evaluationType instanceof ArrayType && !(prop.evaluationType instanceof ArrayType)
                ? ArrayType.createFromType(prop.evaluationType)
                : prop.evaluationType
        };
    }

    visitFilter(context: FilterContext): Node {
        const collection = this.visit(context._filterCollection);
        const predicateContext = context.predicate();
        if (!predicateContext) {
            return {
                children: [collection],
                context,
                nodeType: "COLLECTION_FILTER",
                scope: this.scopes.peek(),
                evaluationType: collection.evaluationType
            };
        }
        let predicate;
        this.scopes.push(
            this.createScope(
                "FILTER",
                this.scopes.peek(),
                this.unwrapTypeForIteration(collection.evaluationType)
            )
        );
        const valuePredicate = predicateContext.valuePredicate();
        if (valuePredicate) {
            predicate = this.visit(valuePredicate);
        }
        const value = predicateContext.value();
        if (value) {
            predicate = this.visit(value);
        }
        if (!predicate) throw new Error("Predicate is not defined");
        this.scopes.pop();
        return {
            children: [collection, predicate],
            context,
            nodeType: "COLLECTION_FILTER",
            scope: this.scopes.peek(),
            evaluationType: collection.evaluationType
        };
    }
    visitPrecedence(context: PrecedenceContext): Node {
        return this.visit(context.value());
    }
    visitExponent(context: ExponentContext): Node {
        return this._visitTwoChildren(context, "EXPONENT", Type.NUMBER);
    }
    visitExponentValue(context: ExponentContext): Node {
        return this._visitTwoChildren(context, "EXPONENT", Type.NUMBER);
    }
    visitNegative(context: NegativeContext): Node {
        return {
            context,
            nodeType: "NEGATIVE",
            evaluationType: Type.NUMBER,
            scope: this.scopes.peek(),
            children: [this.visit(context.value())]
        };
    }
    visitIn(context: InContext): Node {
        return this._visitTwoChildren(context, "IN", Type.BOOLEAN);
    }
    visitInPredicate(context: InContext): Node {
        return this._visitTwoChildren(context, "IN", Type.BOOLEAN);
    }
    visitSubtractionOrAddition(context: SubtractionOrAdditionContext): Node {
        return this._visitTwoChildren(
            context,
            this.getSubtractionAdditionType(context),
            Type.NUMBER
        );
    }
    visitSubtractionOrAdditionValue(context: SubtractionOrAdditionValueContext): Node {
        return this.visitSubtractionOrAddition(context);
    }
    visitMultiplicationOrDivisionValue(context: MultiplicationOrDivisionValueContext): Node {
        return this._visitTwoChildren(
            context,
            this.getMultiplicationOrDivisionType(context),
            Type.NUMBER
        );
    }
    visitMultiplicationOrDivision(context: MultiplicationOrDivisionContext): Node {
        return this._visitTwoChildren(
            context,
            this.getMultiplicationOrDivisionType(context),
            Type.NUMBER
        );
    }
    visitInlineArray(context: InlineArrayContext): Node {
        function determineInlineArrayItemType(types: Type[]): Type {
            if (!types.length) {
                return Type.ANY;
            }
            let result = types[0];
            for (const type of types) {
                result = result.resolveCommonTypeOf(type) ?? Type.ANY;
            }
            return result;
        }
        const valueList = context.valueList();
        const items = valueList
            ? valueList.value().map(v => this.visit(v))
            : [];

        return {
            children: items,
            context,
            nodeType: "INLINE_ARRAY",
            evaluationType: ArrayType.createFromType(
                determineInlineArrayItemType(
                    items.map(item => item.evaluationType)
                )
            ),
            scope: this.scopes.peek()
        };
    }
    visitInlineMap(context: InlineMapContext): Node {
        const thiz = this;
        function buildMapType(): Type {
            const pairs = context.keyValuePairs().keyValuePair().map(kv => {
                const { text } = kv._key;
                return ({
                    key: text!,
                    value: thiz.visit(kv.value())
                });
            });
            return Type.create({
                name: `InlineMap_${counter++}`,
                extendedTypes: [],
                known: true,
                primitive: false,
                properties: SymbolTable.create({
                    functions: [],
                    references: pairs.reduce(Reducer.toObject(
                        p => p.key,
                        p => VariableSymbol.create({
                            name: p.key,
                            type: p.value.evaluationType
                        })
                    ), {})
                })
            });
        }
        return {
            children: context.keyValuePairs().keyValuePair().map(p => this.visit(p)),
            context,
            nodeType: "INLINE_MAP",
            scope: this.scopes.peek(),
            evaluationType: buildMapType()
        };
    }
    visitIfValue(context: IfValueContext): Node {
        const children: Node[] = [];
        const iff = this.visit(context._condition);
        children.push(iff);
        const then = this.visit(context._thenExpression);
        children.push(then);
        if (context._elseExpression) {
            const elze = this.visit(context._elseExpression);
            children.push(elze);
        }
        return {
            children,
            context,
            nodeType: "IF",
            evaluationType: then.evaluationType,
            scope: this.scopes.peek()
        };
    }
    visitThis(context: ThisContext): Node {
        const scope = this.scopes.peek();
        return {
            context,
            nodeType: "THIS",
            evaluationType: scope.type,
            scope,
            children: []
        };
    }
    visitThisValue(context: ThisValueContext): Node {
        return this.visitThis(context);
    }
    visitReferenceValue(context: ReferenceValueContext): Node {
        const children = [];
        const ref = this.visit(context.reference());
        children.push(ref);
        return {
            context,
            children,
            nodeType: "REFERENCE",
            scope: this.scopes.peek(),
            evaluationType: ref.evaluationType
        };
    }
    visitForEach(context: ForEachContext): Node {
        return this._visitIteration(
            context,
            "FOR",
            evaluationType => evaluationType instanceof ArrayType
                ? evaluationType
                : ArrayType.createFromType(evaluationType)
        );
    }
    visitForEvery(context: ForEveryContext): Node {
        return this._visitIteration(context, "EVERY");
    }
    visitForEveryPredicate(context: ForEveryPredicateContext): Node {
        return this._visitIteration(context, "EVERY");
    }
    visitForSome(context: ForSomeContext): Node {
        return this._visitIteration(context, "SOME");
    }
    visitForSomePredicate(context: ForSomePredicateContext): Node {
        return this._visitIteration(context, "SOME");
    }
    visitNegativeValue(context: NegativeValueContext): Node {
        const value = this.visit(context.value());
        return {
            children: [value],
            context,
            nodeType: "NEGATIVE",
            evaluationType: value.evaluationType,
            scope: this.scopes.peek()
        };
    }
    visitExpression(context: ExpressionContext): Node {
        const value = context.value();
        if (value) {
            return this.visit(value);
        }
        return {
            children: [],
            context,
            nodeType: "NULL",
            scope: this.scopes.peek(),
            evaluationType: Type.ANY
        };
    }
    visitDate(context: DateContext): Node {
        return { context, nodeType: "DATE", evaluationType: Type.DATE, scope: this.scopes.peek(), children: [] };
    }
    visitDateTime(context: DateTimeContext): Node {
        const nodeType = "DATETIME";
        return { context, nodeType, evaluationType: Type.DATETIME, scope: this.scopes.peek(), children: [] };
    }
    visitBoolean(context: BooleanContext): Node {
        return { context, nodeType: "BOOLEAN", evaluationType: Type.BOOLEAN, scope: this.scopes.peek(), children: [] };
    }
    visitDecimal(context: DecimalContext): Node {
        return { context, nodeType: "DECIMAL", evaluationType: Type.NUMBER, scope: this.scopes.peek(), children: [] };
    }
    visitString(context: StringContext): Node {
        return { context, nodeType: "STRING", evaluationType: Type.STRING, scope: this.scopes.peek(), children: [] };
    }
    visitNull(context: NullContext): Node {
        return { context, nodeType: "NULL", evaluationType: Type.ANY, scope: this.scopes.peek(), children: [] };
    }
    visitIdentifier(context: IdentifierContext): Node {
        const scope = this.scopes.peek();
        return {
            context,
            nodeType: "IDENTIFIER",
            evaluationType: scope.resolveReferenceSymbol(context.text)?.type ?? Type.UNKNOWN,
            scope,
            children: []
        };
    }
    visitFunctionCall(context: FunctionCallContext): Node {
        function resolveFunctionOrThrow(
            fxScope: Scope, functionName: string, argsCount: number
        ): FunctionSymbol | undefined {
            const fs = fxScope.resolveFunctionSymbol(functionName, argsCount);
            if (fs) {
                return fs;
            }
            return;
        }
        function calculateGenericEvaluationType(fxType: Type, genericType: Type): Type {
            if (fxType instanceof ArrayType) {
                return ArrayType.createFromType(
                    calculateGenericEvaluationType(
                        fxType.elementType, genericType
                    )
                );
            }
            if (fxType instanceof GenericType) {
                return genericType;
            }
            return fxType;
        }
        const scope = this.scopes.peek();
        const rawArguments = context._arguments?.value() ?? [];
        const functionSymbol = resolveFunctionOrThrow(
            scope,
            context._functionName.text,
            rawArguments.length ?? 0
        );
        if (!functionSymbol) {
            return {
                context,
                nodeType: "FUNCTION",
                scope,
                evaluationType: Type.UNKNOWN,
                children: rawArguments.map(this.visit.bind(this))
            };
        }
        let evaluationType = functionSymbol.type;
        const scalarType = this.unwrapScalarType(functionSymbol.type);
        const params = rawArguments.map(this.visit.bind(this));
        if (scalarType instanceof GenericType) {
            const fxParam = functionSymbol.findGenericParameter(scalarType);
            const type = params[fxParam.parameterIndex].evaluationType;
            evaluationType = calculateGenericEvaluationType(
                functionSymbol.type,
                this.unwrapScalarType(type)
            );
        }

        return { context, nodeType: "FUNCTION", scope, evaluationType, children: params };
    }

    /**
     * @private
     * @template C
     * @param {C} context
     * @param {NodeType} nodeType
     * @param {(returnType: Type) => Type} [resolveEvaluationType]  default is return evaluation type
     * @returns {Node}
     * @memberof KelTraversingVisitor
     */
    private _visitIteration<C extends {
        _var: IdentifierContext;
        _collection: ValueContext;
        _returnExpression: ValueContext;
    } & ParserRuleContext>(
        context: C,
        nodeType: NodeType,
        resolveEvaluationType: (returnType: Type) => Type = (returnType: Type) => returnType
    ): Node {
        // expression: 'for '
        // expression: 'for r in '
        // expression: 'for r in riskItems'
        if (!context._collection) {
            return {
                children: [],
                context,
                evaluationType: resolveEvaluationType(Type.UNKNOWN),
                nodeType,
                scope: this.scopes.peek()
            };
        }

        const collection = this.visit(context._collection);

        // expression: 'for r in riskItems return'
        if (!context._returnExpression) {
            return {
                children: [collection],
                context,
                evaluationType: resolveEvaluationType(Type.UNKNOWN),
                nodeType,
                scope: this.scopes.peek()
            };
        }

        const variable = context._var.text;
        const forScopeType = this.buildTypeForIterationContext(collection.evaluationType, variable);
        this.scopes.push(
            this.createScope(
                "FOR RETURN EXPRESSION",
                this.scopes.peek(),
                forScopeType
            )
        );
        const returnExpression = this.visit(context._returnExpression);
        this.scopes.pop();

        return {
            context,
            nodeType,
            evaluationType: resolveEvaluationType(returnExpression.evaluationType),
            scope: this.scopes.peek(),
            children: [collection, returnExpression]
        };
    }

    private _visitTwoChildren<
        CONTEXT extends {
            value(): ValueContext[];
            value(i: number): ValueContext;
        } & ValueContext
        >(context: CONTEXT, nodeType: NodeType, evaluationType: Type): Node {
        return {
            children: [
                this.visit(context.value(0)),
                this.visit(context.value(1))
            ],
            context,
            nodeType,
            evaluationType,
            scope: this.scopes.peek()
        };
    }

    private getNumericalComparisonType(context: NumericalComparisonContext): NodeType {
        let nodeType: NodeType;
        if (context.OP_LESS()) {
            nodeType = "LESS_THAN";
        } else if (context.OP_LESS_EQUALS()) {
            nodeType = "LESS_THAN_OR_EQUALS";
        } else if (context.OP_MORE()) {
            nodeType = "MORE_THAN";
        } else if (context.OP_MORE_EQUALS()) {
            nodeType = "MORE_THAN_OR_EQUALS";
        } else {
            throw new Error("Failed to determine node type: in context" + JSON.stringify(context));
        }
        return nodeType;
    }
    private getMultiplicationOrDivisionType(context: MultiplicationOrDivisionContext): NodeType {
        let nodeType: NodeType;
        if (context.OP_MULT()) {
            nodeType = "MULTIPLICATION";
        } else if (context.OP_DIV()) {
            nodeType = "DIVISION";
        } else if (context.OP_MOD()) {
            nodeType = "MODULUS";
        } else {
            throw new Error("Failed to determine node type: in context" + JSON.stringify(context));
        }
        return nodeType;
    }
    private getTypeComparisonType(context: TypeComparisonContext): NodeType {
        let nodeType: NodeType;
        if (context.OP_INSTANCEOF()) {
            nodeType = "INSTANCEOF";
        } else if (context.OP_TYPEOF()) {
            nodeType = "TYPEOF";
        } else {
            throw new Error("Failed to determine node type: in context" + JSON.stringify(context));
        }
        return nodeType;
    }
    private getSubtractionAdditionType(context: SubtractionOrAdditionContext): NodeType {
        let nodeType: NodeType;
        if (context.OP_MINUS()) {
            nodeType = "SUBTRACTION";
        } else if (context.OP_ADD()) {
            nodeType = "ADDITION";
        } else {
            throw new Error("Failed to determine node type: in context" + JSON.stringify(context));
        }
        return nodeType;
    }
    private getEqComparisonType(context: EqualityComparisonPredicateContext): NodeType {
        return context.OP_EQUALS() ? "EQUALS" : "NOT_EQUALS";
    }
    private createScope(scopeType: ScopeType, parentScope: Scope | undefined, evaluationType: Type): Scope {
        const scope = new Scope({ __type: "kraken.el.scope.Scope" }, {});
        scope.scopeType = scopeType;
        scope.parentScope = parentScope;
        scope.type = evaluationType;
        scope.name = `${parentScope ? parentScope.name + "->" : ""}${evaluationType.name}`;
        return scope;
    }
    private unwrapScalarType(type: Type): Type {
        if (type instanceof ArrayType) {
            return this.unwrapScalarType(type.elementType);
        }
        return type;
    }
    private unwrapArrayType(type: Type): Type {
        if (type instanceof ArrayType) {
            return type.elementType;
        }
        if (type.equals(Type.ANY)) {
            return Type.ANY;
        }
        return Type.UNKNOWN;
    }
    private unwrapTypeForIteration(type: Type): Type {
        return this.unwrapArrayType(type);
    }
    private buildTypeForIterationContext(collectionType: Type, variable: string): Type {
        return Type.create({
            name: `for_${variable}_${counter++}`,
            known: true,
            primitive: false,
            extendedTypes: [],
            properties: SymbolTable.create({
                functions: [],
                references: {
                    [variable]: VariableSymbol.create({
                        name: variable,
                        type: this.unwrapTypeForIteration(collectionType)
                    })
                }
            })
        });
    }
}
