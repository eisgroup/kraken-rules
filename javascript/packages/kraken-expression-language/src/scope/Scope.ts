import { createParser } from "kraken-expression-language-visitor";
import { ClassName } from "../factory/ClassName";
import { ClassNameMarker } from "../factory/ClassNameMarker";
import { FactoryInstance } from "../factory/FactoryInstance";
import { Json } from "../factory/Json";
import { FunctionSymbol } from "../symbol/FunctionSymbol";
import { VariableSymbol } from "../symbol/VariableSymbol";
import { Type } from "../type/Type";
import { TypeRegistry } from "../TypeRegistry";
import { TypeGeneratingVisitor } from "../visitor/TypeGeneratingVisitor";
import { ScopeType } from "./ScopeType";

export interface ScopeData extends Json {
    name: string;
    scopeType: ScopeType;
    type: Type;
    allTypes: TypeRegistry;
    parentScope?: Scope;
}

export class Scope extends FactoryInstance {

    constructor(json: Json & ClassNameMarker, typeRegistry: TypeRegistry) {
        super(json, typeRegistry);
        this.allTypes = typeRegistry;
    }

    name!: string;
    scopeType!: ScopeType;
    type!: Type;
    allTypes!: TypeRegistry;
    parentScope?: Scope;
    // tslint:disable-next-line: variable-name
    __type!: ClassName;

    /**
     * If symbol exists in current immediate scope a symbol is in current
     * immediate scope if the scope is static and has symbol by name
     * or a scope is dynamic and no static parent scope has this symbol
     */
    isReferenceInCurrentScope(symbolName: string): boolean {
        return symbolName in this.type.properties.references
            || this.type.equals(Type.ANY)
            && !(this.parentScope?.isReferenceStrictlyInScope(symbolName) ?? false);
    }

    /** resolves actual function symbol accessible from within this scope */
    resolveFunctionSymbol(name: string, paramCount: number): FunctionSymbol | undefined {
        if (this.type.equals(Type.ANY) && this.scopeType === "GLOBAL") {
            throw new Error("Global scope with type any is not supported");
        }

        const functionSymbol = this
            .type.properties.functions
            .find(fx => fx.name === name && fx.parameters.length === paramCount);
        if (functionSymbol) {
            return functionSymbol;
        }
        if (this.parentScope) {
            return this.parentScope.resolveFunctionSymbol(name, paramCount);
        }
        return undefined;
    }

    /** resolves actual reference symbol accessible from within this scope */
    resolveReferenceSymbol(name: string): VariableSymbol | undefined {
        if (this.type.equals(Type.ANY) && this.isReferenceInCurrentScope(name)) {
            return VariableSymbol.create({ name, type: Type.ANY });
        }
        const local = this.type.properties.references[name];
        if (local) {
            return local;
        }
        if (this.parentScope) {
            return this.parentScope.resolveReferenceSymbol(name);
        }
        return;
    }

    resolveTypeOf(typeToken: string): Type {
        if (this.type.equals(Type.ANY)) {
            return Type.ANY;
        }
        if (this.parentScope) {
            return this.parentScope.resolveTypeOf(typeToken);
        }
        const typeContext = createParser(typeToken).type();
        const visitor = new TypeGeneratingVisitor(this.allTypes);
        return visitor.visit(typeContext);
    }

    /**
     * if symbol exists in strict scope including parents;
     * if scope is dynamic then reference is not strictly in scope;
     */
    private isReferenceStrictlyInScope(name: string): boolean {
        return Boolean(this.type.properties.references[name])
            || (this.parentScope?.isReferenceStrictlyInScope(name) ?? false);
    }
}
