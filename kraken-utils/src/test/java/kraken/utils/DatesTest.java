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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

/**
 * @author Mindaugas Ulevicius
 */
public class DatesTest {

    @Test
    public void shouldParseAndFormatDateTime() {
        var originalIso = "2022-01-01T22:00:00Z";
        var dateTime = Dates.convertISOToLocalDateTime(originalIso);
        var parsedIso = Dates.convertLocalDateTimeToISO(dateTime);
        assertThat(parsedIso, equalTo(originalIso));
    }

}
