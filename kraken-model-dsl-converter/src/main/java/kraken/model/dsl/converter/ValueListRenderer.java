/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.dsl.converter;

import java.util.Locale;
import java.util.stream.Collectors;

import org.stringtemplate.v4.AttributeRenderer;

import kraken.el.ast.builder.Literals;
import kraken.el.math.Numbers;
import kraken.model.ValueList;

/**
 * A custom renderer that renders values stored in {@link ValueList} as a
 * String.
 *
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
public final class ValueListRenderer implements AttributeRenderer {

    @Override
    public String toString(Object valueListObject, String formatString, Locale locale) {
        ValueList valueList = (ValueList)  valueListObject;

        switch (valueList.getValueType()) {
            case STRING:
                return valueList.getValues()
                    .stream()
                    .map(String.class::cast)
                    .map(value -> "\"" + Literals.escape(value) + "\"")
                    .collect(Collectors.joining(", "));

            case DECIMAL:
                return valueList.getValues()
                    .stream()
                    .map(Number.class::cast)
                    .map(n -> Numbers.toString(n))
                    .collect(Collectors.joining(", "));

            default:
                throw new IllegalArgumentException("Unknown value list data type.");
        }
    }

}
