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

import com.owlike.genson.Context;
import com.owlike.genson.Converter;
import com.owlike.genson.stream.ObjectReader;
import com.owlike.genson.stream.ObjectWriter;

import kraken.el.scope.type.Type;

public class TypeProxyConverter implements Converter<Type> {

    public static TypeProxyConverter instance = new TypeProxyConverter();

    @Override
    public void serialize(Type object, ObjectWriter writer, Context ctx) {
        writer.beginObject();
        writer.writeBoolean("__proxy", true);
        writer.writeString("name", object.getName());
        writer.endObject();
    }

    @Override
    public Type deserialize(ObjectReader reader, Context ctx) {
        return null;
    }
}