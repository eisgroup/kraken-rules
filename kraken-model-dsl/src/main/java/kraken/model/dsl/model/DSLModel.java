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
package kraken.model.dsl.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents Kraken DSL document that may contain contexts, entry points and rules in DSL.
 * <p>
 * Note, that models are grouped by type (Context, EntryPoint, Rule) rather than absolute order in DSL file.
 *
 * @author mulevicius
 */
public class DSLModel {

    /**
     * All contexts defined at root level as loose Context declarations
     */
    private final List<DSLContext> contexts;

    /**
     * All entry points defined at root level as loose EntryPoint declarations
     */
    private final List<DSLEntryPoint> entryPoints;

    /**
     * All rules defined at root level as loose Rule declarations
     */
    private final List<DSLRule> rules;

    /**
     * All Contexts scope blocks
     */
    private final List<DSLContexts> contextBlocks;

    /**
     * External context bound to this model.
     */
    private final DSLExternalContext externalContext;

    /**
     * All external context definitions.
     */
    private final List<DSLExternalContextDefinition> externalContextDefinitions;

    /**
     * All EntryPoints scope blocks
     */
    private final List<DSLEntryPoints> entryPointBlocks;

    /**
     * All Rules scope blocks
     */
    private final List<DSLRules> ruleBlocks;

    /**
     * Namespace name for this DSL model.
     */
    private final String namespace;

    /**
     * Namespace includes of other namespaces for this DSL model.
     */
    private final List<String> includes;

    /**
     * Rule imports from other namespaces for this DSL model.
     */
    private final List<DSLImportReference> ruleImports;

    /**
     * A list of function signature declarations
     */
    private final List<DSLFunctionSignature> functionSignatures;

    /**
     * A list of DSL function implementations
     */
    private final List<DSLFunction> functions;

    private final List<DSLDimension> dimensions;

    public DSLModel(List<DSLContext> contexts,
                    List<DSLEntryPoint> entryPoints,
                    List<DSLRule> rules,
                    List<DSLContexts> contextBlocks,
                    List<DSLEntryPoints> entryPointBlocks,
                    List<DSLRules> ruleBlocks,
                    String namespace,
                    List<String> includes,
                    List<DSLImportReference> ruleImports,
                    DSLExternalContext externalContext,
                    List<DSLExternalContextDefinition> externalContextDefinitions,
                    List<DSLFunctionSignature> functionSignatures,
                    List<DSLFunction> functions,
                    List<DSLDimension> dimensions) {
        this.externalContext = externalContext;
        this.externalContextDefinitions = externalContextDefinitions;
        this.contexts = Objects.requireNonNull(contexts);
        this.entryPoints = Objects.requireNonNull(entryPoints);
        this.rules = Objects.requireNonNull(rules);
        this.contextBlocks = Objects.requireNonNull(contextBlocks);
        this.entryPointBlocks = Objects.requireNonNull(entryPointBlocks);
        this.ruleBlocks = Objects.requireNonNull(ruleBlocks);

        this.namespace = namespace;
        this.includes = Objects.requireNonNull(includes);
        this.ruleImports = Objects.requireNonNull(ruleImports);
        this.functionSignatures = Objects.requireNonNull(functionSignatures);
        this.functions = Objects.requireNonNull(functions);
        this.dimensions = Objects.requireNonNull(dimensions);
    }

    public List<DSLEntryPoints> getEntryPointBlocks() {
        return Collections.unmodifiableList(entryPointBlocks);
    }

    public List<DSLContexts> getContextBlocks() {
        return Collections.unmodifiableList(contextBlocks);
    }

    public DSLExternalContext getExternalContext() {
        return externalContext;
    }

    public List<DSLExternalContextDefinition> getExternalContextDefinitions() {
        return externalContextDefinitions;
    }

    public List<DSLRules> getRuleBlocks() {
        return Collections.unmodifiableList(ruleBlocks);
    }

    public List<DSLContext> getContexts() {
        return Collections.unmodifiableList(contexts);
    }

    public List<DSLEntryPoint> getEntryPoints() {
        return Collections.unmodifiableList(entryPoints);
    }

    public List<DSLRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    public String getNamespace() {
        return namespace;
    }

    public List<String> getIncludes() {
        return Collections.unmodifiableList(includes);
    }

    public List<DSLImportReference> getRuleImports() {
        return Collections.unmodifiableList(ruleImports);
    }

    public List<DSLFunctionSignature> getFunctionSignatures() {
        return Collections.unmodifiableList(functionSignatures);
    }

    public List<DSLFunction> getFunctions() {
        return Collections.unmodifiableList(functions);
    }

    public List<DSLDimension> getDimensions() {
        return Collections.unmodifiableList(dimensions);
    }

}
