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
package kraken.el.ast.token;

/**
 * Represents part of expression text
 *
 * @author mulevicius
 */
public class Token {

    private int start;

    private int end;

    private String text;

    public Token(int start, int end, String text) {
        this.start = start;
        this.end = end;
        this.text = text;
    }

    /**
     *
     * @return expression text that this token represents
     */
    public String getText() {
        return text;
    }

    /**
     * @return char index in expression that this token begins at
     */
    public int getStart() {
        return start;
    }

    /**
     * @return char index in expression that this token ends at
     */
    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return text;
    }
}
