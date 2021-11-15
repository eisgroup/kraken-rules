/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

package kraken.el.serialization;

import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;

import kraken.el.scope.Scope;
import kraken.el.scope.type.TypeRef;

public class ScopeSerialization {

    private static final Genson typeRegistrySerializer = new GensonBuilder()
        .useClassMetadata(true)
        .useRuntimeType(true)
        .useIndentation(true)
        .withConverter(TypeProxyConverter.instance, TypeRef.class)
        .create();

    private static final Genson typeSerializer = new GensonBuilder()
        .useClassMetadata(true)
        .useIndentation(true)
        .useRuntimeType(true)
        .exclude("allTypes", Scope.class)
        .withConverter(TypeProxyConverter.instance, TypeRef.class)
//        .withConverter(TypeProxyConverter.instance, Type.class)
        .create();

    public static String serializeTypeRegistry(TypeRegistry typeRegistry) {
        return typeRegistrySerializer.serialize(
            typeRegistry
        );
    }

    public static String serializeScope(Scope scope) {
        return typeSerializer.serialize(new Scope(
            scope.getScopeType(),
            scope.getParentScope(),
            new RootType(scope.getType())
        ));
    }
}
