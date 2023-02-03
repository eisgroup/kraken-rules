import { ClassName } from '../factory/ClassName'
import { ClassNameMarker } from '../factory/ClassNameMarker'
import { FactoryInstance } from '../factory/FactoryInstance'
import { Json } from '../factory/Json'
import { FunctionSymbol } from '../symbol/FunctionSymbol'
import { VariableSymbol } from '../symbol/VariableSymbol'
import { TypeRegistry } from '../TypeRegistry'
import { TypeGeneratingVisitor } from '../visitor/TypeGeneratingVisitor'
import { ScopeType } from './ScopeType'
import { KelParser } from 'kraken-expression-language-visitor'
import { Type } from '../type/Types'

export class Scope extends FactoryInstance {
    /**
     * Creates Scope object of a certain type.
     *
     * @param scopeType
     * @param parentScope
     * @param evaluationType
     */
    static createScope(scopeType: ScopeType, parentScope: Scope | undefined, evaluationType: Type): Scope {
        const scope = new Scope({ __type: 'kraken.el.scope.Scope' }, {})
        scope.scopeType = scopeType
        scope.parentScope = parentScope
        scope.type = evaluationType
        scope.name = `${parentScope ? parentScope.name + '->' : ''}${evaluationType.name}`
        return scope
    }

    /**
     * Creates empty Scope object which has no functions and no properties
     */
    static createEmptyScope(): Scope {
        return this.createScope('PATH', undefined, Type.ANY)
    }

    constructor(json: Json & ClassNameMarker, typeRegistry: TypeRegistry) {
        super(json, typeRegistry)
        this.allTypes = typeRegistry
    }

    name!: string
    scopeType!: ScopeType
    type!: Type
    allTypes!: TypeRegistry
    parentScope?: Scope

    __type!: ClassName

    /**
     * If symbol exists in current immediate scope.
     * A symbol is in current immediate scope if the scope is static and has symbol by name
     * or a scope is dynamic and symbol is not in a static parent scope of type GLOBAL, LOCAL, PATH or FOR
     */
    isReferenceInCurrentScope(name: string): boolean {
        if (this.isReferenceStrictlyInImmediateScope(name)) {
            return true
        }
        if (this.isDynamic()) {
            const t = this.parentScope?.findScopeTypeOfStrictReference(name)
            if (t) {
                /*
                   Symbol is in current dynamically typed scope only if there are NO ascending static scope of type
                   GLOBAL, LOCAL, PATH or VARIABLES that has that symbol.
                   The reason for this, is because a static scope of type
                   GLOBAL (for CCR) or LOCAL (attributes of target ContextDefinition) or VARIABLES
                   must override dynamically typed filter scope for symbol resolution.
                   Statically typed FILTER does not override symbol resolution because in nested filters
                   the symbol is resolved from the closest filter regardless if filter element is dynamic or static.
                 */
                return t !== 'GLOBAL' && t !== 'LOCAL' && t !== 'PATH' && t !== 'VARIABLES_MAP'
            }
            return true
        }
        return false
    }

    /**
     * Returns a type of scope that strictly contains symbol. This does not guarantee that symbol will be
     * resolved from that scope, because in some cases a closer dynamically typed scope could have the symbol.
     */
    private findScopeTypeOfStrictReference(name: string): ScopeType | undefined {
        if (this.isReferenceStrictlyInImmediateScope(name)) {
            return this.scopeType
        }
        return this.parentScope?.findScopeTypeOfStrictReference(name)
    }

    /** resolves actual function symbol accessible from within this scope */
    resolveFunctionSymbol(name: string, paramCount: number): FunctionSymbol | undefined {
        if (this.isDynamic() && this.scopeType === 'GLOBAL') {
            throw new Error('Global scope with type any is not supported')
        }

        const functionSymbol = this.type.properties.functions.find(
            fx => fx.name === name && fx.parameters.length === paramCount,
        )
        if (functionSymbol) {
            return functionSymbol
        }
        if (this.parentScope) {
            return this.parentScope.resolveFunctionSymbol(name, paramCount)
        }
        return undefined
    }

    /** resolves actual reference symbol accessible from within this scope */
    resolveReferenceSymbol(name: string): VariableSymbol | undefined {
        if (this.isDynamic() && this.isReferenceInCurrentScope(name)) {
            return VariableSymbol.create({ name, type: Type.ANY })
        }
        if (this.isReferenceStrictlyInImmediateScope(name)) {
            return this.type.properties.references[name]
        }
        if (this.parentScope) {
            return this.parentScope.resolveReferenceSymbol(name)
        }
        return
    }

    resolveTypeOf(typeToken: string): Type {
        if (this.parentScope) {
            return this.parentScope.resolveTypeOf(typeToken)
        }
        const typeContext = new KelParser(typeToken).parseType()
        const visitor = new TypeGeneratingVisitor(this.allTypes, {})
        return visitor.visit(typeContext)
    }

    /**
     * if symbol exists in strict scope including parents;
     * if scope is dynamic then reference is not strictly in scope;
     */
    isReferenceStrictlyInScope(name: string): boolean {
        return (
            this.isReferenceStrictlyInImmediateScope(name) ||
            (this.parentScope?.isReferenceStrictlyInScope(name) ?? false)
        )
    }

    /**
     * @return true if this scope is dynamically typed
     */
    isDynamic(): boolean {
        return this.type.isDynamic()
    }

    /**
     * @param name of symbol
     * @return true if symbol exists in strict immediate scope
     */
    isReferenceStrictlyInImmediateScope(name: string): boolean {
        return Boolean(this.type.properties.references[name])
    }

    /**
     * @return closest scope that can be referenced by 'this'.
     *         Only elements from FILTER and LOCAL objects can be referenced by 'this'.
     */
    resolveClosestReferencableScope(): Scope {
        if (this.scopeType === 'FILTER' || this.scopeType === 'LOCAL') {
            return this
        }
        if (this.parentScope) {
            return this.parentScope.resolveClosestReferencableScope()
        }
        return this
    }
}
