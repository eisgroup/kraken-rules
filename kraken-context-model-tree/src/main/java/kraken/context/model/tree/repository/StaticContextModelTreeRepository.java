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
package kraken.context.model.tree.repository;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.impl.ContextModelTreeConstructException;
import kraken.context.model.tree.ContextModelTreeMetadata;
import kraken.context.model.tree.impl.ContextModelTreeImpl;
import kraken.el.TargetEnvironment;
import kraken.utils.ResourceLoader;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ContextModelTree} repository, that loads all resources from classpath. Resources must be
 * with extension * {@link StaticContextModelTreeRepository#EXTENSION} ("krakenmodel"). All files
 * from classpath with this extension will be loaded and stored by
 * {@link ContextModelTreeMetadata#getTargetEnvironment()} and {@link ContextModelTreeMetadata#getNamespace()}.
 * By these keys it will be queried in {@link ContextModelTreeRepository#get(String, TargetEnvironment)}
 *
 * @author psurinin
 */
public class StaticContextModelTreeRepository implements ContextModelTreeRepository {

    public static final String EXTENSION = "krakenmodel";

    public static StaticContextModelTreeRepository initialize() {
        final Collection<URL> urls;
        try {
            urls = ResourceLoader.builder()
                    .baseDir("")
                    .pattern(".*\\." + EXTENSION)
                    .build()
                    .load();
        } catch (UncheckedIOException e) {
            throw new ContextModelTreeConstructException("Exception while loading resources", e);
        }
        final Map<String, ContextModelTree> resources = urls.stream()
                .map(StaticContextModelTreeRepository::modelTree)
                .collect(Collectors.toMap(
                        mt -> cacheKey(mt.getMetadata().getNamespace(), mt.getMetadata().getTargetEnvironment()),
                        mt -> mt,
                        // more than one model tree in classpath
                        (mt1, mt2) -> mt1
                ));
        return new StaticContextModelTreeRepository(resources);
    }

    private static String cacheKey(String namespace, TargetEnvironment targetEnvironment) {
        return String.format("%s-%s", namespace, targetEnvironment);
    }

    private static ContextModelTreeImpl modelTree(URL url) {
        try (ObjectInputStream ois = new ObjectInputStream(url.openStream())) {
            return ((ContextModelTreeImpl) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new ContextModelTreeConstructException("Exception while loading resources", e);
        }
    }

    private Map<String, ContextModelTree> repository;

    @Override
    public ContextModelTree get(String namespace, TargetEnvironment targetEnvironment) {
        return repository.get(cacheKey(namespace, targetEnvironment));
    }

    private StaticContextModelTreeRepository(Map<String, ContextModelTree> repository) {
        this.repository = repository;
    }
}
