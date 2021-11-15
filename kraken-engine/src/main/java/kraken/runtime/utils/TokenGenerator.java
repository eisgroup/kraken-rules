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
package kraken.runtime.utils;

import javax.xml.bind.DatatypeConverter;
import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Generates unique token for particular date
 *
 * @author rimas
 * @since 1.0.6
 */
public class TokenGenerator {

    private SecureRandom random;

    /**
     * Default constructor, initialises random number generator
     */
    public TokenGenerator() {
        random = new SecureRandom();
    }

    /**
     * Generate unique token. Token is prefixed by timestamp if date is supplied
     *
     * @param localDateTime  date to use for prefix
     * @return      token
     */
    public String generateNewToken(LocalDateTime localDateTime) {
        byte bytes[] = new byte[10];
        random.nextBytes(bytes);
        String token = DatatypeConverter.printHexBinary(bytes);
        if (localDateTime != null) {
            token = localDateTime + ":" + token;
        }
        return token;
    }

}
