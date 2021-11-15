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

import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * @author Pavel Surinin
 */
public class LineParseCancellationException extends ParseCancellationException {

    private final int line;
    private final int column;

    public LineParseCancellationException(String message, int line, int column) {
        super(message);
        this.line = line;
        this.column = column;
    }

    public LineParseCancellationException(String message, int line, int column, Throwable t) {
        super(message, t);
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
