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

import kraken.el.TargetEnvironment;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.COMPILE)
public class ContextModelTreeGenerator extends AbstractMojo {

    @Parameter(property = "outputDir", defaultValue = "${project.build.directory}/classes")
    private String outputDir;

    @Parameter(property = "baseDir", defaultValue = "")
    private String baseDir = "";

    @Parameter(property = "baseDirs", defaultValue = "")
    private String[] baseDirs;

    @Parameter(property = "namespace", required = true)
    private String[] namespace;

    @Parameter(property = "environment", required = true)
    private TargetEnvironment environment;

    @Parameter(property = "isPretty")
    private boolean isPretty = false;

    @Parameter(property = "excludes")
    private String[] excludes;

    @Override
    public void execute() throws MojoFailureException {
        List<String> excludePatterns = excludes != null ? Arrays.asList(excludes) : new ArrayList<>();
        List<String> baseDirectories = baseDirs.length > 0 ? Arrays.asList(baseDirs) : List.of(baseDir);
        new ContextModelTreeOutputResourceWriter(
                baseDirectories,
                outputDir,
                Arrays.asList(namespace),
                environment,
                isPretty,
                excludePatterns
        ).execute();
    }
}