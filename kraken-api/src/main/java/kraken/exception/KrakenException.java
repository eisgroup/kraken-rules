/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.exception;

import kraken.message.SystemMessage;

/**
 * A generic exception which is common parent for all Kraken exceptions
 *
 */
public abstract class KrakenException extends RuntimeException {

    private static final long serialVersionUID = 5432635944422254148L;

    public KrakenException(SystemMessage message) {
        super(message.formatMessageWithCode());
    }

    public KrakenException(SystemMessage message, Throwable cause) {
        super(message.formatMessageWithCode(), cause);
    }

}
