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
package kraken.el.ast.builder;

import org.antlr.v4.runtime.CharStream;

import kraken.el.Common;

/**
 * @author mulevicius
 */
public class CommonLexer extends Common {

    public CommonLexer(CharStream input) {
        super(input);
    }

    /**
     *
     * It overrides popMode() of Abstract Lexer to gracefully handle
     * syntactically incorrect expressions when expression has more closing curly braces than open curly braces.
     * Without this change, ANTLR4 get's stuck in infinte loop and crashes with StackOverflowException when parsing expression.
     *
     * @return
     */
    @Override
    public int popMode() {
        return _modeStack.isEmpty()
            ? DEFAULT_MODE
            : super.popMode();
    }
}
