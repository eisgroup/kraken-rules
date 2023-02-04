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
package kraken.el.functionregistry.documentation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;

import kraken.el.functionregistry.FunctionHeader;

public class AdditionalInfoReader {

    public static String read(FunctionHeader functionHeader) {
        String resourceName = String
            .format("docs/%s(%s).md", functionHeader.getName(), functionHeader.getParameterCount());

        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(resourceName);

            if (url != null) {
                // A workaround for potential concurrency issue in Java. Disable JVM built-in
                // URL caching to avoid issues when resources loaded as a stream are closed by
                // JAR file being closed elsewhere.
                URLConnection urlConnection = url.openConnection();
                urlConnection.setUseCaches(false);

                try (InputStream inputStream = urlConnection.getInputStream()) {
                    if (inputStream != null) {
                        List<String> lines = IOUtils.readLines(inputStream, Charset.defaultCharset());
                        return String.join(System.lineSeparator(), lines);
                    }
                }
            }

            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Error while reading function documentation from: " + resourceName, e);
        }
    }
}
