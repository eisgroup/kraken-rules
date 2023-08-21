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
package kraken.runtime;

import kraken.exception.KrakenException;
import kraken.message.SystemMessage;

/**
 * Superclass for all runtime execution exceptions
 *
 * @author rimas
 * @since 1.0
 */
public class KrakenRuntimeException extends KrakenException {

    private static final long serialVersionUID = -7921177886566471653L;

    /**
     * Constructor to build exception with message
     *
     * @param message   message text
     */
    public KrakenRuntimeException(SystemMessage message) {
        super(message);
    }

    /**
     * Constructor to build exception instance with message and cause
     *
     * @param message   message text
     * @param cause     cause exception
     */
    public KrakenRuntimeException(SystemMessage message, Throwable cause) {
        super(message, cause);
    }

}
