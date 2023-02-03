/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.el.ast.dependency;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class ReferenceResolvingVisitorTest {

    @Test
    public void shouldResolveReferenceFromProperty() {
        Type policyType = type("PolicyType", Map.of("policyCd", Type.STRING));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("policyCd", scope);
        assertThat(dependencies, hasSize(1));
        assertThat(dependencies.get(0), equalTo(ref("PolicyType","policyCd", false)));
    }

    @Test
    public void shouldResolveGlobalReferenceFromProperty() {
        Scope scope = scope("Policy", type("PolicyType", Map.of()));

        var dependencies = resolve("Policy", scope);
        assertThat(dependencies, hasSize(1));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
    }

    @Test
    public void shouldNotResolveReferenceOfUnknownProperty() {
        Type policyType = type("PolicyType", Map.of("policyCd", Type.STRING));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("Policy.unknownProperty", scope);
        assertThat(dependencies, hasSize(1));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
    }

    @Test
    public void shouldResolveReferencesFromPath() {
        Type policyType = type("PolicyType", Map.of("vehicle",
            type("VehicleType", Map.of("make", Type.STRING))));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("Policy.vehicle.make", scope);
        assertThat(dependencies, hasSize(3));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
        assertThat(dependencies.get(1), equalTo(ref("PolicyType","vehicle", false)));
        assertThat(dependencies.get(2), equalTo(ref("VehicleType","make", false)));
    }

    @Test
    public void shouldResolveReferencesFromVariable() {
        Type policyType = type("PolicyType", Map.of("vehicle",
            type("VehicleType", Map.of("make", Type.STRING))));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("set p to Policy set v to p.vehicle set m to v.make return m", scope);
        assertThat(dependencies, hasSize(3));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
        assertThat(dependencies.get(1), equalTo(ref("PolicyType","vehicle", false)));
        assertThat(dependencies.get(2), equalTo(ref("VehicleType","make", false)));
    }

    @Test
    public void shouldResolveReferencesFromIteration() {
        Type policyType = type("PolicyType", Map.of("vehicles",
            ArrayType.of(type("VehicleType", Map.of("make", Type.STRING)))));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("some v in Policy.vehicles satisfies v.make", scope);
        assertThat(dependencies, hasSize(3));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
        assertThat(dependencies.get(1), equalTo(ref("PolicyType","vehicles", false)));
        assertThat(dependencies.get(2), equalTo(ref("VehicleType","make", false)));
    }

    @Test
    public void shouldResolveReferencesFromFilter() {
        Type policyType = type("PolicyType", Map.of("vehicles",
            ArrayType.of(type("VehicleType", Map.of("make", Type.STRING)))));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("Policy.vehicles[make == 'Audi']", scope);
        assertThat(dependencies, hasSize(3));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
        assertThat(dependencies.get(1), equalTo(ref("PolicyType","vehicles", false)));
        assertThat(dependencies.get(2), equalTo(ref("VehicleType","make", false)));
    }

    @Test
    public void shouldResolveReferencesUniquely() {
        Type policyType = type("PolicyType", Map.of("vehicles",
            ArrayType.of(type("VehicleType", Map.of("make", Type.STRING)))));
        Scope scope = scope("Policy", policyType);

        var dependencies = resolve("Policy.vehicles[0].make == Policy.vehicles[1].make", scope);
        assertThat(dependencies, hasSize(3));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
        assertThat(dependencies.get(1), equalTo(ref("PolicyType","vehicles", false)));
        assertThat(dependencies.get(2), equalTo(ref("VehicleType","make", false)));
    }

    @Test
    public void shouldResolveReferencesFromFunctionReturnType() {
        Type policyType = type("PolicyType", Map.of("policyCd", Type.STRING));
        FunctionSymbol function = new FunctionSymbol(
            "GetVehicle",
            type("VehicleType", Map.of("make", Type.STRING)),
            List.of(new FunctionParameter(0, policyType))
        );
        Scope scope = scope("Policy", policyType, List.of(function));

        var dependencies = resolve("GetVehicle(Policy).make", scope);
        assertThat(dependencies, hasSize(2));
        assertThat(dependencies.get(0), equalTo(ref(null,"Policy", true)));
        assertThat(dependencies.get(1), equalTo(ref("VehicleType","make", false)));
    }

    @Test
    public void shouldNotResolveReferenceWhenScopeIsDynamic() {
        Scope scope = Scope.dynamic();
        var dependencies = resolve("property", scope);
        assertThat(dependencies, empty());
    }

    private Scope scope(String variableName, Type type) {
       return scope(variableName, type, List.of());
    }

    private Scope scope(String variableName, Type type, Collection<FunctionSymbol> functions) {
        Map<String, Type> allTypes = new HashMap<>();
        collectAllTypes(type, allTypes);
        functions.forEach(f -> collectAllTypes(f.getType(), allTypes));
        VariableSymbol variable = new VariableSymbol(variableName, type);
        return new Scope(
            ScopeType.LOCAL,
            new Scope(
                new Type("MAP", new SymbolTable(functions, Map.of(variable.getName(), variable))),
                allTypes
            ),
            type
        );
    }

    /**
     * Collects all types defined within type structure and adds to allTypes map.
     * This allows to define test in a cleaner manner without having to build allTypes manually each time.
     *
     * @param type that will be traversed and all defined types added to allTypes map
     * @param allTypes collected types will be added to this map
     */
    private void collectAllTypes(Type type, Map<String, Type> allTypes) {
        if(type.isDynamic()
            || type.isPrimitive()
            || !type.isKnown()
            || allTypes.containsKey(type.getName())) {
            return;
        }
        if(type instanceof ArrayType) {
            type = ((ArrayType) type).getElementType();
        }
        allTypes.put(type.getName(), type);
        type.getExtendedTypes().forEach(t -> collectAllTypes(t, allTypes));
        type.getProperties().getReferences().values().stream()
            .map(VariableSymbol::getType)
            .forEach(t -> collectAllTypes(t, allTypes));
    }

    private Type type(String typeName, Map<String, Type> attributes) {
        var variables = attributes.entrySet().stream()
            .map(e -> new VariableSymbol(e.getKey(), e.getValue()))
            .collect(Collectors.toMap(VariableSymbol::getName, v -> v));
       return new Type(typeName, new SymbolTable(List.of(), variables));
    }

    private Reference ref(String typeName, String referenceName, boolean global) {
        return new Reference(typeName, referenceName, global);
    }

    private List<Reference> resolve(String expression, Scope scope) {
        ReferenceResolvingVisitor visitor = new ReferenceResolvingVisitor(scope);
        Ast ast = AstBuilder.from(expression, scope);
        visitor.visit(ast.getExpression());
        return new ArrayList<>(visitor.getReferences());
    }

}
