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
package kraken.model.dsl.error;

import java.util.ArrayList;

import static java.lang.String.join;

/**
 * @since 1.8.0
 * @author Pavel Surinin
 */
public class DslErrorFormatter {

    public static String format(String dsl, int line, int startColumn, int endColumn) {
        int lineNumberLength = String.valueOf(line).length();
        var template = "%" + lineNumberLength + "s %s";

        String[] dslLines = dsl.split("\\r?\\n");

        var lines = new ArrayList<String>();

        if (line != 1) {
            lines.add(String.format(template, line - 1, dslLines[line - 2]));
        }
        lines.add(String.format(template, line, dslLines[line - 1]));
        lines.add(lineWithHighlight(lineNumberLength, startColumn, endColumn));

        if (line < dslLines.length) {
            lines.add(String.format(template, line + 1, dslLines[line]));
        }
        return join("\n", lines);
    }

    private static String lineWithHighlight(int lineNumberLength, int startColumn, int endColumn) {
        String prefix = " ".repeat(lineNumberLength);
        String underline = "^".repeat(endColumn - startColumn);
        String start = " ".repeat(startColumn);
        return join("", prefix, start, underline);
    }
}
