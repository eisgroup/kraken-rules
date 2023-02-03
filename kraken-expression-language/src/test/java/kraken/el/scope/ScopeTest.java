/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el.scope;

import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.el.scope.type.TypeRef;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mulevicius
 */
public class ScopeTest {

    @Test
    public void shouldBuildDynamicGlobalScope() {
        Scope globalScope = new Scope(Type.ANY, Map.of());

        assertThat(globalScope.isReferenceInCurrentScope("anyReference"), is(true));
        assertThat(globalScope.isReferenceInGlobalScope("anyReference"), is(true));
        assertThat(globalScope.resolveReferenceSymbol("anyReference").get().getName(), is("anyReference"));
        assertThat(globalScope.resolveFunctionSymbol("anyFunction", 1).get().getName(), is("anyFunction"));
        assertThat(globalScope.findScopeTypeOfReference("anyReference"), is(ScopeType.GLOBAL));
    }

    @Test
    public void shouldBuildStaticGlobalScopeWithVariable() {
        VariableSymbol policyCd = new VariableSymbol("policyCd", Type.STRING);
        Type policyType = new Type("PolicyType", new SymbolTable(List.of(), Map.of(policyCd.getName(), policyCd)));
        VariableSymbol policy = new VariableSymbol("Policy", policyType);
        Scope globalScope = new Scope(
                new Type("MAP", new SymbolTable(List.of(), Map.of(policy.getName(), policy))),
                Map.of(policyType.getName(), policyType)
        );

        assertThat(globalScope.isReferenceInCurrentScope("Policy"), is(true));
        assertThat(globalScope.isReferenceInGlobalScope("Policy"), is(true));
        assertThat(globalScope.resolveReferenceSymbol("Policy").get().getName(), is("Policy"));
        assertThat(globalScope.findScopeTypeOfReference("Policy"), is(ScopeType.GLOBAL));

        assertThat(globalScope.resolveTypeOf("PolicyType"), is(policyType));

        assertThat(globalScope.isReferenceInCurrentScope("anyReference"), is(false));
        assertThat(globalScope.isReferenceInGlobalScope("anyReference"), is(false));
        assertThat(globalScope.resolveReferenceSymbol("anyReference").isPresent(), is(false));
        assertThat(globalScope.resolveFunctionSymbol("anyFunction", 1).isPresent(), is(false));
        assertThat(globalScope.findScopeTypeOfReference("anyReference"), nullValue());
    }

    @Test
    public void shouldBuildStaticLocalScopeWithStaticGlobalScope() {
        VariableSymbol policyCd = new VariableSymbol("policyCd", Type.STRING);
        Type policyType = new Type("PolicyType", new SymbolTable(List.of(), Map.of(policyCd.getName(), policyCd)));
        VariableSymbol policy = new VariableSymbol("Policy", policyType);
        Scope globalScope = new Scope(
            new Type("MAP", new SymbolTable(List.of(), Map.of(policy.getName(), policy))),
            Map.of(policyType.getName(), policyType)
        );

        Scope localScope = new Scope(
            ScopeType.LOCAL,
            globalScope,
            new Type("MAP", new SymbolTable(List.of(), Map.of(policyCd.getName(), policyCd)))
        );

        assertThat(localScope.isReferenceInCurrentScope("policyCd"), is(true));
        assertThat(localScope.isReferenceInGlobalScope("policyCd"), is(false));
        assertThat(localScope.resolveReferenceSymbol("policyCd").get().getName(), is("policyCd"));
        assertThat(localScope.findScopeTypeOfReference("policyCd"), is(ScopeType.LOCAL));

        assertThat(localScope.isReferenceInCurrentScope("Policy"), is(false));
        assertThat(localScope.isReferenceInGlobalScope("Policy"), is(true));
        assertThat(localScope.resolveReferenceSymbol("Policy").get().getName(), is("Policy"));
        assertThat(localScope.findScopeTypeOfReference("Policy"), is(ScopeType.GLOBAL));

        assertThat(localScope.resolveTypeOf("PolicyType"), is(policyType));
        assertThat(localScope.resolveTypeOf("PolicyType[]"), is(ArrayType.of(policyType)));
        assertThat(localScope.resolveTypeOf("Any"), is(Type.ANY));
    }

    @Test
    public void shouldExpandTypeRefInScope() {
        Map<String, Type> allTypes = new HashMap<>();

        VariableSymbol coverage = new VariableSymbol("coverage", new TypeRef("CoverageType", type -> allTypes.get(type)));
        Type policyType = new Type("PolicyType", new SymbolTable(List.of(), Map.of(coverage.getName(), coverage)));
        VariableSymbol policy = new VariableSymbol("Policy", policyType);

        VariableSymbol coverageCd = new VariableSymbol("coverageCd", Type.STRING);
        Type coverageType = new Type("CoverageType", new SymbolTable(List.of(), Map.of(coverageCd.getName(), coverageCd)));

        allTypes.put(policyType.getName(), policyType);
        allTypes.put(coverageType.getName(), coverageType);

        Scope globalScope = new Scope(
                new Type("MAP", new SymbolTable(List.of(), Map.of(policy.getName(), policy))),
                allTypes
        );

        assertThat(globalScope.resolveTypeOf("CoverageType"), is(coverageType));
    }

    @Test
    public void shouldResolveTypeFromDynamicScope() {
        Type policyType = new Type("PolicyType", new SymbolTable(List.of(), Map.of()));
        Scope globalScope = new Scope(Type.ANY, Map.of(policyType.getName(), policyType));

        assertThat(globalScope.resolveTypeOf("PolicyType").getName(), is("PolicyType"));
    }

}
