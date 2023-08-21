/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.utils;

import java.util.stream.Collectors;

/**
 * @author Mindaugas Ulevicius
 */
public class MessageUtils {

    /**
     * Aligns string message by prepending two spaces before each new line.
     * Should mostly bew used for formatting validation and error messages for display.
     *
     * @param message to align. Can have any new line symbols.
     * @return aligned message with spaces prepended
     */
    public static String withSpaceBeforeEachLine(String message) {
        return message.lines()
            .map(line -> "  " + line)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}
