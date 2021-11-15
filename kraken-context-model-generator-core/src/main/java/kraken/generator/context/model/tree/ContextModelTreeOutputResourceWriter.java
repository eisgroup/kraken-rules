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
package kraken.generator.context.model.tree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.impl.ContextModelTrees;
import kraken.context.model.tree.impl.ContextRepository;
import kraken.context.model.tree.repository.StaticContextModelTreeRepository;
import kraken.converter.KrakenProjectConverter;
import kraken.el.TargetEnvironment;
import kraken.model.dsl.read.DSLReader;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.resource.Resource;
import kraken.namespace.Namespaced;
import kraken.utils.GsonUtils;

public class ContextModelTreeOutputResourceWriter {

    private static final Logger logger = LoggerFactory.getLogger(ContextModelTreeOutputResourceWriter.class);
    private static final Function<String, String> fileName = ns -> "kraken_model_tree_" + ns;
    private static String namespace(String ns) {
        return "GLOBAL".equals(ns) ? Namespaced.GLOBAL : ns;
    }

    private final Collection<String> baseDirs;
    private final String targetDir;
    private final Collection<String> namespaces;
    private final TargetEnvironment environment;
    private final List<String> excludePatterns;
    private Gson gson;

    /**
     * Creates resource writer.
     * @param baseDirs        base directories from where to read resources. It is loading resources from class path.
     *                        baseDir is jar directory start.
     * @param outputDir       directory to write model trees.
     * @param namespace       Namespace for context definitions
     * @param environment     JAVA or JAVASCRIPT
     * @param isPretty        is output formatted output or magnified.
     * @param excludePatterns how to set up exclude patterns see {@link DSLReader}
     *
     * @since 1.0.29
     */
    public ContextModelTreeOutputResourceWriter(
            Collection<String> baseDirs,
            String outputDir,
            Collection<String> namespace,
            TargetEnvironment environment,
            boolean isPretty,
            List<String> excludePatterns
    ) {
        this.baseDirs = baseDirs;
        this.targetDir = outputDir;
        this.namespaces = namespace;
        this.environment = environment;
        this.excludePatterns = excludePatterns;
        this.gson = isPretty ? GsonUtils.prettyGson() : GsonUtils.gson();
    }

    public OutputResult execute() {
        Map<String, ContextModelTree> trees = buildContextModelTrees();
        trees.forEach(write());
        return new OutputResult(trees.keySet());
    }

    private Map<String, ContextModelTree> buildContextModelTrees() {
        DSLReader dslReader = new DSLReader(excludePatterns);
        Collection<Resource> resources = dslReader.read(baseDirs);
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);

        final List<String> availableNs = resources.stream().map(Resource::getNamespace).distinct().collect(Collectors.toList());
        logger.info("Available namespaces to generate model tree: {}", availableNs);
        logger.info("Requested to generate model tree: {}", namespaces);

        return namespaces.stream()
                .filter(requestedNamespace -> availableNs.contains(requestedNamespace))
                .distinct()
                .collect(Collectors.toMap(
                        fileName,
                        ns -> ContextModelTrees.create(
                                ContextRepository.from(
                                        new KrakenProjectConverter(
                                                krakenProjectBuilder.buildKrakenProject(namespace(ns)),
                                                environment
                                        ).convert()
                                ),
                                namespace(ns),
                                environment
                        )));
    }

    private BiConsumer<String, ContextModelTree> write() {
        if (environment.equals(TargetEnvironment.JAVA)) {
            return this::writeBinary;
        }
        return this::writeJson;
    }

    private String getJavaScriptFilePath(String fileName) {
        return getFilePath(fileName, "ts");
    }

    private void writeJson(String fileName, ContextModelTree modelTree) {
        String json = gson.toJson(modelTree);
        String prefix = "// tslint:disable\nexport const " + fileName.toUpperCase() + " = ";
        String file = prefix.concat(json);
        final String filePath = getJavaScriptFilePath(fileName);
        final boolean mkdirs = new File(filePath).getParentFile().mkdirs();
        try (FileOutputStream stream = new FileOutputStream(filePath)) {
            IOUtils.write(file, stream);
            logger.info(String.format("ContextModelTree by name: %s was written to location: %s", fileName, filePath));
        } catch (IOException exception) {
            logger.error("Failed to write ContextModelTree.", exception);
        }
    }

    private void writeBinary(String fileName, ContextModelTree t) {
        final String filePath = getFilePath(fileName, StaticContextModelTreeRepository.EXTENSION);
        final boolean mkdirs = new File(filePath).getParentFile().mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(t);
            logger.info(String.format("ContextModelTree by name: %s was written to location: %s", fileName, filePath));
        } catch (Exception ex) {
            logger.error("Failed to write ContextModelTree.", ex);
        }
    }

    private String getFilePath(String fileName, String extension) {
        return targetDir + "/" + fileName + "." + extension;
    }

}
