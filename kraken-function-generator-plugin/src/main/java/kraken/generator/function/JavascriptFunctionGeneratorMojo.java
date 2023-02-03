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
package kraken.generator.function;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import kraken.model.dsl.read.DSLReader;
import kraken.model.project.KrakenProject;
import kraken.model.project.builder.ResourceKrakenProjectBuilder;
import kraken.model.resource.Resource;
import kraken.utils.GsonUtils;

/**
 * @author mulevicius
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class JavascriptFunctionGeneratorMojo extends AbstractMojo {

    private final Gson gson = GsonUtils.prettyGson();

    private static final Logger logger = LoggerFactory.getLogger(JavascriptFunctionGeneratorMojo.class);

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}/classes")
    private String outputDir;

    @Parameter(property = "baseDirs")
    private String[] baseDirs;

    @Parameter(property = "namespaces", required = true)
    private String[] namespaces;

    @Parameter(property = "excludes")
    private String[] excludes;

    @Override
    public void execute() throws MojoFailureException {
        List<String> excludePatterns = excludes != null ? Arrays.asList(excludes) : new ArrayList<>();
        List<String> baseDirectories = baseDirs != null  ? Arrays.asList(baseDirs) : List.of("");

        JavascriptFunctionGenerator generator = new JavascriptFunctionGenerator();

        DSLReader dslReader = new DSLReader(excludePatterns);
        Collection<Resource> resources = dslReader.read(baseDirectories);
        ResourceKrakenProjectBuilder krakenProjectBuilder = new ResourceKrakenProjectBuilder(resources);

        for(String namespace : namespaces) {
            KrakenProject krakenProject = krakenProjectBuilder.buildKrakenProject(namespace);
            List<KelFunction> functions = generator.generate(krakenProject);
            writeJson(namespace, functions);
        }
    }

    private void writeJson(String namespace, List<KelFunction> functions) throws MojoFailureException {
        String fileName = "kraken_functions_" + namespace;
        Path path = Paths.get(outputDir, fileName + ".ts");
        try {
            Files.createDirectories(path.getParent());
            String contents = "// tslint:disable\nexport const " + fileName.toUpperCase() + " = " + gson.toJson(functions);
            Files.writeString(path, contents);
            logger.info(String.format("Functions for namespace '%s' was written to file '%s'", namespace, path));
        } catch (IOException exception) {
            String template = "Error while writing functions for namespace '%s' to file '%s'";
            String message = String.format(template, namespace, path);
            throw new MojoFailureException(message, exception);
        }
    }
}
