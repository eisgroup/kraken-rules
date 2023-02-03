/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mulevicius
 */
public class Kraken {

    private final static Logger LOGGER = LoggerFactory.getLogger(Kraken.class);

    private static final String KRAKEN_VERSION_PROPERTIES_FILE = "kraken-version.properties";
    private static final String KRAKEN_VERSION_PROPERTY = "kraken.version";

    public static final String VERSION;

    static {
        Properties properties = new Properties();
        try (InputStream is = Kraken.class.getClassLoader().getResourceAsStream(KRAKEN_VERSION_PROPERTIES_FILE)) {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.error("Failed to read '" + KRAKEN_VERSION_PROPERTIES_FILE + "' file.");
        }

        VERSION = properties.getProperty(KRAKEN_VERSION_PROPERTY);
    }
}
