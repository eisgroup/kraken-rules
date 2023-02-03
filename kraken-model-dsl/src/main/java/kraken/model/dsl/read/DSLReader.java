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
package kraken.model.dsl.read;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import kraken.model.dsl.DSLParsingException;
import kraken.model.dsl.KrakenDSLModelParser;
import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.resource.Resource;

import kraken.utils.ResourceLoader;
import org.apache.commons.io.IOUtils;

/**
 * @author mulevicius
 */
public class DSLReader {

    public static final String NAME_PATTERN = ".*\\.rules";
    private static final Lock resourceLock = new ReentrantLock();

    private final Collection<String> excludePatterns;

    /**
     * Creates DSLReader with ignore patterns. URL is ignores if one of patterns is matching.
     * @param excludePatterns are patterns for folders or text in extracted from classpath URL.
     *                        it can be **ignoredToken** or **\/ignoredFolder\/**
     */
    public DSLReader(Collection<String> excludePatterns) {
        this.excludePatterns = excludePatterns;
    }

    public DSLReader() {
        this(List.of());
    }

    public List<Resource> read(String directory) {
            return read(List.of(directory));
    }

    public List<Resource> read(Collection<String> directories) {
        resourceLock.lock();
        try {
            return directories.stream()
                    .flatMap(source -> collectUrls(source, NAME_PATTERN).stream())
                    .distinct()
                    .map(this::read)
                    .collect(Collectors.toList());
        } finally {
            resourceLock.unlock();
        }
    }

    private Resource read(URL url) {
        try (InputStream inputStream = url.openStream()) {
            String dsl = IOUtils.toString(inputStream, Charset.defaultCharset());
            return KrakenDSLModelParser.parseResource(dsl, url.toURI());
        } catch (IOException | DSLParsingException e) {
            throw new DSLReadingException("Invalid DSL at: " + url, e);
        } catch (LineParseCancellationException exception) {
            var template = "Invalid DSL at %s:%s:%s";
            throw new DSLReadingException(
                    String.format(
                            template,
                            url,
                            exception.getLine(),
                            exception.getColumn() + 1
                    ),
                    exception
            );
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to parse url to url", e);
        }
    }

    private Collection<URL> collectUrls(String prefix, String fileNamePattern) {
        try {
            ResourceLoader.Builder builder = ResourceLoader.builder();
            excludePatterns.forEach(builder::excludePattern);
            return builder
                    .baseDir(prefix)
                    .pattern(fileNamePattern)
                    .build()
                    .load();
        } catch (UncheckedIOException | DSLParsingException e) {
            throw new DSLReadingException("Exception while loading resources", e);
        }
    }
}
