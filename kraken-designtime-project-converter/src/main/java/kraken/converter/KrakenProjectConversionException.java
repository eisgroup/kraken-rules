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
package kraken.converter;

import kraken.exception.KrakenException;
import kraken.message.SystemMessage;

/**
 * Indicates a critical error during kraken model translation
 *
 * @author mulevicius
 */
public class KrakenProjectConversionException extends KrakenException {

    private static final long serialVersionUID = 8245394778256093264L;

    public KrakenProjectConversionException(SystemMessage message) {
        super(message);
    }

    public KrakenProjectConversionException(SystemMessage message, Throwable cause) {
        super(message, cause);
    }
}
