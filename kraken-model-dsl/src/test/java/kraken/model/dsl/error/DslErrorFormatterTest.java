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

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class DslErrorFormatterTest {

    @Test
    public void shouldFormat() {
        var line = 3;
        var startColumn = 6;
        var endColumn = 11;
        var dsl = "" +
                "one\n" +
                "two\n" +
                "     error three\n" +
                "four\n" +
                "five\n";
        String message = DslErrorFormatter.format(dsl, line, startColumn, endColumn);
        Assert.assertThat(message, CoreMatchers.is("" +
                "2 two\n" +
                "3      error three\n" +
                "       ^^^^^\n" +
                "4 four"
        ));
    }

    @Test
    public void shouldFormat_errorOnFirstLine() {
        var line = 1;
        var startColumn = 1;
        var endColumn = 6;
        var dsl = "" +
                "errorone\n" +
                "two\n" +
                "     error three\n" +
                "four\n" +
                "five\n";
        String message = DslErrorFormatter.format(dsl, line, startColumn, endColumn);
        Assert.assertThat(message, CoreMatchers.is("" +
                "1 errorone\n" +
                "  ^^^^^\n" +
                "2 two"
        ));
    }

    @Test
    public void shouldFormat_errorOnLastLine() {
        var line = 3;
        var startColumn = 7;
        var endColumn = 12;
        var dsl = "" +
                "one\n" +
                "two\n" +
                "three error\n";
        String message = DslErrorFormatter.format(dsl, line, startColumn, endColumn);
        Assert.assertThat(message, CoreMatchers.is("" +
                "2 two\n" +
                "3 three error\n" +
                "        ^^^^^"
        ));
    }

    @Test
    public void shouldFormat_different_lineNumberLength() {
        var line = 10;
        var startColumn = 5;
        var endColumn = 10;
        var dsl = "" +
                "1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "5\n" +
                "6\n" +
                "7\n" +
                "8\n" +
                "nine\n" +
                "ten error ten\n" +
                "eleven\n";
        String message = DslErrorFormatter.format(dsl, line, startColumn, endColumn);
        Assert.assertThat(message, CoreMatchers.is("" +
                " 9 nine\n" +
                "10 ten error ten\n" +
                "       ^^^^^\n" +
                "11 eleven"
        ));
    }

}