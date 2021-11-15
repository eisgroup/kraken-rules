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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import kraken.model.dsl.DSLParsingException;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSL.ModelContext;
import kraken.model.dsl.KrakenDSL.NamespaceContext;
import kraken.model.dsl.KrakenDSL.NamespaceImportContext;
import kraken.model.dsl.KrakenDSL.RuleImportContext;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLContext;
import kraken.model.dsl.model.DSLContexts;
import kraken.model.dsl.model.DSLEntryPoint;
import kraken.model.dsl.model.DSLEntryPoints;
import kraken.model.dsl.model.DSLExternalContext;
import kraken.model.dsl.model.DSLExternalContextDefinition;
import kraken.model.dsl.model.DSLFunction;
import kraken.model.dsl.model.DSLImportReference;
import kraken.model.dsl.model.DSLModel;
import kraken.model.dsl.model.DSLRule;
import kraken.model.dsl.model.DSLRules;
import kraken.namespace.Namespaced;

/**
 * @author mulevicius
 */
public class DSLModelVisitor extends KrakenDSLBaseVisitor<DSLModel> {

    private DSLContextVisitor contextVisitor = new DSLContextVisitor();

    private DSLExternalContextVisitor externalContextVisitor = new DSLExternalContextVisitor();

    private DSLExternalContextDefinitionVisitor externalContextDefinitionVisitor = new DSLExternalContextDefinitionVisitor();

    private DSLEntryPointVisitor entryPointVisitor = new DSLEntryPointVisitor();

    private DSLRuleVisitor ruleVisitor = new DSLRuleVisitor();

    private DSLContextsVisitor contextsVisitor = new DSLContextsVisitor();

    private DSLEntryPointsVisitor entryPointsVisitor = new DSLEntryPointsVisitor();

    private DSLRulesVisitor rulesVisitor = new DSLRulesVisitor();

    private DSLImportReferenceVisitor importReferenceVisitor = new DSLImportReferenceVisitor();

    private DSLFunctionVisitor functionVisitor = new DSLFunctionVisitor();

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
                List.of()
            );
        }

        List<NamespaceImportContext> namespaceImports = ctx.anImport().stream()
            .filter(i -> i.namespaceImport() != null)
            .map(i -> i.namespaceImport()).collect(
            Collectors.toList());

        List<RuleImportContext> ruleImports = ctx.anImport().stream()
            .filter(i -> i.ruleImport() != null)
            .map(i -> i.ruleImport()).collect(
                Collectors.toList());

        NamespaceContext namespaceContext = ctx.namespace();
        if(namespaceContext == null && !namespaceImports.isEmpty()) {
            throw new DSLParsingException("Global namespace cannot include other namespaces");
        }
        if(namespaceContext == null && !ruleImports.isEmpty()) {
            throw new DSLParsingException("Global namespace cannot import rules from other namespaces");
        }

        String namespace = namespaceContext != null
            ? namespaceContext.namespaceName().getText()
            : Namespaced.GLOBAL;

        List<KrakenDSL.ExternalContextContext> externalContext = ctx.model().stream()
                .map(ModelContext::externalContext)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (externalContext.size() > 1) {
            throw new DSLParsingException("Only one Root External Context is allowed per namespace.");
        }

        DSLExternalContext dslExternalContext = externalContext.size() == 1
                ? externalContextVisitor.visitExternalContext(externalContext.get(0)) : null;

        List<DSLExternalContextDefinition> dslExternalContextDefinitions = ctx.model().stream()
                .map(ModelContext::externalContextDefinition)
                .filter(Objects::nonNull)
                .map(context -> externalContextDefinitionVisitor.visitExternalContextDefinition(context))
                .collect(Collectors.toList());

        List<DSLContext> contexts = ctx.model().stream()
                .map(ModelContext::context)
                .filter(Objects::nonNull)
                .map(context -> contextVisitor.visitContext(context))
                .collect(Collectors.toList());

        List<DSLEntryPoint> entryPoints = ctx.model().stream()
                .map(ModelContext::entryPoint)
                .filter(Objects::nonNull)
                .map(entryPoint -> entryPointVisitor.visitEntryPoint(entryPoint))
                .collect(Collectors.toList());

        List<DSLRule> rules = ctx.model().stream()
                .map(ModelContext::aRule)
                .filter(Objects::nonNull)
                .map(rule -> ruleVisitor.visitARule(rule))
                .collect(Collectors.toList());

        List<DSLContexts> contextDefinitionBlocks = ctx.model().stream()
                .map(ModelContext::contexts)
                .filter(Objects::nonNull)
                .map(contextBlock -> contextsVisitor.visit(contextBlock))
                .collect(Collectors.toList());

        List<DSLEntryPoints> entryPointBlocks = ctx.model().stream()
                .map(ModelContext::entryPoints)
                .filter(Objects::nonNull)
                .map(entryPointBlock -> entryPointsVisitor.visit(entryPointBlock))
                .collect(Collectors.toList());

        List<DSLRules> ruleBlocks = ctx.model().stream()
                .map(ModelContext::rules)
                .filter(Objects::nonNull)
                .map(ruleBlock -> rulesVisitor.visit(ruleBlock))
                .collect(Collectors.toList());

        List<DSLFunction> functions = ctx.model().stream()
            .map(ModelContext::functionSignature)
            .filter(Objects::nonNull)
            .map(function -> functionVisitor.visit(function))
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
            functions
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
