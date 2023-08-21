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
package kraken.model.dsl.visitor;

import static kraken.message.SystemMessageBuilder.Message.KRAKEN_DSL_GLOBAL_NAMESPACE_IMPORTS_RULE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_DSL_GLOBAL_NAMESPACE_INCLUDES_NAMESPACE;
import static kraken.message.SystemMessageBuilder.Message.KRAKEN_DSL_GLOBAL_NAMESPACE_MULTIPLE_EXTERNAL_CONTEXT;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import kraken.message.SystemMessageBuilder;
import kraken.model.dsl.DSLParsingException;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSL.ModelContext;
import kraken.model.dsl.KrakenDSL.NamespaceContext;
import kraken.model.dsl.KrakenDSL.NamespaceImportContext;
import kraken.model.dsl.KrakenDSL.RuleImportContext;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLContext;
import kraken.model.dsl.model.DSLContexts;
import kraken.model.dsl.model.DSLDimension;
import kraken.model.dsl.model.DSLEntryPoint;
import kraken.model.dsl.model.DSLEntryPoints;
import kraken.model.dsl.model.DSLExternalContext;
import kraken.model.dsl.model.DSLExternalContextDefinition;
import kraken.model.dsl.model.DSLFunction;
import kraken.model.dsl.model.DSLFunctionSignature;
import kraken.model.dsl.model.DSLImportReference;
import kraken.model.dsl.model.DSLModel;
import kraken.model.dsl.model.DSLRule;
import kraken.model.dsl.model.DSLRules;
import kraken.namespace.Namespaced;

/**
 * @author mulevicius
 */
public class DSLModelVisitor extends KrakenDSLBaseVisitor<DSLModel> {

    private final DSLContextVisitor contextVisitor = new DSLContextVisitor();

    private final DSLExternalContextVisitor externalContextVisitor = new DSLExternalContextVisitor();

    private final DSLExternalContextDefinitionVisitor externalContextDefinitionVisitor = new DSLExternalContextDefinitionVisitor();

    private final DSLEntryPointVisitor entryPointVisitor = new DSLEntryPointVisitor();

    private final DSLRuleVisitor ruleVisitor = new DSLRuleVisitor();

    private final DSLContextsVisitor contextsVisitor = new DSLContextsVisitor();

    private final DSLEntryPointsVisitor entryPointsVisitor = new DSLEntryPointsVisitor();

    private final DSLRulesVisitor rulesVisitor = new DSLRulesVisitor();

    private final DSLImportReferenceVisitor importReferenceVisitor = new DSLImportReferenceVisitor();

    private final DSLFunctionSignatureVisitor functionSignatureVisitor = new DSLFunctionSignatureVisitor();

    private final DSLFunctionVisitor functionVisitor = new DSLFunctionVisitor();

    private final DSLDimensionVisitor dimensionVisitor = new DSLDimensionVisitor();

    @Override
    public DSLModel visitKraken(KrakenDSL.KrakenContext ctx) {
        if (ctx.model() == null) {
            return new DSLModel(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Namespaced.GLOBAL,
                List.of(),
                List.of(),
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
            );
        }

        List<NamespaceImportContext> namespaceImports = ctx.anImport().stream()
            .filter(i -> i.namespaceImport() != null)
            .map(i -> i.namespaceImport())
            .collect(Collectors.toList());

        List<RuleImportContext> ruleImports = ctx.anImport().stream()
            .filter(i -> i.ruleImport() != null)
            .map(i -> i.ruleImport())
            .collect(Collectors.toList());

        NamespaceContext namespaceContext = ctx.namespace();
        if(namespaceContext == null && !namespaceImports.isEmpty()) {
            var m = SystemMessageBuilder.create(KRAKEN_DSL_GLOBAL_NAMESPACE_INCLUDES_NAMESPACE).build();
            throw new DSLParsingException(m);
        }
        if(namespaceContext == null && !ruleImports.isEmpty()) {
            var m = SystemMessageBuilder.create(KRAKEN_DSL_GLOBAL_NAMESPACE_IMPORTS_RULE).build();
            throw new DSLParsingException(m);
        }

        String namespace = namespaceContext != null
            ? namespaceContext.namespaceName().getText()
            : Namespaced.GLOBAL;

        List<KrakenDSL.ExternalContextContext> externalContext = ctx.model().stream()
                .map(ModelContext::externalContext)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (externalContext.size() > 1) {
            var m = SystemMessageBuilder.create(KRAKEN_DSL_GLOBAL_NAMESPACE_MULTIPLE_EXTERNAL_CONTEXT).build();
            throw new DSLParsingException(m);
        }

        DSLExternalContext dslExternalContext = externalContext.size() == 1
                ? externalContextVisitor.visit(externalContext.get(0)) : null;

        List<DSLExternalContextDefinition> dslExternalContextDefinitions = ctx.model().stream()
                .map(ModelContext::externalContextDefinition)
                .filter(Objects::nonNull)
                .map(externalContextDefinitionVisitor::visit)
                .collect(Collectors.toList());

        List<DSLContext> contexts = ctx.model().stream()
                .map(ModelContext::context)
                .filter(Objects::nonNull)
                .map(contextVisitor::visit)
                .collect(Collectors.toList());

        List<DSLEntryPoint> entryPoints = ctx.model().stream()
                .map(ModelContext::entryPoint)
                .filter(Objects::nonNull)
                .map(entryPointVisitor::visit)
                .collect(Collectors.toList());

        List<DSLRule> rules = ctx.model().stream()
                .map(ModelContext::aRule)
                .filter(Objects::nonNull)
                .map(ruleVisitor::visit)
                .collect(Collectors.toList());

        List<DSLContexts> contextDefinitionBlocks = ctx.model().stream()
                .map(ModelContext::contexts)
                .filter(Objects::nonNull)
                .map(contextsVisitor::visit)
                .collect(Collectors.toList());

        List<DSLEntryPoints> entryPointBlocks = ctx.model().stream()
                .map(ModelContext::entryPoints)
                .filter(Objects::nonNull)
                .map(entryPointsVisitor::visit)
                .collect(Collectors.toList());

        List<DSLRules> ruleBlocks = ctx.model().stream()
                .map(ModelContext::rules)
                .filter(Objects::nonNull)
                .map(rulesVisitor::visit)
                .collect(Collectors.toList());

        List<DSLFunctionSignature> functionSignatures = ctx.model().stream()
            .map(ModelContext::functionSignature)
            .filter(Objects::nonNull)
            .map(functionSignatureVisitor::visit)
            .collect(Collectors.toList());

        List<DSLFunction> functions = ctx.model().stream()
            .map(ModelContext::functionImplementation)
            .filter(Objects::nonNull)
            .map(functionVisitor::visit)
            .collect(Collectors.toList());

        List<DSLDimension> dimensions = ctx.model().stream()
            .map(ModelContext::dimension)
            .filter(Objects::nonNull)
            .map(dimensionVisitor::visit)
            .collect(Collectors.toList());

        return new DSLModel(
            contexts,
            entryPoints,
            rules,
            contextDefinitionBlocks,
            entryPointBlocks,
            ruleBlocks,
            namespace,
            parseNamespaceImports(namespaceImports),
            parseRuleImports(ruleImports),
            dslExternalContext,
            dslExternalContextDefinitions,
            functionSignatures,
            functions,
            dimensions
        );
    }

    private List<String> parseNamespaceImports(List<NamespaceImportContext> include){
        return include.stream()
                .map(i -> i.namespaceName().getText())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<DSLImportReference> parseRuleImports(List<RuleImportContext> ctx){
        return ctx.stream()
                .map(importReferenceVisitor::visitRuleImport)
                .collect(Collectors.toList());
    }

}
