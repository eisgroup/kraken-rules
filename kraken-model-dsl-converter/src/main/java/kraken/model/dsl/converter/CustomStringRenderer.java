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
package kraken.model.dsl.converter;

import kraken.el.ast.builder.Literals;
import kraken.model.context.PrimitiveFieldDataType;
import org.stringtemplate.v4.StringRenderer;

import java.util.Locale;

/**
 * Custom string renderer used to format {@link String} with custom formats.
 * Format <code>deescapeString</code> will revert model string escapes when writing string literals from model to DSL
 *
 * @author avasiliauskas
 * @since 1.0.28
 */
class CustomStringRenderer extends StringRenderer {

    @Override
    public String toString(Object o, String formatString, Locale locale) {
        if("deescapeString".equals(formatString)) {
            return Literals.deescape((String)o);
        }
        return super.toString(o, formatString, locale);
    }
}
